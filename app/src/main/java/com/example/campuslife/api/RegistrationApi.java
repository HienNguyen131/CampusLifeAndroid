package com.example.campuslife.api;

import com.example.campuslife.entity.Activity;
import com.example.campuslife.entity.ActivityParticipationRequest;
import com.example.campuslife.entity.ActivityParticipationResponse;
import com.example.campuslife.entity.ActivityRegistrationRequest;
import com.example.campuslife.entity.ActivityRegistrationResponse;
import com.example.campuslife.entity.CheckInQrRequest;
import com.example.campuslife.entity.CheckInRespone;
import com.example.campuslife.entity.TokenValidationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RegistrationApi {
    @POST("api/registrations")
    Call<ApiResponse<ActivityRegistrationResponse>> register(
            @Body ActivityRegistrationRequest body
    );
    @GET("api/registrations/my")
    Call<ApiResponse<List<ActivityRegistrationResponse>>> getMyRegistrations(
            );

    @GET("api/registrations/{registrationId}")
    Call<ApiResponse<ActivityRegistrationResponse>> detail(@Path("registrationId") Long registrationId);

    @DELETE("api/registrations/activity/{activityId}")
    Call<ApiResponse<Void>> cancelRegistration(
            @Path("activityId") Long activityId,
            @Header("Authorization") String authHeader
    );
    @GET("api/registrations/my/{status}")
    Call<ApiResponse<List<ActivityRegistrationResponse>>> getMyRegistrationsStatus(
            @Path("status") String status
    );

    @GET("api/registrations/search")
    Call<ApiResponse<List<ActivityRegistrationResponse>>> search(
            @Query("keyword") String keyword,
            @Query("status") String status
    );

    @POST("api/registrations/checkin/qr")
    Call<ApiResponse<CheckInRespone>> checkInQr(@Body CheckInQrRequest request);


}
