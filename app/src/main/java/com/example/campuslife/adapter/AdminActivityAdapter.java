package com.example.campuslife.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campuslife.BuildConfig;
import com.example.campuslife.R;
import com.example.campuslife.entity.Activity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminActivityAdapter extends RecyclerView.Adapter<AdminActivityAdapter.ViewHolder> {

    private final Context context;
    private final List<Activity> list;

    public AdminActivityAdapter(Context context, List<Activity> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activity act = list.get(position);

        holder.tvTitle.setText(act.name != null ? act.name : "Không có tên");
        holder.tvLocation.setText(act.location != null ? act.location : "Không xác định");
        
        holder.tvDate.setText((act.startDate != null) ? formatDateShort(act.startDate) : "");

        holder.tvCategory.setText(mapActivityType(act.type));
        holder.tvScoreType.setText(mapScoreType(act.scoreType));
        
        boolean isDraft = (act.isDraft != null && act.isDraft);
        if (isDraft) {
            holder.tvDateStatus.setText("Bản nháp");
            holder.tvDateStatus.setTextColor(android.graphics.Color.parseColor("#EA580C")); // Orange
        } else {
            holder.tvDateStatus.setText(getEventTimeStatus(act.startDate, act.endDate));
            String status = holder.tvDateStatus.getText().toString();
            if (status.contains("Đang diễn ra")) {
                holder.tvDateStatus.setTextColor(android.graphics.Color.parseColor("#16A34A")); // Green
            } else if (status.contains("Sắp diễn ra")) {
                holder.tvDateStatus.setTextColor(android.graphics.Color.parseColor("#001C44")); // Blue/Dark
            } else {
                holder.tvDateStatus.setTextColor(android.graphics.Color.parseColor("#4B5563")); // Gray
            }
        }
        
        ArrayList<String> metrics = new ArrayList<>();
        if (act.getParticipantCount() > 0) metrics.add("👥 " + act.getParticipantCount());
        if (act.maxPoints != null && act.maxPoints.compareTo(BigDecimal.ZERO) > 0) metrics.add("🏆 " + act.maxPoints + "đ");
        if (act.ticketQuantity != null && act.ticketQuantity > 0) metrics.add("🎫 " + act.ticketQuantity + " vé");

        if (metrics.isEmpty()) {
            holder.tvMetrics.setVisibility(View.GONE);
        } else {
            holder.tvMetrics.setVisibility(View.VISIBLE);
            holder.tvMetrics.setText(String.join("  |  ", metrics));
        }

        holder.tvPrepStatus.setVisibility(View.GONE);

        String img = act.bannerUrl;
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
                 .into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.ic_placeholder_transparent);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.example.campuslife.activity.AdminPreparationDashboardActivity.class);
            intent.putExtra("ACTIVITY_ID", act.id);
            context.startActivity(intent);
        });
    }

    private String formatDateShort(String isoDateTime) {
        try {
            LocalDateTime dt = LocalDateTime.parse(isoDateTime);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd 'Th'MM");
            return dt.format(fmt);
        } catch (Exception e) {
            if (isoDateTime != null && isoDateTime.contains("T")) {
                return isoDateTime.split("T")[0];
            }
            return isoDateTime;
        }
    }

    private String mapActivityType(String type) {
        if (type == null) return "Sự kiện";
        switch (type) {
            case "SU_KIEN": return "Sự kiện";
            case "MINIGAME": return "Mini Game";
            case "CONG_TAC_XA_HOI": return "Công tác xã hội";
            case "CHUYEN_DE_DOANH_NGHIEP": return "Chuyên đề doanh nghiệp";
            case "SUKIEN": return "Sự kiện";
            default: return type;
        }
    }

    private String mapScoreType(String scoreType) {
        if (scoreType == null) return "Điểm rèn luyện";
        switch (scoreType) {
            case "REN_LUYEN": return "Điểm rèn luyện";
            case "CONG_TAC_XA_HOI": return "Điểm CTXH";
            case "CHUYEN_DE": return "Điểm chuyên đề";
            default: return scoreType;
        }
    }

    private String getEventTimeStatus(String startDate, String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            LocalDateTime now = LocalDateTime.now();
            
            if (now.isAfter(end)) {
                return "✅ Đã kết thúc";
            } else if (now.isAfter(start) && now.isBefore(end)) {
                return "🟢 Đang diễn ra";
            } else {
                return "⏰ Sắp diễn ra";
            }
        } catch (Exception e) {
            return "⏰ Sắp diễn ra";
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvCategory, tvScoreType, tvTitle, tvDate, tvLocation, tvPrepStatus, tvDateStatus, tvMetrics;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvScoreType = itemView.findViewById(R.id.tvScoreType);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrepStatus = itemView.findViewById(R.id.tvPrepStatus);
            tvDateStatus = itemView.findViewById(R.id.tvDateStatus);
            tvMetrics = itemView.findViewById(R.id.tvMetrics);
        }
    }
}
