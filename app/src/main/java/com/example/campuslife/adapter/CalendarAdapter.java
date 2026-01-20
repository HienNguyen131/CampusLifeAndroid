package com.example.campuslife.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.entity.ActivityRegistrationResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    private List<LocalDate> days;
    private Map<String, List<ActivityRegistrationResponse>> eventMap;
    private OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(LocalDate date, List<ActivityRegistrationResponse> events);
    }

    public CalendarAdapter(List<LocalDate> days,
                           Map<String, List<ActivityRegistrationResponse>> eventMap,
                           OnDayClickListener listener) {
        this.days = days;
        this.eventMap = eventMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.calendar_day_item, parent, false);
        return new DayViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        LocalDate date = days.get(position);

        holder.txtDayNumber.setText(String.valueOf(date.getDayOfMonth()));

        String key = date.toString(); // yyyy-MM-dd

        if (eventMap.containsKey(key)) {
            holder.eventIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.eventIndicator.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            List<ActivityRegistrationResponse> events = eventMap.getOrDefault(key, new ArrayList<>());
            listener.onDayClick(date, events);
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {

        TextView txtDayNumber;
        View eventIndicator;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDayNumber = itemView.findViewById(R.id.txtDayNumber);
            eventIndicator = itemView.findViewById(R.id.eventIndicator);
        }
    }
}
