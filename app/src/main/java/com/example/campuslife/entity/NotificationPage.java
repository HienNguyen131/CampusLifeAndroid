package com.example.campuslife.entity;

import java.util.List;

public class NotificationPage {

    private List<AppNotification> content;
    private int totalElements;
    private int totalPages;
    private int size;
    private int number;


    public List<AppNotification> getContent() {
        return content;
    }

    public void setContent(List<AppNotification> content) {
        this.content = content;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
