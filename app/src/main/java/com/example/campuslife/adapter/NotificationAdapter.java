package com.example.campuslife.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.campuslife.R;
import com.example.campuslife.entity.AppNotification;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<AppNotification> list;
    private OnNotificationClick listener;

    public interface OnNotificationClick {
        void onClick(AppNotification noti);
    }

    public NotificationAdapter(List<AppNotification> list, OnNotificationClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder h, int pos) {
        AppNotification n = list.get(pos);

        h.txtType.setText(n.getType());
        h.txtTitle.setText(n.getTitle());
        h.txtContent.setText(n.getContent());
        h.txtDate.setText(formatTime(n.getCreatedAt()));

        h.dotUnread.setVisibility(n.isUnread() ? View.VISIBLE : View.GONE);

        h.itemView.setOnClickListener(v -> listener.onClick(n));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtType, txtTitle, txtContent, txtDate;

        View dotUnread;

        public ViewHolder(@NonNull View v) {
            super(v);
            txtType = v.findViewById(R.id.txtType);
            txtTitle = v.findViewById(R.id.txtTitle);
            txtContent = v.findViewById(R.id.txtMessage);
            txtDate = v.findViewById(R.id.txtDate);

            dotUnread = v.findViewById(R.id.viewUnread);
        }
    }


    private String formatTime(String iso) {
        try {
            LocalDateTime time = LocalDateTime.parse(iso);
            Duration diff = Duration.between(time, LocalDateTime.now());

            long hours = diff.toHours();
            long minutes = diff.toMinutes();

            if (hours > 0)
                return hours + " hours before";
            else
                return minutes + " minute before";

        } catch (Exception e) {
            return iso;
        }
    }
}
