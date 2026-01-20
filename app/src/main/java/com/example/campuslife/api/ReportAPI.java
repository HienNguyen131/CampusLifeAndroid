package com.example.campuslife.api;

import com.example.campuslife.entity.ActivityRegistrationRequest;
import com.example.campuslife.entity.ActivityRegistrationResponse;
import com.example.campuslife.entity.ActivityReportRequest;
import com.example.campuslife.entity.ActivityReportRespone;
import com.example.campuslife.entity.SubmissionResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ReportAPI {
    @GET("api/assignments/activity/{activityId}/student/{studentId}")
    Call<ApiResponse<List<ActivityReportRespone>>> getAssignmentsByActivityAndStudent(
            @Path("activityId") long activityId,
            @Path("studentId") long studentId
    );
    @Multipart
    @POST("api/submissions/task/{taskId}")
    Call<ApiResponse<Object>> submitTask(
            @Path("taskId") long taskId,
            @Part("content") RequestBody content,
            @Part MultipartBody.Part file
    );
    @GET("api/submissions/task/{taskId}/my")
    Call<ApiResponse<SubmissionResponse>> getMySubmissions(
            @Path("taskId") long taskId
    );
    @DELETE("api/submissions/{submissionId}")
    Call<ApiResponse<Object>> deleteSubmission(@Path("submissionId") long submissionId);
    @Multipart
    @PUT("api/submissions/{submissionId}")
    Call<ApiResponse<Object>> updateSubmission(
            @Path("submissionId") long submissionId,
            @Part("content") RequestBody content,
            @Part MultipartBody.Part file
    );


}
