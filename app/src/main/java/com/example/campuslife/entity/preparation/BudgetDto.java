package com.example.campuslife.entity.preparation;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BudgetDto implements Serializable {
    @SerializedName(value = "totalAmount", alternate = { "total", "budget" })
    public String totalAmount;

    @SerializedName(value = "spentAmount", alternate = { "spent" })
    public String spentAmount;

    @SerializedName(value = "remainingAmount", alternate = { "remaining", "remain" })
    public String remainingAmount;
}
