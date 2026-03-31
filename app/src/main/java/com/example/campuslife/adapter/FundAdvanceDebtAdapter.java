package com.example.campuslife.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.entity.preparation.FundAdvanceDebtDto;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FundAdvanceDebtAdapter extends RecyclerView.Adapter<FundAdvanceDebtAdapter.ViewHolder> {
    private final Context context;
    private final List<FundAdvanceDebtDto> list;
    private final NumberFormat currencyFormatter;

    public FundAdvanceDebtAdapter(Context context, List<FundAdvanceDebtDto> list) {
        this.context = context;
        this.list = list;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fund_advance_debt_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FundAdvanceDebtDto item = list.get(position);

        holder.tvStudentName.setText(item.studentName != null ? item.studentName : "Unknown");

        String amountToDisplay = item.holdingAmount != null ? item.holdingAmount : item.totalDebtAmount;
        
        if (amountToDisplay != null) {
            try {
                holder.tvTotalDebtAmount.setText(currencyFormatter.format(Double.parseDouble(amountToDisplay)));
            } catch (Exception e) {
                holder.tvTotalDebtAmount.setText(amountToDisplay + "đ");
            }
        } else {
            holder.tvTotalDebtAmount.setText("0đ");
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvTotalDebtAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvTotalDebtAmount = itemView.findViewById(R.id.tvTotalDebtAmount);
        }
    }
}
