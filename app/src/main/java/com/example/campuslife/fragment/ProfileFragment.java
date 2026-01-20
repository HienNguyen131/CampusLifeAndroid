package com.example.campuslife.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.bumptech.glide.Glide;
import com.example.campuslife.R;
import com.example.campuslife.activity.ForgotActivity;
import com.example.campuslife.activity.LoginActivity;
import com.example.campuslife.activity.ProfileDetailActivity;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.api.ProfileAPI;
import com.example.campuslife.auth.TokenStore;
import com.example.campuslife.entity.Student;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileFragment extends Fragment {
    private MaterialCardView btnProfileDetail, btnsupport, btnLogout;
    private ShapeableImageView imgAvatar;
    private TextView tvName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnLogout=view.findViewById(R.id.btnLogoout);
        btnsupport=view.findViewById(R.id.btnSupport);
        btnProfileDetail=view.findViewById(R.id.btnManageProfile);
        imgAvatar =view.findViewById(R.id.imgAvatar);
        tvName=view.findViewById(R.id.tvName);
        btnProfileDetail.setOnClickListener(v->{
            Intent intent = new Intent(requireContext(), ProfileDetailActivity.class);
            startActivity(intent);
        });
        btnsupport.setOnClickListener(v->{
            Intent intent = new Intent(requireContext(), ForgotActivity.class);
            startActivity(intent);
        });
        btnLogout.setOnClickListener(v -> handleLogout());
        imgProfile();
    }
    private void imgProfile() {

        Context ctx = getContext();
        if (ctx == null) return;

        ProfileAPI api = ApiClient.profile(ctx);

        api.getMyProfile().enqueue(new Callback<ApiResponse<Student>>() {
            @Override
            public void onResponse(Call<ApiResponse<Student>> call, Response<ApiResponse<Student>> resp) {

                if (!isAdded() || getContext() == null) return;

                if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {

                    Student student = resp.body().getData();
                    if (student != null && !TextUtils.isEmpty(student.getFullName())) {
                        tvName.setText(student.getFullName());
                    } else {
                        tvName.setText("Chưa cập nhật");
                    }
                    String url = student.getAvatarUrl() == null
                            ? ""
                            : student.getAvatarUrl()
                            .replace("http://localhost:8080", "http://172.21.13.137:8080");

//                    url = url.replace("http://localhost:8080", "http://10.0.2.2:8080");

                    if (url != null && url.startsWith("http")) {

                        Glide.with(ProfileFragment.this)
                                .load(url)
                                .placeholder(R.drawable.ic_profile_2)
                                .error(R.drawable.ic_profile_2)
                                .circleCrop()
                                .into(imgAvatar);

                    } else {
                        imgAvatar.setImageResource(R.drawable.ic_profile_2);
                    }

                } else {
                    Toast.makeText(getContext(),
                            "Không thể tải thông tin cá nhân",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Student>> call, Throwable throwable) {

                if (!isAdded() || getContext() == null) return;

                Toast.makeText(getContext(),
                        "Lỗi: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
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
