package com.example.campuslife.entity.preparation;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class PreparationDashboardDto implements Serializable {
    @SerializedName(value = "hasPreparation", alternate = { "preparation", "enabled" })
    public boolean hasPreparation;

    @SerializedName(value = "tasks", alternate = { "taskList" })
    public List<PreparationTaskDto> tasks;

    @SerializedName(value = "activityBudget", alternate = { "budget", "finance", "budgetInfo" })
    public ActivityBudgetDto activityBudget;

    @SerializedName(value = "financeMessage", alternate = { "message", "budgetMessage" })
    public String financeMessage;
}
