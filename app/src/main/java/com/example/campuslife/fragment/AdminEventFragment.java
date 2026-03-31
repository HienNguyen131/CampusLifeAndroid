package com.example.campuslife.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import com.example.campuslife.R;
import com.example.campuslife.activity.CreateEventActivity;
import com.example.campuslife.adapter.AdminActivityAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.Activity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminEventFragment extends Fragment {

    private RecyclerView rvActivities;
    private AdminActivityAdapter adapter;
    private List<Activity> allActivitiesList;
    private List<Activity> filteredActivitiesList;

    private String currentStatusFilter = "ALL";
    private String currentTypeFilter = "ALL";
    private String currentPrepFilter = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rvActivities = view.findViewById(R.id.rvActivities);

        rvActivities.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        allActivitiesList = new ArrayList<>();
        filteredActivitiesList = new ArrayList<>();
        adapter = new AdminActivityAdapter(requireContext(), filteredActivitiesList);
        rvActivities.setAdapter(adapter);

        setupFilters(view);

        View fabCreateEvent = view.findViewById(R.id.fabCreateEvent);
        if (fabCreateEvent != null) {
            fabCreateEvent.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), CreateEventActivity.class));
            });
        }

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof com.example.campuslife.activity.AdminMainActivity) {
                    ((com.example.campuslife.activity.AdminMainActivity) getActivity()).selectHomeTab();
                }
            });
        }

        fetchActivities();
    }

    private void setupFilters(View view) {
        com.google.android.material.chip.ChipGroup cgStatusFilter = view.findViewById(R.id.cgStatusFilter);
        com.google.android.material.chip.ChipGroup cgTypeFilter = view.findViewById(R.id.cgTypeFilter);
        com.google.android.material.chip.ChipGroup cgPrepFilter = view.findViewById(R.id.cgPrepFilter);

        cgStatusFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipOngoing) currentStatusFilter = "ONGOING";
            else if (id == R.id.chipUpcoming) currentStatusFilter = "UPCOMING";
            else if (id == R.id.chipEnded) currentStatusFilter = "ENDED";
            else if (id == R.id.chipDrafts) currentStatusFilter = "DRAFTS";
            else currentStatusFilter = "ALL";
            applyFilters();
        });

        cgTypeFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipTypeEvent) currentTypeFilter = "SU_KIEN";
            else if (id == R.id.chipTypeMinigame) currentTypeFilter = "MINIGAME";
            else if (id == R.id.chipTypeCTXH) currentTypeFilter = "CONG_TAC_XA_HOI";
            else if (id == R.id.chipTypeSeminar) currentTypeFilter = "CHUYEN_DE_DOANH_NGHIEP";
            else currentTypeFilter = "ALL";
            applyFilters();
        });

        cgPrepFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipPrepEnabled) currentPrepFilter = "ENABLED";
            else if (id == R.id.chipPrepDisabled) currentPrepFilter = "DISABLED";
            else currentPrepFilter = "ALL";
            applyFilters();
        });
    }

    private void applyFilters() {
        if (!isAdded()) return;
        
        filteredActivitiesList.clear();
        LocalDateTime now = LocalDateTime.now();

        for (Activity act : allActivitiesList) {
            // Filter by Type
            if (!currentTypeFilter.equals("ALL")) {
                if (act.type == null || !act.type.equals(currentTypeFilter)) {
                    continue;
                }
            }

            // Filter by Status
            if (!currentStatusFilter.equals("ALL")) {
                boolean isDraft = (act.isDraft != null && act.isDraft);
                
                if (currentStatusFilter.equals("DRAFTS") && !isDraft) {
                    continue;
                } else if (!currentStatusFilter.equals("DRAFTS") && isDraft) {
                    continue;
                }
                
                if (!isDraft && !currentStatusFilter.equals("DRAFTS")) {
                    LocalDateTime start = parseDate(act.startDate);
                    LocalDateTime end = parseDate(act.endDate);
                    
                    if (start != null && end != null) {
                        if (currentStatusFilter.equals("ONGOING")) {
                            if (now.isBefore(start) || now.isAfter(end)) continue;
                        } else if (currentStatusFilter.equals("UPCOMING")) {
                            if (!now.isBefore(start)) continue;
                        } else if (currentStatusFilter.equals("ENDED")) {
                            if (!now.isAfter(end)) continue;
                        }
                    } else {
                         continue;
                    }
                }
            }

            // Filter by Preparation Status
            if (!currentPrepFilter.equals("ALL")) {
                boolean hasPrep = act.isHasPreparation();
                if (currentPrepFilter.equals("ENABLED") && !hasPrep) {
                    continue;
                } else if (currentPrepFilter.equals("DISABLED") && hasPrep) {
                    continue;
                }
            }

            filteredActivitiesList.add(act);
        }
        adapter.notifyDataSetChanged();
    }

    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            return LocalDateTime.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
    
    private void fetchActivities() {
        ApiClient.activities(requireContext()).getAllActivities().enqueue(new Callback<ApiResponse<List<Activity>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Activity>>> call, Response<ApiResponse<List<Activity>>> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<Activity> data = response.body().getData();
                    allActivitiesList.clear();

                    if (data != null) {
                        for (Activity act : data) {
                            if (!act.isDeleted && act.getSeries() == null) {
                                allActivitiesList.add(act);
                            }
                        }
                    }
                    applyFilters();
                } else {
                    Toast.makeText(requireContext(), "Lỗi tải sự kiện", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Activity>>> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Log.e("AdminEventFragment", "Lỗi fetch sự kiện", t);
                Toast.makeText(requireContext(), "Không thể kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
