package com.example.campuslife.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuslife.R;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.AttemptDetailResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MiniGameResultActivity extends AppCompatActivity {

    private TextView tvScore, tvCorrectSummary;
    private TextView tvTP, tvCorrect, tvIncorrect, tvAverage;
    private ImageView imgResultIcon;

    private long attemptId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minigame_result);

        attemptId = getIntent().getLongExtra("attemptId", 0);

        initViews();
        loadResult();
    }

    private void initViews() {
        tvScore = findViewById(R.id.tvScore);
        tvCorrectSummary = findViewById(R.id.tvCorrectSummary);

        tvTP = findViewById(R.id.tvTP);

        tvCorrect = findViewById(R.id.tvCorrect);
        tvIncorrect = findViewById(R.id.tvIncorrect);
        tvAverage = findViewById(R.id.tvAverage);

        imgResultIcon = findViewById(R.id.imgResultIcon);
    }

    private void loadResult() {
        ApiClient.miniGames(this)
                .result(attemptId)
                .enqueue(new Callback<ApiResponse<AttemptDetailResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<AttemptDetailResponse>> call,
                            Response<ApiResponse<AttemptDetailResponse>> response
                    ) {
                        if (!response.isSuccessful() || response.body() == null) return;

                        AttemptDetailResponse data = response.body().getData();
                        if (data == null) return;

                        updateUI(data);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AttemptDetailResponse>> call, Throwable t) {
                        // có thể show toast/log nếu muốn
                    }
                });
    }

    private void updateUI(AttemptDetailResponse data) {

        // SCORE
        tvScore.setText(data.getPointsEarned() + " Points");

        // SUMMARY
        tvCorrectSummary.setText(
                "Answered " + data.getCorrectCount() + " of " +
                        data.getTotalQuestions() + " questions correctly"
        );

        // REWARD
        tvTP.setText("+" + data.getPointsEarned() + " TP");

        // PERFORMANCE
        tvCorrect.setText(String.valueOf(data.getCorrectCount()));
        tvIncorrect.setText(
                String.valueOf(data.getTotalQuestions() - data.getCorrectCount())
        );

        long durationSeconds = calcDurationSeconds(
                data.getStartedAt(),
                data.getSubmittedAt()
        );
        tvAverage.setText(durationSeconds + "s");

        // ICON STATE
        boolean passed =
                data.getCorrectCount() >= data.getRequiredCorrectAnswers();

        imgResultIcon.setImageResource(
                passed ? R.drawable.ic_success : R.drawable.ic_fail
        );
    }

    private long calcDurationSeconds(String start, String end) {
        try {
            java.time.LocalDateTime s = java.time.LocalDateTime.parse(start);
            java.time.LocalDateTime e = java.time.LocalDateTime.parse(end);
            return java.time.Duration.between(s, e).getSeconds();
        } catch (Exception ex) {
            return 0;
        }
    }
}
