package com.example.campuslife.entity;

import java.util.List;

public class Question {
    public long id;
    public String questionText;
    public int displayOrder;
    public List<Option> options;
}
