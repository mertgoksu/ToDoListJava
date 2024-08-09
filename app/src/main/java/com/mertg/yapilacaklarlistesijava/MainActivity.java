package com.mertg.yapilacaklarlistesijava;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // Durum çubuğu rengini değiştirme
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#1F2626")); // İstediğiniz renk kodunu buraya ekleyin

        // Toolbar'ı ayarlayalım
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Yapılacaklar Listesi");

        // Firestore veritabanı örneğini al
        db = FirebaseFirestore.getInstance();

        // Görev listesi ve adaptörünü oluştur
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, this);

        // RecyclerView'i ayarla
        RecyclerView tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksRecyclerView.setAdapter(taskAdapter);

        // FloatingActionButton'a tıklama dinleyicisi ekle
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog(); // Yeni görev ekleme dialogunu göster
            }
        });

        // Görevleri Firestore'dan oku
        readTasks();

        // ItemTouchHelper ile görevleri sürükle ve bırak özelliklerini ayarla
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.RIGHT) {
                    showUpdateTaskDialog(taskList.get(position)); // Görevi güncelle
                } else if (direction == ItemTouchHelper.LEFT) {
                    deleteTask(taskList.get(position).getId()); // Görevi sil
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                Paint paint = new Paint();
                if (dX > 0) {
                    paint.setColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_blue_light)); // Sağ swipe renk
                    c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), paint);
                } else if (dX < 0) {
                    paint.setColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_light)); // Sol swipe renk
                    c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                }
            }
        });

        // ItemTouchHelper'ı RecyclerView'e ata
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        readTasks(); // Görev listesini yenile
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Yeni görev ekleme dialogunu göster
    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText taskNameEditText = dialogView.findViewById(R.id.taskNameEditText);
        TextView taskDateTextView = dialogView.findViewById(R.id.taskDateTextView);
        Button addButton = dialogView.findViewById(R.id.addButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        // Tarih seçici gösterme
        taskDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(taskDateTextView);
            }
        });

        AlertDialog alertDialog = builder.create();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskName = taskNameEditText.getText().toString();
                String taskDate = taskDateTextView.getText().toString();
                if (!taskName.isEmpty() && !taskDate.isEmpty()) {
                    createTask(taskName, taskDate); // Yeni görev oluştur
                    alertDialog.dismiss();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readTasks(); // Görev listesini yenile
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    // Görev güncelleme dialogunu göster
    private void showUpdateTaskDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText taskNameEditText = dialogView.findViewById(R.id.taskNameEditText);
        TextView taskDateTextView = dialogView.findViewById(R.id.taskDateTextView);
        Button addButton = dialogView.findViewById(R.id.addButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        taskNameEditText.setText(task.getName());
        taskDateTextView.setText(task.getDate());
        addButton.setText("Update");

        // Tarih seçici gösterme
        taskDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(taskDateTextView);
            }
        });

        AlertDialog alertDialog = builder.create();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskName = taskNameEditText.getText().toString();
                String taskDate = taskDateTextView.getText().toString();
                if (!taskName.isEmpty() && !taskDate.isEmpty()) {
                    updateTask(task.getId(), taskName, taskDate); // Görevi güncelle
                    alertDialog.dismiss();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readTasks(); // Görev listesini yenile
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    // Tarih seçici dialogunu göster
    private void showDatePickerDialog(TextView dateTextView) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                dateTextView.setText(selectedDate); // Seçilen tarihi TextView'e ata
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    // Yeni görev oluştur
    private void createTask(String taskName, String taskDate) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return; // Kullanıcı giriş yapmamışsa çık

        long timestamp = System.currentTimeMillis(); // Zaman damgası

        Map<String, Object> task = new HashMap<>();
        task.put("name", taskName);
        task.put("date", taskDate);
        task.put("timestamp", timestamp);
        task.put("completed", false); // Yeni görevler tamamlanmamış olarak başlar

        db.collection("users").document(currentUser.getUid()).collection("tasks")
                .add(task)
                .addOnSuccessListener(documentReference -> {
                    taskList.add(new Task(documentReference.getId(), taskName, taskDate, timestamp)); // Yeni görevi listeye ekle
                    taskAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Görev Ekleme Başarısız", Toast.LENGTH_SHORT).show();
                });
    }

    // Görevleri Firestore'dan oku
    private void readTasks() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return; // Kullanıcı giriş yapmamışsa çık

        db.collection("users").document(currentUser.getUid()).collection("tasks")
                .orderBy("timestamp") // Zaman damgasına göre sıralı çek
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        taskList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String taskId = document.getId();
                            String taskName = document.getString("name");
                            String taskDate = document.getString("date");
                            long timestamp = document.getLong("timestamp");
                            boolean completed = document.getBoolean("completed");
                            taskList.add(new Task(taskId, taskName, taskDate, timestamp)); // Görevleri listeye ekle
                        }
                        taskAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Görev Okuma Başarısız", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Görevi güncelle
    private void updateTask(String taskId, String taskName, String taskDate) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return; // Kullanıcı giriş yapmamışsa çık

        db.collection("users").document(currentUser.getUid()).collection("tasks").document(taskId)
                .update("name", taskName, "date", taskDate)
                .addOnSuccessListener(aVoid -> {
                    for (Task task : taskList) {
                        if (task.getId().equals(taskId)) {
                            task.setName(taskName);
                            task.setDate(taskDate);
                            break;
                        }
                    }
                    taskAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Görev Güncelleme Başarısız", Toast.LENGTH_SHORT).show();
                });
    }

    // Görevi sil
    private void deleteTask(String taskId) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return; // Kullanıcı giriş yapmamışsa çık

        db.collection("users").document(currentUser.getUid()).collection("tasks").document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    for (Task task : taskList) {
                        if (task.getId().equals(taskId)) {
                            taskList.remove(task); // Görevi listeden kaldır
                            break;
                        }
                    }
                    taskAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Görev Silme Başarısız", Toast.LENGTH_SHORT).show();
                });
    }

    // Görev tıklandığında detaylarını göster
    @Override
    public void onTaskClick(int position) {
        showTaskDetailsDialog(taskList.get(position));
    }

    // Görev detaylarını gösteren dialog
    private void showTaskDetailsDialog(Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_task_details, null);
        builder.setView(dialogView);

        TextView taskNameTextView = dialogView.findViewById(R.id.taskNameTextView);
        TextView taskDateTextView = dialogView.findViewById(R.id.taskDateTextView);
        Button closeButton = dialogView.findViewById(R.id.closeButton);

        taskNameTextView.setText(task.getName());
        taskDateTextView.setText(task.getDate());

        AlertDialog alertDialog = builder.create();

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss(); // Dialogu kapat
            }
        });

        alertDialog.show();
    }
}
