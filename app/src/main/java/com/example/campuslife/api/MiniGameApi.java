package com.example.campuslife.api;

import com.example.campuslife.entity.AttemptDetailResponse;
import com.example.campuslife.entity.MiniGameQuestionsResponse;
import com.example.campuslife.entity.Minigame;

import com.example.campuslife.entity.AttemptDetailResponse;
import com.example.campuslife.entity.SubmitRequest;
import com.example.campuslife.entity.SubmitResultResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MiniGameApi {
    @GET("api/minigames/activity/{activityId}")
    Call<ApiResponse<Minigame>> getMinigameByActivityId(
            @Path("activityId") long activityId
    );

    @POST("api/minigames/{miniGameId}/start")
    Call<ApiResponse<AttemptDetailResponse>> start(
            @Path("miniGameId") long miniGameId
    );

    @POST("api/minigames/attempts/{attemptId}/submit")
    Call<ApiResponse<SubmitResultResponse>> submit(
            @Path("attemptId") long attemptId,
            @Body Map<String, Object> request
    );

    @GET("api/minigames/{miniGameId}/questions")
    Call<ApiResponse<MiniGameQuestionsResponse>> getQuestions(
            @Path("miniGameId") long miniGameId
    );
    @GET("api/minigames/attempts/{attemptId}")
    Call<ApiResponse<AttemptDetailResponse>> result(
            @Path("attemptId") long attemptId
    );

}

