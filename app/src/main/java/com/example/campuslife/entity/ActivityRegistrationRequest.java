package com.example.campuslife.entity;

public class ActivityRegistrationRequest {
    private long activityId;


    public ActivityRegistrationRequest(long activityId) {
        this.activityId = activityId;

    }

    public long getActivityId() { return activityId; }

}
