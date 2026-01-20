package com.example.campuslife.api;

import com.example.campuslife.entity.AppNotification;
import com.example.campuslife.entity.NotificationPage;
import com.example.campuslife.entity.Student;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationsApi {
    @GET("/api/notifications")
    Call<ApiResponse<NotificationPage>> getMyNotifications(
            @Query("page") int page,
            @Query("size") int size
    );


    @PUT("/api/notifications/{notificationId}/read")
    Call<ApiResponse<AppNotification>> markAsRead(
            @Path("notificationId") long notificationId
    );

}
