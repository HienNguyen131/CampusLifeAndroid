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
    private final DecimalFormat df = new DecimalFormat("#,###");

    public PreparationExpenseAdapter(Context context, List<ExpenseDto> list) {
        this.context = context;
        this.list = list;
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

        holder.tvExpenseDesc.setText(expense.description != null ? expense.description : "No Description");
        holder.tvReportedBy.setText(expense.reportedByName != null ? "Reported by " + expense.reportedByName : "Reported by Unknown");

        if (expense.amount != null) {
            try {
                holder.tvAmount.setText(df.format(Double.parseDouble(expense.amount)) + " VNĐ");
            } catch (Exception e) {
                holder.tvAmount.setText(expense.amount + " VNĐ");
            }
        } else {
            holder.tvAmount.setText("0 VNĐ");
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

        // Approval button logic
        if (expense.approved == null) {
            holder.llActions.setVisibility(View.VISIBLE);
            holder.tvStatus.setVisibility(View.GONE);
            
            holder.btnApprove.setOnClickListener(v -> handleApproval(holder, position, expense, true));
            holder.btnReject.setOnClickListener(v -> handleApproval(holder, position, expense, false));
        } else if (expense.approved) {
            holder.llActions.setVisibility(View.GONE);
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("Đã duyệt");
            holder.tvStatus.setTextColor(Color.parseColor("#065F46"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_squircle_green);
        } else {
            holder.llActions.setVisibility(View.GONE);
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("Từ chối");
            holder.tvStatus.setTextColor(Color.parseColor("#991B1B"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_squircle_red);
        }
    }

    private void handleApproval(ViewHolder holder, int position, ExpenseDto expense, boolean isApproved) {
        ApiClient.preparation(context).approveExpense(expense.id, new ApproveExpenseRequest(isApproved))
                .enqueue(new Callback<ApiResponse<ExpenseDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ExpenseDto>> call, Response<ApiResponse<ExpenseDto>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            expense.approved = isApproved;
                            notifyItemChanged(position);
                            Toast.makeText(context, isApproved ? "Đã duyệt" : "Đã từ chối", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Thao tác thất bại", Toast.LENGTH_SHORT).show();
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
        TextView tvExpenseDesc, tvReportedBy, tvAmount, tvStatus;
        View llActions;
        MaterialButton btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEvidence = itemView.findViewById(R.id.ivEvidence);
            tvExpenseDesc = itemView.findViewById(R.id.tvExpenseDesc);
            tvReportedBy = itemView.findViewById(R.id.tvReportedBy);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            llActions = itemView.findViewById(R.id.llActions);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
