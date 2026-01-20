package com.example.campuslife.activity;

import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.campuslife.R;
import com.example.campuslife.adapter.GridSpacingItemDecoration;
import com.example.campuslife.adapter.PhotoAdapter;
import com.example.campuslife.adapter.PreviewAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.ActivityPhotoResponse;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoViewerActivity extends AppCompatActivity {

    private ImageView imgMain, btnNext, btnPrev, btnClose;
    private TextView tvIndex, tvUploader;
    private RecyclerView rvPreview;

    private ArrayList<String> photos = new ArrayList<>();
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        imgMain = findViewById(R.id.imgMain);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnClose = findViewById(R.id.btnClose);
        tvIndex = findViewById(R.id.tvIndex);
        tvUploader = findViewById(R.id.tvUploader);
        rvPreview = findViewById(R.id.rvPreview);

        long activityId = getIntent().getLongExtra("activityId", -1);


        rvPreview.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        );

        if (activityId > 0) {
            LoadPhoto(activityId);
        }

        btnClose.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> nextPhoto());
        btnPrev.setOnClickListener(v -> prevPhoto());
    }

    private void updateMainPhoto() {
        if (photos.isEmpty()) return;

        Glide.with(this)
                .load(photos.get(currentIndex))
                .centerInside()
                .into(imgMain);

        tvIndex.setText((currentIndex + 1) + " / " + photos.size());
    }

    private void nextPhoto() {
        if (currentIndex < photos.size() - 1) {
            currentIndex++;
            updateMainPhoto();
        }
    }

    private void prevPhoto() {
        if (currentIndex > 0) {
            currentIndex--;
            updateMainPhoto();
        }
    }

    private void LoadPhoto(long id) {
        ApiClient.photo(this)
                .getActivityPhotos(id)
                .enqueue(new Callback<ApiResponse<List<ActivityPhotoResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ActivityPhotoResponse>>> call,
                                           Response<ApiResponse<List<ActivityPhotoResponse>>> response) {

                        if (!response.isSuccessful() || response.body() == null) return;

                        List<ActivityPhotoResponse> res = response.body().getData();

                        photos.clear();
                        for (ActivityPhotoResponse p : res) {
                            photos.add("http://10.0.2.2:8080" + p.getImageUrl());
                            tvUploader.setText(p.getCaption());
                        }

                        rvPreview.setAdapter(new PreviewAdapter(photos, pos -> {
                            currentIndex = pos;
                            updateMainPhoto();
                        }));

                        updateMainPhoto();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ActivityPhotoResponse>>> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
    }
}
