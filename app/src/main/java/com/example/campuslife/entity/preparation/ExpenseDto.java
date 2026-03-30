package com.example.campuslife.entity.preparation;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ExpenseDto implements Serializable {
    public Long id;
    public Long activityId;
    public Long taskId;
    public String taskName;
    public Long categoryId;
    public String categoryName;
    public String amount;
    public String description;
    public String evidenceUrl;
    public String status;
    public Long createdById;
    public String createdByName;
    public String createdAt;
}
