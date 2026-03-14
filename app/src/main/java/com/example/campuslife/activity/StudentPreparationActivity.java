package com.example.campuslife.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.adapter.PreparationEventAdapter;
import com.example.campuslife.api.ActivityApi;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.api.PreparationApi;
import com.example.campuslife.entity.Activity;
import com.example.campuslife.entity.preparation.PreparationDashboardDto;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentPreparationActivity extends AppCompatActivity {

    private ProgressBar progress;
    private TextView tvEmpty;
    private RecyclerView rvEvents;
    private PreparationEventAdapter adapter;
    private boolean dashboardErrorShown = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_preparation);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvEvents = findViewById(R.id.rvEvents);

        adapter = new PreparationEventAdapter(activityId -> {
            Intent i = new Intent(this, StudentPreparationDetailActivity.class);
            i.putExtra("activityId", activityId);
            startActivity(i);
        });

        rvEvents.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rvEvents.setAdapter(adapter);

        load();
    }

    private void load() {
        showLoading(true);
        tvEmpty.setVisibility(View.GONE);

        PreparationApi api = ApiClient.preparation(this);
        api.myPreparationActivityIds().enqueue(new Callback<ApiResponse<List<Long>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Long>>> call, Response<ApiResponse<List<Long>>> resp) {
                if (!resp.isSuccessful()) {
                    showLoading(false);
                    toast(extractHttpMessage(resp));
                    return;
                }
                if (resp.body() == null || !resp.body().isStatus()) {
                    showLoading(false);
                    toast(resp.body() != null ? resp.body().getMessage() : "Lỗi dữ liệu");
                    return;
                }

                List<Long> ids = resp.body().getData();
                if (ids == null || ids.isEmpty()) {
                    showLoading(false);
                    adapter.submit(new ArrayList<>());
                    tvEmpty.setVisibility(View.VISIBLE);
                    return;
                }

                fetchDashboardsAndActivities(ids);
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Long>>> call, Throwable t) {
                showLoading(false);
                toast("Lỗi mạng: " + (t.getMessage() != null ? t.getMessage() : ""));
            }
        });
    }

    private static class PendingItem {
        public long activityId;
        public Activity activity;
        public PreparationDashboardDto dashboard;
        public boolean gotActivity;
        public boolean gotDashboard;
        public boolean finished;
    }

    private void fetchDashboardsAndActivities(List<Long> ids) {
        PreparationApi prep = ApiClient.preparation(this);
        ActivityApi activities = ApiClient.activities(this);

        List<PreparationEventAdapter.Item> out = new ArrayList<>();
        final int total = ids.size();
        final int[] done = { 0 };

        for (Long idObj : ids) {
            long activityId = idObj != null ? idObj : -1;
            if (activityId <= 0) {
                done[0]++;
                if (done[0] >= total)
                    finishDashboards(out);
                continue;
            }

            PendingItem pending = new PendingItem();
            pending.activityId = activityId;

            prep.getDashboard(activityId).enqueue(new Callback<ApiResponse<PreparationDashboardDto>>() {
                @Override
                public void onResponse(Call<ApiResponse<PreparationDashboardDto>> call,
                        Response<ApiResponse<PreparationDashboardDto>> resp) {
                    if (!resp.isSuccessful()) {
                        if (!dashboardErrorShown) {
                            dashboardErrorShown = true;
                            toast("Dashboard: " + extractHttpMessage(resp));
                        }
                    } else if (resp.body() != null && resp.body().isStatus()) {
                        pending.dashboard = resp.body().getData();
                    }
                    pending.gotDashboard = true;
                    tryFinishOne(pending, out, done, total);
                }

                @Override
                public void onFailure(Call<ApiResponse<PreparationDashboardDto>> call, Throwable t) {
                    if (!dashboardErrorShown) {
                        dashboardErrorShown = true;
                        toast("Dashboard: Lỗi mạng");
                    }
                    pending.gotDashboard = true;
                    tryFinishOne(pending, out, done, total);
                }
            });

            activities.detail(activityId).enqueue(new Callback<ApiResponse<Activity>>() {
                @Override
                public void onResponse(Call<ApiResponse<Activity>> call, Response<ApiResponse<Activity>> resp) {
                    if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                        pending.activity = resp.body().getData();
                    } else {
                        Activity a = new Activity();
                        a.id = activityId;
                        a.name = "Activity #" + activityId;
                        pending.activity = a;
                    }
                    pending.gotActivity = true;
                    tryFinishOne(pending, out, done, total);
                }

                @Override
                public void onFailure(Call<ApiResponse<Activity>> call, Throwable t) {
                    Activity a = new Activity();
                    a.id = activityId;
                    a.name = "Activity #" + activityId;
                    pending.activity = a;
                    pending.gotActivity = true;
                    tryFinishOne(pending, out, done, total);
                }
            });
        }
    }

    private void tryFinishOne(PendingItem pending, List<PreparationEventAdapter.Item> out, int[] done, int total) {
        if (pending.finished)
            return;
        if (!pending.gotActivity || !pending.gotDashboard)
            return;
        pending.finished = true;

        PreparationEventAdapter.Item it = new PreparationEventAdapter.Item();
        it.activity = pending.activity;
        if (pending.dashboard != null) {
            it.dashboard = pending.dashboard;
        } else {
            PreparationDashboardDto d = new PreparationDashboardDto();
            d.hasPreparation = true;
            d.financeMessage = "Không tải được dữ liệu";
            it.dashboard = d;
        }
        out.add(it);

        done[0]++;
        if (done[0] >= total)
            finishDashboards(out);
    }

    private void finishDashboards(List<PreparationEventAdapter.Item> out) {
        showLoading(false);
        adapter.submit(out);
        tvEmpty.setVisibility(out == null || out.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private String extractHttpMessage(Response<?> resp) {
        if (resp == null) return "Lỗi";
        try {
            ResponseBody err = resp.errorBody();
            if (err != null) {
                String raw = err.string();
                JsonElement el = JsonParser.parseString(raw);
                if (el != null && el.isJsonObject()) {
                    JsonObject obj = el.getAsJsonObject();
                    if (obj.has("message") && !obj.get("message").isJsonNull()) {
                        String m = obj.get("message").getAsString();
                        if (m != null && !m.trim().isEmpty()) return m;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "HTTP " + resp.code();
    }
}
