package com.example.campuslife.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.adapter.ActivityListAdapter;
import com.example.campuslife.api.ActivityApi;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.entity.Activity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListActivity extends AppCompatActivity {

    private ActivityListAdapter adapter;
    private RecyclerView rvEvents;
    private View progress;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_category);

        String mode  = getIntent().getStringExtra("EXTRA_MODE");
        String type  = getIntent().getStringExtra("EXTRA_TYPE");

        TextView txtTitle = findViewById(R.id.txtNameCategory);
        txtTitle.setText(getIntent().getStringExtra("EXTRA_TITLE"));

        rvEvents = findViewById(R.id.rvEvents);
        progress = findViewById(R.id.progress);

        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActivityListAdapter();
        rvEvents.setAdapter(adapter);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        fetch(mode, type);
    }

    private void fetch(String mode, String type) {
        showLoading(true);
        ActivityApi api = ApiClient.activities(this);
        Call<List<Activity>> call;

        if ("MONTH".equals(mode)) {
            call = api.thisMonth();
        } else if ("FORYOU".equals(mode)) {
            call = api.myActivities();
        } else {
            call = api.byType(type);
        }

        call.enqueue(new Callback<List<Activity>>() {
            @Override
            public void onResponse(Call<List<Activity>> c, Response<List<Activity>> r) {
                showLoading(false);
                if (r.isSuccessful() && r.body() != null) {
                    Log.d("LIST_FETCH", "Loaded " + r.body().size() + " activities");
                    adapter.updateData(r.body());
                } else {
                    Toast.makeText(ListActivity.this, "HTTP " + r.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Activity>> c, Throwable t) {
                showLoading(false);
                Toast.makeText(ListActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ACTIVITY_DETAIL", "onFailure", t);
            }
        });
    }

    private void showLoading(boolean b) {
        if (progress != null) progress.setVisibility(b ? View.VISIBLE : View.GONE);
        if (rvEvents != null) rvEvents.setAlpha(b ? 0.4f : 1f);
    }
}
