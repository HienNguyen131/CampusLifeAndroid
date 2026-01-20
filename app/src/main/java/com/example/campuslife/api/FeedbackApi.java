package com.example.campuslife.api;

import com.example.campuslife.entity.ActivityFeedbackRequest;
import com.example.campuslife.entity.ActivityFeedbackResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface FeedbackApi {

    @POST("/api/ratings/create")
    Call<ApiResponse<Object>> createRating(
            @Query("activityId") long activityId,
            @Query("studentId") long studentId,
            @Query("rating") float rating,
            @Query("comment") String comment
    );
    @GET("/api/ratings/by")
    Call<ActivityFeedbackResponse> getRatingByActivityAndStudent(
            @Query("activityId") long activityId,
            @Query("studentId") long studentId
    );
}
