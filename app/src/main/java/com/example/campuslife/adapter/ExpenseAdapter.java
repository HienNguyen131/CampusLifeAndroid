package com.example.campuslife.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.databinding.ItemExpenseBinding;
import com.example.campuslife.entity.preparation.ExpenseDto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.VH> {

    private final List<ExpenseDto> items = new ArrayList<>();
    private final OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseClick(ExpenseDto expense);
    }

    public ExpenseAdapter(OnExpenseClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<ExpenseDto> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        ItemExpenseBinding b;

        VH(ItemExpenseBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExpenseBinding b = ItemExpenseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ExpenseDto e = items.get(position);

        h.b.tvExpenseAmount.setText(formatMoney(e.amount) + "đ");
        h.b.tvExpenseDescription.setText(e.description != null ? e.description : "Không có mô tả");
        
        if (e.taskName != null && !e.taskName.isEmpty()) {
            h.b.tvExpenseTask.setVisibility(View.VISIBLE);
            h.b.tvExpenseTask.setText("Task: " + e.taskName);
        } else {
            h.b.tvExpenseTask.setVisibility(View.GONE);
        }

        if (e.categoryName != null) {
            h.b.tvExpenseCategory.setVisibility(View.VISIBLE);
            h.b.tvExpenseCategory.setText(e.categoryName);
        } else {
            h.b.tvExpenseCategory.setVisibility(View.GONE);
        }

        h.b.tvExpenseCreator.setText("Người tạo: " + (e.createdByName != null ? e.createdByName : "Unknown"));

        String statusStr = e.status != null ? e.status : "PENDING";
        h.b.tvExpenseStatus.setText(mapStatusLabel(statusStr));
        h.b.tvExpenseStatus.setTextColor(mapStatusColor(statusStr));

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onExpenseClick(e);
        });
    }

    private String mapStatusLabel(String status) {
        switch (status) {
            case "PENDING_LEADER": return "Chờ Leader";
            case "PENDING_ADMIN": return "Chờ Admin";
            case "APPROVED": return "Đã duyệt";
            case "REJECTED": return "Từ chối";
            default: return status;
        }
    }

    private int mapStatusColor(String status) {
        switch (status) {
            case "PENDING_LEADER": 
            case "PENDING_ADMIN": return Color.parseColor("#F2994A"); // Orange
            case "APPROVED": return Color.parseColor("#27AE60"); // Green
            case "REJECTED": return Color.parseColor("#EB5757"); // Red
            default: return Color.GRAY;
        }
    }

    private String formatMoney(String amount) {
        if (amount == null || amount.trim().isEmpty()) return "0";
        try {
            BigDecimal v = new BigDecimal(amount);
            return String.format("%,d", v.longValue());
        } catch (Exception ex) {
            return amount;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
