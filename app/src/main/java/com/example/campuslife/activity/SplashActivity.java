package com.example.campuslife.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuslife.R;
import com.example.campuslife.auth.FirstLaunchStore;
import com.example.campuslife.auth.TokenStore;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        requestNotificationPermissionIfNeeded();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            if (FirstLaunchStore.isFirstLaunch(this)) {
                FirstLaunchStore.setFirstLaunchDone(this);
                startActivity(new Intent(this, OnboardingActivity.class));
                finish();
                return;
            }

            if (TokenStore.isLoggedIn(this)) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }

            finish();

        }, 1200);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }
}
