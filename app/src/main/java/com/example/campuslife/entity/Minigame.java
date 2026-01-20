package com.example.campuslife.entity;

import java.math.BigDecimal;

public class Minigame {

    private Long id;

    private String title;

    private String description;


    private Integer questionCount;


    private Integer timeLimit;


    private boolean isActive = true;


    private String type;


    private Activity activity;


    private Integer requiredCorrectAnswers;


    private BigDecimal rewardPoints;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Integer getRequiredCorrectAnswers() {
        return requiredCorrectAnswers;
    }

    public void setRequiredCorrectAnswers(Integer requiredCorrectAnswers) {
        this.requiredCorrectAnswers = requiredCorrectAnswers;
    }

    public BigDecimal getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(BigDecimal rewardPoints) {
        this.rewardPoints = rewardPoints;
    }
}
