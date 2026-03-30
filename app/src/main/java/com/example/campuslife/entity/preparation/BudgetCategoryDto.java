package com.example.campuslife.entity.preparation;

import java.io.Serializable;

public class BudgetCategoryDto implements Serializable {
    public Long id;
    public String name;
    public String allocatedAmount;
    public String allocatedToTasksAmount;
    public String availableToAllocateAmount;
    public String cashOutsideAmount;
    public String cashAvailableAmount;
    public String usedAmount;
    public String remainingAmount;
    public Double usedPercent;
}
