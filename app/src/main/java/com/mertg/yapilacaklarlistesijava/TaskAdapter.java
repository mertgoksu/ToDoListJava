package com.mertg.yapilacaklarlistesijava;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private OnTaskListener onTaskListener;

    public TaskAdapter(List<Task> taskList, OnTaskListener onTaskListener) {
        this.taskList = taskList;
        this.onTaskListener = onTaskListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view, onTaskListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskNameTextView.setText(task.getName());
        holder.taskDateTextView.setText(task.getDate());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView taskNameTextView, taskDateTextView;
        OnTaskListener onTaskListener;

        public TaskViewHolder(@NonNull View itemView, OnTaskListener onTaskListener) {
            super(itemView);
            taskNameTextView = itemView.findViewById(R.id.taskNameTextView);
            taskDateTextView = itemView.findViewById(R.id.taskDateTextView);
            this.onTaskListener = onTaskListener;
            itemView.setOnClickListener(this); // ItemView tıklama olayını set et
        }

        @Override
        public void onClick(View v) {
            onTaskListener.onTaskClick(getAdapterPosition()); // Adapter pozisyonunu ilet
        }
    }

    public interface OnTaskListener {
        void onTaskClick(int position);
    }
}
