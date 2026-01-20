package com.example.campuslife.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.campuslife.R;
import com.example.campuslife.entity.ActivityPhotoResponse;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotosViewHolder>{
    private final List<ActivityPhotoResponse> photos;
    private static final String BASE_URL = "http://10.0.2.2:8080";

    public PhotoAdapter() {
        this.photos = new ArrayList<>();
    }
    public void updateData(List<ActivityPhotoResponse> newPhotos) {
        this.photos.clear();
        this.photos.addAll(newPhotos);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public PhotoAdapter.PhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photos, parent, false);
        return new PhotoAdapter.PhotosViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoAdapter.PhotosViewHolder holder, int position) {
        ActivityPhotoResponse p = photos.get(position);
        Glide.with(holder.imgPhoto.getContext())
                .load(BASE_URL + p.getImageUrl())
                .centerCrop()
                .into(holder.imgPhoto);
    }

    @Override
    public int getItemCount() {
        return photos.size();

    }

    public class PhotosViewHolder extends RecyclerView.ViewHolder {
        private ShapeableImageView imgPhoto;
        public PhotosViewHolder(View v) {
            super(v);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
        }
    }
}
