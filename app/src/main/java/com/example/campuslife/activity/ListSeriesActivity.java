package com.example.campuslife.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.campuslife.R;
import com.example.campuslife.adapter.ActivityListAdapter;
import com.example.campuslife.adapter.SeriesAdapter;
import com.example.campuslife.api.ActivityApi;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.Activity;
import com.example.campuslife.entity.ActivitySeries;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ListSeriesActivity extends AppCompatActivity {

    private SeriesAdapter seriesAdapter;
    private RecyclerView rvSeries;
    private View progress;
    private ImageView btnBack;
    private TextView tvEmptySeries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_series);

        TextView txtTitle = findViewById(R.id.txtNameCategory);
        rvSeries = findViewById(R.id.rvEvents);
        progress = findViewById(R.id.progress);
        btnBack = findViewById(R.id.btnBack);
        tvEmptySeries = findViewById(R.id.tvEmptySeries);

        rvSeries.setLayoutManager(new LinearLayoutManager(this));


        seriesAdapter = new SeriesAdapter();
        rvSeries.setAdapter(seriesAdapter);

        btnBack.setOnClickListener(v -> finish());


        loadSeries();
    }

    private void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void loadSeries() {

        showLoading(true);

        ApiClient.series(this)
                .getAllSeries()
                .enqueue(new Callback<ApiResponse<List<ActivitySeries>>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<List<ActivitySeries>>> call,
                                           Response<ApiResponse<List<ActivitySeries>>> r) {

                        showLoading(false);

                        if (r.isSuccessful() && r.body() != null) {

                            List<ActivitySeries> data = r.body().getData();

                            if (data == null || data.isEmpty()) {
                                rvSeries.setVisibility(View.GONE);
                                tvEmptySeries.setVisibility(View.VISIBLE);
                            } else {
                                tvEmptySeries.setVisibility(View.GONE);
                                rvSeries.setVisibility(View.VISIBLE);


                                seriesAdapter.submit(data);
                            }

                        } else {
                            toast("HTTP " + r.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ActivitySeries>>> call, Throwable t) {
                        showLoading(false);
                        toast("Lỗi mạng: " + t.getMessage());
                    }
                });
    }
}
