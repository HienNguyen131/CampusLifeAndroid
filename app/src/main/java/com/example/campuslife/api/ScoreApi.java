package com.example.campuslife.api;

import com.example.campuslife.entity.ScoreHistoryResponse;
import com.example.campuslife.entity.ScoreResponse;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ScoreApi {
    @GET("/api/scores/student/{studentId}/semester/{semesterId}")
    Call<ApiResponse<ScoreResponse>> getScores(
            @Path("studentId") long studentId,
            @Path("semesterId") long semesterId
    );
    @GET("api/scores/history/student/{studentId}")
    Call<ApiResponse<ScoreHistoryResponse>> getScoreHistory(
            @Path("studentId") Long studentId,
            @Query("semesterId") Long semesterId,
            @Query("scoreType") String scoreType,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

}
