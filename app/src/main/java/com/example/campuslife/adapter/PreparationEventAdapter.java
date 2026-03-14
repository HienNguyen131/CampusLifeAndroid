package com.example.campuslife.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campuslife.BuildConfig;
import com.example.campuslife.R;
import com.example.campuslife.databinding.ItemPreparationEventBinding;
import com.example.campuslife.entity.Activity;
import com.example.campuslife.entity.preparation.PreparationDashboardDto;
import com.example.campuslife.entity.preparation.PreparationTaskDto;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PreparationEventAdapter extends RecyclerView.Adapter<PreparationEventAdapter.VH> {

    public interface Listener {
        void onClick(long activityId);
    }

    public static class Item {
        public Activity activity;
        public PreparationDashboardDto dashboard;
    }

    private final List<Item> items = new ArrayList<>();
    private final Listener listener;

    public PreparationEventAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Item> data) {
        items.clear();
        if (data != null)
            items.addAll(data);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        ItemPreparationEventBinding b;

        VH(ItemPreparationEventBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPreparationEventBinding b = ItemPreparationEventBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Item it = items.get(position);
        Context context = h.itemView.getContext();

        Activity a = it.activity;
        PreparationDashboardDto d = it.dashboard;

        h.b.tvTitle.setText(a != null ? safe(a.getName()) : "");
        h.b.tvLocation.setText(a != null ? safe(a.getLocation()) : "");

        String date = fmtDateOnly(a != null ? a.getStartDate() : null) + " - "
                + fmtDateOnly(a != null ? a.getEndDate() : null);
        h.b.tvDate.setText(date);

        int tasksCount = d != null && d.tasks != null ? d.tasks.size() : 0;
        int pendingCount = countPending(d != null ? d.tasks : null);
        h.b.tvTasks.setText("Nhiệm vụ: " + tasksCount);
        h.b.tvPending.setText("Chờ: " + pendingCount);

        if (d != null && d.budget != null) {
            String remaining = formatMoney(d.budget.remainingAmount);
            h.b.tvFinance.setText("Tài chính: Còn lại " + remaining);
        } else if (d != null && d.financeMessage != null && !d.financeMessage.trim().isEmpty()) {
            h.b.tvFinance.setText("Tài chính: " + d.financeMessage);
        } else {
            h.b.tvFinance.setText("Tài chính: -");
        }

        String img = a != null ? a.getBannerUrl() : null;
        img = fixLocalhost(img);

        String full = null;
        if (img != null && !img.isEmpty()) {
            if (img.startsWith("http")) {
                full = img;
            } else {
                String base = BuildConfig.BASE_URL;
                if (!base.endsWith("/"))
                    base += "/";
                if (img.startsWith("/"))
                    img = img.substring(1);
                if (!img.startsWith("uploads/"))
                    img = "uploads/" + img;
                full = base + img;
            }
        }

        Glide.with(context)
                .load(full)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(h.b.imgBanner);

        h.itemView.setOnClickListener(v -> {
            if (listener != null && a != null && a.getId() != null) {
                listener.onClick(a.getId());
            }
        });
    }

    private int countPending(List<PreparationTaskDto> tasks) {
        if (tasks == null)
            return 0;
        int c = 0;
        for (PreparationTaskDto t : tasks) {
            if (t != null && t.status != null && "PENDING".equalsIgnoreCase(t.status))
                c++;
        }
        return c;
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

    private String fixLocalhost(String url) {
        if (url == null)
            return null;
        return url.replace("http://localhost:8080", "http://10.0.2.2:8080");
    }

    private String formatMoney(String amount) {
        if (amount == null || amount.trim().isEmpty())
            return "-";
        try {
            BigDecimal v = new BigDecimal(amount);
            return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(v);
        } catch (Exception e) {
            return amount;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
