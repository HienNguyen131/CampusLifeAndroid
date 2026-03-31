package com.example.campuslife.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.entity.preparation.OrganizerDto;

import java.util.List;

public class PreparationTaskAddMemberAdapter extends RecyclerView.Adapter<PreparationTaskAddMemberAdapter.ViewHolder> {

    private final Context context;
    private final List<OrganizerDto> list;
    private final OnMemberAddListener listener;

    public interface OnMemberAddListener {
        void onAddMember(OrganizerDto organizer);
    }

    public PreparationTaskAddMemberAdapter(Context context, List<OrganizerDto> list, OnMemberAddListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_preparation_organizer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrganizerDto config = list.get(position);
        holder.tvName.setText(config.getFullName() != null ? config.getFullName() : "Unknown");
        holder.tvStudentId.setText(config.getStudentId() != null ? String.valueOf(config.getStudentId()) : "Unknown");

        if (holder.tvAvatarInitials != null) {
            String name = config.getFullName();
            if (name != null && !name.trim().isEmpty()) {
                String[] parts = name.trim().split("\\s+");
                String initials = "";
                if (parts.length >= 2) {
                    initials = parts[parts.length - 2].substring(0, 1) + parts[parts.length - 1].substring(0, 1);
                } else {
                    initials = parts[0].substring(0, 1);
                }
                holder.tvAvatarInitials.setText(initials.toUpperCase());
            } else {
                holder.tvAvatarInitials.setText("U");
            }
        }

        // Change Delete button into Add button
        if (holder.btnDelete != null && holder.btnDelete.getChildCount() > 0) {
            View child = holder.btnDelete.getChildAt(0);
            if (child instanceof android.widget.ImageView) {
                ((android.widget.ImageView) child).setImageResource(R.drawable.ic_add);
                ((android.widget.ImageView) child).setColorFilter(android.graphics.Color.parseColor("#F76B10"));
            }
        }
        
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onAddMember(config);
        });
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAddMember(config);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarInitials, tvName, tvStudentId;
        android.widget.LinearLayout btnDelete; 
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarInitials = itemView.findViewById(R.id.tvAvatarInitials);
            tvName = itemView.findViewById(R.id.tvName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
