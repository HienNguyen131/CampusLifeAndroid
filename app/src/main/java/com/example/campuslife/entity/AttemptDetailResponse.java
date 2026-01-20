package com.example.campuslife.entity;

import java.time.LocalDateTime;
import java.util.List;

public class AttemptDetailResponse {
    private Long id;
    private String status;
    private Integer correctCount;
    private Integer totalQuestions;
    private Integer pointsEarned;
    private Integer requiredCorrectAnswers;
    private List<QuestionResult> questions;
    private String startedAt;
    private String submittedAt;

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }

    public Integer getRequiredCorrectAnswers() { return requiredCorrectAnswers; }
    public void setRequiredCorrectAnswers(Integer requiredCorrectAnswers) { this.requiredCorrectAnswers = requiredCorrectAnswers; }

    public List<QuestionResult> getQuestions() { return questions; }
    public void setQuestions(List<QuestionResult> questions) { this.questions = questions; }
}
