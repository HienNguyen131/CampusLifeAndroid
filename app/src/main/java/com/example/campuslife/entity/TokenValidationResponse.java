package com.example.campuslife.entity;


public class TokenValidationResponse {
    private boolean valid;
    private String message;

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }
}

