package com.example.campuslife.entity;

import java.time.LocalDateTime;

public class User {

    private Long id;


    private String username;


    private String password; // hashed


    private String email;


    private String role;


    private boolean isActivated = false;

    private String lastLogin;


    private String createdAt;


    private String updatedAt;


    private boolean isDeleted = false;
}
