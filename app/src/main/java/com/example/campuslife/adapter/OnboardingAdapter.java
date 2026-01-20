package com.example.campuslife.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.item.OnboardingItem;


import java.text.CollationElementIterator;
import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final List<OnboardingItem> items;

    public OnboardingAdapter(List<OnboardingItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingItem item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.image.setImageResource(item.getImageRes());
        holder.titleMain.setText(item.getTitleMain());
        holder.titleHighlight.setText(item.getTitleHighlight());
        holder.titleHighlight.setTextColor(item.getAccentColor());
        holder.desc.setText(item.getDescription());
        holder.progressText.setText(String.format("%02d / %02d", position + 1, getItemCount()));

        holder.listContainer.removeAllViews();
        for (String text : item.getBullets()) {
            TextView tv = new TextView(context);
            tv.setText(text);
            tv.setTextSize(14);
            tv.setTextColor(ContextCompat.getColor(context, R.color.text_dark));
            tv.setPadding(0, 8, 0, 0);
            holder.listContainer.addView(tv);
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView titleMain, titleHighlight, desc,progressText;
        LinearLayout listContainer;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageOnboarding);
            titleMain = itemView.findViewById(R.id.titleOnboarding);
            titleHighlight = itemView.findViewById(R.id.titleHighlight);
            desc = itemView.findViewById(R.id.descOnboarding);
            listContainer = itemView.findViewById(R.id.listContainer);
            progressText = itemView.findViewById(R.id.progressText);

        }
    }
}
