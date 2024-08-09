package com.mertg.yapilacaklarlistesijava;

public class Task {
    private String id;
    private String name;
    private String date;
    private long timestamp;

    public Task() {}

    public Task(String id, String name, String date, long timestamp) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
