package com.example.campuslife.entity;

import java.util.List;

public class ScoreResponse {

    private long studentId;
    private long semesterId;
    private List<ScoreSummary> summaries;

    public long getStudentId() { return studentId; }
    public long getSemesterId() { return semesterId; }
    public List<ScoreSummary> getSummaries() { return summaries; }
}
