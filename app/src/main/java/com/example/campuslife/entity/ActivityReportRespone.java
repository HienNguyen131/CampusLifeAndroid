package com.example.campuslife.entity;

public class ActivityReportRespone {
    private Long id;
    private Task task;
    private String status;




    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
