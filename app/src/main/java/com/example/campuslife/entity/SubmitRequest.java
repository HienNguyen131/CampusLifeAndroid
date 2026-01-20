package com.example.campuslife.entity;

import java.util.List;

public class SubmitRequest {
    public long attemptId;
    public List<SubmitAnswer> answers;
    public SubmitRequest(long attemptId, List<SubmitAnswer> answers){
        this.attemptId = attemptId; this.answers = answers;
    }
}