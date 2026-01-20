package com.example.campuslife.entity;

public class SubmitAnswer {
    public long questionId;
    public long optionId;
    public SubmitAnswer(long q, long o){ this.questionId=q; this.optionId=o; }
}
