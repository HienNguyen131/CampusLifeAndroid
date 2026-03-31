package com.example.campuslife.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campuslife.R;

import com.example.campuslife.activity.ListActivity;
import com.example.campuslife.activity.ListSeriesActivity;
import com.example.campuslife.activity.LoginActivity;
import com.example.campuslife.activity.NotificationActivity;

import com.example.campuslife.activity.ProfileDetailActivity;
import com.example.campuslife.activity.SearchActivity;
import com.example.campuslife.activity.StudentPreparationActivity;
import com.example.campuslife.adapter.ActivityForYouAdapter;
import com.example.campuslife.adapter.ActivityUpcomingAdapter;
import com.example.campuslife.adapter.SeriesAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.api.ProfileAPI;
import com.example.campuslife.auth.TokenStore;
import com.example.campuslife.entity.Activity;
import com.example.campuslife.entity.ActivitySeries;
import com.example.campuslife.entity.Student;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private RecyclerView rvUpcoming, rvForYou, rvSeries;
    private ActivityUpcomingAdapter upcomingAdapter;
    private ActivityForYouAdapter forYouAdapter;
    private SeriesAdapter seriesAdapter;
    private ProgressBar pbLoading, pbForYou, pbSeries;
    private TextView tvEmpty, tvEmptyForYou, tvEmptySeries, txtUpcoming, txtForYou;
    private ImageView ivProfile, ivSearch, ivNoti;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView txtUser = view.findViewById(R.id.txtUserName);
        String username = TokenStore.getUsername(requireContext());

        txtUser.setText(username != null ? username : "User");

        rvUpcoming = view.findViewById(R.id.rvUpcoming);
        pbLoading = view.findViewById(R.id.pbLoading);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        ivProfile = view.findViewById(R.id.ivProfile);
        ivSearch = view.findViewById(R.id.ivSearch);
        ivNoti = view.findViewById(R.id.ivNoti);
        txtUpcoming = view.findViewById(R.id.txtUpcoming);
        txtForYou = view.findViewById(R.id.txtForYou);
        ivSearch.setOnClickListener(t -> {
            Intent intent = new Intent(requireContext(), SearchActivity.class);
            startActivity(intent);
        });
        ivNoti.setOnClickListener(t -> {
            Intent intent = new Intent(requireContext(), NotificationActivity.class);
            startActivity(intent);
        });
        imgProfile();
        ivProfile.setOnClickListener(t -> {
            Intent intent = new Intent(requireContext(), ProfileDetailActivity.class);
            startActivity(intent);
        });
        upcomingAdapter = new ActivityUpcomingAdapter();
        rvUpcoming.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvUpcoming.setAdapter(upcomingAdapter);
        loadUpcoming();

        rvForYou = view.findViewById(R.id.rvForYou);
        pbForYou = view.findViewById(R.id.pbForYou);
        tvEmptyForYou = view.findViewById(R.id.tvEmptyForYou);
        forYouAdapter = new ActivityForYouAdapter();
        rvForYou.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvForYou.setAdapter(forYouAdapter);
        loadForYou();

        rvSeries = view.findViewById(R.id.rvSeries);
        pbSeries = view.findViewById(R.id.pbSeries);
        tvEmptySeries = view.findViewById(R.id.tvEmptySeries);
        seriesAdapter = new SeriesAdapter();
        rvSeries.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvSeries.setAdapter(seriesAdapter);
        loadSeries();

        view.findViewById(R.id.txtAllupcoming).setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), ListActivity.class);
            i.putExtra("EXTRA_MODE", "MONTH");
            i.putExtra("EXTRA_TITLE", "Upcoming this month");
            startActivity(i);
        });

        view.findViewById(R.id.txtAllForYou).setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), ListActivity.class);
            i.putExtra("EXTRA_MODE", "FORYOU");
            i.putExtra("EXTRA_TITLE", "Activity for you");
            startActivity(i);
        });
        view.findViewById(R.id.txtAllSeries).setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), ListSeriesActivity.class);
            startActivity(i);
        });

        
        String currentMonth = LocalDate.now()
                .getMonth()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        txtUpcoming.setText("Upcoming activity (" + currentMonth + ")");

        // Chip click
        view.findViewById(R.id.chipTranningPoint).setOnClickListener(v -> openCategory("REN_LUYEN", "Training Point"));
        view.findViewById(R.id.chipBussiness)
                .setOnClickListener(v -> openCategory("CONG_TAC_XA_HOI", "Business Score"));
        view.findViewById(R.id.chipSocial)
                .setOnClickListener(v -> openCategory("CHUYEN_DE_DOANH_NGHIEP", "Social Activity"));
    }

    private void loadSeries() {
        ApiClient.series(requireContext())
                .getAllSeries()
                .enqueue(new Callback<ApiResponse<List<ActivitySeries>>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<List<ActivitySeries>>> call,
                            Response<ApiResponse<List<ActivitySeries>>> r) {

                        showLoading(false);

                        if (r.isSuccessful() && r.body() != null) {

                            List<ActivitySeries> data = r.body().getData();

                            if (data == null || data.isEmpty()) {
                                rvSeries.setVisibility(View.GONE);
                                tvEmptySeries.setVisibility(View.VISIBLE);
                            } else {
                                tvEmptySeries.setVisibility(View.GONE);
                                rvSeries.setVisibility(View.VISIBLE);
                                seriesAdapter.submit(data);
                            }

                        } else {
                            toast("HTTP " + r.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ActivitySeries>>> call, Throwable t) {
                        showLoading(false);
                        toast("Lỗi mạng: " + t.getMessage());
                        Log.e("HOME_DEBUG", "→ onFailure: " + t.getMessage(), t);
                    }
                });
    }

    private void openCategory(String type, String title) {
        Intent i = new Intent(requireContext(), ListActivity.class);
        i.putExtra("EXTRA_TYPE", type);
        i.putExtra("EXTRA_TITLE", title);
        startActivity(i);
    }

    private void imgProfile() {

        Context ctx = getContext();
        if (ctx == null)
            return;

        ProfileAPI api = ApiClient.profile(ctx);

        api.getMyProfile().enqueue(new Callback<ApiResponse<Student>>() {
            @Override
            public void onResponse(Call<ApiResponse<Student>> call, Response<ApiResponse<Student>> resp) {

                if (!isAdded() || getContext() == null)
                    return;

                if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {

                    Student student = resp.body().getData();
                    String url = student.getAvatarUrl() == null
                            ? ""
                            : student.getAvatarUrl()
                                    .replace("http://localhost:8080", "http://10.0.2.2:8080");

                    // url = url.replace("http://localhost:8080", "http://10.0.2.2:8080");

                    if (url != null && url.startsWith("http")) {

                        Glide.with(HomeFragment.this)
                                .load(url)
                                .placeholder(R.drawable.ic_profile_2)
                                .error(R.drawable.ic_profile_2)
                                .circleCrop()
                                .into(ivProfile);
                    } else {
                        ivProfile.setImageResource(R.drawable.ic_profile_2);
                    }

                } else {
                    Toast.makeText(getContext(),
                            "Không thể tải thông tin cá nhân",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Student>> call, Throwable throwable) {

                if (!isAdded() || getContext() == null)
                    return;

                Toast.makeText(getContext(),
                        "Lỗi: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUpcoming() {
        android.util.Log.d("HOME_DEBUG", "→ loadUpcoming() called");
        showLoading(true);
        ApiClient.activities(requireContext())
                .thisMonth()
                .enqueue(new retrofit2.Callback<List<Activity>>() {
                    @Override
                    public void onResponse(Call<List<Activity>> call, Response<List<Activity>> r) {
                        showLoading(false);
                        if (r.isSuccessful() && r.body() != null) {
                            List<Activity> data = r.body();
                            android.util.Log.d("HOME_DEBUG", "Upcoming count = " + data.size());
                            if (data.isEmpty()) {
                                rvUpcoming.setVisibility(View.GONE);
                                tvEmpty.setVisibility(View.VISIBLE);
                            } else {
                                tvEmpty.setVisibility(View.GONE);
                                rvUpcoming.setVisibility(View.VISIBLE);
                                upcomingAdapter.submit(data);
                            }
                        } else {
                            toast("HTTP " + r.code());

                        }
                    }

                    @Override
                    public void onFailure(Call<List<Activity>> call, Throwable t) {
                        showLoading(false);
                        toast("Lỗi mạng: " + t.getMessage());
                        android.util.Log.e("HOME_DEBUG", "→ onFailure: " + t.getMessage(), t);
                    }
                });
    }

    private void loadForYou() {
        showLoadingForYou(true);
        ApiClient.activities(requireContext()).myActivities()
                .enqueue(new retrofit2.Callback<List<Activity>>() {
                    @Override
                    public void onResponse(Call<List<Activity>> c, Response<List<Activity>> r) {
                        showLoadingForYou(false);

                        if (r.code() == 401 || r.code() == 403) {
                            TokenStore.clearAll(requireContext());

                            startActivity(new Intent(requireContext(), LoginActivity.class));
                            requireActivity().finish();
                            return;
                        }
                        if (r.isSuccessful() && r.body() != null) {
                            List<Activity> data = r.body();
                            if (data.isEmpty()) {
                                rvForYou.setVisibility(View.GONE);
                                tvEmptyForYou.setVisibility(View.VISIBLE);
                            } else {
                                tvEmptyForYou.setVisibility(View.GONE);
                                rvForYou.setVisibility(View.VISIBLE);
                                forYouAdapter.submit(data);
                            }
                        } else {
                            toast("HTTP " + r.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Activity>> c, Throwable t) {
                        showLoadingForYou(false);
                        toast("Lỗi mạng: " + t.getMessage());
                    }
                });
    }

    private void showLoading(boolean b) {
        if (pbLoading != null)
            pbLoading.setVisibility(b ? View.VISIBLE : View.GONE);
        if (rvUpcoming != null)
            rvUpcoming.setAlpha(b ? 0.4f : 1f);
    }

    private void showLoadingForYou(boolean b) {
        if (pbForYou != null)
            pbForYou.setVisibility(b ? View.VISIBLE : View.GONE);
        if (rvForYou != null)
            rvForYou.setAlpha(b ? 0.4f : 1f);
    }

    private void toast(String s) {
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
    }

}
