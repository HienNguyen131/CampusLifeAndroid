package com.example.campuslife.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campuslife.R;
import com.example.campuslife.activity.ForgotActivity;
import com.example.campuslife.activity.LoginActivity;
import com.example.campuslife.activity.ProfileDetailActivity;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.auth.TokenStore;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

public class AdminProfileFragment extends Fragment {
    private MaterialCardView btnProfileDetail, btnsupport, btnLogout;
    private ShapeableImageView imgAvatar;
    private TextView tvName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        btnLogout = view.findViewById(R.id.btnLogoout);
        btnsupport = view.findViewById(R.id.btnSupport);
        btnProfileDetail = view.findViewById(R.id.btnManageProfile);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvName = view.findViewById(R.id.tvName);
        
        // Hide Manage Profile since admins don't have a detailed profile like students yet
        btnProfileDetail.setVisibility(View.GONE);

        btnsupport.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ForgotActivity.class);
            startActivity(intent);
        });
        
        btnLogout.setOnClickListener(v -> handleLogout());
        
        loadAdminProfile();
    }

    private void loadAdminProfile() {
        String username = TokenStore.getUsername(requireContext());
        if (username != null && !username.isEmpty()) {
            // Capitalize first letter
            String displayName = username.substring(0, 1).toUpperCase() + username.substring(1);
            tvName.setText("Quản trị viên: " + displayName);
        } else {
            tvName.setText("Quản trị viên");
        }
        
        // Admins don't have avatars in User entity, set a default
        imgAvatar.setImageResource(R.drawable.ic_profile_2);
    }

    private void handleLogout() {
        TokenStore.clearAll(requireContext());
        ApiClient.reset();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
