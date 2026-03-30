package com.example.campuslife.entity.preparation;

import java.util.List;

public class UpsertActivityBudgetRequest {
    public String totalAmount;
    public List<UpsertBudgetCategoryRequest> categories;

    public UpsertActivityBudgetRequest() {}

    public UpsertActivityBudgetRequest(String totalAmount, List<UpsertBudgetCategoryRequest> categories) {
        this.totalAmount = totalAmount;
        this.categories = categories;
    }
}
