package com.example.campuslife.fragment;

import android.graphics.Color;
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
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.ApproveExpenseRequest;
import com.example.campuslife.entity.preparation.ExpenseDto;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskLeaderReviewFragment extends Fragment {

    private static final String ARG_TASK_ID = "taskId";
    private static final String ARG_ACTIVITY_ID = "activityId";

    private long taskId;
    private long activityId;

    private ProgressBar progressReview;
    private TextView tvEmpty;
    private RecyclerView rvReview;
    private LeaderReviewAdapter adapter;
    private final List<ExpenseDto> reviewList = new ArrayList<>();

    public static TaskLeaderReviewFragment newInstance(long taskId, long activityId) {
        TaskLeaderReviewFragment f = new TaskLeaderReviewFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_TASK_ID, taskId);
        b.putLong(ARG_ACTIVITY_ID, activityId);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskId = getArguments().getLong(ARG_TASK_ID, -1);
            activityId = getArguments().getLong(ARG_ACTIVITY_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_leader_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressReview = view.findViewById(R.id.progressReview);
        tvEmpty = view.findViewById(R.id.tvEmptyReview);
        rvReview = view.findViewById(R.id.rvLeaderReview);

        adapter = new LeaderReviewAdapter(reviewList, this::decideExpense);
        rvReview.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvReview.setAdapter(adapter);

        load();
    }

    private void load() {
        progressReview.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.preparation(requireContext())
                .listExpenses(activityId, "PENDING_LEADER")
                .enqueue(new Callback<ApiResponse<List<ExpenseDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ExpenseDto>>> call,
                            Response<ApiResponse<List<ExpenseDto>>> resp) {
                        progressReview.setVisibility(View.GONE);
                        if (!isAdded()) return;
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            List<ExpenseDto> all = resp.body().getData();
                            reviewList.clear();
                            if (all != null) {
                                for (ExpenseDto e : all) {
                                    if (e.taskId != null && e.taskId == taskId) {
                                        reviewList.add(e);
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(reviewList.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<List<ExpenseDto>>> call, Throwable t) {
                        progressReview.setVisibility(View.GONE);
                        if (!isAdded()) return;
                        toast("Lỗi mạng");
                    }
                });
    }

    private void decideExpense(ExpenseDto expense, boolean approved) {
        ApproveExpenseRequest req = new ApproveExpenseRequest(approved);
        ApiClient.preparation(requireContext())
                .leaderDecisionExpense(expense.id, req)
                .enqueue(new Callback<ApiResponse<ExpenseDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ExpenseDto>> call,
                            Response<ApiResponse<ExpenseDto>> resp) {
                        if (!isAdded()) return;
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            toast(approved ? "Đã duyệt chi phí" : "Đã từ chối chi phí");
                            load();
                        } else {
                            toast(resp.body() != null ? resp.body().getMessage() : "Lỗi khi duyệt");
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<ExpenseDto>> call, Throwable t) {
                        if (!isAdded()) return;
                        toast("Lỗi mạng: " + t.getMessage());
                    }
                });
    }

    private void toast(String msg) {
        if (!isAdded()) return;
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    // --- Inner adapter ---
    interface OnDecisionListener {
        void onDecide(ExpenseDto expense, boolean approved);
    }

    private static class LeaderReviewAdapter
            extends RecyclerView.Adapter<LeaderReviewAdapter.VH> {
        private final List<ExpenseDto> list;
        private final OnDecisionListener listener;
        private final DecimalFormat df = new DecimalFormat("#,###");

        LeaderReviewAdapter(List<ExpenseDto> list, OnDecisionListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_my_expense, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            ExpenseDto e = list.get(position);
            h.tvDesc.setText(e.description != null ? e.description : "Chi phí");
            try {
                h.tvAmount.setText(df.format(new BigDecimal(e.amount != null ? e.amount : "0")) + " đ");
            } catch (Exception ex) {
                h.tvAmount.setText(e.amount + " đ");
            }
            h.tvCategory.setText(e.categoryName != null ? "Ví: " + e.categoryName : "");
            h.tvStatus.setText("Chờ leader duyệt");
            h.tvStatus.setTextColor(Color.parseColor("#A03A00"));
            h.tvStatus.setBackgroundResource(R.drawable.bg_squircle_orange);
            h.tvDate.setText((e.createdAt != null ? e.createdAt.split("T")[0] : "")
                    + (e.createdByName != null ? " | " + e.createdByName : ""));

            // Override click to show approve/reject
            h.itemView.setOnClickListener(v -> showDecisionDialog(v, e));
        }

        private void showDecisionDialog(View anchor, ExpenseDto expense) {
            new android.app.AlertDialog.Builder(anchor.getContext())
                    .setTitle("Duyệt chi phí")
                    .setMessage("Bạn có muốn duyệt chi phí này không?\n"
                            + (expense.description != null ? expense.description : ""))
                    .setPositiveButton("✅ Duyệt", (d, w) -> listener.onDecide(expense, true))
                    .setNegativeButton("❌ Từ chối", (d, w) -> listener.onDecide(expense, false))
                    .setNeutralButton("Hủy", null)
                    .show();
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvDesc, tvAmount, tvCategory, tvStatus, tvDate, tvEvidence;

            VH(View v) {
                super(v);
                tvDesc = v.findViewById(R.id.tvExpenseDesc);
                tvAmount = v.findViewById(R.id.tvExpenseAmount);
                tvCategory = v.findViewById(R.id.tvExpenseCategory);
                tvStatus = v.findViewById(R.id.tvExpenseStatus);
                tvDate = v.findViewById(R.id.tvExpenseDate);
                tvEvidence = v.findViewById(R.id.tvEvidenceIndicator);
            }
        }
    }
}
