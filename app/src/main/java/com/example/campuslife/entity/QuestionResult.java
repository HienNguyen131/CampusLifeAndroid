package com.example.campuslife.entity;

import java.util.List;

public class QuestionResult {
    public Long id;
    public String questionText;
    public Integer displayOrder;
    public List<OptionResult> options;
    public Long correctOptionId;
    public Long selectedOptionId;
    public boolean isCorrect;
}
