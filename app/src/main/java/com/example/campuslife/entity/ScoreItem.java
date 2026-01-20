package com.example.campuslife.entity;

import java.util.List;

public class ScoreItem {

    private Long id;
    private Double oldScore;
    private Double newScore;
    private String changeDate;
    private String reason;

    private Long activityId;
    private String activityName;
    private Long seriesId;
    private String seriesName;

    private String sourceType;  // MINIGAME, ACTIVITY
    private String changedByUsername;
    private String changedByFullName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getOldScore() {
        return oldScore;
    }

    public void setOldScore(Double oldScore) {
        this.oldScore = oldScore;
    }

    public Double getNewScore() {
        return newScore;
    }

    public void setNewScore(Double newScore) {
        this.newScore = newScore;
    }

    public String getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(String changeDate) {
        this.changeDate = changeDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public Long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Long seriesId) {
        this.seriesId = seriesId;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getChangedByUsername() {
        return changedByUsername;
    }

    public void setChangedByUsername(String changedByUsername) {
        this.changedByUsername = changedByUsername;
    }

    public String getChangedByFullName() {
        return changedByFullName;
    }

    public void setChangedByFullName(String changedByFullName) {
        this.changedByFullName = changedByFullName;
    }

    // GETTERS & SETTERS
}
