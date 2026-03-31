package com.example.campuslife.entity.preparation;

public class AllocationAdjustmentSourceRequest {
    public Long categoryId;
    public String amount;

    public AllocationAdjustmentSourceRequest(Long categoryId, String amount) {
        this.categoryId = categoryId;
        this.amount = amount;
    }
}
