package com.example.campuslife.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.entity.preparation.BudgetCategoryDto;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.DecimalFormat;
import java.util.List;

public class PreparationAdminWalletAdapter extends RecyclerView.Adapter<PreparationAdminWalletAdapter.ViewHolder> {
    private final Context context;
    private final List<BudgetCategoryDto> walletList;
    private final DecimalFormat df = new DecimalFormat("#,###");

    public PreparationAdminWalletAdapter(Context context, List<BudgetCategoryDto> walletList) {
        this.context = context;
        this.walletList = walletList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget_admin_wallet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BudgetCategoryDto wallet = walletList.get(position);

        holder.tvWalletName.setText(wallet.name != null ? wallet.name : "Ví không tên");

        try {
            double alloc = wallet.allocatedAmount != null ? Double.parseDouble(wallet.allocatedAmount) : 0;
            holder.tvWalletAllocated.setText(df.format(alloc) + "đ");
        } catch (Exception e) {
            holder.tvWalletAllocated.setText(wallet.allocatedAmount + "đ");
        }

        try {
            double used = wallet.usedAmount != null ? Double.parseDouble(wallet.usedAmount) : 0;
            holder.tvWalletUsed.setText(df.format(used) + "đ");
        } catch (Exception e) {
            holder.tvWalletUsed.setText(wallet.usedAmount + "đ");
        }

        try {
            double remain = wallet.remainingAmount != null ? Double.parseDouble(wallet.remainingAmount) : 0;
            holder.tvWalletRemaining.setText(df.format(remain) + "đ");
        } catch (Exception e) {
            holder.tvWalletRemaining.setText(wallet.remainingAmount + "đ");
        }

        if (holder.progressWalletUsed != null) {
            int pct = wallet.usedPercent != null ? (int) Math.min(Math.round(wallet.usedPercent), 100) : 0;
            holder.progressWalletUsed.setProgress(pct);
            if (holder.tvWalletUsedPercent != null) holder.tvWalletUsedPercent.setText(pct + "%");
            if (pct >= 90) {
                holder.progressWalletUsed.setIndicatorColor(android.graphics.Color.parseColor("#DC2626"));
            } else if (pct >= 70) {
                holder.progressWalletUsed.setIndicatorColor(android.graphics.Color.parseColor("#EA580C"));
            } else {
                holder.progressWalletUsed.setIndicatorColor(android.graphics.Color.parseColor("#10B981"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return walletList != null ? walletList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvWalletName, tvWalletAllocated, tvWalletUsed, tvWalletRemaining, tvWalletUsedPercent;
        LinearProgressIndicator progressWalletUsed;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWalletName = itemView.findViewById(R.id.tvWalletName);
            tvWalletAllocated = itemView.findViewById(R.id.tvWalletAllocated);
            tvWalletUsed = itemView.findViewById(R.id.tvWalletUsed);
            tvWalletRemaining = itemView.findViewById(R.id.tvWalletRemaining);
            progressWalletUsed = itemView.findViewById(R.id.progressWalletUsed);
            tvWalletUsedPercent = itemView.findViewById(R.id.tvWalletUsedPercent);
        }
    }
}
