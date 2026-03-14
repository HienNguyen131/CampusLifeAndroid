package com.example.campuslife.entity.preparation;

public class CreateExpenseRequest {
    public String amount;
    public String description;
    public String evidenceUrl;

    public CreateExpenseRequest(String amount, String description, String evidenceUrl) {
        this.amount = amount;
        this.description = description;
        this.evidenceUrl = evidenceUrl;
    }
}
