package com.example.campuslife.entity.preparation;

import java.io.Serializable;
import java.math.BigDecimal;

public class AllocationAdjustmentSourcePlanDto implements Serializable {
    public Long categoryId;
    public String categoryName;
    public BigDecimal amount;
}
