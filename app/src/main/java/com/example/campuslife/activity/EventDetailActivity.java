// EventDetailActivity.java
package com.example.campuslife.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.campuslife.BuildConfig;
import com.example.campuslife.R;
import com.example.campuslife.adapter.ActivityReportAdapter;
import com.example.campuslife.adapter.ArticleFeedAdapter;
import com.example.campuslife.adapter.GridSpacingItemDecoration;
import com.example.campuslife.adapter.PhotoAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.auth.TokenStore;
import com.example.campuslife.entity.Activity;
import com.example.campuslife.entity.ActivityPhotoResponse;
import com.example.campuslife.entity.ActivityRegistrationResponse;
import com.example.campuslife.entity.ActivityRegistrationRequest;
import com.example.campuslife.entity.Department;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.security.AccessController.getContext;

public class EventDetailActivity extends AppCompatActivity {

    private ImageView imgBanner;
    private ImageView btnBack;

    private TextView tvTitle;
    private TextView tvAddress;
    private TextView tvDate;

    private TextView tvOrganizerName;
    private TextView tvOrganizerRole;

    private TextView tvDesc;
    private TextView tvBenefits;
    private TextView tvRequirements, txtAll;
    private TextView tvContactInfo, tvLink;
    private TextView tvJoined, tvDay;

    private MaterialButton btnJoin, btnCate;

    private RecyclerView rvPhotos;
    private long activityId = -1;
    private String activityType, ScoreType;
    private PhotoAdapter adapter;

    private boolean hasMiniGame = false;

    private final DateTimeFormatter inDate = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
    private final DateTimeFormatter outDate = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        imgBanner = findViewById(R.id.imgBanner);
        btnBack = findViewById(R.id.btnBack);

        tvTitle = findViewById(R.id.tvTitle);
        tvAddress = findViewById(R.id.tvLocation);
        tvDate = findViewById(R.id.tvDate);
        btnCate = findViewById(R.id.btnCate);
        tvOrganizerName = findViewById(R.id.tvOrganiser);

        tvDesc = findViewById(R.id.tvDesc);
        tvBenefits = findViewById(R.id.tvBenefits);
        tvRequirements = findViewById(R.id.tvRequirements);
        tvContactInfo = findViewById(R.id.tvContactInfo);
        tvOrganizerRole = findViewById(R.id.tvOrganiserTilte);

        btnJoin = findViewById(R.id.btnJoin);
        tvLink = findViewById(R.id.tvLink);

        tvDay = findViewById(R.id.tvDay);

        long id = getIntent().getLongExtra("activity_id", -1);

        btnBack.setOnClickListener(v -> finish());

