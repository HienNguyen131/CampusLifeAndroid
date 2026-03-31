package com.example.campuslife.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.entity.preparation.MyPreparationTaskDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyTaskAdapter extends RecyclerView.Adapter<MyTaskAdapter.ViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClicked(MyPreparationTaskDto task);
    }

    private final Context context;
    private final List<MyPreparationTaskDto> list;
    private final OnTaskClickListener listener;

    public MyTaskAdapter(Context context, List<MyPreparationTaskDto> list, OnTaskClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_preparation_task, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyPreparationTaskDto task = list.get(position);

        holder.tvTaskTitle.setText(task.title != null ? task.title : "");
        holder.tvAssigneeName.setText(task.ownerName != null ? task.ownerName : "Chưa có người nhận");

        bindStatus(holder.tvTaskStatus, task.status);
        bindRoleBadge(holder.tvRoleBadge, task.myRole);

        if (holder.tvFinancialBadge != null) {
            boolean isFinancial = task.isFinancial != null && task.isFinancial;
            holder.tvFinancialBadge.setVisibility(isFinancial ? View.VISIBLE : View.GONE);
        }

        if (task.deadline != null) {
            try {
                LocalDateTime dt = LocalDateTime.parse(task.deadline);
                holder.tvDeadline.setText(dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } catch (Exception e) {
                holder.tvDeadline.setText(task.deadline.contains("T") ? task.deadline.split("T")[0] : task.deadline);
            }
        } else {
            holder.tvDeadline.setText("Không có hạn");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTaskClicked(task);
        });
    }

    private void bindStatus(TextView tv, String status) {
        if (status == null) status = "PENDING";
        switch (status) {
            case "COMPLETED":
                tv.setText("Hoàn thành");
                tv.setTextColor(Color.parseColor("#065F46"));
                tv.setBackgroundResource(R.drawable.bg_squircle_green);
                break;
            case "ACCEPTED":
                tv.setText("Đang làm");
                tv.setTextColor(Color.parseColor("#1E40AF"));
                tv.setBackgroundResource(R.drawable.bg_squircle_blue);
                break;
            case "COMPLETION_REQUESTED":
                tv.setText("Chờ duyệt HT");
                tv.setTextColor(Color.parseColor("#6B21A8"));
                tv.setBackgroundResource(R.drawable.bg_squircle_blue);
                break;
            default:
                tv.setText("Chưa nhận");
                tv.setTextColor(Color.parseColor("#A03A00"));
                tv.setBackgroundResource(R.drawable.bg_squircle_orange);
                break;
        }
    }

    private void bindRoleBadge(TextView tv, String role) {
        if (tv == null) return;
        if ("LEADER".equalsIgnoreCase(role)) {
            tv.setText("Leader");
            tv.setTextColor(Color.parseColor("#065F46"));
            tv.setBackgroundResource(R.drawable.bg_squircle_green);
        } else {
            tv.setText("Member");
            tv.setTextColor(Color.parseColor("#374151"));
            tv.setBackgroundResource(R.drawable.bg_squircle_orange);
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void submit(List<MyPreparationTaskDto> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle, tvTaskStatus, tvRoleBadge, tvFinancialBadge, tvAssigneeName, tvDeadline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskStatus = itemView.findViewById(R.id.tvTaskStatus);
            tvRoleBadge = itemView.findViewById(R.id.tvRoleBadge);
            tvFinancialBadge = itemView.findViewById(R.id.tvFinancialBadge);
            tvAssigneeName = itemView.findViewById(R.id.tvAssigneeName);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
        }
    }
}
