package com.example.campuslife.api;

import com.example.campuslife.entity.Student;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StudentApi {
    @GET("/api/students/search")
    Call<ApiResponse<StudentSearchResponse>> searchStudents(
            @Query("keyword") String keyword,
            @Query("page") int page,
            @Query("size") int size
    );

    class StudentSearchResponse {
        public List<Student> content;
    }
}
