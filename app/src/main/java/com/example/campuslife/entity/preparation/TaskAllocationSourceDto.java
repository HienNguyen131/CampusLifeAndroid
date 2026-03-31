package com.example.campuslife.entity.preparation;

import java.io.Serializable;
import java.math.BigDecimal;

public class TaskAllocationSourceDto implements Serializable {
    public Long categoryId;
    public String categoryName;
    public BigDecimal allocatedAmount;
    public BigDecimal holdingAdvanceAmount;
    public BigDecimal approvedSpentAmount;
    public BigDecimal allocationRemainingAmount;
}
