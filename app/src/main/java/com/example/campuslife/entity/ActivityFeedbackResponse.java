package com.example.campuslife.entity;

public class ActivityFeedbackResponse {
    private long studentId;
    private long activityId;
    private float rating;
    private String comment;

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public long getActivityId() { return activityId; }
    public void setActivityId(long activityId) { this.activityId = activityId; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
