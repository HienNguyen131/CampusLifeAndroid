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
    private final OnTaskStatusChangeListener listener;

    public interface OnTaskStatusChangeListener {
        void onStatusChanged(PreparationTaskDto task, String newStatus);
    }

    public PreparationTaskAdapter(Context context, List<PreparationTaskDto> list, long currentStudentId, OnTaskStatusChangeListener listener) {
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
            boolean isMine = task.assigneeId != null && task.assigneeId == currentStudentId;
            if (!isMine) {
                android.widget.Toast.makeText(context, "Chỉ người phụ trách mới được cập nhật trạng thái nhiệm vụ này.", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            showStatusUpdateBottomSheet(task);
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

    private void showStatusUpdateBottomSheet(PreparationTaskDto task) {
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_update_status, null);
        dialog.setContentView(view);

        MaterialCardView cardPending = view.findViewById(R.id.cardPending);
        MaterialCardView cardInProgress = view.findViewById(R.id.cardInProgress);
        MaterialCardView cardCompleted = view.findViewById(R.id.cardCompleted);

        View ivCheckPending = view.findViewById(R.id.ivCheckPending);
        View ivCheckInProgress = view.findViewById(R.id.ivCheckInProgress);
        View ivCheckCompleted = view.findViewById(R.id.ivCheckCompleted);

        View btnConfirmStatus = view.findViewById(R.id.btnConfirmStatus);

        final String[] selectedStatus = {task.status == null ? "PENDING" : task.status};

        Runnable updateUI = () -> {
            ivCheckPending.setVisibility(View.GONE);
            ivCheckInProgress.setVisibility(View.GONE);
            ivCheckCompleted.setVisibility(View.GONE);

            cardPending.setCardBackgroundColor(Color.WHITE);
            cardInProgress.setCardBackgroundColor(Color.WHITE);
            cardCompleted.setCardBackgroundColor(Color.WHITE);

            cardPending.setStrokeColor(Color.parseColor("#E5E7EB"));
            cardInProgress.setStrokeColor(Color.parseColor("#E5E7EB"));
            cardCompleted.setStrokeColor(Color.parseColor("#E5E7EB"));

            switch (selectedStatus[0]) {
                case "PENDING":
                    ivCheckPending.setVisibility(View.VISIBLE);
                    cardPending.setCardBackgroundColor(Color.parseColor("#FFFBEB"));
                    cardPending.setStrokeColor(Color.parseColor("#F59E0B"));
                    break;
                case "ACCEPTED":
                    ivCheckInProgress.setVisibility(View.VISIBLE);
                    cardInProgress.setCardBackgroundColor(Color.parseColor("#EFF6FF"));
                    cardInProgress.setStrokeColor(Color.parseColor("#3B82F6"));
                    break;
                case "COMPLETED":
                    ivCheckCompleted.setVisibility(View.VISIBLE);
                    cardCompleted.setCardBackgroundColor(Color.parseColor("#ECFDF5"));
                    cardCompleted.setStrokeColor(Color.parseColor("#10B981"));
                    break;
            }
        };

        updateUI.run();

        cardPending.setOnClickListener(v -> {
            selectedStatus[0] = "PENDING";
            updateUI.run();
        });

        cardInProgress.setOnClickListener(v -> {
            selectedStatus[0] = "ACCEPTED";
            updateUI.run();
        });

        cardCompleted.setOnClickListener(v -> {
            selectedStatus[0] = "COMPLETED";
            updateUI.run();
        });

        btnConfirmStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStatusChanged(task, selectedStatus[0]);
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}
