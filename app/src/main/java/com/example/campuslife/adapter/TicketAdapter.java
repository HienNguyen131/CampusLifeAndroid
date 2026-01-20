package com.example.campuslife.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.activity.FeedbackActivity;
import com.example.campuslife.activity.TicketDetailActivity;
import com.example.campuslife.entity.ActivityRegistrationResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<ActivityRegistrationResponse> tickets;

    public TicketAdapter(List<ActivityRegistrationResponse> tickets) {
        this.tickets = tickets;
    }

    public void updateData(List<ActivityRegistrationResponse> newData) {
        this.tickets = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        ActivityRegistrationResponse t = tickets.get(position);

        holder.tvTitle.setText(t.getActivityName());
        holder.tvDate.setText(fmtDateOnly(t.getRegisteredDate()));
        holder.tvStatus.setText(t.getStatus());
        holder.tvLocation.setText(t.getActivityLocation());


        String scoreType = t.getScoreType();

        if (scoreType == null) {
            holder.flagCate.setText("Unknown");
        } else {
            switch (scoreType) {
                case "REN_LUYEN":
                    holder.flagCate.setText("Training Point");
                    break;
                case "CONG_TAC_XA_HOI":
                    holder.flagCate.setText("Business Score");
                    break;
                default:
                    holder.flagCate.setText("Social Activity");
            }
        }



        if (t.isImportant()) {
            holder.flagImport.setText("Important");
            holder.flagImport.setVisibility(View.VISIBLE);
        } else if (t.isMandatoryForFacultyStudents()) {
            holder.flagImport.setText("Mandatory");
            holder.flagImport.setVisibility(View.VISIBLE);
        } else {
            holder.flagImport.setVisibility(View.GONE);
        }


        holder.btnDetail.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), TicketDetailActivity.class);
            i.putExtra("registration_id", t.getId());
            v.getContext().startActivity(i);
        });


    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    private String fmtDateOnly(String isoDateTime) {
        try {
            LocalDateTime dt = LocalDateTime.parse(isoDateTime.replace(" ", "T"));
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return isoDateTime;
        }
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvStatus, tvLocation;
        MaterialButton flagCate, flagImport, btnDetail;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            flagCate = itemView.findViewById(R.id.flagCate);
            flagImport = itemView.findViewById(R.id.flagImport);
            btnDetail = itemView.findViewById(R.id.btnDetail);

        }
    }
}

