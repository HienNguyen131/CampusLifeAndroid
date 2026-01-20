package com.example.campuslife.entity;

import java.util.List;

public class MiniGameQuestionsResponse {
    public long miniGameId;
    public String title;
    public String description;
    public int questionCount;
    public int timeLimit;
    public List<Question> questions;
}
