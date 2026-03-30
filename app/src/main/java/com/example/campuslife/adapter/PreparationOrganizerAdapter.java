package com.example.campuslife.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.OrganizerDto;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreparationOrganizerAdapter extends RecyclerView.Adapter<PreparationOrganizerAdapter.ViewHolder> {

    private final Context context;
    private final List<OrganizerDto> list;
    private final long activityId;
    private Runnable onListChanged;

    public PreparationOrganizerAdapter(Context context, List<OrganizerDto> list, long activityId, Runnable onListChanged) {
        this.context = context;
        this.list = list;
        this.activityId = activityId;
        this.onListChanged = onListChanged;
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

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xóa ban tổ chức")
                    .setMessage("Bạn có chắc chắn muốn xóa " + config.getFullName() + " khỏi ban tổ chức không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        ApiClient.preparation(context).removeOrganizer(activityId, config.getStudentId())
                            .enqueue(new Callback<ApiResponse<Object>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                                        Toast.makeText(context, "Đã xóa khỏi ban tổ chức", Toast.LENGTH_SHORT).show();
                                        if (onListChanged != null) onListChanged.run();
                                    } else {
                                        Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
                            });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivAvatar;
        TextView tvName;
        TextView tvStudentId;
        android.view.View btnDelete;
        TextView tvAvatarInitials;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // ivAvatar is optional if you use placeholder
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvAvatarInitials = itemView.findViewById(R.id.tvAvatarInitials);
        }
    }
}
