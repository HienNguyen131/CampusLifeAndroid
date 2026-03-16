package com.example.campuslife.entity.preparation;

public class CreatePreparationTaskRequest {
    public Long activityId;
    public Long assigneeId;
    public String title;
    public String description;
    public String deadline;

    public CreatePreparationTaskRequest() {}

    public CreatePreparationTaskRequest(Long activityId, Long assigneeId, String title, String description, String deadline) {
        this.activityId = activityId;
        this.assigneeId = assigneeId;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
    }
}
