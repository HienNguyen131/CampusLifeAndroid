package com.example.campuslife.entity;

import java.time.LocalDateTime;

public class ActivityReminderResponse {
    public Long reminderId;
    public Long eventId;
    public String eventName;
    public String location;
    public String startDate;

    public boolean remind1Day;
    public boolean remind1Hour;
    public Long registrationId;

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public Long getReminderId() {
        return reminderId;
    }

    public void setReminderId(Long reminderId) {
        this.reminderId = reminderId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public boolean isRemind1Day() {
        return remind1Day;
    }

    public void setRemind1Day(boolean remind1Day) {
        this.remind1Day = remind1Day;
    }

    public boolean isRemind1Hour() {
        return remind1Hour;
    }

    public void setRemind1Hour(boolean remind1Hour) {
        this.remind1Hour = remind1Hour;
    }


}

