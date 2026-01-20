package com.example.campuslife.entity;

import java.util.List;

public class ScoreHistoryResponse {

    private Long studentId;
    private String studentCode;
    private String studentName;
    private Long semesterId;
    private String semesterName;
    private String scoreType;
    private Double currentScore;

    private List<ScoreItem> scoreHistories;
    private List<ActivityParticipationResponse> activityParticipations;

    private int totalRecords;
    private int page;
    private int size;
    private int totalPages;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }

    public String getScoreType() {
        return scoreType;
    }

    public void setScoreType(String scoreType) {
        this.scoreType = scoreType;
    }

    public Double getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(Double currentScore) {
        this.currentScore = currentScore;
    }

    public List<ScoreItem> getScoreHistories() {
        return scoreHistories;
    }

    public void setScoreHistories(List<ScoreItem> scoreHistories) {
        this.scoreHistories = scoreHistories;
    }

    public List<ActivityParticipationResponse> getActivityParticipations() {
        return activityParticipations;
    }

    public void setActivityParticipations(List<ActivityParticipationResponse> activityParticipations) {
        this.activityParticipations = activityParticipations;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}

