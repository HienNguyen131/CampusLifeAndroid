package com.example.campuslife.activity;


import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.campuslife.R;
import com.example.campuslife.api.ActivityReminderAPI;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.entity.ActivityReminderResponse;

import com.example.campuslife.fragment.ArticleFragment;
import com.example.campuslife.fragment.HomeFragment;
import com.example.campuslife.fragment.ProfileFragment;
import com.example.campuslife.fragment.ScoreFragment;
import com.example.campuslife.fragment.TicketFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String CHANNEL_ID = "activity_reminder_channel";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        BottomNavigationView bottomBar = findViewById(R.id.bottomBar);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomBar.setSelectedItemId(R.id.nav_home);
        }
        bottomBar.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_preparation) {
                Intent prepIntent = new Intent(this, StudentPreparationActivity.class);
                startActivity(prepIntent);
                return true;
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else if (id == R.id.nav_ticket) {
                fragment = new TicketFragment();
            }
            else if (id == R.id.nav_score) {
                fragment = new ScoreFragment();
            }
            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });

    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}