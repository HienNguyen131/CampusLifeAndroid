package com.example.campuslife.entity.preparation;

public class UpsertBudgetRequest {
    public Double totalAmount;
    public String description;

    public UpsertBudgetRequest() {}

    public UpsertBudgetRequest(Double totalAmount, String description) {
        this.totalAmount = totalAmount;
        this.description = description;
    }
}
