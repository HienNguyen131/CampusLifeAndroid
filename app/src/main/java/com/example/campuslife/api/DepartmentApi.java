package com.example.campuslife.api;

import com.example.campuslife.entity.Department;
import com.example.campuslife.entity.StudentClass;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DepartmentApi {
    @GET("/api/departments")
    Call<List<Department>> getAll();

    @GET("api/departments/{id}")
    Call<Department> getById(@Path("id") Long id);

    @GET("/api/classes/department/{departmentId}")
    Call<ApiResponse<List<StudentClass>>> getClassByDepartment(@Path("departmentId") Long id);

}
