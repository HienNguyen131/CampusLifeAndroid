package com.example.campuslife.entity.preparation;

public class CreateExpenseRequest {
    public Long taskId;
    public Long categoryId;
    public String amount;
    public String description;
    public String evidenceUrl;

    public CreateExpenseRequest() {}

    public CreateExpenseRequest(Long taskId, Long categoryId, String amount, String description, String evidenceUrl) {
        this.taskId = taskId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.description = description;
        this.evidenceUrl = evidenceUrl;
    }
}
