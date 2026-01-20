package com.example.campuslife.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.campuslife.R;
import com.example.campuslife.adapter.OnboardingAdapter;
import com.example.campuslife.item.OnboardingItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView skipBtn, nextBtn,progressText;
    private OnboardingAdapter adapter;
    private boolean navigating = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPagerOnboarding);
        skipBtn   = findViewById(R.id.skipBtn);
        nextBtn   = findViewById(R.id.nextBtn);

        List<OnboardingItem> data = new ArrayList<>();


        data.add(new OnboardingItem(
                R.drawable.onbroading_01,
                "Find Events That",
                "Match You",
                "Explore thousands of campus activities, from academic workshops to social events, all in one place.",

                Color.parseColor("#FF6B00"),
                Arrays.asList(
                        "Personalized event recommendations",
                        "Never miss important deadlines",
                        "Filter by location and interests"
                )
        ));

        data.add(new OnboardingItem(
                R.drawable.onbroading_02,
                "Grow Your",
                "Campus Network",
                "Meet students who share your passions, join clubs, and build meaningful connections that last.",
                Color.parseColor("#FF6B00"),
                Arrays.asList(
                        "Join clubs and organizations",
                        "Chat with event organizers",
                        "Find study partners and teammates"
                )
        ));

        data.add(new OnboardingItem(
                R.drawable.onbroading_03,
                "Track Your",
                "Success Story",
                "Earn training points for every activity, unlock achievements, and showcase your campus involvement.",
                Color.parseColor("#FF6B00"),
                Arrays.asList(
                        "Collect points and badges",
                        "Track your participation history",
                        "Unlock exclusive rewards"
                )
        ));


        adapter = new OnboardingAdapter(data);
        viewPager.setAdapter(adapter);

        final int lastIndex = adapter.getItemCount() - 1;

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                nextBtn.setText(position == lastIndex ? "Get Started" : "Next");
                skipBtn.setText("Skip");
            }
        });

        // Next button
        nextBtn.setOnClickListener(v -> {
            int next = viewPager.getCurrentItem() + 1;
            if (next <= lastIndex) {
                viewPager.setCurrentItem(next, true);
            }
            if (next > lastIndex) {
                safeGoToMain();
            }
        });

        skipBtn.setOnClickListener(v -> {
            safeGoToMain();
        });

        if (savedInstanceState != null) {
            int pos = savedInstanceState.getInt("ob_pos", 0);
            viewPager.setCurrentItem(pos, false);
        }
    }

    private void safeGoToMain() {
        if (navigating) return;
        navigating = true;
        markOnboardingSeen();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void markOnboardingSeen() {
        getSharedPreferences("app", MODE_PRIVATE)
                .edit().putBoolean("onboarding_seen", true).apply();
    }

    @Override
    public void onBackPressed() {
        if (viewPager != null && viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("ob_pos", viewPager.getCurrentItem());
    }
}
