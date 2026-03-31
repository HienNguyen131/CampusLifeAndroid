package com.example.campuslife.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campuslife.BuildConfig;
import com.example.campuslife.R;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.ApproveExpenseRequest;
import com.example.campuslife.entity.preparation.ExpenseDto;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.DecimalFormat;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreparationExpenseAdapter extends RecyclerView.Adapter<PreparationExpenseAdapter.ViewHolder> {

    private final Context context;
    private final List<ExpenseDto> list;
    private final boolean isHistoryMode;
    private final DecimalFormat df = new DecimalFormat("#,###");
    private OnExpenseDecisionListener listener;

    public interface OnExpenseDecisionListener {
        void onDecisionMade();
    }

    public PreparationExpenseAdapter(Context context, List<ExpenseDto> list, boolean isHistoryMode, OnExpenseDecisionListener listener) {
        this.context = context;
        this.list = list;
        this.isHistoryMode = isHistoryMode;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_preparation_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpenseDto expense = list.get(position);

        holder.tvDescription.setText(expense.description != null ? expense.description : "No Description");
        holder.tvCreatedByName.setText(expense.createdByName != null ? "Người báo cáo: " + expense.createdByName : "Người báo cáo: Chưa rõ");
        
        if (expense.categoryName == null || expense.categoryName.isEmpty()) {
            holder.tvCategoryName.setVisibility(View.GONE);
        } else {
            holder.tvCategoryName.setVisibility(View.VISIBLE);
            holder.tvCategoryName.setText(expense.categoryName);
        }
        
        if (expense.taskName == null || expense.taskName.isEmpty()) {
            holder.tvTaskName.setVisibility(View.GONE);
        } else {
            holder.tvTaskName.setVisibility(View.VISIBLE);
            holder.tvTaskName.setText(expense.taskName);
        }

        if (expense.amount != null) {
            try {
                holder.tvAmount.setText(df.format(Double.parseDouble(expense.amount)) + " đ");
            } catch (Exception e) {
                holder.tvAmount.setText(expense.amount + " đ");
            }
        } else {
            holder.tvAmount.setText("0 đ");
        }

        // Image loading
        String img = expense.evidenceUrl;
        if (img != null && !img.isEmpty()) {
            img = img.replace("http://localhost:8080", "http://10.0.2.2:8080");
            String fullImageUrl = img;
            
            if (!img.startsWith("http")) {
                String base = BuildConfig.BASE_URL;
                if (!base.endsWith("/")) base += "/";
                if (img.startsWith("/")) img = img.substring(1);
                if (!img.startsWith("uploads/")) img = "uploads/" + img;
                fullImageUrl = base + img;
            }

            Glide.with(context)
                 .load(fullImageUrl)
                 .placeholder(R.drawable.ic_placeholder_transparent)
                 .error(R.drawable.ic_placeholder_transparent)
                 .into(holder.ivEvidence);
        } else {
            holder.ivEvidence.setImageResource(R.drawable.ic_placeholder_transparent);
        }

        // Action vs Badge toggling
        if (!isHistoryMode) {
            holder.layoutActions.setVisibility(View.VISIBLE);
            holder.tvStatusBadge.setVisibility(View.GONE);
            
            holder.btnApprove.setOnClickListener(v -> handleApproval(holder, position, expense, true));
            holder.btnReject.setOnClickListener(v -> handleApproval(holder, position, expense, false));
        } else {
            holder.layoutActions.setVisibility(View.GONE);
            holder.tvStatusBadge.setVisibility(View.VISIBLE);
            
            if ("APPROVED".equals(expense.status)) {
                holder.tvStatusBadge.setText("ĐÃ DUYỆT");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#10B981"));
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_squircle_green);
            } else {
                holder.tvStatusBadge.setText("TỪ CHỐI");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#EF4444"));
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_squircle_red);
            }
        }
    }

    private void handleApproval(ViewHolder holder, int position, ExpenseDto expense, boolean isApproved) {
        Call<ApiResponse<ExpenseDto>> apiCall;
        if (expense.status == null || "PENDING_LEADER".equals(expense.status)) {
            apiCall = ApiClient.preparation(context).leaderDecisionExpense(expense.id, new ApproveExpenseRequest(isApproved));
        } else {
            apiCall = ApiClient.preparation(context).adminDecisionExpense(expense.id, new ApproveExpenseRequest(isApproved));
        }
        apiCall.enqueue(new Callback<ApiResponse<ExpenseDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ExpenseDto>> call, Response<ApiResponse<ExpenseDto>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            Toast.makeText(context, isApproved ? "Đã duyệt" : "Đã từ chối", Toast.LENGTH_SHORT).show();
                            if (listener != null) {
                                listener.onDecisionMade();
                            }
                        } else {
                            String errMessage = "Thao tác thất bại";
                            if (response.errorBody() != null) {
                                try {
                                    String json = response.errorBody().string();
                                    org.json.JSONObject obj = new org.json.JSONObject(json);
                                    if (obj.has("message")) errMessage = obj.getString("message");
                                } catch (Exception e) {}
                            } else if (response.body() != null && response.body().getMessage() != null) {
                                errMessage = response.body().getMessage();
                            }
                            Toast.makeText(context, errMessage, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ExpenseDto>> call, Throwable t) {
                        Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivEvidence;
        TextView tvDescription, tvCreatedByName, tvAmount, tvStatusBadge, tvCategoryName, tvTaskName;
        View layoutActions;
        MaterialButton btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEvidence = itemView.findViewById(R.id.ivEvidence);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCreatedByName = itemView.findViewById(R.id.tvCreatedByName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
