package com.example.campuslife.api;

import com.example.campuslife.entity.ActivityRegistrationResponse;
import com.example.campuslife.entity.Address;
import com.example.campuslife.entity.Student;
import com.example.campuslife.entity.StudentProfileUpdateRequest;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ProfileAPI {
    @GET("/api/student/profile")
    Call<ApiResponse<Student>> getMyProfile();



    @PUT("/api/student/profile")
    Call<ApiResponse<Object>> updateMyProfile(
            @Body StudentProfileUpdateRequest request
    );
    @Multipart
    @POST("/api/upload/image")
    Call<Map<String, Object>> uploadImage(@Part MultipartBody.Part file);


}
