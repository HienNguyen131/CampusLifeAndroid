package com.example.campuslife.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.adapter.NotificationAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.AppNotification;
import com.example.campuslife.entity.NotificationPage;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView rv;
    NotificationAdapter adapter;
    ImageView btnBack;
    TextView txtTilte;
    List<AppNotification> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_notification);

        rv = findViewById(R.id.rvNotification);
        btnBack = findViewById(R.id.btnBack);

        txtTilte = findViewById(R.id.txtTilte);
        txtTilte.setText("Notifications");
        adapter = new NotificationAdapter(data, noti -> openNotification(noti));


        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);


        btnBack.setOnClickListener(v -> finish());


        loadNotifications();
    }


    private void loadNotifications() {
        ApiClient.notifications(this)
                .getMyNotifications(0, 20)
                .enqueue(new Callback<ApiResponse<NotificationPage>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<NotificationPage>> call,
                                           Response<ApiResponse<NotificationPage>> resp) {

                        if (!resp.isSuccessful() || resp.body() == null) return;

                        NotificationPage page = resp.body().getData();
                        data.clear();
                        data.addAll(page.getContent());

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<NotificationPage>> call, Throwable t) {
                        Log.e("NOTI", "ERROR: " + t.getMessage());
                    }
                });
    }

    private void openNotification(AppNotification n) {


        ApiClient.notifications(this)
                .markAsRead(n.getId())
                .enqueue(new Callback<ApiResponse<AppNotification>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AppNotification>> call, Response<ApiResponse<AppNotification>> resp) {
                        n.setStatus("READ");
                        adapter.notifyItemChanged(data.indexOf(n));
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AppNotification>> call, Throwable t) {}
                });

    }



}
