package com.example.campuslife.entity.preparation;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;

public class PreparationTaskDto implements Serializable {
    public Long id;
    public Long activityId;

    @SerializedName(value = "ownerId", alternate = { "assigneeId", "assignedToId" })
    public Long assigneeId;

    @SerializedName(value = "ownerName", alternate = { "assigneeName", "assignedToName" })
    public String assigneeName;

    public String title;
    public String description;
    public String deadline;
    public BigDecimal allocatedAmount;
    public Boolean isFinancial;
    public String status;
}
