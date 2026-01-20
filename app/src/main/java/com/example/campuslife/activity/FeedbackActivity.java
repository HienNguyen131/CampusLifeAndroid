package com.example.campuslife.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.campuslife.BuildConfig;
import com.example.campuslife.R;
import com.example.campuslife.api.ActivityApi;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.api.FeedbackApi;
import com.example.campuslife.entity.Activity;
import com.example.campuslife.entity.ActivityFeedbackRequest;

import com.example.campuslife.entity.ActivityFeedbackResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackActivity extends AppCompatActivity {

    private ShapeableImageView imgEvent;
    private TextView tvTitle, tvDate, tvLocation, txtRateLabel;
    private RatingBar ratingBar;
    private TextInputEditText edtFeedback;
    private MaterialButton btnSubmit;
    private ImageView btnBack;

    private long activityId = -1;
    private long studentId = -1;
    private FeedbackApi feedbackApi;
    private ActivityApi activityApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);


        imgEvent = findViewById(R.id.imgThumb);
        tvTitle = findViewById(R.id.tvTitle);
        tvDate = findViewById(R.id.tvDate);
        tvLocation = findViewById(R.id.tvLocation);
        txtRateLabel = findViewById(R.id.txtRateLabel);
        ratingBar = findViewById(R.id.rating);
        edtFeedback = findViewById(R.id.edtFeedback);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack=findViewById(R.id.btnBack);


        activityId = getIntent().getLongExtra("activity_id", -1);
        studentId = getIntent().getLongExtra("student_id", -1);

        feedbackApi = ApiClient.activityFeedback(this);
        activityApi = ApiClient.activities(this);
        activityDetail(activityId);
        loadFeedback(activityId, studentId);

        btnBack.setOnClickListener(v -> finish());
        ratingBar.setOnRatingBarChangeListener((bar, value, fromUser) -> {
            if (fromUser) {
                txtRateLabel.setText(String.format("Your rating: %.1f ★", value));
            }
        });


        btnSubmit.setOnClickListener(v -> {
            float ratingValue = ratingBar.getRating();
            String content = edtFeedback.getText() != null ? edtFeedback.getText().toString().trim() : "";

            submitFeedback(activityId, studentId, ratingValue, content);
        });
    }


    private void loadFeedback(long activityId, long studentId) {
        feedbackApi.getRatingByActivityAndStudent(activityId, studentId)
                .enqueue(new Callback<ActivityFeedbackResponse>() {
                    @Override
                    public void onResponse(Call<ActivityFeedbackResponse> call,
                                           Response<ActivityFeedbackResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ActivityFeedbackResponse data = response.body();
                            ratingBar.setRating(data.getRating());
                            edtFeedback.setText(data.getComment());
                            txtRateLabel.setText(String.format("Your rating: %.1f ★", data.getRating()));
                        }
                    }

                    @Override
                    public void onFailure(Call<ActivityFeedbackResponse> call, Throwable t) {
                        Toast.makeText(FeedbackActivity.this,
                                "Không thể tải dữ liệu đánh giá: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void activityDetail(long id) {
        activityApi.detail(id).enqueue(new Callback<ApiResponse<Activity>>() {
            @Override
            public void onResponse(Call<ApiResponse<Activity>> call, Response<ApiResponse<Activity>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Activity data = response.body().getData();

                    tvTitle.setText(data.getName());
                    tvDate.setText(data.getStartDate() != null ? data.getStartDate() : "");
                    tvLocation.setText(data.getLocation() != null ? data.getLocation() : "");

                    Glide.with(FeedbackActivity.this)
                            .load(data.getBannerUrl())
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder)
                            .into(imgEvent);
                } else {
                    String msg = response.body() != null && response.body().getMessage() != null
                            ? response.body().getMessage()
                            : "Không thể tải dữ liệu!";
                    Toast.makeText(FeedbackActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Activity>> call, Throwable t) {
                Toast.makeText(FeedbackActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void submitFeedback(long activityId, long studentId, float ratingValue, String content) {
        ActivityFeedbackRequest req = new ActivityFeedbackRequest();
        req.setActivityId(activityId);
        req.setStudentId(studentId);
        req.setRating(ratingValue);
        req.setComment(content);

        feedbackApi.createRating(activityId, studentId, ratingValue, content)
                .enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call,
                                   Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> res = response.body();
                    if (res.isStatus()) {
                        Toast.makeText(FeedbackActivity.this,
                                "Submit thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(FeedbackActivity.this,
                                res.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(FeedbackActivity.this,
                            "Submit thất bại: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(FeedbackActivity.this,
                        "Lỗi mạng: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
