package com.example.todolistdemo.Model;

public class ToDoModel {
    private String task;
    private int id,status;

    public int getId() {
        return id;
    }

    public String getTask() {
        return task;
    }

    public int getStatus() {
        return status;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setTask(String task) {
        this.task = task;
    }
    public void setStatus(int status) {
        this.status = status;
    }



}
