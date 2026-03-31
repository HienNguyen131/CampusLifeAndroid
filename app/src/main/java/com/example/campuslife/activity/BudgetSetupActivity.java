package com.example.campuslife.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.adapter.BudgetSetupCategoryAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.ActivityBudgetDto;
import com.example.campuslife.entity.preparation.BudgetCategoryDto;
import com.example.campuslife.entity.preparation.PreparationDashboardDto;
import com.example.campuslife.entity.preparation.UpsertActivityBudgetRequest;
import com.example.campuslife.entity.preparation.UpsertBudgetCategoryRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetSetupActivity extends AppCompatActivity {

    public static void start(Context context, long activityId) {
        Intent starter = new Intent(context, BudgetSetupActivity.class);
        starter.putExtra("ACTIVITY_ID", activityId);
        context.startActivity(starter);
    }

    private long activityId;
    private TextInputEditText etTotalEventBudget;
    private MaterialButton btnAddCategoryInput;
    private RecyclerView rvCategoryInputs;
    private TextView tvPreviewResidualAmount;
    private View tvPreviewErrorNegative;
    private MaterialButton btnSaveSetup;

    private BudgetSetupCategoryAdapter adapter;
    private final List<UpsertBudgetCategoryRequest> categories = new ArrayList<>();
    private final DecimalFormat df = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_setup);

        activityId = getIntent().getLongExtra("ACTIVITY_ID", -1);
        if (activityId == -1) {
            Toast.makeText(this, "Không tìm thấy sự kiện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadExistingBudget();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        etTotalEventBudget = findViewById(R.id.etTotalEventBudget);
        btnAddCategoryInput = findViewById(R.id.btnAddCategoryInput);
        rvCategoryInputs = findViewById(R.id.rvCategoryInputs);
        tvPreviewResidualAmount = findViewById(R.id.tvPreviewResidualAmount);
        tvPreviewErrorNegative = findViewById(R.id.tvPreviewErrorNegative);
        btnSaveSetup = findViewById(R.id.btnSaveSetup);

        adapter = new BudgetSetupCategoryAdapter(categories, this::calculatePreview);
        rvCategoryInputs.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryInputs.setAdapter(adapter);
        rvCategoryInputs.setNestedScrollingEnabled(false);

        etTotalEventBudget.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calculatePreview();
            }
        });

        btnAddCategoryInput.setOnClickListener(v -> {
            categories.add(new UpsertBudgetCategoryRequest());
            adapter.notifyItemInserted(categories.size() - 1);
            calculatePreview();
        });

        btnSaveSetup.setOnClickListener(v -> saveBudget());
    }

    private void loadExistingBudget() {
        ApiClient.preparation(this).getDashboard(activityId).enqueue(new Callback<ApiResponse<PreparationDashboardDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<PreparationDashboardDto>> call, Response<ApiResponse<PreparationDashboardDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    PreparationDashboardDto dto = response.body().getData();
                    if (dto != null && dto.activityBudget != null) {
                        ActivityBudgetDto budget = dto.activityBudget;
                        
                        if (budget.totalAmount != null) {
                            etTotalEventBudget.setText(budget.totalAmount);
                        }
                        
                        if (budget.categories != null) {
                            categories.clear();
                            for (BudgetCategoryDto cat : budget.categories) {
                                if (cat.name != null && (cat.name.equalsIgnoreCase("Khác") || cat.name.equalsIgnoreCase("Khac"))) {
                                    continue; // Skip residual wallet
                                }
                                UpsertBudgetCategoryRequest req = new UpsertBudgetCategoryRequest();
                                req.name = cat.name;
                                req.allocatedAmount = cat.allocatedAmount;
                                categories.add(req);
                            }
                            adapter.notifyDataSetChanged();
                        }
                        calculatePreview();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PreparationDashboardDto>> call, Throwable t) {}
        });
    }

    private void calculatePreview() {
        try {
            String totalStr = etTotalEventBudget.getText().toString().trim();
            if (totalStr.isEmpty()) totalStr = "0";

            double total = Double.parseDouble(totalStr);
            double sum = 0;

            for (UpsertBudgetCategoryRequest cat : categories) {
                if (cat.allocatedAmount != null && !cat.allocatedAmount.isEmpty()) {
                    try {
                        sum += Double.parseDouble(cat.allocatedAmount);
                    } catch (Exception e) {}
                }
            }

            double residual = total - sum;
            tvPreviewResidualAmount.setText((residual < 0 ? "-" : "") + df.format(Math.abs(residual)) + "đ");

            if (residual < 0) {
                tvPreviewErrorNegative.setVisibility(View.VISIBLE);
                btnSaveSetup.setEnabled(false);
                btnSaveSetup.setAlpha(0.5f);
            } else {
                tvPreviewErrorNegative.setVisibility(View.GONE);
                btnSaveSetup.setEnabled(true);
                btnSaveSetup.setAlpha(1.0f);
            }
        } catch (Exception e) {
            tvPreviewResidualAmount.setText("0đ");
        }
    }

    private void saveBudget() {
        String totalStr = etTotalEventBudget.getText().toString().trim();
        if (totalStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tổng ngân sách", Toast.LENGTH_SHORT).show();
            return;
        }

        // Filter valid categories (skip empty names or amounts)
        List<UpsertBudgetCategoryRequest> validCats = new ArrayList<>();
        for (UpsertBudgetCategoryRequest cat : categories) {
            String cName = cat.name != null ? cat.name.trim() : "";
            if (!cName.isEmpty() && (!cName.equalsIgnoreCase("Khác") && !cName.equalsIgnoreCase("Khac"))) {
                UpsertBudgetCategoryRequest req = new UpsertBudgetCategoryRequest();
                req.name = cName;
                req.allocatedAmount = cat.allocatedAmount != null ? cat.allocatedAmount.trim() : "0";
                if (req.allocatedAmount.isEmpty()) req.allocatedAmount = "0";
                validCats.add(req);
            }
        }

        UpsertActivityBudgetRequest request = new UpsertActivityBudgetRequest(totalStr, validCats);

        btnSaveSetup.setEnabled(false);
        ApiClient.preparation(this).upsertBudget(activityId, request).enqueue(new Callback<ApiResponse<ActivityBudgetDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<ActivityBudgetDto>> call, Response<ApiResponse<ActivityBudgetDto>> response) {
                btnSaveSetup.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(BudgetSetupActivity.this, "Lưu cấu trúc quỹ thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(BudgetSetupActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ActivityBudgetDto>> call, Throwable t) {
                btnSaveSetup.setEnabled(true);
                Toast.makeText(BudgetSetupActivity.this, "Lưu thất bại: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
