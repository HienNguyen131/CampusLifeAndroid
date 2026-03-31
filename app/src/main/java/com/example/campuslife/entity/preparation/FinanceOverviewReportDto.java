package com.example.campuslife.entity.preparation;

import java.io.Serializable;
import java.util.List;

public class FinanceOverviewReportDto implements Serializable {
    public Long activityId;
    public String totalBudget;
    public String totalAllocatedToTasks;
    public String totalApprovedSpent;
    public String varianceAllocatedVsApproved;
    public List<BudgetCategoryDto> wallets;
    public List<TaskSpendStatusDto> tasks;
}
