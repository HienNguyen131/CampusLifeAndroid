package com.example.campuslife.auth;

public class TokenResponse {
    public boolean status;
    public String message;
    public Body body;

    public static class Body {
        public String token;
        public String refreshToken;
    }
}