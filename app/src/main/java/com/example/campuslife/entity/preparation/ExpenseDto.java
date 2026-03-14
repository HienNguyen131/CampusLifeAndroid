package com.example.campuslife.entity.preparation;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ExpenseDto implements Serializable {
    public Long id;

    public String amount;

    public String description;

    public String createdAt;

    @SerializedName(value = "reportedByName", alternate = { "createdByName" })
    public String reportedByName;

    public String evidenceUrl;

    public Boolean approved;
}
