package com.example.campuslife.entity.preparation;

import java.io.Serializable;

public class AllocateTaskAmountRequest implements Serializable {
    public Long categoryId;
    public String allocatedAmount;

    public AllocateTaskAmountRequest() {}

    public AllocateTaskAmountRequest(Long categoryId, String allocatedAmount) {
        this.categoryId = categoryId;
        this.allocatedAmount = allocatedAmount;
    }
}
