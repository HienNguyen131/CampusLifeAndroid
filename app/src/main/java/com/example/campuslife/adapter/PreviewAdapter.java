package com.example.campuslife.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.campuslife.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.Holder> {

    public interface OnClick {
        void onClick(int pos);
    }

    private List<String> photos;
    private OnClick onClick;

    public PreviewAdapter(List<String> photos, OnClick onClick) {
        this.photos = photos;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_preview_photo, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        Glide.with(h.img.getContext())
                .load(photos.get(pos))
                .centerCrop()
                .into(h.img);

        h.itemView.setOnClickListener(view -> onClick.onClick(pos));
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        ImageView img;

        public Holder(View v) {
            super(v);
            img = v.findViewById(R.id.imgThumb);
        }
    }
}
