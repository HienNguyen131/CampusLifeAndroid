package com.example.campuslife.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.campuslife.R;
import com.example.campuslife.fragment.AdminHomeFragment;
import com.example.campuslife.fragment.AdminProfileFragment;
import com.example.campuslife.fragment.BlankFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        BottomNavigationView bottomBarAdmin = findViewById(R.id.bottomBarAdmin);

        // Load the default fragment
        if (savedInstanceState == null) {
            loadFragment(new AdminHomeFragment());
            bottomBarAdmin.setSelectedItemId(R.id.nav_home);
        }

        bottomBarAdmin.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(new AdminHomeFragment());
                return true;
            } else if (itemId == R.id.nav_events) {
                loadFragment(new com.example.campuslife.fragment.AdminEventFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new AdminProfileFragment());
                return true;
            } else {
                loadFragment(new BlankFragment());
                return true;
            }
        });
    }

    public void selectEventsTab() {
        BottomNavigationView bottomBarAdmin = findViewById(R.id.bottomBarAdmin);
        if (bottomBarAdmin != null) {
            bottomBarAdmin.setSelectedItemId(R.id.nav_events);
        }
    }

    public void selectHomeTab() {
        BottomNavigationView bottomBarAdmin = findViewById(R.id.bottomBarAdmin);
        if (bottomBarAdmin != null) {
            bottomBarAdmin.setSelectedItemId(R.id.nav_home);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
