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
import com.example.campuslife.adapter.PreparationTaskAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.PreparationDashboardDto;
import com.example.campuslife.entity.preparation.PreparationTaskDto;
import com.example.campuslife.entity.preparation.UpdateTaskStatusRequest;

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

    private ProgressBar progress;
    private TextView tvEmpty;
    private RecyclerView rvTasks;
    private PreparationTaskAdapter adapter;

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

        adapter = new com.example.campuslife.adapter.PreparationTaskAdapter(requireContext(), new java.util.ArrayList<>(), studentId, this::updateStatus);

        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        rvTasks.setAdapter(adapter);

        load();
    }

    private void load() {
        showLoading(true);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.preparation(requireContext())
                .getDashboard(activityId)
                .enqueue(new Callback<ApiResponse<PreparationDashboardDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PreparationDashboardDto>> call,
                            Response<ApiResponse<PreparationDashboardDto>> resp) {
                        showLoading(false);
                        if (!resp.isSuccessful() || resp.body() == null || !resp.body().isStatus()) {
                            toast(resp.body() != null ? resp.body().getMessage() : ("HTTP " + resp.code()));
                            adapter.submit(null);
                            tvEmpty.setVisibility(View.VISIBLE);
                            return;
                        }

                        PreparationDashboardDto d = resp.body().getData();
                        if (d == null || d.tasks == null || d.tasks.isEmpty()) {
                            adapter.submit(null);
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            adapter.submit(d.tasks);
                            tvEmpty.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PreparationDashboardDto>> call, Throwable t) {
                        showLoading(false);
                        toast("Lỗi mạng: " + (t.getMessage() != null ? t.getMessage() : ""));
                    }
                });
    }

    private void updateStatus(PreparationTaskDto task, String newStatus) {
        showLoading(true);

        ApiClient.preparation(requireContext())
                .updateTaskStatus(task.id, new UpdateTaskStatusRequest(newStatus))
                .enqueue(new Callback<ApiResponse<PreparationTaskDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PreparationTaskDto>> call,
                            Response<ApiResponse<PreparationTaskDto>> resp) {
                        showLoading(false);
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            toast("Cập nhật thành công");
                            load();
                        } else {
                            toast(resp.body() != null ? resp.body().getMessage() : ("HTTP " + resp.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PreparationTaskDto>> call, Throwable t) {
                        showLoading(false);
                        toast("Lỗi mạng: " + (t.getMessage() != null ? t.getMessage() : ""));
                    }
                });
    }

    private void showLoading(boolean show) {
        if (progress != null)
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void toast(String msg) {
        if (!isAdded())
            return;
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
