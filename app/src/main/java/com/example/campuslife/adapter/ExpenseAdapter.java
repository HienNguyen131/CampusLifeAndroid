package com.example.campuslife.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campuslife.BuildConfig;
import com.example.campuslife.R;
import com.example.campuslife.databinding.ItemExpenseBinding;
import com.example.campuslife.entity.preparation.ExpenseDto;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.VH> {

    private final List<ExpenseDto> items = new ArrayList<>();

    public void submit(List<ExpenseDto> data) {
        items.clear();
        if (data != null)
            items.addAll(data);
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
        Context context = h.itemView.getContext();

        h.b.tvAmount.setText(formatMoney(e != null ? e.amount : null));
        h.b.tvDesc.setText(safe(e != null ? e.description : null));

        String meta = fmtDateOnly(e != null ? e.createdAt : null);
        if (e != null && e.reportedByName != null && !e.reportedByName.trim().isEmpty()) {
            meta += " • " + e.reportedByName;
        }
        h.b.tvMeta.setText(meta);

        h.b.chipStatus.setText(mapStatusLabel(e != null ? e.approved : null));

        String url = buildUrl(e != null ? e.evidenceUrl : null);
        if (url == null || url.trim().isEmpty()) {
            h.b.imgEvidence.setVisibility(View.GONE);
        } else {
            h.b.imgEvidence.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(url)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(h.b.imgEvidence);
            h.b.imgEvidence.setOnClickListener(v -> showImage(context, url));
        }
    }

    private void showImage(Context context, String url) {
        ImageView img = new ImageView(context);
        img.setAdjustViewBounds(true);
        int pad = (int) (16 * context.getResources().getDisplayMetrics().density);
        img.setPadding(pad, pad, pad, pad);
        Glide.with(context).load(url).into(img);

        new AlertDialog.Builder(context)
                .setView(img)
                .setPositiveButton("Đóng", (d, w) -> d.dismiss())
                .show();
    }

    private String buildUrl(String raw) {
        if (raw == null || raw.trim().isEmpty())
            return null;
        String url = raw.replace("http://localhost:8080", "http://10.0.2.2:8080");
        if (url.startsWith("http"))
            return url;

        String base = BuildConfig.BASE_URL;
        if (!base.endsWith("/"))
            base += "/";
        if (url.startsWith("/"))
            url = url.substring(1);
        return base + url;
    }

    private String mapStatusLabel(Boolean approved) {
        if (approved == null)
            return "WAITING_APPROVAL";
        return approved ? "APPROVED" : "REJECTED";
    }

    private String fmtDateOnly(String isoDateTime) {
        if (isoDateTime == null)
            return "-";
        try {
            LocalDateTime dt = LocalDateTime.parse(isoDateTime);
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return isoDateTime;
        }
    }

    private String formatMoney(String amount) {
        if (amount == null || amount.trim().isEmpty())
            return "-";
        try {
            BigDecimal v = new BigDecimal(amount);
            return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(v);
        } catch (Exception ex) {
            return amount;
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
