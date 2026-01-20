package com.example.campuslife.api;

import com.example.campuslife.entity.AcademicYear;
import com.example.campuslife.entity.ActivitySeries;
import com.example.campuslife.entity.Semester;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SemesterApi {
    @GET("/api/academic/years")
    Call<ApiResponse<List<AcademicYear>>> listYears();


    @GET("/api/academic/years/{id}/semesters")
    Call<ApiResponse<List<Semester>>> listSemesters(@Path("id") long id);

}
