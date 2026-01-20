package com.example.campuslife.auth;

public class LoginRequest {
    public final String username;
    public final String password;
    public LoginRequest(String u, String p) { this.username = u; this.password = p; }
}