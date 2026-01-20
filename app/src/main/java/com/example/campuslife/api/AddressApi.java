package com.example.campuslife.api;

import com.example.campuslife.entity.Address;
import com.example.campuslife.entity.Department;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AddressApi {
    @GET("/api/departments")
    Call<List<Department>> getAll();
    @GET("/api/addresses/my")
    Call<ApiResponse<Address>> getMyAddress();
    @FormUrlEncoded
    @POST("/api/addresses/my")
    Call<ApiResponse<Object>> createMyAddress(
            @Field("provinceCode") int provinceCode,
            @Field("provinceName") String provinceName,
            @Field("wardCode") int wardCode,
            @Field("wardName") String wardName,
            @Field("street") String street,
            @Field("note") String note
    );

    @FormUrlEncoded
    @PUT("/api/addresses/my")
    Call<ApiResponse<Object>> updateMyAddress(
            @Field("provinceCode") int provinceCode,
            @Field("provinceName") String provinceName,
            @Field("wardCode") int wardCode,
            @Field("wardName") String wardName,
            @Field("street") String street,
            @Field("note") String note
    );

}
