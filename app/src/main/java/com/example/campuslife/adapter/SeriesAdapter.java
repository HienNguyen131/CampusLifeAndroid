package com.example.campuslife.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.campuslife.R;
import com.example.campuslife.activity.EventDetailActivity;
import com.example.campuslife.activity.SeriesDetailActivity;
import com.example.campuslife.entity.ActivityRegistrationResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.campuslife.entity.ActivitySeries;

public class SeriesAdapter extends RecyclerView.Adapter<SeriesAdapter.SeriesViewHolder> {

    private List<ActivitySeries> series = new ArrayList<>();

    @NonNull
    @Override
    public SeriesAdapter.SeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_series, parent, false);
        return new SeriesAdapter.SeriesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SeriesAdapter.SeriesViewHolder holder, int position) {
        ActivitySeries s = series.get(position);

        holder.txtNameEvent.setText(s.getName());

        holder.textEventTime.setText(
                fmtDateOnly(s.getRegistrationStartDate()) +
                        " - " +
                        fmtDateOnly(s.getRegistrationDeadline())
        );

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), SeriesDetailActivity.class);
            i.putExtra("series_id", s.getId());
            v.getContext().startActivity(i);
        });
    }


    @Override
    public int getItemCount() {
        return series == null ? 0 : Math.min(series.size(), 5);
    }

    private String fmtDateOnly(String isoDateTime) {
        try {
            LocalDateTime dt = LocalDateTime.parse(isoDateTime);
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return isoDateTime;
        }
    }

    public void submit(List<ActivitySeries> data) {
        series.clear();
        if (data != null) series.addAll(data);
        notifyDataSetChanged();
    }

    static class SeriesViewHolder extends RecyclerView.ViewHolder {
        TextView txtNameEvent, textEventTime;

        public SeriesViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNameEvent  = itemView.findViewById(R.id.txtNameEvent);
            textEventTime = itemView.findViewById(R.id.textEventTime);
        }
    }
}
