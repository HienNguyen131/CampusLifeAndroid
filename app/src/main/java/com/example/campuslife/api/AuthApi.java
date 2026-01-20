package com.example.campuslife.api;

import com.example.campuslife.auth.LoginRequest;
import com.example.campuslife.auth.RefreshRequest;
import com.example.campuslife.auth.TokenResponse;
import com.example.campuslife.entity.ForgotPasswordRequest;
import com.example.campuslife.entity.ResetPasswordRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("api/auth/login")
    Call<TokenResponse> login(@Body LoginRequest req);

    @POST("api/auth/refresh")
    Call<TokenResponse> refresh(@Body RefreshRequest req);

}

