package com.example.campuslife.api;

import com.example.campuslife.entity.Activity;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import retrofit2.http.POST;
import com.example.campuslife.entity.CreateActivityRequest;

public interface ActivityApi {
    @POST("/api/activities")
    Call<ApiResponse<Activity>> createActivity(@retrofit2.http.Body CreateActivityRequest request);

    @GET("api/activities/score-type/{scoreType}")
    Call<List<Activity>> byType(@Path("scoreType") String scoreType);

    @GET("/api/activities")
    Call<ApiResponse<List<Activity>>> getAllActivities();

    @GET("/api/activities")
    Call<ResponseBody> listAllRaw(@Query("type") String type);

    @GET("api/activities/month")
    Call<List<Activity>> thisMonth();

    @GET("api/activities/my")
    Call<List<Activity>> myActivities();
    @GET("api/activities/{id}")
    Call<ApiResponse<Activity>> detail(@Path("id") long id);
    @GET("api/activities/upcoming")
    Call<List<Activity>> searchActivities(
            @Query("keyword") String keyword
    );


}
