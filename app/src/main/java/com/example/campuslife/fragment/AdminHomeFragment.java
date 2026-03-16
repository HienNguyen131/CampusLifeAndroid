package com.example.campuslife.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campuslife.R;
import com.example.campuslife.adapter.AdminActivityAdapter;

import com.example.campuslife.activity.ListActivity;
import com.example.campuslife.activity.ListSeriesActivity;
import com.example.campuslife.activity.LoginActivity;
import com.example.campuslife.activity.NotificationActivity;

import com.example.campuslife.activity.ProfileDetailActivity;
import com.example.campuslife.activity.CreateEventActivity;
import com.example.campuslife.activity.ScanQRActivity;
import com.example.campuslife.activity.StudentPreparationActivity;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.api.ProfileAPI;
import com.example.campuslife.auth.TokenStore;
import com.example.campuslife.entity.Activity;
import com.example.campuslife.entity.ActivitySeries;
import com.example.campuslife.entity.Student;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminHomeFragment extends Fragment {

    private RecyclerView rvActivities;
    private AdminActivityAdapter adapter;
    private List<Activity> activityList;

    private TextView tvGreeting;
    private TextView tvActiveEventCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rvActivities = view.findViewById(R.id.rvActivities);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvActiveEventCount = view.findViewById(R.id.tvActiveEventCount);

        // Name from token store
        String username = TokenStore.getUsername(requireContext());
        if (username != null && !username.isEmpty()) {
            tvGreeting.setText("Xin chào, " + username);
        }

        TextView tvViewAll = view.findViewById(R.id.tvViewAll);
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> {
                if (getActivity() instanceof com.example.campuslife.activity.AdminMainActivity) {
                    ((com.example.campuslife.activity.AdminMainActivity) getActivity()).selectEventsTab();
                }
            });
        }

        // Setup Quick Actions
        View cardCreateEvent = view.findViewById(R.id.cardCreateEvent);
        if (cardCreateEvent != null) {
            cardCreateEvent.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), CreateEventActivity.class));
            });
        }
        
        View cardScanQR = view.findViewById(R.id.cardScanQR);
        if (cardScanQR != null) {
            cardScanQR.setOnClickListener(v -> {
                startActivity(new Intent(requireContext(), ScanQRActivity.class));
            });
        }

        // Setup RecyclerView
        rvActivities.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        activityList = new ArrayList<>();
        adapter = new AdminActivityAdapter(requireContext(), activityList);
        rvActivities.setAdapter(adapter);

        // Fetch data
        fetchActivities();
    }
    
    private void fetchActivities() {
        ApiClient.activities(requireContext()).getAllActivities().enqueue(new Callback<ApiResponse<List<Activity>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Activity>>> call, Response<ApiResponse<List<Activity>>> response) {
                if (!isAdded() || getContext() == null) return;
                
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<Activity> data = response.body().getData();
                    activityList.clear();
                    
                    if (data != null) {
                        for(Activity act : data) {
                            if (!act.isDeleted) {
                                activityList.add(act);
                            }
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                    
                    if (tvActiveEventCount != null) {
                        tvActiveEventCount.setText(String.format("%02d", activityList.size()));
                    }

                } else {
                    Toast.makeText(requireContext(), "Lỗi tải sự kiện", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Activity>>> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Log.e("AdminHomeFragment", "Lỗi fetch sự kiện", t);
                Toast.makeText(requireContext(), "Không thể kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
