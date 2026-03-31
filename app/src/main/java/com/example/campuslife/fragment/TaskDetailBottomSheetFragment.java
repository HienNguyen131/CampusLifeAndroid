package com.example.campuslife.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.example.campuslife.R;
import com.example.campuslife.adapter.TaskDetailPagerAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.PreparationTaskDto;
import com.example.campuslife.entity.preparation.PreparationTaskMemberDto;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskDetailBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_TASK_ID = "taskId";
    private static final String ARG_ACTIVITY_ID = "activityId";
    private static final String ARG_MY_ROLE = "myRole";

    private long taskId;
    private long activityId;
    private String myRole;

    private Runnable onDismissCallback;
    private ProgressBar progressDetail;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public static TaskDetailBottomSheetFragment newInstance(long taskId, long activityId, String myRole) {
        TaskDetailBottomSheetFragment f = new TaskDetailBottomSheetFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_TASK_ID, taskId);
        b.putLong(ARG_ACTIVITY_ID, activityId);
        b.putString(ARG_MY_ROLE, myRole);
        f.setArguments(b);
        return f;
    }

    public void setOnDismissCallback(Runnable callback) {
        this.onDismissCallback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_MaterialComponents_BottomSheetDialog);
        if (getArguments() != null) {
            taskId = getArguments().getLong(ARG_TASK_ID, -1);
            activityId = getArguments().getLong(ARG_ACTIVITY_ID, -1);
            myRole = getArguments().getString(ARG_MY_ROLE, "MEMBER");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_task_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDetail = view.findViewById(R.id.progressDetail);
        tabLayout = view.findViewById(R.id.tabLayoutDetail);
        viewPager = view.findViewById(R.id.viewPagerDetail);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        loadTaskAndSetupTabs();
    }

    @Override
    public void onStart() {
        super.onStart();
        android.view.View view = getView();
        if (view != null && view.getParent() instanceof android.view.View) {
            com.google.android.material.bottomsheet.BottomSheetBehavior<android.view.View> behavior =
                    com.google.android.material.bottomsheet.BottomSheetBehavior.from((android.view.View) view.getParent());
            behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissCallback != null) onDismissCallback.run();
    }

    private void loadTaskAndSetupTabs() {
        showProgress(true);

        AtomicInteger pendingCalls = new AtomicInteger(1);
        final PreparationTaskDto[] taskHolder = new PreparationTaskDto[1];
        final List<PreparationTaskMemberDto>[] membersHolder = new List[1];

        Runnable trySetup = () -> {
            if (pendingCalls.decrementAndGet() == 0) {
                showProgress(false);
                if (taskHolder[0] != null) setupTabs(taskHolder[0], membersHolder[0]);
            }
        };

        // Add parallel call for members
        pendingCalls.incrementAndGet();
        ApiClient.preparation(requireContext()).getTaskMembers(taskId)
                .enqueue(new Callback<ApiResponse<List<PreparationTaskMemberDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<PreparationTaskMemberDto>>> call,
                            Response<ApiResponse<List<PreparationTaskMemberDto>>> resp) {
                        if (!isAdded()) return;
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            membersHolder[0] = resp.body().getData();
                        }
                        trySetup.run();
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<List<PreparationTaskMemberDto>>> call, Throwable t) {
                        if (!isAdded()) return;
                        trySetup.run();
                    }
                });

        ApiClient.preparation(requireContext()).getTaskDetail(taskId)
                .enqueue(new Callback<ApiResponse<PreparationTaskDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PreparationTaskDto>> call,
                            Response<ApiResponse<PreparationTaskDto>> resp) {
                        if (!isAdded()) return;
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            taskHolder[0] = resp.body().getData();
                        } else {
                            toast(resp.body() != null ? resp.body().getMessage() : "Lỗi tải nhiệm vụ");
                        }
                        trySetup.run();
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<PreparationTaskDto>> call, Throwable t) {
                        if (!isAdded()) return;
                        toast("Lỗi mạng: " + t.getMessage());
                        trySetup.run();
                    }
                });
    }

    private void setupTabs(PreparationTaskDto task, List<PreparationTaskMemberDto> members) {
        if (!isAdded()) return;

        boolean isLeader = "LEADER".equalsIgnoreCase(myRole);
        boolean isFinancial = task.isFinancial != null && task.isFinancial;

        TaskDetailPagerAdapter adapter = new TaskDetailPagerAdapter(this);

        // Tab 1: Chi tiết (always)
        adapter.addFragment(
                TaskDetailInfoFragment.newInstance(task, members, myRole),
                "Chi tiết");

        // Tab 2: Chi phí (if financial)
        if (isFinancial) {
            adapter.addFragment(
                    TaskExpensesFragment.newInstance(taskId, activityId, myRole),
                    "Chi phí");
        }

        // Tab 3: Duyệt cấp 1 (leader + financial)
        if (isLeader && isFinancial) {
            adapter.addFragment(
                    TaskLeaderReviewFragment.newInstance(taskId, activityId),
                    "Duyệt cấp 1");
        }

        // Tab 4: Tạm ứng (leader)
        if (isLeader) {
            adapter.addFragment(
                    TaskAdvancesFragment.newInstance(taskId, activityId),
                    "Tạm ứng");
        }

        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(adapter.getTitle(position))).attach();
    }

    private void showProgress(boolean show) {
        if (progressDetail != null)
            progressDetail.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void toast(String msg) {
        if (!isAdded()) return;
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
