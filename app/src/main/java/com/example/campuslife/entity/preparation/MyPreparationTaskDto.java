package com.example.campuslife.entity.preparation;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;

public class MyPreparationTaskDto implements Serializable {
    public Long id;
    public Long activityId;

    @SerializedName(value = "ownerId", alternate = {"assigneeId"})
    public Long ownerId;

    @SerializedName(value = "ownerName", alternate = {"assigneeName"})
    public String ownerName;

    public String title;
    public String description;
    public String deadline;
    public BigDecimal allocatedAmount;

    @SerializedName(value = "isFinancial", alternate = {"financial"})
    public Boolean isFinancial;

    public String status;

    @SerializedName(value = "myRole", alternate = {"role"})
    public String myRole;
}
