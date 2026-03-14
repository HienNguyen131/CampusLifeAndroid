package com.example.campuslife.entity.preparation;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PreparationTaskDto implements Serializable {
    public Long id;

    public String title;

    public String description;

    public String deadline;

    @SerializedName(value = "assigneeId", alternate = { "assignedToId" })
    public Long assigneeId;

    @SerializedName(value = "assigneeName", alternate = { "assignedToName" })
    public String assigneeName;

    public String status;
}
