package com.example.campuslife.fragment;


import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.adapter.ActivityParticipationAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.AcademicYear;
import com.example.campuslife.entity.ActivityParticipationResponse;
import com.example.campuslife.entity.ScoreHistoryResponse;
import com.example.campuslife.entity.ScoreItem;
import com.example.campuslife.entity.ScoreResponse;
import com.example.campuslife.entity.ScoreSummary;
import com.example.campuslife.entity.Semester;
import com.example.campuslife.entity.Student;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScoreFragment extends Fragment {

    private TextView tvScore1, tvScore2, tvScore3;
    private TextView tvSemesterTitle, tvAcademicYear, tvSemesterDate;
    private LinearLayout btnSelectSemester;

    private RecyclerView rvParticipation;

    private final ArrayList<ActivityParticipationResponse> participationList = new ArrayList<>();
    private ActivityParticipationAdapter participationAdapter;
    private HashMap<Long, String> activityTypeMap = new HashMap<>();

    private long studentId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_score, container, false);

        tvScore1 = view.findViewById(R.id.tvScore1);
        tvScore2 = view.findViewById(R.id.tvScore2);
        tvScore3 = view.findViewById(R.id.tvScore3);

        tvSemesterTitle = view.findViewById(R.id.tvSemesterTitle);
        tvAcademicYear = view.findViewById(R.id.tvAcademicYear);
        tvSemesterDate = view.findViewById(R.id.tvSemesterDate);
        btnSelectSemester = view.findViewById(R.id.btnSelectSemester);

        rvParticipation = view.findViewById(R.id.rvParticipationTraing);
        rvParticipation.setLayoutManager(new LinearLayoutManager(requireContext()));

        participationAdapter = new ActivityParticipationAdapter(participationList);
        rvParticipation.setAdapter(participationAdapter);

        autoLoadDefaultSemester();

        btnSelectSemester.setOnClickListener(v -> showYearDialog());

        return view;
    }


    private void fetchStudentId(Consumer<Long> callback) {
        ApiClient.profile(requireContext())
                .getMyProfile()
                .enqueue(new Callback<ApiResponse<Student>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Student>> call,
                                           Response<ApiResponse<Student>> response) {

                        if (!response.isSuccessful() || response.body() == null) return;

                        Student s = response.body().getData();
                        if (s == null) return;

                        studentId = s.getId();
                        Log.e("STUDENT_ID", "Loaded studentId = " + studentId);

                        callback.accept(studentId);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Student>> call, Throwable t) {
                    }
                });
    }


    private void autoLoadDefaultSemester() {
        ApiClient.semester(requireContext()).listYears()
                .enqueue(new Callback<ApiResponse<List<AcademicYear>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<AcademicYear>>> call,
                                           Response<ApiResponse<List<AcademicYear>>> response) {

                        if (!response.isSuccessful()) return;

                        List<AcademicYear> years = response.body().getData();
                        if (years == null || years.isEmpty()) return;

                        AcademicYear newest = years.get(years.size() - 1);
                        loadFirstSemesterOfYear(newest);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<AcademicYear>>> call, Throwable t) {
                    }
                });
    }

    private void loadFirstSemesterOfYear(AcademicYear year) {
        ApiClient.semester(requireContext()).listSemesters(year.getId())
                .enqueue(new Callback<ApiResponse<List<Semester>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Semester>>> call,
                                           Response<ApiResponse<List<Semester>>> response) {

                        if (!response.isSuccessful()) return;

                        List<Semester> list = response.body().getData();
                        if (list == null || list.isEmpty()) return;

                        updateSemesterUI(year, list.get(0));
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Semester>>> call, Throwable t) {
                    }
                });
    }


    private void showYearDialog() {
        ApiClient.semester(requireContext()).listYears()
                .enqueue(new Callback<ApiResponse<List<AcademicYear>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<AcademicYear>>> call,
                                           Response<ApiResponse<List<AcademicYear>>> response) {

                        if (!response.isSuccessful()) return;

                        List<AcademicYear> years = response.body().getData();
                        if (years == null || years.isEmpty()) return;

                        String[] items = new String[years.size()];
                        for (int i = 0; i < years.size(); i++) items[i] = years.get(i).getName();

                        new AlertDialog.Builder(requireContext())
                                .setTitle("Select Academic Year")
                                .setItems(items, (dialog, i) -> fetchSemesters(years.get(i)))
                                .show();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<AcademicYear>>> call, Throwable t) {
                    }
                });
    }

    private void fetchSemesters(AcademicYear year) {
        ApiClient.semester(requireContext()).listSemesters(year.getId())
                .enqueue(new Callback<ApiResponse<List<Semester>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Semester>>> call,
                                           Response<ApiResponse<List<Semester>>> response) {

                        List<Semester> list = response.body().getData();
                        if (list == null || list.isEmpty()) return;

                        String[] items = new String[list.size()];
                        for (int i = 0; i < list.size(); i++) items[i] = list.get(i).getName();

                        new AlertDialog.Builder(requireContext())
                                .setTitle("Select Semester")
                                .setItems(items, (dialog, i) -> updateSemesterUI(year, list.get(i)))
                                .show();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Semester>>> call, Throwable t) {
                    }
                });
    }


    private void updateSemesterUI(AcademicYear year, Semester semester) {
        tvSemesterTitle.setText(semester.getName());
        tvAcademicYear.setText(year.getName());
        tvSemesterDate.setText(semester.getStartDate() + " – " + semester.getEndDate());

        fetchStudentId(id -> {
            loadScores(id, semester.getId());        // Hiện điểm tổng
            loadScoreHistory(id, semester.getId());  // Hiện participation cho học kỳ mới
        });
    }



    private void loadScores(long studentId, long semesterId) {
        tvScore1.setText("0.0");
        tvScore2.setText("0.0");
        tvScore3.setText("0.0");
        ApiClient.score(requireContext()).getScores(studentId, semesterId)
                .enqueue(new Callback<ApiResponse<ScoreResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ScoreResponse>> call,
                                           Response<ApiResponse<ScoreResponse>> response) {

                        if (!response.isSuccessful() || response.body() == null) return;

                        ScoreResponse score = response.body().getData();
                        if (score == null) return;

                        for (ScoreSummary s : score.getSummaries()) {
                            switch (s.getScoreType()) {
                                case "REN_LUYEN":
                                    tvScore1.setText(String.valueOf(s.getTotal()));
                                    break;
                                case "CONG_TAC_XA_HOI":
                                    tvScore2.setText(String.valueOf(s.getTotal()));
                                    break;
                                case "CHUYEN_DE":
                                    tvScore3.setText(String.valueOf(s.getTotal()));
                                    break;
                            }
                        }

                        participationList.clear();
                        participationAdapter.notifyDataSetChanged();
                        loadScoreHistory(studentId, semesterId);  // hiện activityParticipations từ lịch sử

                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ScoreResponse>> call, Throwable t) {
                    }
                });
    }


    private void loadScoreHistory(long studentId, long semesterId) {

        ApiClient.score(requireContext())
                .getScoreHistory(studentId, semesterId, null, 0, 50)
                .enqueue(new Callback<ApiResponse<ScoreHistoryResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ScoreHistoryResponse>> call,
                                           Response<ApiResponse<ScoreHistoryResponse>> response) {

                        if (!response.isSuccessful() || response.body() == null) return;

                        ScoreHistoryResponse history = response.body().getData();
                        if (history == null) return;

                        participationList.clear();

                        if (history.getScoreHistories() != null) {

                            for (ScoreItem item : history.getScoreHistories()) {

                                ActivityParticipationResponse mapped = new ActivityParticipationResponse();

                                String title = item.getActivityName();
                                if (title == null || title.trim().isEmpty()) {
                                    title = item.getReason();
                                }

                                mapped.setActivityName(title);
                                mapped.setActivityType(item.getSourceType());
                                mapped.setDate(item.getChangeDate());
                                mapped.setPointsEarned(item.getNewScore());

                                participationList.add(mapped);
                            }

                        }

                        participationAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ScoreHistoryResponse>> call, Throwable t) {}
                });
    }


}




