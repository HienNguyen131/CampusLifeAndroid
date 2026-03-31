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

    @PUT("/api/preparation/activities/{activityId}/toggle")
    Call<ApiResponse<Object>> togglePreparation(
            @Path("activityId") long activityId,
            @Query("enabled") boolean enabled);

    @GET("/api/preparation/activities/{activityId}/dashboard")
    Call<ApiResponse<PreparationDashboardDto>> getDashboard(@Path("activityId") long activityId);

    @PUT("/api/preparation/tasks/{taskId}/status")
    Call<ApiResponse<PreparationTaskDto>> updateTaskStatus(
            @Path("taskId") long taskId,
            @Body UpdateTaskStatusRequest body);

    @GET("/api/preparation/activities/{activityId}/organizers")
    Call<ApiResponse<List<com.example.campuslife.entity.preparation.OrganizerDto>>> getOrganizers(
            @Path("activityId") long activityId);

    @PUT("/api/preparation/expenses/{expenseId}/leader-decision")
    Call<ApiResponse<ExpenseDto>> leaderDecisionExpense(
            @Path("expenseId") long expenseId,
            @Body com.example.campuslife.entity.preparation.ApproveExpenseRequest body);

    @PUT("/api/preparation/expenses/{expenseId}/admin-decision")
    Call<ApiResponse<ExpenseDto>> adminDecisionExpense(
            @Path("expenseId") long expenseId,
            @Body com.example.campuslife.entity.preparation.ApproveExpenseRequest body);

    @GET("/api/preparation/activities/{activityId}/expenses")
    Call<ApiResponse<List<ExpenseDto>>> listExpenses(
            @Path("activityId") long activityId,
            @Query("status") String status);

    @Multipart
    @POST("/api/preparation/tasks/{taskId}/expenses/evidence")
    Call<ApiResponse<UploadResultDto>> uploadEvidence(
            @Path("taskId") long taskId,
            @Part MultipartBody.Part file);

    @POST("/api/preparation/tasks/{taskId}/expenses")
    Call<ApiResponse<ExpenseDto>> createExpense(
            @Path("taskId") long taskId,
            @Body CreateExpenseRequest body);

    @POST("/api/preparation/activities/{activityId}/organizers/{studentId}")
    Call<ApiResponse<Object>> addOrganizer(
            @Path("activityId") long activityId,
            @Path("studentId") long studentId);

    @retrofit2.http.DELETE("/api/preparation/activities/{activityId}/organizers/{studentId}")
    Call<ApiResponse<Object>> removeOrganizer(
            @Path("activityId") long activityId,
            @Path("studentId") long studentId);

    @PUT("/api/preparation/activities/{activityId}/budget")
    Call<ApiResponse<com.example.campuslife.entity.preparation.ActivityBudgetDto>> upsertBudget(
            @Path("activityId") long activityId,
            @Body com.example.campuslife.entity.preparation.UpsertActivityBudgetRequest body);

    @GET("/api/preparation/activities/{activityId}/reports/finance-overview")
    Call<ApiResponse<com.example.campuslife.entity.preparation.FinanceOverviewReportDto>> getFinanceOverview(
            @Path("activityId") long activityId);

    @POST("/api/preparation/activities/{activityId}/tasks")
    Call<ApiResponse<PreparationTaskDto>> assignTask(
            @Path("activityId") long activityId,
            @Body com.example.campuslife.entity.preparation.CreatePreparationTaskRequest body);

    @GET("/api/preparation/activities/{activityId}/workload-warnings")
    Call<ApiResponse<List<com.example.campuslife.entity.preparation.WorkloadWarningDto>>> getWorkloadWarnings(
            @Path("activityId") long activityId);

    @GET("/api/preparation/stats/{studentId}")
    Call<com.example.campuslife.entity.preparation.StudentStatsDto> getStudentStats(
            @Path("studentId") long studentId);

    // --- Task Status Management ---
    @PUT("/api/preparation/tasks/{taskId}/accept")
    Call<ApiResponse<PreparationTaskDto>> acceptTask(@Path("taskId") long taskId);

    @PUT("/api/preparation/tasks/{taskId}/request-complete")
    Call<ApiResponse<PreparationTaskDto>> requestCompleteTask(@Path("taskId") long taskId);

    @PUT("/api/preparation/tasks/{taskId}/complete-decision")
    Call<ApiResponse<PreparationTaskDto>> approveTaskCompletion(
            @Path("taskId") long taskId,
            @Body com.example.campuslife.entity.preparation.ApproveTaskCompletionRequest request);

    // --- Task Members Management ---
    @GET("/api/preparation/tasks/{taskId}/members")
    Call<ApiResponse<List<com.example.campuslife.entity.preparation.PreparationTaskMemberDto>>> getTaskMembers(
            @Path("taskId") long taskId);

    @POST("/api/preparation/tasks/{taskId}/members/{studentId}")
    Call<ApiResponse<Object>> addTaskMember(
            @Path("taskId") long taskId,
            @Path("studentId") long studentId);

    @retrofit2.http.DELETE("/api/preparation/tasks/{taskId}/members/{studentId}")
    Call<ApiResponse<Object>> removeTaskMember(
            @Path("taskId") long taskId,
            @Path("studentId") long studentId);

    @POST("/api/preparation/tasks/{taskId}/leaders/{studentId}")
    Call<ApiResponse<Object>> setTaskLeader(
            @Path("taskId") long taskId,
            @Path("studentId") long studentId);

    @retrofit2.http.DELETE("/api/preparation/tasks/{taskId}/leaders/{studentId}")
    Call<ApiResponse<Object>> demoteTaskLeader(
            @Path("taskId") long taskId,
            @Path("studentId") long studentId);

    // --- Task Finance Management ---
    @GET("/api/preparation/activities/{activityId}/budget")
    Call<ApiResponse<com.example.campuslife.entity.preparation.ActivityBudgetDto>> getActivityBudget(
            @Path("activityId") long activityId);
    @PUT("/api/preparation/tasks/{taskId}/allocation")
    Call<ApiResponse<PreparationTaskDto>> allocateTaskAmount(
            @Path("taskId") long taskId,
            @Body com.example.campuslife.entity.preparation.AllocateTaskAmountRequest request);

    @POST("/api/preparation/tasks/{taskId}/fund-advances")
    Call<ApiResponse<com.example.campuslife.entity.preparation.FundAdvanceDto>> requestFundAdvance(
            @Path("taskId") long taskId,
            @Body com.example.campuslife.entity.preparation.CreateFundAdvanceRequest request);

    @GET("/api/preparation/tasks/{taskId}/fund-advances")
    Call<ApiResponse<List<com.example.campuslife.entity.preparation.FundAdvanceDto>>> listFundAdvances(
            @Path("taskId") long taskId);

    @GET("/api/preparation/tasks/{taskId}/fund-advance-source-suggestions")
    Call<ApiResponse<List<com.example.campuslife.entity.preparation.FundAdvanceSourceSuggestionDto>>> suggestFundAdvanceSources(
            @Path("taskId") long taskId,
            @Query("amount") String amount);

    @PUT("/api/preparation/fund-advances/{fundAdvanceId}/admin-decision")
    Call<ApiResponse<com.example.campuslife.entity.preparation.FundAdvanceDto>> adminDecisionFundAdvance(
            @Path("fundAdvanceId") long fundAdvanceId,
            @Body com.example.campuslife.entity.preparation.ApproveFundAdvanceRequest body);

    @PUT("/api/preparation/fund-advances/{fundAdvanceId}/return")
    Call<ApiResponse<com.example.campuslife.entity.preparation.FundAdvanceDto>> adminReturnFundAdvance(
            @Path("fundAdvanceId") long fundAdvanceId);

    @GET("/api/preparation/activities/{activityId}/fund-advance-debts")
    Call<ApiResponse<List<com.example.campuslife.entity.preparation.FundAdvanceDebtDto>>> listFundAdvanceDebts(
            @Path("activityId") long activityId,
            @Query("studentId") Long studentId);

    // --- Allocation Adjustments ---
    @GET("/api/preparation/activities/{activityId}/allocation-adjustments")
    Call<ApiResponse<List<com.example.campuslife.entity.preparation.AllocationAdjustmentRequestDto>>> listAllocationAdjustments(
            @Path("activityId") long activityId);

    @POST("/api/preparation/tasks/{taskId}/allocation-adjustments")
    Call<ApiResponse<com.example.campuslife.entity.preparation.AllocationAdjustmentRequestDto>> createAllocationAdjustment(
            @Path("taskId") long taskId,
            @Body com.example.campuslife.entity.preparation.CreateAllocationAdjustmentRequest request);

    @GET("/api/preparation/allocation-adjustments/{requestId}/source-plan")
    Call<ApiResponse<List<com.example.campuslife.entity.preparation.AllocationAdjustmentSourcePlanDto>>> getAllocationAdjustmentSourcePlan(
            @Path("requestId") long requestId);

    @PUT("/api/preparation/allocation-adjustments/{requestId}/admin-decision")
    Call<ApiResponse<com.example.campuslife.entity.preparation.AllocationAdjustmentRequestDto>> adminDecisionAllocationAdjustment(
            @Path("requestId") long requestId,
            @Body com.example.campuslife.entity.preparation.AdminDecisionAllocationAdjustmentRequest request);
}
