package com.example.campuslife.api;

import com.example.campuslife.entity.ActivityReminderResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ActivityReminderAPI {
    @GET("/api/reminder/student/{studentId}")
    Call<List<ActivityReminderResponse>>getRemindersByStudent (@Path("studentId") long studentId);
}
