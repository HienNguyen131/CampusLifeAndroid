package com.example.campuslife.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.adapter.ActivityReportAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.ActivityReportRespone;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportActivity extends AppCompatActivity {
    private ActivityReportAdapter adapter;
    private RecyclerView rvTasks;
    private ImageView btnBack;
    private TextView txtTilte;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        long activityId = getIntent().getLongExtra("activity_id", -1);
        long studentId = getIntent().getLongExtra("student_id", -1);
        if (activityId != -1 && studentId != -1) {
            loadAssignments(activityId, studentId);
        } else {
            Toast.makeText(this, "Thiếu dữ liệu cần thiết", Toast.LENGTH_SHORT).show();
            finish();
        }
        rvTasks = findViewById(R.id.rvTask);

        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActivityReportAdapter();
        rvTasks.setAdapter(adapter);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v->finish());
        TextView txtTitle = findViewById(R.id.txtTilte);
        txtTitle.setText("View the list of tasks");

    }

    private void loadAssignments(long activityId, long studentId) {
        ApiClient.activityReports(this)
                .getAssignmentsByActivityAndStudent(activityId, studentId)
                .enqueue(new Callback<ApiResponse<List<ActivityReportRespone>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ActivityReportRespone>>> call,
                                           Response<ApiResponse<List<ActivityReportRespone>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            List<ActivityReportRespone> list = response.body().getData();
                            adapter.submit(list);


                        } else {
                            Toast.makeText(ReportActivity.this, "Không tải được danh sách báo cáo", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ActivityReportRespone>>> call, Throwable t) {
                        Toast.makeText(ReportActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
