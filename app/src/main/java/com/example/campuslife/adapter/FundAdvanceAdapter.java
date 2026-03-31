package com.example.campuslife.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.ApproveFundAdvanceRequest;
import com.example.campuslife.entity.preparation.FundAdvanceDto;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FundAdvanceAdapter extends RecyclerView.Adapter<FundAdvanceAdapter.ViewHolder> {
    private final Context context;
    private final List<FundAdvanceDto> list;
    private final NumberFormat currencyFormatter;
    private final boolean isAdmin;

    private java.util.Map<Long, Double> debtMap;

    public FundAdvanceAdapter(Context context, List<FundAdvanceDto> list, boolean isAdmin) {
        this.context = context;
        this.list = list;
        this.isAdmin = isAdmin;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    public void setDebtMap(java.util.Map<Long, Double> debtMap) {
        this.debtMap = debtMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fund_advance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FundAdvanceDto item = list.get(position);

        String name = item.studentName != null ? item.studentName : "Unknown Student";
        String amtStr = "0đ";
        try {
            amtStr = currencyFormatter.format(item.amount.doubleValue());
        } catch (Exception e) {}
        holder.tvStudentName.setText(name + " • " + amtStr);

        String catName = item.categoryName != null ? item.categoryName : "Chưa rõ";
        String tTitle = item.taskTitle != null ? item.taskTitle : "#" + item.taskId;
        String creator = item.requestedByName != null ? item.requestedByName : "Ẩn danh";
        holder.tvCategoryTaskInfo.setText("Task " + tTitle + " • Ví: " + catName + " • Người tạo: " + creator);

        if (item.createdAt != null) {
            String dateFormatted = item.createdAt;
            if (dateFormatted.contains("T")) {
                try {
                    java.util.Date date = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.createdAt);
                    dateFormatted = new java.text.SimpleDateFormat("HH:mm:ss dd/MM/yyyy").format(date);
                } catch (Exception e) {}
            }
            holder.tvDate.setText("Tạo lúc: " + dateFormatted);
        } else {
            holder.tvDate.setText("");
        }

        holder.tvDebtWarning.setVisibility(View.GONE);
        if (debtMap != null && item.studentId != null) {
             Double debt = debtMap.get(item.studentId);
             if (debt != null && debt > 0) {
                  holder.tvDebtWarning.setVisibility(View.VISIBLE);
                  holder.tvDebtWarning.setText("Cảnh báo: đang có khoản HOLDING " + currencyFormatter.format(debt));
             }
        }

        String status = item.status != null ? item.status : "UNKNOWN";
        
        if ("REQUESTED".equals(status)) {
            holder.layoutRequestedActions.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            holder.layoutHoldingActions.setVisibility(View.GONE);
            try {
                holder.tvAmountInfo.setText(currencyFormatter.format(item.amount.doubleValue()));
            } catch (Exception e) {
                holder.tvAmountInfo.setText(item.amount + "đ");
            }
            holder.tvAmountInfo.setVisibility(View.GONE); // Amount is in title now
        } else if ("HOLDING".equals(status)) {
            holder.layoutRequestedActions.setVisibility(View.GONE);
            holder.layoutHoldingActions.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            holder.tvAmountInfo.setVisibility(View.VISIBLE);
            try {
                holder.tvAmountInfo.setText("Đang giữ: " + currencyFormatter.format(item.remainingAmount.doubleValue()));
            } catch (Exception e) {
                holder.tvAmountInfo.setText("Đang giữ: " + item.remainingAmount + "đ");
            }
        } else {
            holder.layoutRequestedActions.setVisibility(View.GONE);
            holder.layoutHoldingActions.setVisibility(View.GONE);
            holder.tvAmountInfo.setVisibility(View.VISIBLE);
            if ("SETTLED".equals(status)) {
                holder.tvAmountInfo.setText("Đã hoàn ứng");
            } else if ("REJECTED".equals(status)) {
                holder.tvAmountInfo.setText("Bị từ chối");
                holder.tvAmountInfo.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            }
        }

        // Action Handlers
        holder.btnApproveFund.setOnClickListener(v -> submitDecision(item.id, true, position, "HOLDING"));
        holder.btnRejectFund.setOnClickListener(v -> submitDecision(item.id, false, position, "REJECTED"));
        holder.btnReturnFund.setOnClickListener(v -> submitReturnFund(item.id, position));
    }

    private void submitDecision(Long fundId, boolean isApproved, int position, String newStatus) {
        ApiClient.preparation(context).adminDecisionFundAdvance(fundId, new ApproveFundAdvanceRequest(isApproved))
                .enqueue(new Callback<ApiResponse<FundAdvanceDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<FundAdvanceDto>> call, Response<ApiResponse<FundAdvanceDto>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            list.get(position).status = newStatus;
                            if (isApproved) {
                                list.get(position).remainingAmount = list.get(position).amount;
                            } else {
                                list.get(position).remainingAmount = java.math.BigDecimal.ZERO;
                            }
                            notifyItemChanged(position);
                            Toast.makeText(context, "Thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<FundAdvanceDto>> call, Throwable t) {
                        Toast.makeText(context, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void submitReturnFund(Long fundId, int position) {
        ApiClient.preparation(context).adminReturnFundAdvance(fundId)
                .enqueue(new Callback<ApiResponse<FundAdvanceDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<FundAdvanceDto>> call, Response<ApiResponse<FundAdvanceDto>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            list.get(position).status = "SETTLED";
                            list.get(position).remainingAmount = java.math.BigDecimal.ZERO;
                            notifyItemChanged(position);
                            Toast.makeText(context, "Đã hoàn ứng thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<FundAdvanceDto>> call, Throwable t) {
                        Toast.makeText(context, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvCategoryTaskInfo, tvAmountInfo, tvDate, tvDebtWarning;
        LinearLayout layoutRequestedActions, layoutHoldingActions;
        MaterialButton btnApproveFund, btnRejectFund, btnReturnFund;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvCategoryTaskInfo = itemView.findViewById(R.id.tvCategoryTaskInfo);
            tvAmountInfo = itemView.findViewById(R.id.tvAmountInfo);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDebtWarning = itemView.findViewById(R.id.tvDebtWarning);

            layoutRequestedActions = itemView.findViewById(R.id.layoutRequestedActions);
            layoutHoldingActions = itemView.findViewById(R.id.layoutHoldingActions);

            btnApproveFund = itemView.findViewById(R.id.btnApproveFund);
            btnRejectFund = itemView.findViewById(R.id.btnRejectFund);
            btnReturnFund = itemView.findViewById(R.id.btnReturnFund);
        }
    }
}
