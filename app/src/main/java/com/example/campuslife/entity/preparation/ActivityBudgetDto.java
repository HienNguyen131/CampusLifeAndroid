package com.example.campuslife.entity.preparation;

import java.io.Serializable;
import java.util.List;

public class ActivityBudgetDto implements Serializable {
    public Long id;
    public Long activityId;
    public String totalAmount;
    public List<BudgetCategoryDto> categories;
}
