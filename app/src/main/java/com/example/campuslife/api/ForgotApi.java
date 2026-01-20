package com.example.campuslife.api;

import com.example.campuslife.entity.ForgotPasswordRequest;
import com.example.campuslife.entity.ResetPasswordRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ForgotApi {
    @POST("api/auth/forgot-password")
    Call<ApiResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("api/auth/reset-password")
    Call<ApiResponse> resetPassword(@Body ResetPasswordRequest request);
}
