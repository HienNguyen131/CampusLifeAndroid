package com.example.campuslife.entity.preparation;

public class ApproveExpenseRequest {
    private Boolean approved;

    public ApproveExpenseRequest() {}

    public ApproveExpenseRequest(Boolean approved) {
        this.approved = approved;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
}
