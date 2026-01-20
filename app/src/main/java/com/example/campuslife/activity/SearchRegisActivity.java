package com.example.campuslife.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.campuslife.R;
import com.example.campuslife.adapter.TicketAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.ActivityRegistrationResponse;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchRegisActivity extends AppCompatActivity {

    private EditText edtSearch;
    private ImageView btnClear, btnBack;
    private RecyclerView rvEvents;
    private TicketAdapter adapter;
    private String status = null;
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

        txtTitle.setText("Search tickets");
        adapter = new TicketAdapter(new ArrayList<>());
        rvEvents.setAdapter(adapter);

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
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? ImageView.VISIBLE : ImageView.GONE);

                loadSearch(s.toString()); // search realtime
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadSearch(String keyword) {
        ApiClient.activityRegistrations(this)
                .search(keyword, status)
                .enqueue(new Callback<ApiResponse<List<ActivityRegistrationResponse>>>() {

                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<ActivityRegistrationResponse>>> call,
                            Response<ApiResponse<List<ActivityRegistrationResponse>>> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            List<ActivityRegistrationResponse> data = response.body().getData();
                            adapter.updateData(data);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ActivityRegistrationResponse>>> call, Throwable t) {
                    }
                });

    }
}
