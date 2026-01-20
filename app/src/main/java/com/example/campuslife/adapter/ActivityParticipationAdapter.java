package com.example.campuslife.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.entity.ActivityParticipationResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ActivityParticipationAdapter
        extends RecyclerView.Adapter<ActivityParticipationAdapter.ParticipationViewHolder> {

    private final List<ActivityParticipationResponse> list;

    public ActivityParticipationAdapter(List<ActivityParticipationResponse> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ParticipationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participation_record, parent, false);

        return new ParticipationViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ParticipationViewHolder holder, int position) {

        ActivityParticipationResponse item = list.get(position);

        // Set Score
        holder.tvScore.setText(String.valueOf(item.getPointsEarned()));

        // Set Type
        holder.tvType.setText(item.getActivityType());
        applyTypeStyle(item.getActivityType(), holder.tvType);

        // Set Title
        holder.tvTitle.setText(item.getActivityName());

        // Set Date
        holder.tvDate.setText(formatDate(item.getDate()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ParticipationViewHolder extends RecyclerView.ViewHolder {

        TextView tvScore, tvType, tvTitle, tvDate;

        public ParticipationViewHolder(@NonNull View itemView) {
            super(itemView);

            tvScore = itemView.findViewById(R.id.tvScore);
            tvType = itemView.findViewById(R.id.tvType);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }


    private String formatDate(String isoDate) {
        if (isoDate == null) return "";

        try {
            // Parse
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS").parse(isoDate);

            // Output type: dd/MM/yyyy HH:mm
            return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);

        } catch (ParseException e) {
            return isoDate;  // fallback
        }
    }


    private void applyTypeStyle(String type, TextView tvType) {

        if (type == null) return;

        switch (type) {
            case "MINIGAME":
                tvType.setBackgroundResource(R.drawable.chip_blue);
                tvType.setTextColor(0xFF0D47A1);
                break;

            case "SUKIEN":
                tvType.setBackgroundResource(R.drawable.chip_green);
                tvType.setTextColor(0xFF1B5E20);
                break;

            case "HOC_TAP":
                tvType.setBackgroundResource(R.drawable.chip_yellow);
                tvType.setTextColor(0xFFE65100);
                break;

            default:
                tvType.setBackgroundResource(R.drawable.chip_gray);
                tvType.setTextColor(0xFF444444);
        }
    }
}
