package com.example.campuslife.entity.preparation;

import java.io.Serializable;

public class CreateFundAdvanceRequest implements Serializable {
    public Long studentId;
    public Long categoryId;
    public String amount;

    public CreateFundAdvanceRequest() {}

    public CreateFundAdvanceRequest(Long studentId, Long categoryId, String amount) {
        this.studentId = studentId;
        this.categoryId = categoryId;
        this.amount = amount;
    }
}
