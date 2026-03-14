package com.example.campuslife.adapter;

import android.content.Intent;
import android.util.Log;
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
import com.example.campuslife.activity.EventDetailActivity;
import com.example.campuslife.entity.Activity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ActivityForYouAdapter extends RecyclerView.Adapter<ActivityForYouAdapter.VH> {
    private final List<Activity> items = new ArrayList<>();

    public void submit(List<Activity> data) {
        items.clear();
        if (data != null)
            items.addAll(data);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imageEvent;
        TextView txtNameEvent, textEventTime, textEventLocation;

        VH(@NonNull View itemView) {
            super(itemView);

            imageEvent = itemView.findViewById(R.id.imgThumb);
            txtNameEvent = itemView.findViewById(R.id.tvTitle);
            textEventTime = itemView.findViewById(R.id.tvDate);
            textEventLocation = itemView.findViewById(R.id.tvLocation);
        }
    }

    @NonNull
    @Override
    public ActivityForYouAdapter.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_foryou, parent, false);
        return new ActivityForYouAdapter.VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityForYouAdapter.VH h, int position) {
        Activity a = items.get(position);

        Log.d("BIND", "Item " + position + ": " + a.getName());
        h.txtNameEvent.setText(a.getName());
        h.textEventTime.setText(fmtDateOnly(a.getStartDate()) + " - " + fmtDateOnly(a.getEndDate()));
        h.textEventLocation.setText(a.getLocation() != null ? a.getLocation() : "");

        String img = a.getBannerUrl();
        // img = img.replace("http://localhost:8080", "http://196.169.1.192:8080");
        img = img.replace("http://localhost:8080", "http://10.0.2.2:8080");

        String full = null;
        Log.e("BASE_URL_CHECK", "BuildConfig.BASE_URL = " + BuildConfig.BASE_URL);
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
        android.util.Log.d("IMG", "load: " + full);

        Glide.with(h.itemView.getContext())
                .load(full)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(h.imageEvent);

        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), EventDetailActivity.class);
            i.putExtra("activity_id", a.getId());
            v.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(items.size(), 5);
    }

    private String fmtDateOnly(String isoDateTime) {
        try {
            LocalDateTime dt = LocalDateTime.parse(isoDateTime);
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return isoDateTime;
        }
    }

}
