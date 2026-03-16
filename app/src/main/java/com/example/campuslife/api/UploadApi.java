package com.example.campuslife.api;

import okhttp3.MultipartBody;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadApi {
    @Multipart
    @POST("/api/upload/image")
    Call<Map<String, Object>> uploadImage(@Part MultipartBody.Part file);
}
