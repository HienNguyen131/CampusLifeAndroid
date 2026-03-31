package com.example.campuslife.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.campuslife.BuildConfig;
import com.example.campuslife.R;
import com.example.campuslife.adapter.PreparationPagerAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.Activity;
import com.example.campuslife.entity.Student;
import com.example.campuslife.entity.preparation.PreparationDashboardDto;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentPreparationDetailActivity extends AppCompatActivity {

    private long activityId;
    private long studentId = -1;
    private boolean showFinance = false;
    private boolean gotStudentId = false;
    private boolean gotDashboard = false;
    private boolean pagerSetup = false;

    private ProgressBar progress;
    private ShapeableImageView imgBanner;
    private TextView tvName, tvTime, tvLocation;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_preparation_detail);

        activityId = getIntent().getLongExtra("activityId", -1);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        progress = findViewById(R.id.progress);
        imgBanner = findViewById(R.id.imgBanner);
        tvName = findViewById(R.id.tvName);
        tvTime = findViewById(R.id.tvTime);
        tvLocation = findViewById(R.id.tvLocation);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        loadHeader();
        loadStudentId();
        loadDashboard();
    }

    private void loadHeader() {
        ApiClient.activities(this).detail(activityId).enqueue(new Callback<ApiResponse<Activity>>() {
            @Override
            public void onResponse(Call<ApiResponse<Activity>> call, Response<ApiResponse<Activity>> resp) {
                if (!resp.isSuccessful() || resp.body() == null || !resp.body().isStatus())
                    return;

                Activity a = resp.body().getData();
                if (a == null)
                    return;

                tvName.setText(a.getName() != null ? a.getName() : "");
                tvTime.setText(fmtDateOnly(a.getStartDate()) + " - " + fmtDateOnly(a.getEndDate()));
                tvLocation.setText(a.getLocation() != null ? a.getLocation() : "");

                String img = a.getBannerUrl();
                img = fixLocalhost(img);

                String full = null;
                if (img != null && !img.isEmpty()) {
                    if (img.startsWith("http")) {
                        full = img;
                    } else {
                        String base = BuildConfig.BASE_URL;
                        if (!base.endsWith("/"))
                            base += "/";
                        if (img.startsWith("/"))
                            img = img.substring(1);
                        if (!img.startsWith("uploads/"))
                            img = "uploads/" + img;
                        full = base + img;
                    }
                }

                Glide.with(StudentPreparationDetailActivity.this)
                        .load(full)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(imgBanner);
            }

            @Override
            public void onFailure(Call<ApiResponse<Activity>> call, Throwable t) {
            }
        });
    }

    private void loadStudentId() {
        ApiClient.profile(this).getMyProfile().enqueue(new Callback<ApiResponse<Student>>() {
            @Override
            public void onResponse(Call<ApiResponse<Student>> call, Response<ApiResponse<Student>> resp) {
                if (!resp.isSuccessful() || resp.body() == null || !resp.body().isStatus()) {
                    gotStudentId = true;
                    studentId = -1;
                    maybeSetupPager();
                    return;
                }
                Student s = resp.body().getData();
                studentId = s != null && s.getId() != null ? s.getId() : -1;
                gotStudentId = true;
                maybeSetupPager();
            }

            @Override
            public void onFailure(Call<ApiResponse<Student>> call, Throwable t) {
                gotStudentId = true;
                studentId = -1;
                maybeSetupPager();
            }
        });
    }

    private void loadDashboard() {
        showLoading(true);
        ApiClient.preparation(this).getDashboard(activityId)
                .enqueue(new Callback<ApiResponse<PreparationDashboardDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PreparationDashboardDto>> call,
                            Response<ApiResponse<PreparationDashboardDto>> resp) {
                        showLoading(false);
                        gotDashboard = true;
                        if (!resp.isSuccessful() || resp.body() == null || !resp.body().isStatus()) {
                            toast(resp.body() != null ? resp.body().getMessage() : ("HTTP " + resp.code()));
                            showFinance = false;
                            maybeSetupPager();
                            return;
                        }
                        PreparationDashboardDto d = resp.body().getData();
                        showFinance = d != null && d.activityBudget != null;
                        maybeSetupPager();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PreparationDashboardDto>> call, Throwable t) {
                        showLoading(false);
                        gotDashboard = true;
                        showFinance = false;
                        maybeSetupPager();
                    }
                });
    }

    private void maybeSetupPager() {
        if (pagerSetup)
            return;
        if (activityId <= 0)
            return;
        if (!gotStudentId || !gotDashboard)
            return;

        PreparationPagerAdapter adapter = new PreparationPagerAdapter(this, activityId, studentId, showFinance);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (showFinance) {
                tab.setText(position == 0 ? "Nhiệm vụ" : "Tài chính");
            } else {
                tab.setText("Nhiệm vụ");
            }
        }).attach();

        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);
        pagerSetup = true;
    }

    private void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private String fmtDateOnly(String isoDateTime) {
        if (isoDateTime == null)
            return "-";
        try {
            LocalDateTime dt = LocalDateTime.parse(isoDateTime);
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return isoDateTime;
        }
    }

    private String fixLocalhost(String url) {
        if (url == null)
            return null;
        return url.replace("http://localhost:8080", "http://10.0.2.2:8080");
    }
}
