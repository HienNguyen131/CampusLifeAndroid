package com.example.campuslife.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.campuslife.R;
import com.example.campuslife.activity.EventDetailActivity;
import com.example.campuslife.entity.Activity;
import com.example.campuslife.entity.ActivitySeries;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SeriesEventAdapter extends RecyclerView.Adapter<SeriesEventAdapter.SeriesEventViewHolder> {

    private List<Activity> event = new ArrayList<>();

    public void submit(List<Activity> data) {
        event.clear();
        if (data != null) event.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SeriesEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_series_session, parent, false);

        return new SeriesEventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SeriesEventViewHolder holder, int position) {
        Activity s = event.get(position);

        holder.txtTitle.setText(s.getName());
        holder.txtTime.setText(fmtDateOnly(s.getStartDate()) + " - " + fmtDateOnly(s.getEndDate()));
        holder.txtLocation.setText(s.getLocation());
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), EventDetailActivity.class);
            i.putExtra("activity_id", s.getId());
            v.getContext().startActivity(i);
        });


    }

    private String fmtDateOnly(String isoDateTime) {
        try {
            LocalDateTime dt = LocalDateTime.parse(isoDateTime);
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return isoDateTime;
        }
    }

    @Override
    public int getItemCount() {
        return event.size();
    }

    static class SeriesEventViewHolder extends RecyclerView.ViewHolder {
        MaterialButton txtSessionBadge, txtStatus;
        TextView txtTitle, txtTime, txtLocation;

        public SeriesEventViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtLocation = itemView.findViewById(R.id.txtLocation);
        }
    }
}
