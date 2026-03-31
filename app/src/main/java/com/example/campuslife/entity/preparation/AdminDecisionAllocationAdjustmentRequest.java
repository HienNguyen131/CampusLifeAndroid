package com.example.campuslife.entity.preparation;

import java.util.List;

public class AdminDecisionAllocationAdjustmentRequest {
    public Boolean approved;
    public Long categoryId;
    public List<AllocationAdjustmentSourceRequest> sources;

    public AdminDecisionAllocationAdjustmentRequest(Boolean approved, List<AllocationAdjustmentSourceRequest> sources) {
        this.approved = approved;
        this.sources = sources;
    }
}
