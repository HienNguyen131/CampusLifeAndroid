package com.example.campuslife.entity;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ActivitySeries {
    private Long id;
    private String name;
    private String description;

    @SerializedName("registrationStartDate")
    private String registrationStartDate;

    @SerializedName("registrationDeadline")
    private String registrationDeadline;

    @SerializedName("milestonePoints")
    private String milestonePoints;

    private String scoreType;
    private Boolean requiresApproval;
    private Integer ticketQuantity;

    private String createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRegistrationStartDate() {
        return registrationStartDate;
    }

    public void setRegistrationStartDate(String registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    public String getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(String registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public String getMilestonePoints() {
        return milestonePoints;
    }

    public void setMilestonePoints(String milestonePoints) {
        this.milestonePoints = milestonePoints;
    }

    public String getScoreType() {
        return scoreType;
    }

    public void setScoreType(String scoreType) {
        this.scoreType = scoreType;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public Integer getTicketQuantity() {
        return ticketQuantity;
    }

    public void setTicketQuantity(Integer ticketQuantity) {
        this.ticketQuantity = ticketQuantity;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
