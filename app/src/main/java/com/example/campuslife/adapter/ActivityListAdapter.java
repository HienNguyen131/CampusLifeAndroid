package com.example.campuslife.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.campuslife.BuildConfig;
import com.example.campuslife.R;
import com.example.campuslife.activity.EventDetailActivity;
import com.example.campuslife.activity.LoginActivity;
import com.example.campuslife.activity.MiniGameActivity;
import com.example.campuslife.activity.SearchActivity;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.databinding.ItemEventListBinding;
import com.example.campuslife.entity.Activity;
import com.example.campuslife.entity.ActivityRegistrationRequest;
import com.example.campuslife.entity.ActivityRegistrationResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityListAdapter extends RecyclerView.Adapter<ActivityListAdapter.VH> {

    private final List<Activity> items = new ArrayList<>();

    public void updateData(List<Activity> data) {
        items.clear();
        if (data != null)
            items.addAll(data);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        ItemEventListBinding b;

        VH(ItemEventListBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventListBinding b = ItemEventListBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Activity a = items.get(position);
        Context context = h.itemView.getContext();

        h.b.tvTitle.setText(a.getName());
        h.b.tvLocation.setText(a.getLocation());

        String date = fmtDateOnly(a.getStartDate()) + " - " + fmtDateOnly(a.getEndDate());
        h.b.tvDate.setText(date);

        // Load banner
        String img = a.getBannerUrl();
        // img = img.replace("http://localhost:8080", "http://196.169.1.192:8080");
        img = img.replace("http://localhost:8080", "http://10.0.2.2:8080");

        String full = null;
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

        Glide.with(context)
                .load(full)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(h.b.imgBanner);

        h.b.btnDangKy.setOnClickListener(v -> tryJoinNow(context, a));

        h.itemView.setOnClickListener(v -> {

            Intent i = new Intent(context, EventDetailActivity.class);
            i.putExtra("activity_id", a.getId());
            context.startActivity(i);
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

    private void tryJoinNow(Context context, Activity activity) {

        long activityId = activity.getId();
        String activityType = activity.getType();

        if (activityId <= 0) {
            Toast.makeText(context, "Thiếu activityId", Toast.LENGTH_SHORT).show();
            return;
        }

        ActivityRegistrationRequest body = new ActivityRegistrationRequest(activityId);

        ApiClient.activityRegistrations(context)
                .register(body)
                .enqueue(new Callback<ApiResponse<ActivityRegistrationResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<ActivityRegistrationResponse>> call,
                            Response<ApiResponse<ActivityRegistrationResponse>> response) {
                        if (response.code() == 401 || response.code() == 403) {
                            Toast.makeText(context, "Phiên đăng nhập hết hạn. Hãy đăng nhập lại.",
                                    Toast.LENGTH_LONG).show();
                            context.startActivity(new Intent(context, LoginActivity.class));
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<ActivityRegistrationResponse> api = response.body();

                            Toast.makeText(context,
                                    api.getMessage() != null ? api.getMessage() : "Đăng ký thành công",
                                    Toast.LENGTH_LONG).show();

                            if ("MINIGAME".equalsIgnoreCase(activityType)) {
                                Intent i = new Intent(context, MiniGameActivity.class);
                                i.putExtra("activity_id", activityId);
                                context.startActivity(i);
                            }

                        } else {

                            String msg = "Already registered for this activity";
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

                            if ("MINIGAME".equalsIgnoreCase(activityType)) {
                                Intent i = new Intent(context, MiniGameActivity.class);
                                i.putExtra("activity_id", activityId);
                                context.startActivity(i);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ActivityRegistrationResponse>> call, Throwable t) {
                        Toast.makeText(context,
                                "Lỗi mạng/Server: " + (t.getMessage() != null ? t.getMessage() : ""),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