        if (id > 0) {
            activityId = id;
            fetchDetail(id);

        } else {
            Toast.makeText(this, "Thiếu activity_id", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnJoin.setOnClickListener(v -> tryJoinNow());
    }

    private void fetchDetail(long id) {
        ApiClient.activities(this).detail(id).enqueue(new Callback<ApiResponse<Activity>>() {
            @Override
            public void onResponse(Call<ApiResponse<Activity>> call, Response<ApiResponse<Activity>> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(EventDetailActivity.this, "HTTP " + resp.code(), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                ApiResponse<Activity> api = resp.body();
                if (!api.isStatus() || api.getData() == null) {
                    Toast.makeText(EventDetailActivity.this,
                            api.getMessage() != null ? api.getMessage() : "Không lấy được dữ liệu",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                bind(api.getData());
            }

            @Override
            public void onFailure(Call<ApiResponse<Activity>> call, Throwable t) {
                Toast.makeText(EventDetailActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showOrganizers(List<Department> list) {
        if (list == null || list.isEmpty()) {
            tvOrganizerName.setVisibility(View.GONE);
            return;
        }

        String names = list.stream()
                .map(Department::getName)
                .collect(Collectors.joining(", "));

        tvOrganizerName.setText(names);
        tvOrganizerName.setVisibility(View.VISIBLE);
    }

    private void bind(Activity a) {

        if (a.getId() != null && a.getId() > 0) {
            activityId = a.getId();
        }
        activityType = a.getType();
        Log.d("EVENT_TYPE", "activityType = " + activityType);
        ScoreType = a.getScoreType();
        if ("REN_LUYEN".equalsIgnoreCase(activityType)) {
            setTextOrHide(btnCate, "Training Point");
        } else if ("CONG_TAC_XA_HOI".equalsIgnoreCase(activityType)) {
            setTextOrHide(btnCate, "Business Point");
        } else {
            setTextOrHide(btnCate, "Social Point");
        }

        setTextOrHide(tvTitle, a.getName());
        setTextOrHide(tvAddress, a.getLocation());
        tvDate.setText(fmtDateOnly(a.getStartDate()));

        if (a.organizerIds != null && !a.organizerIds.isEmpty()) {

            StringBuilder orgNames = new StringBuilder();

            for (Long depId : a.organizerIds) {
                ApiClient.departments(this).getById(depId).enqueue(new Callback<Department>() {
                    @Override
                    public void onResponse(Call<Department> call, Response<Department> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            orgNames.append(response.body().getName()).append(", ");
                            tvOrganizerName.setText(orgNames.toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<Department> call, Throwable t) {
                    }
                });
            }
        }

        setTextOrHide(tvDesc, a.getDescription());
        setTextOrHide(tvBenefits, a.getBenefits());
        setTextOrHide(tvRequirements, a.getRequirements());
        setTextOrHide(tvContactInfo, a.getContactInfo());

        long joined = safeLong(a.getParticipantCount());
        int total = safeInt(a.getTicketQuantity());
        LocalDate now = LocalDate.now();
        LocalDate regStart = LocalDate.parse(a.getRegistrationStartDate().substring(0, 10));
        LocalDate regEnd = LocalDate.parse(a.getRegistrationDeadline().substring(0, 10));

        if (now.isBefore(regStart)) {
            tvDay.setText("Not opened");
            tvDay.setTextColor(Color.GRAY);

            btnJoin.setEnabled(false);
            btnJoin.setText("Not opened");
            btnJoin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
            return;
        }

        if (now.isAfter(regEnd)) {
            tvDay.setText("Ended");
            tvDay.setTextColor(Color.parseColor("#B00020"));

            btnJoin.setEnabled(false);
            btnJoin.setText("Ended");
            btnJoin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#CCCCCC")));
            return;
        }
        long daysLeft = ChronoUnit.DAYS.between(now, regEnd);
        tvDay.setText(daysLeft + " days left");
        tvDay.setTextColor(Color.parseColor("#666666"));

        btnJoin.setEnabled(true);
        btnJoin.setAlpha(1f);
        btnJoin.setText("Join in");
        btnJoin.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.primary)));

        setTextOrHide(tvLink, a.getShareLink());

        String url = a.getBannerUrl();
        // url = url.replace("http://localhost:8080", "http://196.169.1.192:8080");
        url = url.replace("http://localhost:8080", "http://10.0.2.2:8080");

        if (url != null && !url.startsWith("http")) {
            url = BuildConfig.BASE_URL + (url.startsWith("/") ? url.substring(1) : url);
        }
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(imgBanner);

    }

    private String fmtDateOnly(String isoDateTime) {
        try {
            LocalDateTime dt = LocalDateTime.parse(isoDateTime);
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return isoDateTime;
        }
    }

    private void tryJoinNow() {
        if (activityId <= 0) {
            Toast.makeText(this, "Thiếu activityId", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasJwtToken()) {
            Toast.makeText(this, "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        setJoining(true);

        ActivityRegistrationRequest body = new ActivityRegistrationRequest(activityId);

        ApiClient.activityRegistrations(this)
                .register(body)
                .enqueue(new Callback<ApiResponse<ActivityRegistrationResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<ActivityRegistrationResponse>> call,
                            Response<ApiResponse<ActivityRegistrationResponse>> response) {
                        setJoining(false);
                        if (response.code() == 401 || response.code() == 403) {
                            Toast.makeText(EventDetailActivity.this,
                                    "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.",
                                    Toast.LENGTH_LONG).show();
                            startActivity(new Intent(EventDetailActivity.this, LoginActivity.class));
                            return;
                        }

                        boolean isMiniGame = "MINIGAME".equalsIgnoreCase(activityType);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<ActivityRegistrationResponse> api = response.body();

                            // Hiện message nếu có
                            if (api.getMessage() != null) {
                                Toast.makeText(EventDetailActivity.this,
                                        api.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }

                            // Đăng ký OK → cập nhật UI
                            if (api.isStatus()) {
                                setJoinedUI();
                            }
                            // Already registered → vẫn coi là joined
                            else if (api.getMessage() != null
                                    && api.getMessage().toLowerCase(Locale.ROOT).contains("already")) {
                                setJoinedUI();

                            }

                        } else {

                            Toast.makeText(EventDetailActivity.this,
                                    "Already registered for this activity",
                                    Toast.LENGTH_LONG).show();
                            setJoinedUI();

                        }

                        if ("MINIGAME".equalsIgnoreCase(activityType)) {
                            Intent i = new Intent(EventDetailActivity.this, MiniGameActivity.class);
                            i.putExtra("activity_id", activityId);
                            startActivity(i);
                        }

                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<ActivityRegistrationResponse>> call,
                            Throwable t) {
                        setJoining(false);

                        Toast.makeText(EventDetailActivity.this,
                                "Lỗi mạng/Server: " +
                                        (t.getMessage() != null ? t.getMessage() : ""),
                                Toast.LENGTH_LONG).show();

                        if ("MINIGAME".equalsIgnoreCase(activityType)) {
                            Intent i = new Intent(EventDetailActivity.this, MiniGameActivity.class);
                            i.putExtra("activity_id", activityId);
                            startActivity(i);
                        }
                    }
                });
    }

    private boolean hasJwtToken() {
        String access = TokenStore.getToken(this);
        android.util.Log.d("AUTH", "Token = " + access);
        return access != null && !access.isEmpty();
    }

    private void setTextOrHide(TextView tv, String value) {
        if (value == null || value.trim().isEmpty()) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(value);
        }
    }

    private void setJoining(boolean joining) {
        btnJoin.setEnabled(!joining);
        btnJoin.setAlpha(joining ? 0.7f : 1f);
        btnJoin.setText(joining ? "Joining..." : "Join in");
    }

    private void setJoinedUI() {
        btnJoin.setEnabled(false);
        btnJoin.setAlpha(0.8f);
        btnJoin.setText("Joined");
        btnJoin.setTextColor(Color.parseColor("#008000"));
        btnJoin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E6FFE6")));
    }

    private String n(String s) {
        return s == null ? "" : s;
    }

    private String buildDateRange(String start, String end) {
        if (isEmpty(start) && isEmpty(end))
            return "";
        if (isEmpty(end))
            return fmtDate(start);
        if (isEmpty(start))
            return fmtDate(end);
        return fmtDate(start) + " – " + fmtDate(end);
    }

    private String fmtDate(String d) {
        try {
            return LocalDate.parse(d, inDate).format(outDate);
        } catch (Exception e) {
            return n(d);
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static class JoinBody {
        private final long activityId;
        private final String feedback;

        public JoinBody(long activityId, String feedback) {
            this.activityId = activityId;
            this.feedback = feedback;
        }

        public long getActivityId() {
            return activityId;
        }

        public String getFeedback() {
            return feedback;
        }
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

}
