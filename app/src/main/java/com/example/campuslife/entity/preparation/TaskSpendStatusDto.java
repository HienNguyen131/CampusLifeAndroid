package com.example.campuslife.entity.preparation;

import java.io.Serializable;
import java.math.BigDecimal;

public class TaskSpendStatusDto implements Serializable {
    public Long taskId;
    public String title;
    public BigDecimal allocatedAmount;
    public BigDecimal committedAmount;
    public BigDecimal approvedSpent;
    public Double usedPercent;
}
