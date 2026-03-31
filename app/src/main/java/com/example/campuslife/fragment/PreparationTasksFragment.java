package com.example.campuslife.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.adapter.MyTaskAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.MyPreparationTaskDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreparationTasksFragment extends Fragment {

    public static PreparationTasksFragment newInstance(long activityId, long studentId) {
        PreparationTasksFragment f = new PreparationTasksFragment();
        Bundle b = new Bundle();
        b.putLong("activityId", activityId);
        b.putLong("studentId", studentId);
        f.setArguments(b);
        return f;
    }

    private long activityId;
    private long studentId;
    private String currentFilter = "ALL";

    private ProgressBar progress;
    private TextView tvEmpty;
    private RecyclerView rvTasks;
    private MyTaskAdapter adapter;
    private List<MyPreparationTaskDto> allTasksList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preparation_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        activityId = args != null ? args.getLong("activityId", -1) : -1;
        studentId = args != null ? args.getLong("studentId", -1) : -1;

        progress = view.findViewById(R.id.progress);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        rvTasks = view.findViewById(R.id.rvTasks);

        adapter = new MyTaskAdapter(requireContext(), new ArrayList<>(), task -> {
            if (task.id == null) return;
            com.example.campuslife.fragment.TaskDetailBottomSheetFragment sheet =
                    com.example.campuslife.fragment.TaskDetailBottomSheetFragment.newInstance(
                            task.id, activityId, task.myRole != null ? task.myRole : "MEMBER");
            sheet.setOnDismissCallback(this::load);
            sheet.show(getChildFragmentManager(), "TaskDetail");
        });
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        rvTasks.setAdapter(adapter);

        setupFilter(view);
        load();
    }

    private void setupFilter(View view) {
        com.google.android.material.chip.ChipGroup cg = view.findViewById(R.id.cgTaskFilter);
        cg.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipTaskPending) currentFilter = "PENDING";
            else if (id == R.id.chipTaskAccepted) currentFilter = "ACCEPTED";
            else if (id == R.id.chipTaskRequested) currentFilter = "COMPLETION_REQUESTED";
            else if (id == R.id.chipTaskCompleted) currentFilter = "COMPLETED";
            else currentFilter = "ALL";
            applyFilter();
        });
    }

    private void applyFilter() {
        if (!isAdded()) return;
        List<MyPreparationTaskDto> filtered = new ArrayList<>();
        for (MyPreparationTaskDto t : allTasksList) {
            if (currentFilter.equals("ALL") || currentFilter.equals(t.status)) {
                filtered.add(t);
            }
        }
        adapter.submit(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void load() {
        if (!isAdded()) return;
        showLoading(true);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.preparation(requireContext())
                .getMyTasks(activityId)
                .enqueue(new Callback<ApiResponse<List<MyPreparationTaskDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<MyPreparationTaskDto>>> call,
                            Response<ApiResponse<List<MyPreparationTaskDto>>> resp) {
                        showLoading(false);
                        if (!isAdded()) return;
                        if (!resp.isSuccessful() || resp.body() == null || !resp.body().isStatus()) {
                            toast(resp.body() != null ? resp.body().getMessage() : ("HTTP " + resp.code()));
                            tvEmpty.setVisibility(View.VISIBLE);
                            return;
                        }
                        List<MyPreparationTaskDto> data = resp.body().getData();
                        allTasksList.clear();
                        if (data != null) allTasksList.addAll(data);
                        applyFilter();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<MyPreparationTaskDto>>> call, Throwable t) {
                        showLoading(false);
                        if (!isAdded()) return;
                        toast("Lỗi mạng: " + (t.getMessage() != null ? t.getMessage() : ""));
                    }
                });
    }

    private void showLoading(boolean show) {
        if (progress != null)
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void toast(String msg) {
        if (!isAdded()) return;
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
