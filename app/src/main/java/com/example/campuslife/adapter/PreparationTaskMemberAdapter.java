package com.example.campuslife.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.entity.preparation.PreparationTaskMemberDto;

import java.util.List;

public class PreparationTaskMemberAdapter extends RecyclerView.Adapter<PreparationTaskMemberAdapter.ViewHolder> {

    private final Context context;
    private final List<PreparationTaskMemberDto> members;
    private final OnMemberActionListener listener;
    private final boolean canEdit;

    public interface OnMemberActionListener {
        void onPromoteLeader(PreparationTaskMemberDto member);
        void onRemoveMember(PreparationTaskMemberDto member);
        void onDemoteLeader(PreparationTaskMemberDto member);
    }

    public PreparationTaskMemberAdapter(Context context, List<PreparationTaskMemberDto> members, boolean canEdit, OnMemberActionListener listener) {
        this.context = context;
        this.members = members;
        this.canEdit = canEdit;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PreparationTaskMemberDto member = members.get(position);
        
        holder.tvMemberName.setText(member.studentName != null ? member.studentName : "Unknown");
        
        // Setup avatar initals
        String initials = "U";
        if (member.studentName != null && !member.studentName.trim().isEmpty()) {
            String[] parts = member.studentName.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = parts[parts.length - 2].substring(0, 1) + parts[parts.length - 1].substring(0, 1);
            } else {
                initials = parts[0].substring(0, 1);
            }
        }
        holder.tvMemberInitials.setText(initials.toUpperCase());

        // Setup role & color
        boolean isLeader = "LEADER".equalsIgnoreCase(member.role);
        
        if (isLeader) {
            holder.tvMemberRole.setText("LEADER");
            holder.tvMemberRole.setTextColor(ContextCompat.getColor(context, R.color.leader_blue));
            holder.btnPromoteLeader.setVisibility(View.GONE);
            if (canEdit) {
                if (holder.btnDemoteLeader != null) holder.btnDemoteLeader.setVisibility(View.VISIBLE);
            } else {
                if (holder.btnDemoteLeader != null) holder.btnDemoteLeader.setVisibility(View.GONE);
            }
        } else {
            holder.tvMemberRole.setText("MEMBER");
            holder.tvMemberRole.setTextColor(ContextCompat.getColor(context, R.color.text_muted));
            if (holder.btnDemoteLeader != null) holder.btnDemoteLeader.setVisibility(View.GONE);
            if (canEdit) {
                holder.btnPromoteLeader.setVisibility(View.VISIBLE);
            } else {
                holder.btnPromoteLeader.setVisibility(View.GONE);
            }
        }
        
        if (!canEdit) {
            holder.btnRemoveMember.setVisibility(View.GONE);
        } else {
            holder.btnRemoveMember.setVisibility(View.VISIBLE);
        }

        // Actions
        holder.btnPromoteLeader.setOnClickListener(v -> {
            if (listener != null) listener.onPromoteLeader(member);
        });

        if (holder.btnDemoteLeader != null) {
            holder.btnDemoteLeader.setOnClickListener(v -> {
                if (listener != null) listener.onDemoteLeader(member);
            });
        }

        holder.btnRemoveMember.setOnClickListener(v -> {
            if (listener != null) listener.onRemoveMember(member);
        });
    }

    @Override
    public int getItemCount() {
        return members != null ? members.size() : 0;
    }
    
    public void updateData(List<PreparationTaskMemberDto> newData) {
        members.clear();
        if (newData != null) {
            members.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberInitials, tvMemberName, tvMemberRole;
        ImageButton btnPromoteLeader, btnRemoveMember, btnDemoteLeader;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberInitials = itemView.findViewById(R.id.tvMemberInitials);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberRole = itemView.findViewById(R.id.tvMemberRole);
            btnPromoteLeader = itemView.findViewById(R.id.btnPromoteLeader);
            btnRemoveMember = itemView.findViewById(R.id.btnRemoveMember);
            btnDemoteLeader = itemView.findViewById(R.id.btnDemoteLeader); // Might be null for now
        }
    }
}
