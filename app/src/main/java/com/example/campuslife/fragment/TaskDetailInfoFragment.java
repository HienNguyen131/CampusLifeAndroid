package com.example.campuslife.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.PreparationTaskDto;
import com.example.campuslife.entity.preparation.PreparationTaskMemberDto;
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskDetailInfoFragment extends Fragment {

    private static final String ARG_TASK = "task";
    private static final String ARG_MEMBERS = "members_json";
    private static final String ARG_ROLE = "myRole";

    private PreparationTaskDto task;
    private List<PreparationTaskMemberDto> members;
    private String myRole;

    private final DecimalFormat df = new DecimalFormat("#,###");

    public static TaskDetailInfoFragment newInstance(
            PreparationTaskDto task,
            List<PreparationTaskMemberDto> members,
            String myRole) {
        TaskDetailInfoFragment f = new TaskDetailInfoFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_TASK, task);
        if (members != null) {
            ArrayList<PreparationTaskMemberDto> memberList = new ArrayList<>(members);
            b.putSerializable(ARG_MEMBERS, memberList);
        }
        b.putString(ARG_ROLE, myRole);
        f.setArguments(b);
        return f;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            task = (PreparationTaskDto) getArguments().getSerializable(ARG_TASK);
            members = (List<PreparationTaskMemberDto>) getArguments().getSerializable(ARG_MEMBERS);
            myRole = getArguments().getString(ARG_ROLE, "MEMBER");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_detail_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (task == null) return;

        // Bind task info
        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvDesc = view.findViewById(R.id.tvDetailDescription);
        TextView tvDeadline = view.findViewById(R.id.tvDetailDeadline);
        LinearLayout rowAllocated = view.findViewById(R.id.rowAllocated);
        TextView tvAllocated = view.findViewById(R.id.tvDetailAllocated);

        tvTitle.setText(task.title != null ? task.title : "");
        tvDesc.setText(task.description != null ? task.description : "Không có mô tả");

        if (task.deadline != null) {
            try {
                LocalDateTime dt = LocalDateTime.parse(task.deadline);
                tvDeadline.setText(dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            } catch (Exception e) {
                tvDeadline.setText(task.deadline);
            }
        } else {
            tvDeadline.setText("Không có hạn");
        }

        if (task.isFinancial != null && task.isFinancial && task.allocatedAmount != null) {
            rowAllocated.setVisibility(View.VISIBLE);
            tvAllocated.setText(df.format(task.allocatedAmount) + " VNĐ");
        }

        // Bind members
        RecyclerView rvMembers = view.findViewById(R.id.rvMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMembers.setAdapter(new MembersAdapter(members != null ? members : new ArrayList<>()));

        // Action buttons
        MaterialButton btnAccept = view.findViewById(R.id.btnAcceptTask);
        MaterialButton btnRequestComplete = view.findViewById(R.id.btnRequestComplete);
        boolean isLeader = "LEADER".equalsIgnoreCase(myRole);

        if ("PENDING".equals(task.status)) {
            btnAccept.setVisibility(View.VISIBLE);
            btnAccept.setOnClickListener(v -> acceptTask());
        }

        if (isLeader && "ACCEPTED".equals(task.status)) {
            btnRequestComplete.setVisibility(View.VISIBLE);
            btnRequestComplete.setOnClickListener(v -> requestComplete());
        }
    }

    private void acceptTask() {
        ApiClient.preparation(requireContext()).acceptTask(task.id)
                .enqueue(new Callback<ApiResponse<PreparationTaskDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PreparationTaskDto>> call,
                            Response<ApiResponse<PreparationTaskDto>> resp) {
                        if (!isAdded()) return;
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            toast("Đã nhận nhiệm vụ!");
                            task.status = "ACCEPTED";
                            refreshButtons();
                        } else {
                            toast(resp.body() != null ? resp.body().getMessage() : "Lỗi khi nhận nhiệm vụ");
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<PreparationTaskDto>> call, Throwable t) {
                        if (!isAdded()) return;
                        toast("Lỗi mạng: " + t.getMessage());
                    }
                });
    }

    private void requestComplete() {
        ApiClient.preparation(requireContext()).requestCompleteTask(task.id)
                .enqueue(new Callback<ApiResponse<PreparationTaskDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PreparationTaskDto>> call,
                            Response<ApiResponse<PreparationTaskDto>> resp) {
                        if (!isAdded()) return;
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            toast("Đã gửi yêu cầu hoàn thành!");
                            task.status = "COMPLETION_REQUESTED";
                            refreshButtons();
                        } else {
                            toast(resp.body() != null ? resp.body().getMessage() : "Lỗi khi gửi yêu cầu");
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<PreparationTaskDto>> call, Throwable t) {
                        if (!isAdded()) return;
                        toast("Lỗi mạng: " + t.getMessage());
                    }
                });
    }

    private void refreshButtons() {
        if (!isAdded() || getView() == null) return;
        MaterialButton btnAccept = getView().findViewById(R.id.btnAcceptTask);
        MaterialButton btnRequestComplete = getView().findViewById(R.id.btnRequestComplete);
        boolean isLeader = "LEADER".equalsIgnoreCase(myRole);
        btnAccept.setVisibility("PENDING".equals(task.status) ? View.VISIBLE : View.GONE);
        btnRequestComplete.setVisibility(
                (isLeader && "ACCEPTED".equals(task.status)) ? View.VISIBLE : View.GONE);
    }

    private void toast(String msg) {
        if (!isAdded()) return;
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    // --- Inner adapter for members list ---
    private static class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.VH> {
        private final List<PreparationTaskMemberDto> data;

        MembersAdapter(List<PreparationTaskMemberDto> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            PreparationTaskMemberDto m = data.get(position);
            holder.text1.setText(m.studentName != null ? m.studentName : "");
            String roleLabel = "LEADER".equalsIgnoreCase(m.role) ? "Leader" : "Member";
            holder.text2.setText(roleLabel);
            holder.text2.setTextColor("LEADER".equalsIgnoreCase(m.role)
                    ? Color.parseColor("#065F46")
                    : Color.parseColor("#6B7280"));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView text1, text2;
            VH(View v) {
                super(v);
                text1 = v.findViewById(android.R.id.text1);
                text2 = v.findViewById(android.R.id.text2);
            }
        }
    }
}
