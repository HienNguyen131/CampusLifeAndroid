package com.example.campuslife.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.activity.SubmitReportActivity;
import com.example.campuslife.entity.ActivityReportRespone;

import java.util.ArrayList;
import java.util.List;

public class ActivityReportAdapter extends RecyclerView.Adapter<ActivityReportAdapter.VH> {

    private final List<ActivityReportRespone> items = new ArrayList<>();

    public void submit(List<ActivityReportRespone> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;
        VH(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_report, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ActivityReportRespone a = items.get(pos);

        if (a.getTask() != null) {
            String title = a.getTask().getName() != null ? a.getTask().getName() : "Không có tiêu đề";
            String deadline = a.getTask().getDeadline() != null ? a.getTask().getDeadline() : "No limit";

            h.tvTitle.setText(title);
            h.tvDate.setText(deadline);

            h.itemView.setOnClickListener(v -> {
                Context ctx = v.getContext();
                Intent intent = new Intent(ctx, SubmitReportActivity.class);
                intent.putExtra("task_id", a.getTask().getId());
                intent.putExtra("task_name", a.getTask().getName());
                ctx.startActivity(intent);
            });

        } else {
            h.tvTitle.setText("Không có dữ liệu");
            h.tvDate.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
