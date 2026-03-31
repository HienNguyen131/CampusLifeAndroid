package com.example.campuslife.entity.preparation;

public class CreateAllocationAdjustmentRequest {
    public String amount;
    public String description;

    public CreateAllocationAdjustmentRequest() {}

    public CreateAllocationAdjustmentRequest(String amount, String description) {
        this.amount = amount;
        this.description = description;
    }
}
