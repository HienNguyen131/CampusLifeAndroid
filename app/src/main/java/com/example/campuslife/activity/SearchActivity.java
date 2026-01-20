package com.example.campuslife.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.adapter.ActivityListAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.entity.Activity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText edtSearch;
    private ImageView btnClear, btnBack;
    private RecyclerView rvEvents;
    private ActivityListAdapter adapter;
    private TextView txtTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_activities);

        initViews();
        setupSearchListeners();
        loadSearch("");
    }

    private void initViews() {
        edtSearch = findViewById(R.id.edtSearch);
        btnClear = findViewById(R.id.btnClear);
        btnBack = findViewById(R.id.btnBack);
        rvEvents = findViewById(R.id.rvEvents);
        txtTitle= findViewById(R.id.txtTitle);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActivityListAdapter();
        rvEvents.setAdapter(adapter);
        txtTitle.setText("Search activities");
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSearchListeners() {


        btnClear.setOnClickListener(v -> {
            edtSearch.setText("");
            btnClear.setVisibility(ImageView.GONE);
            loadSearch("");
        });


        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                loadSearch(edtSearch.getText().toString());
                return true;
            }
            return false;
        });


        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? ImageView.VISIBLE : ImageView.GONE);
                loadSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadSearch(String keyword) {
        ApiClient.activities(this).searchActivities(keyword)
                .enqueue(new Callback<List<Activity>>() {
                    @Override
                    public void onResponse(Call<List<Activity>> call, Response<List<Activity>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.updateData(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Activity>> call, Throwable t) {

                    }
                });
    }
}
