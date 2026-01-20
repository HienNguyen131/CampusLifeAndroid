package com.example.campuslife.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.campuslife.R;
import com.example.campuslife.adapter.SeriesEventAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.Activity;
import com.example.campuslife.entity.ActivityRegistrationResponse;
import com.example.campuslife.entity.ActivitySeries;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeriesDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView txtSeriesName, txtSeriesDate, txtRewardPoints, txtMinimumSessions;
    private RecyclerView rvSeriesEvent;
    private SeriesEventAdapter adapter;
    private long id ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.series_detail);


        btnBack = findViewById(R.id.btnBack);
        txtSeriesName = findViewById(R.id.txtSeriesName);
        txtSeriesDate = findViewById(R.id.txtSeriesDate);
        txtRewardPoints = findViewById(R.id.txtRewardPoints);
        txtMinimumSessions = findViewById(R.id.txtMinimumSessions);
        rvSeriesEvent = findViewById(R.id.rvSeriesEvent);
        rvSeriesEvent.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SeriesEventAdapter();
        rvSeriesEvent.setAdapter(adapter);


        btnBack.setOnClickListener(v -> finish());

        id = getIntent().getLongExtra("series_id", -1);

        if (id != -1) {
            fetchDetail(id);
        } else {
            Toast.makeText(this, "Thiếu series_id", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchDetail(Long id) {
        ApiClient.series(this)
                .detail(id)
                .enqueue(new Callback<ApiResponse<ActivitySeries>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ActivitySeries>> call,
                                           Response<ApiResponse<ActivitySeries>> resp) {
                        if (!resp.isSuccessful() || resp.body() == null) {
                            Toast.makeText(SeriesDetailActivity.this, "HTTP " + resp.code(), Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        ApiResponse<ActivitySeries> r = resp.body();

                        Log.d("SERIES_DEBUG", "success=" + r.isStatus());
                        Log.d("SERIES_DEBUG", "message=" + r.getMessage());
                        Log.d("SERIES_DEBUG", "data=" + r.getData());

                        if (r.isStatus() && r.getData() != null) {
                            bind(r.getData());
                        } else {
                            Toast.makeText(SeriesDetailActivity.this,
                                    r.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ActivitySeries>> call, Throwable t) {
                        Toast.makeText(SeriesDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
    private void fetchActivities(long seriesId) {
        ApiClient.series(this)
                .getActivitySeries(seriesId)
                .enqueue(new Callback<ApiResponse<List<Activity>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Activity>>> call,
                                           Response<ApiResponse<List<Activity>>> resp) {

                        if (!resp.isSuccessful() || resp.body() == null) return;

                        List<Activity> list = resp.body().getData();
                        if (list != null) {
                            adapter.submit(list);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Activity>>> call, Throwable t) {
                        Toast.makeText(SeriesDetailActivity.this,
                                "Lỗi: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void bind(ActivitySeries a) {
        txtSeriesName.setText(a.getName());

        txtSeriesDate.setText(
                fmtDateOnly(a.getRegistrationStartDate()) +
                        " - " +
                        fmtDateOnly(a.getRegistrationDeadline())
        );


        int totalPoint = parseMilestonePoints(a.getMilestonePoints());

        txtRewardPoints.setText(
                "Earn " + totalPoint + " points upon completion"
        );

        txtMinimumSessions.setText(
                "Minimum " + getRequiredSessions(a.getMilestonePoints()) + " sessions required"
        );

        fetchActivities(a.getId());
    }
    private int parseMilestonePoints(String json) {
        if (json == null || json.isEmpty()) return 0;

        try {
            JSONObject obj = new JSONObject(json);
            int total = 0;
            Iterator<String> keys = obj.keys();
            while (keys.hasNext()) {
                total += obj.getInt(keys.next());
            }
            return total;
        } catch (Exception e) {
            Log.e("SERIES_DEBUG", "parseMilestonePoints error", e);
            return 0;
        }
    }

    private int getRequiredSessions(String json) {
        if (json == null || json.isEmpty()) return 0;

        try {
            return new JSONObject(json).length();
        } catch (Exception e) {
            return 0;
        }
    }


    private String fmtDateOnly(String isoDateTime) {
        try {
            LocalDateTime dt = LocalDateTime.parse(isoDateTime.replace(" ", "T"));
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return isoDateTime;
        }
    }
}
