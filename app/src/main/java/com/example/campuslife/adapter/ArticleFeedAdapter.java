package com.example.campuslife.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campuslife.R;
import com.example.campuslife.entity.ActivityPhotoResponse;

import java.util.List;

public class ArticleFeedAdapter extends RecyclerView.Adapter<ArticleFeedAdapter.FeedViewHolder> {

    private final List<ActivityPhotoResponse> photos;
    private static final String BASE_URL = "http://10.0.2.2:8080";

    public ArticleFeedAdapter(List<ActivityPhotoResponse> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_grid, parent, false);
        return new FeedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        ActivityPhotoResponse p = photos.get(position);

        holder.txtUploader.setText(p.getUploadedBy());
        holder.txtCaption.setText(p.getCaption());

        Glide.with(holder.imgPhoto.getContext())
                .load(BASE_URL + p.getImageUrl())
                .centerCrop()
                .into(holder.imgPhoto);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder {

        ImageView imgAvatar, imgPhoto;
        TextView txtUploader, txtCaption;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
            txtUploader = itemView.findViewById(R.id.txtUploader);
            txtCaption = itemView.findViewById(R.id.txtCaption);
        }
    }
}
