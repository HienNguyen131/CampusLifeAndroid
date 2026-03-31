package com.example.campuslife.entity.preparation;

import com.google.gson.annotations.SerializedName;

public class StudentStatsDto {
    @SerializedName("totalTasks")
    private long totalTasks;

    @SerializedName("completedTasks")
    private long completedTasks;

    @SerializedName("pendingTasks")
    private long pendingTasks;

    public long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(long totalTasks) {
        this.totalTasks = totalTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(long completedTasks) {
        this.completedTasks = completedTasks;
    }

    public long getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(long pendingTasks) {
        this.pendingTasks = pendingTasks;
    }
}
