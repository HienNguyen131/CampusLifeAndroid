package com.example.campuslife.entity.preparation;

import java.io.Serializable;

public class FinanceOverviewReportDto implements Serializable {
    public Long activityId;
    public String totalBudget;
    public String totalAllocatedToTasks;
    public String totalApprovedSpent;
    public String varianceAllocatedVsApproved;
}
