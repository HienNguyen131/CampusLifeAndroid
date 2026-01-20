package com.example.campuslife.api;

import com.example.campuslife.entity.Activity;
import com.example.campuslife.entity.ActivitySeries;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SeriesApi {
    @GET("/api/series")
    Call<ApiResponse<List<ActivitySeries>>> getAllSeries();
    @GET("api/series/{id}")
    Call<ApiResponse<ActivitySeries>> detail(@Path("id") long id);
    @GET("/api/series/{id}/activities")
    Call<ApiResponse<List<Activity>>> getActivitySeries(@Path("id") long id);

}
