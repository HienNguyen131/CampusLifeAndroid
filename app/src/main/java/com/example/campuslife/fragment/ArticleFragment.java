package com.example.campuslife.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.campuslife.R;
import com.example.campuslife.adapter.ArticleFeedAdapter;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.entity.ActivityPhotoResponse;

import java.util.ArrayList;
import java.util.List;

public class ArticleFragment extends Fragment {

    private RecyclerView rvPhotos;
    private ArticleFeedAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvPhotos = view.findViewById(R.id.rvArticles);

        // FEED STYLE (full width)
        rvPhotos.setLayoutManager(new LinearLayoutManager(getContext()));

        loadPhotos();
    }

    private void loadPhotos() {

        ApiClient.photo(getContext()).getAllPhotos()
                .enqueue(new Callback<ApiResponse<List<ActivityPhotoResponse>>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<List<ActivityPhotoResponse>>> call,
                                           Response<ApiResponse<List<ActivityPhotoResponse>>> response) {

                        if (!response.isSuccessful() || response.body() == null) return;

                        List<ActivityPhotoResponse> photos = response.body().getData();
                        if (photos == null) photos = new ArrayList<>();

                        adapter = new ArticleFeedAdapter(photos);
                        rvPhotos.setAdapter(adapter);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ActivityPhotoResponse>>> call,
                                          Throwable t) {
                        t.printStackTrace();
                    }
                });
    }
}
