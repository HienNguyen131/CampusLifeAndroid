package com.example.campuslife.api;

import com.example.campuslife.entity.preparation.CreateExpenseRequest;
import com.example.campuslife.entity.preparation.ExpenseDto;
import com.example.campuslife.entity.preparation.PreparationDashboardDto;
import com.example.campuslife.entity.preparation.PreparationTaskDto;
import com.example.campuslife.entity.preparation.UpdateTaskStatusRequest;
import com.example.campuslife.entity.preparation.UploadResultDto;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PreparationApi {
    @GET("/api/preparation/my/activity-ids")
    Call<ApiResponse<List<Long>>> myPreparationActivityIds();

    @GET("/api/preparation/activities/{activityId}/dashboard")
    Call<ApiResponse<PreparationDashboardDto>> getDashboard(@Path("activityId") long activityId);

    @PUT("/api/preparation/tasks/{taskId}/status")
    Call<ApiResponse<PreparationTaskDto>> updateTaskStatus(
            @Path("taskId") long taskId,
            @Body UpdateTaskStatusRequest body);

    @GET("/api/preparation/activities/{activityId}/expenses")
    Call<ApiResponse<List<ExpenseDto>>> listExpenses(
            @Path("activityId") long activityId,
            @Query("status") String status);

    @Multipart
    @POST("/api/preparation/activities/{activityId}/expenses/evidence")
    Call<ApiResponse<UploadResultDto>> uploadEvidence(
            @Path("activityId") long activityId,
            @Part MultipartBody.Part file);

    @POST("/api/preparation/activities/{activityId}/expenses")
    Call<ApiResponse<ExpenseDto>> createExpense(
            @Path("activityId") long activityId,
            @Body CreateExpenseRequest body);
}
