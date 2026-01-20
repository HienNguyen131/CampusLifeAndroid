package com.example.campuslife.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuslife.R;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.MiniGameQuestionsResponse;
import com.example.campuslife.entity.Minigame;
import com.example.campuslife.entity.Option;
import com.example.campuslife.entity.Question;
import com.example.campuslife.entity.AttemptDetailResponse;
import com.example.campuslife.entity.SubmitResultResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MiniGameActivity extends AppCompatActivity {

    private TextView tvQuestionIndex, tvQuestion, tvTimer;

    private RadioGroup radioGroupOptions;
    private RadioButton rbOption1, rbOption2, rbOption3, rbOption4;

    private MaterialButton btnNext, btnPrev;

    private ProgressBar pb;

    private final List<Question> questions = new ArrayList<>();
    private int current = 0;
    private long activityId;
    private long miniGameId;
    private long attemptId;
    private int timeLimit = 0;
    private CountDownTimer countDownTimer;

    private final LongSparseArray<Long> chosen = new LongSparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minigame);
        pb = findViewById(R.id.pbLoading);

        tvQuestionIndex = findViewById(R.id.tvQuestionIndex);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvTimer = findViewById(R.id.tvTimer);

        radioGroupOptions = findViewById(R.id.radioGroupOptions);

        rbOption1 = findViewById(R.id.rbOption1);
        rbOption2 = findViewById(R.id.rbOption2);
        rbOption3 = findViewById(R.id.rbOption3);
        rbOption4 = findViewById(R.id.rbOption4);

        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);






        activityId = getIntent().getLongExtra("activity_id", -1);
        if (activityId <= 0) {
            Toast.makeText(this, "Thiếu activityId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadMiniGame();
    }

    private void loadMiniGame() {
        pb.setVisibility(View.VISIBLE);

        ApiClient.miniGames(this)
                .getMinigameByActivityId(activityId)
                .enqueue(new Callback<ApiResponse<Minigame>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Minigame>> call,
                                           Response<ApiResponse<Minigame>> resp) {

                        if (!resp.isSuccessful() || resp.body() == null || !resp.body().isStatus()) {
                            pb.setVisibility(View.GONE);
                            Toast.makeText(MiniGameActivity.this, "Không tìm thấy minigame", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }

                        Minigame mg = resp.body().getData();
                        miniGameId = mg.getId();
                        timeLimit = mg.getTimeLimit();

                        startAttempt();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Minigame>> call, Throwable t) {
                        pb.setVisibility(View.GONE);
                        Toast.makeText(MiniGameActivity.this, "Lỗi mạng khi tải minigame", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }



    private void startAttempt() {
        pb.setVisibility(View.VISIBLE);

        ApiClient.miniGames(this)
                .start(miniGameId)
                .enqueue(new Callback<ApiResponse<AttemptDetailResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AttemptDetailResponse>> call,
                                           Response<ApiResponse<AttemptDetailResponse>> resp) {

                        if (!resp.isSuccessful() || resp.body() == null || !resp.body().isStatus()) {
                            pb.setVisibility(View.GONE);
                            Toast.makeText(MiniGameActivity.this, "Không thể bắt đầu lượt chơi", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }

                        attemptId = resp.body().getData().getId();

                        loadQuestions();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AttemptDetailResponse>> call, Throwable t) {
                        pb.setVisibility(View.GONE);
                        Toast.makeText(MiniGameActivity.this, "Lỗi start attempt", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }



    private void loadQuestions() {
        ApiClient.miniGames(this)
                .getQuestions(miniGameId)
                .enqueue(new Callback<ApiResponse<MiniGameQuestionsResponse>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<MiniGameQuestionsResponse>> call,
                                           Response<ApiResponse<MiniGameQuestionsResponse>> resp) {

                        pb.setVisibility(View.GONE);

                        if (!resp.isSuccessful() || resp.body() == null || !resp.body().isStatus()) {
                            Toast.makeText(MiniGameActivity.this, "Không tải được câu hỏi", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }

                        MiniGameQuestionsResponse data = resp.body().getData();

                        questions.clear();
                        questions.addAll(data.questions
                        );

                        current = 0;
                        showQuestion();
                        startTimer();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<MiniGameQuestionsResponse>> call, Throwable t) {
                        pb.setVisibility(View.GONE);
                        Toast.makeText(MiniGameActivity.this, "Lỗi tải câu hỏi", Toast.LENGTH_LONG).show();
                    }
                });
    }



    private void showQuestion() {
        if (current < 0 || current >= questions.size()) return;

        Question q = questions.get(current);
        tvQuestionIndex.setText("Câu " + (current + 1) + " / " + questions.size());
        tvQuestion.setText(q.questionText);

        List<RadioButton> radios = List.of(rbOption1, rbOption2, rbOption3, rbOption4);

        radioGroupOptions.clearCheck();

        for (RadioButton rb : radios) rb.setVisibility(View.GONE);

        for (int i = 0; i < q.options.size() && i < radios.size(); i++) {
            Option opt = q.options.get(i);
            RadioButton rb = radios.get(i);

            rb.setVisibility(View.VISIBLE);
            rb.setText(opt.text);
            rb.setTag(opt.id); // LƯU ID OPTION

            long chosenOpt = chosen.get(q.id, -1L);
            if (chosenOpt == opt.id) rb.setChecked(true);
        }

        radioGroupOptions.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checked = findViewById(checkedId);
            if (checked != null) {
                long optId = (long) checked.getTag();
                chosen.put(q.id, optId);
            }
        });


        btnPrev.setEnabled(current > 0);
        btnNext.setText(current == questions.size() - 1 ? "Submit" : "Next");

        btnPrev.setOnClickListener(v -> {
            if (current > 0) {
                current--;
                showQuestion();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (chosen.get(q.id, -1L) == -1L) {
                Toast.makeText(this, "Hãy chọn đáp án", Toast.LENGTH_SHORT).show();
                return;
            }

            if (current == questions.size() - 1) {
                submitAnswers();
            } else {
                current++;
                showQuestion();
            }
        });
    }


    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(timeLimit * 1000L, 1000) {
            @Override
            public void onTick(long ms) {
                long sec = ms / 1000;
                tvTimer.setText(sec + "s");
            }

            @Override
            public void onFinish() {
                submitAnswers();
            }
        }.start();
    }



    private void submitAnswers() {

        if (countDownTimer != null) countDownTimer.cancel();
        pb.setVisibility(View.VISIBLE);

        Map<String, Long> ans = new HashMap<>();
        for (Question q : questions) {
            long opt = chosen.get(q.id, -1L);
            if (opt != -1L) ans.put(String.valueOf(q.id), opt);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("answers", ans);

        ApiClient.miniGames(this)
                .submit(attemptId, body)
                .enqueue(new Callback<ApiResponse<SubmitResultResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<SubmitResultResponse>> call,
                                           Response<ApiResponse<SubmitResultResponse>> resp) {

                        pb.setVisibility(View.GONE);

                        if (!resp.isSuccessful() || resp.body() == null || !resp.body().isStatus()) {
                            Toast.makeText(MiniGameActivity.this, "Nộp bài thất bại", Toast.LENGTH_LONG).show();
                            return;
                        }

                        SubmitResultResponse data = resp.body().getData();

                        Intent i = new Intent(MiniGameActivity.this, MiniGameResultActivity.class);
                        i.putExtra("attemptId", attemptId);
                        i.putExtra("passed", data.passed);
                        i.putExtra("correctCount", data.correctCount);
                        i.putExtra("totalCount", questions.size());
                        startActivity(i);

                        finish();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<SubmitResultResponse>> call, Throwable t) {
                        pb.setVisibility(View.GONE);
                        Toast.makeText(MiniGameActivity.this, "Lỗi mạng", Toast.LENGTH_LONG).show();
                    }
                });
    }

}
