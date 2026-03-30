package com.example.campuslife.entity.preparation;

public class ApproveTaskCompletionRequest {
    public boolean approved;
    public String approvalNote;

    public ApproveTaskCompletionRequest() {}

    public ApproveTaskCompletionRequest(boolean approved, String approvalNote) {
        this.approved = approved;
        this.approvalNote = approvalNote;
    }
}
