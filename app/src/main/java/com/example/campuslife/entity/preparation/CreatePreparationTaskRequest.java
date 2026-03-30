package com.example.campuslife.entity.preparation;

import com.google.gson.annotations.SerializedName;

public class CreatePreparationTaskRequest {
    public Long activityId;

    @SerializedName("ownerId")
    public Long assigneeId;

    public String title;
    public String description;
    public String deadline;
    public Boolean isFinancial;

    public CreatePreparationTaskRequest() {}

    public CreatePreparationTaskRequest(Long activityId, Long assigneeId, String title, String description, String deadline, Boolean isFinancial) {
        this.activityId = activityId;
        this.assigneeId = assigneeId;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.isFinancial = isFinancial;
    }
}
