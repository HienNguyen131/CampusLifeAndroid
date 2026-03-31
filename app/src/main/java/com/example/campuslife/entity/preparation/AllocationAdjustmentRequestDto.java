package com.example.campuslife.entity.preparation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class AllocationAdjustmentRequestDto implements Serializable {
    public Long id;
    public Long taskId;
    public String taskTitle;
    public BigDecimal amount;
    public String description;
    public String status;
    public String createdAt;
    public String createdByName;
}
