package com.example.campuslife.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DeviceTokenApi {

    @POST("/api/device-tokens")
    Call<ApiResponse<Void>> saveToken(@Body Map<String, String> body);
}
