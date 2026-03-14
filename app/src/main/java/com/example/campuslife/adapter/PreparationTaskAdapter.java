package com.example.campuslife.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.databinding.ItemPreparationTaskBinding;
import com.example.campuslife.entity.preparation.PreparationTaskDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PreparationTaskAdapter extends RecyclerView.Adapter<PreparationTaskAdapter.VH> {

    public interface Listener {
        void onAction(PreparationTaskDto task, String newStatus);
    }

    private final List<PreparationTaskDto> items = new ArrayList<>();
    private final long myStudentId;
    private final Listener listener;

    public PreparationTaskAdapter(long myStudentId, Listener listener) {
        this.myStudentId = myStudentId;
        this.listener = listener;
    }

    public void submit(List<PreparationTaskDto> data) {
        items.clear();
        if (data != null)
            items.addAll(data);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        ItemPreparationTaskBinding b;

        VH(ItemPreparationTaskBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPreparationTaskBinding b = ItemPreparationTaskBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PreparationTaskDto t = items.get(position);

        h.b.tvTitle.setText(safe(t != null ? t.title : null));
        h.b.tvDesc.setText(safe(t != null ? t.description : null));

        String meta = "Deadline: " + fmtDateOnly(t != null ? t.deadline : null);
        if (t != null && t.assigneeName != null && !t.assigneeName.trim().isEmpty()) {
            meta += " • " + t.assigneeName;
        }
        h.b.tvMeta.setText(meta);

        String status = t != null && t.status != null ? t.status : "";
        h.b.chipStatus.setText(status);

        boolean isMine = t != null && t.assigneeId != null && t.assigneeId == myStudentId;
        String next = nextStatus(status);

        if (!isMine || next == null) {
            h.b.btnAction.setVisibility(View.GONE);
        } else {
            h.b.btnAction.setVisibility(View.VISIBLE);
            h.b.btnAction.setText(labelFor(next));
            h.b.btnAction.setOnClickListener(v -> {
                if (listener != null)
                    listener.onAction(t, next);
            });
        }
    }

    private String nextStatus(String current) {
        if (current == null)
            return "ACCEPTED";
        if ("PENDING".equalsIgnoreCase(current))
            return "ACCEPTED";
        if ("ACCEPTED".equalsIgnoreCase(current))
            return "COMPLETED";
        return null;
    }

    private String labelFor(String status) {
        if ("ACCEPTED".equalsIgnoreCase(status))
            return "Nhận việc";
        if ("COMPLETED".equalsIgnoreCase(status))
            return "Hoàn thành";
        return status;
    }

    private String fmtDateOnly(String isoDateTime) {
        if (isoDateTime == null)
            return "-";
        try {
            LocalDateTime dt = LocalDateTime.parse(isoDateTime);
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return isoDateTime;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
