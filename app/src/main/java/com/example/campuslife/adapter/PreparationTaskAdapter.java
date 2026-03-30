package com.example.campuslife.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import com.example.campuslife.R;
import com.example.campuslife.entity.preparation.PreparationTaskDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PreparationTaskAdapter extends RecyclerView.Adapter<PreparationTaskAdapter.ViewHolder> {

    private final Context context;
    private final List<PreparationTaskDto> list;
    private final long currentStudentId;
    private final OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClicked(PreparationTaskDto task);
    }

    public PreparationTaskAdapter(Context context, List<PreparationTaskDto> list, long currentStudentId, OnTaskClickListener listener) {
        this.context = context;
        this.list = list;
        this.currentStudentId = currentStudentId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_preparation_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PreparationTaskDto task = list.get(position);

        holder.tvTaskTitle.setText(task.title);
        holder.tvAssigneeName.setText(task.assigneeName != null ? task.assigneeName : "No Assigned");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClicked(task);
            }
        });

        String status = task.status;
        if (status == null) status = "PENDING";
        
        holder.tvTaskStatus.setText(status);
        if ("COMPLETED".equals(status)) {
            holder.tvTaskStatus.setTextColor(Color.parseColor("#065F46"));
            holder.tvTaskStatus.setBackgroundResource(R.drawable.bg_squircle_green);
        } else if ("ACCEPTED".equals(status)) {
            holder.tvTaskStatus.setTextColor(Color.parseColor("#1E40AF"));
            holder.tvTaskStatus.setBackgroundResource(R.drawable.bg_squircle_blue);
        } else {
            holder.tvTaskStatus.setTextColor(Color.parseColor("#A03A00"));
            holder.tvTaskStatus.setBackgroundResource(R.drawable.bg_squircle_orange);
        }

        if (task.deadline != null) {
            try {
                LocalDateTime dt = LocalDateTime.parse(task.deadline);
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                holder.tvDeadline.setText(dt.format(fmt));
            } catch (Exception e) {
                if (task.deadline.contains("T")) {
                    holder.tvDeadline.setText(task.deadline.split("T")[0]);
                } else {
                    holder.tvDeadline.setText(task.deadline);
                }
            }
        } else {
            holder.tvDeadline.setText("No deadline");
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle, tvTaskStatus, tvAssigneeName, tvDeadline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskStatus = itemView.findViewById(R.id.tvTaskStatus);
            tvAssigneeName = itemView.findViewById(R.id.tvAssigneeName);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
        }
    }

    public void submit(List<PreparationTaskDto> newList) {
        list.clear();
        if (newList != null) {
            list.addAll(newList);
        }
        notifyDataSetChanged();
    }


}
