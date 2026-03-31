package com.example.campuslife.entity.preparation;

import java.io.Serializable;
import java.math.BigDecimal;

public class FundAdvanceDto implements Serializable {
    public Long id;
    public Long taskId;
    public String taskTitle;
    public Long studentId;
    public String studentName;
    public Long requestedById;
    public String requestedByName;
    public Long categoryId;
    public String categoryName;
    public BigDecimal amount;
    public BigDecimal remainingAmount;
    public String status;
    public String createdAt;
    public String decidedAt;
}
