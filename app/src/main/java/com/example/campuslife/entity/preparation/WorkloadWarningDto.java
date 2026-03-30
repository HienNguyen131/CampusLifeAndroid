package com.example.campuslife.entity.preparation;

import com.google.gson.annotations.SerializedName;

public class WorkloadWarningDto {
    @SerializedName("studentId")
    private Long studentId;

    @SerializedName("studentName")
    private String studentName;

    @SerializedName("taskCount")
    private Long taskCount;

    @SerializedName("type")
    private String type; // e.g. "OVERLOADED", "UNASSIGNED"

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Long taskCount) {
        this.taskCount = taskCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
