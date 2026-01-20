package com.example.campuslife.api;

import com.example.campuslife.entity.ActivityPhotoResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PhotoApi {
    @GET("/api/activities/photos/all")
    Call<ApiResponse<List<ActivityPhotoResponse>>> getAllPhotos();
    @GET("/api/activities/{id}/photos")
    Call<ApiResponse<List<ActivityPhotoResponse>>> getActivityPhotos(
            @Path("id") Long activityId
    );

}
