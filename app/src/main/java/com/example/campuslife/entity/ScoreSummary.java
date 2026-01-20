package com.example.campuslife.entity;

import java.util.List;

public class ScoreSummary {
    private String scoreType;
    private double total;
    private List<ScoreItem> items;

    public String getScoreType() { return scoreType; }
    public double getTotal() { return total; }
    public List<ScoreItem> getItems() { return items; }
}
