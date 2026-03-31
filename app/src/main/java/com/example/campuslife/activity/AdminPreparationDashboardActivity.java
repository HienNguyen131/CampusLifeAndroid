package com.example.campuslife.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.adapter.PreparationExpenseAdapter;
import com.example.campuslife.adapter.PreparationOrganizerAdapter;
import com.example.campuslife.adapter.PreparationTaskAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.ExpenseDto;
import com.example.campuslife.entity.preparation.OrganizerDto;
import com.example.campuslife.entity.preparation.PreparationDashboardDto;
import com.example.campuslife.entity.preparation.PreparationTaskDto;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPreparationDashboardActivity extends AppCompatActivity {

    private long activityId;
    
    // UI Elements
    private ImageView btnBack;
    private TextView tvTotalAmount, tvSpentAmount, tvRemainingAmount;
    private TextView tvWalletCount, tvTotalUsedPercent;
    private com.google.android.material.progressindicator.LinearProgressIndicator progressTotalBudget;
    
    // Full Budget Tab UI Elements
    private android.view.View layoutEmptyBudget, layoutBudgetData, layoutBudgetFull, cvResidualWallet;
    private TextView tvBigTotalAmount, tvBigSpent, tvBigProgressPercent, tvTotalCategoriesCount;
    private TextView tvResidualAllocated, tvResidualUsed, tvResidualRemaining, tvWarningResidualNegative;
    private com.google.android.material.progressindicator.LinearProgressIndicator progressBigTotal;
    private RecyclerView rvAdminWallets;
    private com.example.campuslife.adapter.PreparationAdminWalletAdapter adminWalletAdapter;
    private List<com.example.campuslife.entity.preparation.BudgetCategoryDto> adminWalletList;
    
    // RecyclerViews
    private RecyclerView rvOrganizers, rvTasks, rvPendingExpenses, rvHistoryExpenses;
    private RecyclerView rvAllocationRequests, rvFundAdvances, rvFundAdvanceDebts;
    
    // Expense Views
    private TextView tvPendingExpensesCount;
    private Spinner spPendingExpenseFilter, spHistoryExpenseFilter;
    
    // Adapters & Data
    private PreparationOrganizerAdapter organizerAdapter;
    private List<OrganizerDto> organizerList;
    
    private PreparationTaskAdapter taskAdapter;
    private List<PreparationTaskDto> taskList;

    private com.example.campuslife.adapter.AllocationAdjustmentAdapter allocationAdjustmentAdapter;
    private List<com.example.campuslife.entity.preparation.AllocationAdjustmentRequestDto> allocationAdjustmentList;

    private com.example.campuslife.adapter.FundAdvanceAdapter fundAdvanceAdapter;
    private List<com.example.campuslife.entity.preparation.FundAdvanceDto> fundAdvanceList;

    private com.example.campuslife.adapter.FundAdvanceDebtAdapter fundAdvanceDebtAdapter;
    private List<com.example.campuslife.entity.preparation.FundAdvanceDebtDto> fundAdvanceDebtList;
    
    private PreparationExpenseAdapter pendingExpenseAdapter;
    private List<ExpenseDto> pendingExpenseList;
    
    private PreparationExpenseAdapter historyExpenseAdapter;
    private List<ExpenseDto> historyExpenseList;
    
    private final DecimalFormat df = new DecimalFormat("#,###");
    private Long selectedAssigneeId = null;
    private boolean hasPreparation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_preparation_dashboard);

        activityId = getIntent().getLongExtra("ACTIVITY_ID", -1);
        if (activityId == -1) {
            Toast.makeText(this, "Không tìm thấy sự kiện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerViews();
        setupFabListener();
        
        loadDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvSpentAmount = findViewById(R.id.tvSpentAmount);
        tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
        tvWalletCount = findViewById(R.id.tvWalletCount);
        tvTotalUsedPercent = findViewById(R.id.tvTotalUsedPercent);
        progressTotalBudget = findViewById(R.id.progressTotalBudget);
        
        rvOrganizers = findViewById(R.id.rvOrganizers);
        rvTasks = findViewById(R.id.rvTasks);
        rvAllocationRequests = findViewById(R.id.rvAllocationRequests);
        rvFundAdvances = findViewById(R.id.rvFundAdvances);
        rvFundAdvanceDebts = findViewById(R.id.rvFundAdvanceDebts);
        
        rvPendingExpenses = findViewById(R.id.rvPendingExpenses);
        rvHistoryExpenses = findViewById(R.id.rvHistoryExpenses);
        tvPendingExpensesCount = findViewById(R.id.tvPendingExpensesCount);
        spPendingExpenseFilter = findViewById(R.id.spPendingExpenseFilter);
        spHistoryExpenseFilter = findViewById(R.id.spHistoryExpenseFilter);
        
        layoutBudgetFull = findViewById(R.id.layoutBudgetFull);
        layoutEmptyBudget = findViewById(R.id.layoutEmptyBudget);
        layoutBudgetData = findViewById(R.id.layoutBudgetData);
        cvResidualWallet = findViewById(R.id.cvResidualWallet);
        tvBigTotalAmount = findViewById(R.id.tvBigTotalAmount);
        tvBigSpent = findViewById(R.id.tvBigSpent);
        tvBigProgressPercent = findViewById(R.id.tvBigProgressPercent);
        tvTotalCategoriesCount = findViewById(R.id.tvTotalCategoriesCount);
        tvResidualAllocated = findViewById(R.id.tvResidualAllocated);
        tvResidualUsed = findViewById(R.id.tvResidualUsed);
        tvResidualRemaining = findViewById(R.id.tvResidualRemaining);
        tvWarningResidualNegative = findViewById(R.id.tvWarningResidualNegative);
        progressBigTotal = findViewById(R.id.progressBigTotal);
        rvAdminWallets = findViewById(R.id.rvAdminWallets);
        
        if (findViewById(R.id.btnCreateBudgetSetup) != null) {
            findViewById(R.id.btnCreateBudgetSetup).setOnClickListener(v -> com.example.campuslife.activity.BudgetSetupActivity.start(this, activityId));
        }
        if (findViewById(R.id.btnEditBudgetSetup) != null) {
            findViewById(R.id.btnEditBudgetSetup).setOnClickListener(v -> com.example.campuslife.activity.BudgetSetupActivity.start(this, activityId));
        }

        com.google.android.material.button.MaterialButton btnToggle = findViewById(R.id.btnTogglePreparation);
        if (btnToggle != null) {
            btnToggle.setOnClickListener(v -> togglePreparation(!hasPreparation));
        }

        android.view.View layoutNoPrep = findViewById(R.id.layoutNoPreparation);
        if (layoutNoPrep != null) {
            layoutNoPrep.setOnClickListener(v -> togglePreparation(true)); // Click on Empty State
        }
    }

    private void setupRecyclerViews() {
        adminWalletList = new ArrayList<>();
        if (rvAdminWallets != null) {
            adminWalletAdapter = new com.example.campuslife.adapter.PreparationAdminWalletAdapter(this, adminWalletList);
            rvAdminWallets.setLayoutManager(new LinearLayoutManager(this));
            rvAdminWallets.setAdapter(adminWalletAdapter);
        }

        organizerList = new ArrayList<>();
        organizerAdapter = new PreparationOrganizerAdapter(this, organizerList, activityId, this::loadOrganizersData);
        rvOrganizers.setLayoutManager(new LinearLayoutManager(this));
        rvOrganizers.setAdapter(organizerAdapter);
        
        taskList = new ArrayList<>();
        taskAdapter = new PreparationTaskAdapter(this, taskList, -1L, task -> {
            com.example.campuslife.utils.PreparationTaskDetailManager manager = new com.example.campuslife.utils.PreparationTaskDetailManager(
                this, activityId, -1L, task, this::loadDashboardData
            );
            manager.show();
        });
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(taskAdapter);
        
        pendingExpenseList = new ArrayList<>();
        pendingExpenseAdapter = new PreparationExpenseAdapter(this, pendingExpenseList, false, this::loadExpensesData);
        rvPendingExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvPendingExpenses.setAdapter(pendingExpenseAdapter);
        
        historyExpenseList = new ArrayList<>();
        historyExpenseAdapter = new PreparationExpenseAdapter(this, historyExpenseList, true, this::loadExpensesData);
        rvHistoryExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvHistoryExpenses.setAdapter(historyExpenseAdapter);

        if (rvAllocationRequests != null) {
            allocationAdjustmentList = new ArrayList<>();
            allocationAdjustmentAdapter = new com.example.campuslife.adapter.AllocationAdjustmentAdapter(this, allocationAdjustmentList, true, activityId);
            rvAllocationRequests.setLayoutManager(new LinearLayoutManager(this));
            rvAllocationRequests.setAdapter(allocationAdjustmentAdapter);
        }

        if (rvFundAdvances != null) {
            fundAdvanceList = new ArrayList<>();
            fundAdvanceAdapter = new com.example.campuslife.adapter.FundAdvanceAdapter(this, fundAdvanceList, true);
            rvFundAdvances.setLayoutManager(new LinearLayoutManager(this));
            rvFundAdvances.setAdapter(fundAdvanceAdapter);
        }

        if (rvFundAdvanceDebts != null) {
            fundAdvanceDebtList = new ArrayList<>();
            fundAdvanceDebtAdapter = new com.example.campuslife.adapter.FundAdvanceDebtAdapter(this, fundAdvanceDebtList);
            rvFundAdvanceDebts.setLayoutManager(new LinearLayoutManager(this));
            rvFundAdvanceDebts.setAdapter(fundAdvanceDebtAdapter);
        }
    }

    private void loadDashboardData() {
        ApiClient.preparation(this).getDashboard(activityId).enqueue(new Callback<ApiResponse<PreparationDashboardDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<PreparationDashboardDto>> call, Response<ApiResponse<PreparationDashboardDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    PreparationDashboardDto dto = response.body().getData();
                    
                    if (dto != null) {
                        hasPreparation = dto.hasPreparation;
                        updatePreparationToggleUI();
                        
                        if (!hasPreparation) return;

                        loadFinanceOverview();
                        loadOrganizersData();
                        loadWorkloadWarningsData();
                        loadExpensesData();

                        try {
                            if (tvWalletCount != null) {
                                int walletCount = (dto.activityBudget != null && dto.activityBudget.categories != null) ? dto.activityBudget.categories.size() : 0;
                                tvWalletCount.setText("• Gồm " + walletCount + " ví nhỏ");
                            }
                        } catch (Exception e) {}
                        
                        // Full budget wallet bindings
                        if (dto.activityBudget != null && dto.activityBudget.categories != null && !dto.activityBudget.categories.isEmpty()) {
                            if (layoutEmptyBudget != null) layoutEmptyBudget.setVisibility(android.view.View.GONE);
                            if (layoutBudgetData != null) layoutBudgetData.setVisibility(android.view.View.VISIBLE);
                            
                            adminWalletList.clear();
                            com.example.campuslife.entity.preparation.BudgetCategoryDto residual = null;
                            for (com.example.campuslife.entity.preparation.BudgetCategoryDto cat : dto.activityBudget.categories) {
                                if (cat.name != null && (cat.name.equalsIgnoreCase("Khác") || cat.name.equalsIgnoreCase("Khac"))) {
                                    residual = cat;
                                } else {
                                    adminWalletList.add(cat);
                                }
                            }
                            
                            if (residual != null) {
                                if (cvResidualWallet != null) cvResidualWallet.setVisibility(android.view.View.VISIBLE);
                                try {
                                    double alloc = residual.allocatedAmount != null ? Double.parseDouble(residual.allocatedAmount) : 0;
                                    if (tvResidualAllocated != null) tvResidualAllocated.setText(df.format(alloc) + "đ");
                                } catch (Exception e) {}
                                try {
                                    double used = residual.usedAmount != null ? Double.parseDouble(residual.usedAmount) : 0;
                                    if (tvResidualUsed != null) tvResidualUsed.setText(df.format(used) + "đ");
                                } catch (Exception e) {}
                                try {
                                    double remain = residual.remainingAmount != null ? Double.parseDouble(residual.remainingAmount) : 0;
                                    if (tvResidualRemaining != null) tvResidualRemaining.setText((remain < 0 ? "-" : "") + df.format(Math.abs(remain)) + "đ");
                                    if (tvWarningResidualNegative != null) {
                                        tvWarningResidualNegative.setVisibility(remain < 0 ? android.view.View.VISIBLE : android.view.View.GONE);
                                    }
                                } catch (Exception e) {}
                            } else {
                                if (cvResidualWallet != null) cvResidualWallet.setVisibility(android.view.View.GONE);
                            }
                            
                            if (tvTotalCategoriesCount != null) {
                                tvTotalCategoriesCount.setText("Số lượng: " + adminWalletList.size() + " ví");
                            }
                            
                            if (adminWalletAdapter != null) adminWalletAdapter.notifyDataSetChanged();
                            
                        } else {
                            if (layoutEmptyBudget != null) layoutEmptyBudget.setVisibility(android.view.View.VISIBLE);
                            if (layoutBudgetData != null) layoutBudgetData.setVisibility(android.view.View.GONE);
                        }
                        
                        // Tasks binding
                        taskList.clear();
                        if (dto.tasks != null && !dto.tasks.isEmpty()) {
                            taskList.addAll(dto.tasks);
                        }
                        taskAdapter.notifyDataSetChanged();

                        loadAllocationAdjustments();
                        loadFundAdvances();
                        loadFundAdvanceDebts();
                    }
                } else {
                    if (response.code() == 400 || response.code() == 403) {
                        hasPreparation = false;
                        updatePreparationToggleUI();
                    } else {
                        Toast.makeText(AdminPreparationDashboardActivity.this, "Lỗi tải Dashboard", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PreparationDashboardDto>> call, Throwable t) {
                Toast.makeText(AdminPreparationDashboardActivity.this, "Lỗi mạng Dashboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePreparationToggleUI() {
        com.google.android.material.button.MaterialButton btnToggle = findViewById(R.id.btnTogglePreparation);
        android.view.View layoutNoPrep = findViewById(R.id.layoutNoPreparation);
        android.view.View scrollView = findViewById(R.id.scrollView);
        com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tabLayout);

        if (btnToggle != null) {
            btnToggle.setVisibility(android.view.View.VISIBLE);
            if (hasPreparation) {
                btnToggle.setText("Tắt");
                btnToggle.setTextColor(android.graphics.Color.parseColor("#4B5563"));
                btnToggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F3F4F6")));
                btnToggle.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E5E7EB")));
            } else {
                btnToggle.setText("Bật");
                btnToggle.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
                btnToggle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#10B981")));
                btnToggle.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#10B981")));
            }
        }

        if (hasPreparation) {
            if (layoutNoPrep != null) layoutNoPrep.setVisibility(android.view.View.GONE);
            if (scrollView != null) scrollView.setVisibility(android.view.View.VISIBLE);
            if (tabLayout != null) tabLayout.setVisibility(android.view.View.VISIBLE);
        } else {
            if (layoutNoPrep != null) layoutNoPrep.setVisibility(android.view.View.VISIBLE);
            if (scrollView != null) scrollView.setVisibility(android.view.View.GONE);
            if (tabLayout != null) tabLayout.setVisibility(android.view.View.GONE);
        }
    }

    private void togglePreparation(boolean enabled) {
        ApiClient.preparation(this).togglePreparation(activityId, enabled).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(AdminPreparationDashboardActivity.this, enabled ? "Đã bật Preparation" : "Đã tắt Preparation", Toast.LENGTH_SHORT).show();
                    hasPreparation = enabled;
                    updatePreparationToggleUI();
                    if (enabled) {
                        loadDashboardData();
                        loadFinanceOverview();
                        loadOrganizersData();
                        loadWorkloadWarningsData();
                        loadExpensesData();
                    }
                } else {
                    Toast.makeText(AdminPreparationDashboardActivity.this, "Lỗi khi chuyển đổi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Toast.makeText(AdminPreparationDashboardActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFinanceOverview() {
        ApiClient.preparation(this).getFinanceOverview(activityId).enqueue(new Callback<ApiResponse<com.example.campuslife.entity.preparation.FinanceOverviewReportDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<com.example.campuslife.entity.preparation.FinanceOverviewReportDto>> call, Response<ApiResponse<com.example.campuslife.entity.preparation.FinanceOverviewReportDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    com.example.campuslife.entity.preparation.FinanceOverviewReportDto dto = response.body().getData();
                    if (dto != null) {
                        try {
                            tvTotalAmount.setText(dto.totalBudget != null ? df.format(Double.parseDouble(dto.totalBudget)) + " VNĐ" : "0 VNĐ");
                        } catch (Exception e) { tvTotalAmount.setText(dto.totalBudget + " VNĐ"); }

                        try {
                            tvSpentAmount.setText(dto.totalApprovedSpent != null ? df.format(Double.parseDouble(dto.totalApprovedSpent)) + " VNĐ" : "0 VNĐ");
                        } catch (Exception e) { tvSpentAmount.setText(dto.totalApprovedSpent + " VNĐ"); }
                        
                        try {
                            double total = dto.totalBudget != null ? Double.parseDouble(dto.totalBudget) : 0;
                            double spent = dto.totalApprovedSpent != null ? Double.parseDouble(dto.totalApprovedSpent) : 0;
                            double remain = total - spent;
                            tvRemainingAmount.setText(df.format(remain) + " VNĐ");

                            if (total > 0) {
                                int pct = (int) Math.round((spent / total) * 100);
                                if (progressTotalBudget != null) progressTotalBudget.setProgress(Math.min(pct, 100));
                                if (tvTotalUsedPercent != null) tvTotalUsedPercent.setText(pct + "%");
                            } else {
                                if (progressTotalBudget != null) progressTotalBudget.setProgress(0);
                                if (tvTotalUsedPercent != null) tvTotalUsedPercent.setText("0%");
                            }
                            
                            if (tvBigTotalAmount != null) tvBigTotalAmount.setText(tvTotalAmount.getText());
                            if (tvBigSpent != null) tvBigSpent.setText("Tổng chi tiêu: " + tvSpentAmount.getText());
                            if (tvBigProgressPercent != null) tvBigProgressPercent.setText(tvTotalUsedPercent.getText());
                            if (progressBigTotal != null && progressTotalBudget != null) progressBigTotal.setProgress(progressTotalBudget.getProgress());
                            
                        } catch (Exception e) { 
                            tvRemainingAmount.setText("0 VNĐ");
                            if (progressTotalBudget != null) progressTotalBudget.setProgress(0);
                            if (tvTotalUsedPercent != null) tvTotalUsedPercent.setText("0%");
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<com.example.campuslife.entity.preparation.FinanceOverviewReportDto>> call, Throwable t) {}
        });
    }

    private void loadOrganizersData() {
        ApiClient.preparation(this).getOrganizers(activityId).enqueue(new Callback<ApiResponse<List<OrganizerDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<OrganizerDto>>> call, Response<ApiResponse<List<OrganizerDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<OrganizerDto> data = response.body().getData();
                    organizerList.clear();
                    if (data != null && !data.isEmpty()) {
                        organizerList.addAll(data);
                    }
                    organizerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<OrganizerDto>>> call, Throwable t) {}
        });
    }

    private void loadWorkloadWarningsData() {
        ApiClient.preparation(this).getWorkloadWarnings(activityId).enqueue(new Callback<ApiResponse<List<com.example.campuslife.entity.preparation.WorkloadWarningDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<com.example.campuslife.entity.preparation.WorkloadWarningDto>>> call, Response<ApiResponse<List<com.example.campuslife.entity.preparation.WorkloadWarningDto>>> response) {
                android.view.View card = findViewById(R.id.cardWorkloadWarnings);
                android.widget.LinearLayout container = findViewById(R.id.llWorkloadContainer);
                if (card == null || container == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<com.example.campuslife.entity.preparation.WorkloadWarningDto> data = response.body().getData();
                    if (data != null && !data.isEmpty()) {
                        card.setVisibility(android.view.View.VISIBLE);
                        container.removeAllViews();
                        
                        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(AdminPreparationDashboardActivity.this);
                        for (com.example.campuslife.entity.preparation.WorkloadWarningDto warning : data) {
                            android.view.View view = inflater.inflate(R.layout.item_workload_warning, container, false);
                            
                            TextView tvName = view.findViewById(R.id.tvWarningName);
                            TextView tvDesc = view.findViewById(R.id.tvWarningDesc);
                            TextView tvBadge = view.findViewById(R.id.tvWarningBadge);
                            
                            tvName.setText(warning.getStudentName() != null ? warning.getStudentName() : "Unknown");
                            tvDesc.setText((warning.getTaskCount() != null ? warning.getTaskCount() : 0) + " tasks currently assigned");
                            
                            String type = warning.getType() != null ? warning.getType() : "UNKNOWN";
                            tvBadge.setText(type);
                            
                            if ("OVERLOADED".equalsIgnoreCase(type)) {
                                tvBadge.setTextColor(android.graphics.Color.parseColor("#93000a"));
                                android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
                                bg.setColor(android.graphics.Color.parseColor("#ffdad6"));
                                bg.setCornerRadius(100f);
                                tvBadge.setBackground(bg);
                            } else if ("UNASSIGNED".equalsIgnoreCase(type)) {
                                tvBadge.setTextColor(android.graphics.Color.parseColor("#594237"));
                                android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
                                bg.setColor(android.graphics.Color.parseColor("#e2e2e2"));
                                bg.setCornerRadius(100f);
                                tvBadge.setBackground(bg);
                            }
                            
                            container.addView(view);
                        }
                        com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tabLayout);
                        if (tabLayout != null) {
                            updateTabSelection(tabLayout.getSelectedTabPosition());
                        } else {
                            card.setVisibility(android.view.View.VISIBLE);
                        }
                    } else {
                        card.setVisibility(android.view.View.GONE);
                    }
                } else {
                    card.setVisibility(android.view.View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<com.example.campuslife.entity.preparation.WorkloadWarningDto>>> call, Throwable t) {
                android.view.View card = findViewById(R.id.cardWorkloadWarnings);
                if (card != null) card.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void loadExpensesData() {
        ApiClient.preparation(this).listExpenses(activityId, null).enqueue(new Callback<ApiResponse<List<ExpenseDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ExpenseDto>>> call, Response<ApiResponse<List<ExpenseDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<ExpenseDto> data = response.body().getData();
                    pendingExpenseList.clear();
                    historyExpenseList.clear();
                    
                    if (data != null && !data.isEmpty()) {
                        for (ExpenseDto expense : data) {
                            if (expense.status != null && expense.status.contains("PENDING")) {
                                pendingExpenseList.add(expense);
                            } else {
                                historyExpenseList.add(expense);
                            }
                        }
                    }
                    
                    if (tvPendingExpensesCount != null) {
                        tvPendingExpensesCount.setText("Có " + pendingExpenseList.size() + " chi phí chờ duyệt");
                    }
                    if (pendingExpenseAdapter != null) pendingExpenseAdapter.notifyDataSetChanged();
                    if (historyExpenseAdapter != null) historyExpenseAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ExpenseDto>>> call, Throwable t) {}
        });
    }

    private void loadAllocationAdjustments() {
        ApiClient.preparation(this).listAllocationAdjustments(activityId).enqueue(new Callback<ApiResponse<List<com.example.campuslife.entity.preparation.AllocationAdjustmentRequestDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<com.example.campuslife.entity.preparation.AllocationAdjustmentRequestDto>>> call, Response<ApiResponse<List<com.example.campuslife.entity.preparation.AllocationAdjustmentRequestDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus() && response.body().getData() != null) {
                    if (allocationAdjustmentList != null) {
                        allocationAdjustmentList.clear();
                        allocationAdjustmentList.addAll(response.body().getData());
                        if (allocationAdjustmentAdapter != null) allocationAdjustmentAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<com.example.campuslife.entity.preparation.AllocationAdjustmentRequestDto>>> call, Throwable t) {}
        });
    }

    private void loadFundAdvanceDebts() {
        ApiClient.preparation(this).listFundAdvanceDebts(activityId, null).enqueue(new Callback<ApiResponse<List<com.example.campuslife.entity.preparation.FundAdvanceDebtDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<com.example.campuslife.entity.preparation.FundAdvanceDebtDto>>> call, Response<ApiResponse<List<com.example.campuslife.entity.preparation.FundAdvanceDebtDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus() && response.body().getData() != null) {
                    if (fundAdvanceDebtList != null) {
                        fundAdvanceDebtList.clear();
                        fundAdvanceDebtList.addAll(response.body().getData());
                        
                        if (fundAdvanceDebtAdapter != null) fundAdvanceDebtAdapter.notifyDataSetChanged();

                        if (fundAdvanceAdapter != null) {
                            java.util.Map<Long, Double> debtMap = new java.util.HashMap<>();
                            for (com.example.campuslife.entity.preparation.FundAdvanceDebtDto debt : fundAdvanceDebtList) {
                                if (debt.studentId != null && debt.holdingAmount != null) {
                                    try {
                                        debtMap.put(debt.studentId, Double.parseDouble(debt.holdingAmount));
                                    } catch (Exception e) {}
                                }
                            }
                            fundAdvanceAdapter.setDebtMap(debtMap);
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<com.example.campuslife.entity.preparation.FundAdvanceDebtDto>>> call, Throwable t) {}
        });
    }

    private void loadFundAdvances() {
        if (taskList == null || taskList.isEmpty()) return;
        
        List<com.example.campuslife.entity.preparation.FundAdvanceDto> mergedAdvances = new ArrayList<>();
        java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger();
        
        // Find financial tasks
        List<PreparationTaskDto> financialTasks = new ArrayList<>();
        for (PreparationTaskDto t : taskList) {
            if (Boolean.TRUE.equals(t.isFinancial)) {
                financialTasks.add(t);
            }
        }
        
        if (financialTasks.isEmpty()) {
            if (fundAdvanceList != null) {
                fundAdvanceList.clear();
                if (fundAdvanceAdapter != null) fundAdvanceAdapter.notifyDataSetChanged();
            }
            return;
        }

        for (PreparationTaskDto t : financialTasks) {
            ApiClient.preparation(this).listFundAdvances(t.id).enqueue(new Callback<ApiResponse<List<com.example.campuslife.entity.preparation.FundAdvanceDto>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<com.example.campuslife.entity.preparation.FundAdvanceDto>>> call, Response<ApiResponse<List<com.example.campuslife.entity.preparation.FundAdvanceDto>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus() && response.body().getData() != null) {
                        synchronized (mergedAdvances) {
                            mergedAdvances.addAll(response.body().getData());
                        }
                    }
                    if (counter.incrementAndGet() == financialTasks.size()) {
                        runOnUiThread(() -> {
                            if (fundAdvanceList != null) {
                                fundAdvanceList.clear();
                                fundAdvanceList.addAll(mergedAdvances);
                                if (fundAdvanceAdapter != null) fundAdvanceAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<com.example.campuslife.entity.preparation.FundAdvanceDto>>> call, Throwable t) {
                    if (counter.incrementAndGet() == financialTasks.size()) {
                        runOnUiThread(() -> {
                            if (fundAdvanceList != null) {
                                fundAdvanceList.clear();
                                fundAdvanceList.addAll(mergedAdvances);
                                if (fundAdvanceAdapter != null) fundAdvanceAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            });
        }
    }

    private void setupFabListener() {
        // android.view.View btnUpdateBudget = findViewById(R.id.btnUpdateBudget);
        // if (btnUpdateBudget != null) {
        //     btnUpdateBudget.setOnClickListener(v -> showUpdateBudgetDialog());
        // }

        android.view.View btnAddOrganizer = findViewById(R.id.btnAddOrganizer);
        if (btnAddOrganizer != null) {
            btnAddOrganizer.setOnClickListener(v -> showAddOrganizerDialog());
        }

        android.view.View btnAssignTask = findViewById(R.id.btnAssignTask);
        if (btnAssignTask != null) {
            btnAssignTask.setOnClickListener(v -> showAssignTaskDialog());
        }
        
        com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tabLayout);
        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    updateTabSelection(tab.getPosition());
                }

                @Override
                public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            });
            // Init with overview
            updateTabSelection(0);
        }
        
        // Handle View All clicks
        findViewById(R.id.tvViewAllBudget).setOnClickListener(v -> {
            if (tabLayout != null) tabLayout.selectTab(tabLayout.getTabAt(1));
        });
        
        findViewById(R.id.tvViewAllOrganizers).setOnClickListener(v -> {
            if (tabLayout != null) tabLayout.selectTab(tabLayout.getTabAt(2));
        });
        
        findViewById(R.id.tvViewAllTasks).setOnClickListener(v -> {
            if (tabLayout != null) tabLayout.selectTab(tabLayout.getTabAt(3));
        });
        
        findViewById(R.id.tvViewAllExpenses).setOnClickListener(v -> {
            if (tabLayout != null) tabLayout.selectTab(tabLayout.getTabAt(4));
        });
    }

    private void updateTabSelection(int position) {
        boolean showAll = position == 0;
        
        // Hide "View All" buttons and custom buttons appropriately
        findViewById(R.id.tvViewAllBudget).setVisibility(showAll ? android.view.View.VISIBLE : android.view.View.GONE);
        // android.view.View btnUpdateBudget = findViewById(R.id.btnUpdateBudget);
        // if (btnUpdateBudget != null) {
        //     btnUpdateBudget.setVisibility(position == 1 ? android.view.View.VISIBLE : android.view.View.GONE);
        // }

        findViewById(R.id.tvViewAllOrganizers).setVisibility(showAll ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.btnAddOrganizer).setVisibility(position == 2 ? android.view.View.VISIBLE : android.view.View.GONE);

        findViewById(R.id.tvViewAllTasks).setVisibility(showAll ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.btnAssignTask).setVisibility(position == 3 ? android.view.View.VISIBLE : android.view.View.GONE);

        findViewById(R.id.tvViewAllExpenses).setVisibility(showAll ? android.view.View.VISIBLE : android.view.View.GONE);
        
        // Overview = 0, Budget = 1, Organizers = 2, Tasks = 3, Expenses = 4
        findViewById(R.id.cvBudget).setVisibility(showAll ? android.view.View.VISIBLE : android.view.View.GONE);
        if (layoutBudgetFull != null) layoutBudgetFull.setVisibility(position == 1 ? android.view.View.VISIBLE : android.view.View.GONE);
        
        findViewById(R.id.cardWorkloadWarnings).setVisibility((showAll || position == 2) && ((android.widget.LinearLayout)findViewById(R.id.llWorkloadContainer)).getChildCount() > 0 ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.layoutOrganizersHeader).setVisibility(showAll || position == 2 ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.rvOrganizers).setVisibility(showAll || position == 2 ? android.view.View.VISIBLE : android.view.View.GONE);
        
        findViewById(R.id.layoutTasksHeader).setVisibility(showAll || position == 3 ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.rvTasks).setVisibility(showAll || position == 3 ? android.view.View.VISIBLE : android.view.View.GONE);
        
        android.view.View layoutAllocation = findViewById(R.id.layoutAllocationRoot);
        if (layoutAllocation != null) layoutAllocation.setVisibility(position == 3 ? android.view.View.VISIBLE : android.view.View.GONE);
        
        android.view.View layoutFundAdvance = findViewById(R.id.layoutFundAdvanceRoot);
        if (layoutFundAdvance != null) layoutFundAdvance.setVisibility(position == 3 ? android.view.View.VISIBLE : android.view.View.GONE);
        
        android.view.View layoutDebt = findViewById(R.id.layoutFundAdvanceDebtRoot);
        if (layoutDebt != null) layoutDebt.setVisibility(position == 3 ? android.view.View.VISIBLE : android.view.View.GONE);
        
        findViewById(R.id.layoutExpensesHeader).setVisibility(showAll || position == 4 ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.layoutExpensesRoot).setVisibility(showAll || position == 4 ? android.view.View.VISIBLE : android.view.View.GONE);
        
        android.view.View historyHeader = findViewById(R.id.layoutHistoryExpensesHeader);
        if (historyHeader != null) historyHeader.setVisibility(position == 4 ? android.view.View.VISIBLE : android.view.View.GONE);
        
        android.view.View rvHistory = findViewById(R.id.rvHistoryExpenses);
        if (rvHistory != null) rvHistory.setVisibility(position == 4 ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void showUpdateBudgetDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.layout_dialog_update_budget, null);
        dialog.setContentView(view);

        com.google.android.material.textfield.TextInputEditText etTotalAmount = view.findViewById(R.id.etTotalAmount);
        com.google.android.material.textfield.TextInputEditText etBudgetDescription = view.findViewById(R.id.etBudgetDescription);

        view.findViewById(R.id.btnCancelBudget).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnSaveBudget).setOnClickListener(v -> {
            String amountStr = etTotalAmount.getText().toString();
            String desc = etBudgetDescription.getText().toString();
            if (!amountStr.isEmpty()) {
                ApiClient.preparation(this).upsertBudget(activityId, 
                        new com.example.campuslife.entity.preparation.UpsertActivityBudgetRequest(amountStr, new java.util.ArrayList<>()))
                        .enqueue(new Callback<ApiResponse<com.example.campuslife.entity.preparation.ActivityBudgetDto>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<com.example.campuslife.entity.preparation.ActivityBudgetDto>> call, Response<ApiResponse<com.example.campuslife.entity.preparation.ActivityBudgetDto>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                                    Toast.makeText(AdminPreparationDashboardActivity.this, "Đã cập nhật ngân sách", Toast.LENGTH_SHORT).show();
                                    loadFinanceOverview();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(AdminPreparationDashboardActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<ApiResponse<com.example.campuslife.entity.preparation.ActivityBudgetDto>> call, Throwable t) {}
                        });
            } else {
                Toast.makeText(this, "Amount is required", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;

    private void showAddOrganizerDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.layout_dialog_add_organizer);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        
        android.widget.EditText etStudentId = dialog.findViewById(R.id.etStudentId);
        android.widget.ProgressBar pbSearchLoading = dialog.findViewById(R.id.pbSearchLoading);
        android.widget.TextView tvStatusMessage = dialog.findViewById(R.id.tvStatusMessage);
        android.widget.ScrollView svSearchResults = dialog.findViewById(R.id.svSearchResults);
        android.widget.LinearLayout llSearchResults = dialog.findViewById(R.id.llSearchResults);
        android.widget.TextView tvNoSearchRes = dialog.findViewById(R.id.tvNoSearchRes);
        android.view.View btnCancel = dialog.findViewById(R.id.btnCancelOrganizer);
        android.view.View btnAdd = dialog.findViewById(R.id.btnAddOrganizer);

        etStudentId.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                String fullText = s.toString();
                int lastCommaIndex = fullText.lastIndexOf(',');
                String keyword = lastCommaIndex == -1 ? fullText.trim() : fullText.substring(lastCommaIndex + 1).trim();

                if (keyword.length() < 2) {
                    if (svSearchResults != null) svSearchResults.setVisibility(android.view.View.GONE);
                    if (pbSearchLoading != null) pbSearchLoading.setVisibility(android.view.View.GONE);
                    if (tvNoSearchRes != null) tvNoSearchRes.setVisibility(android.view.View.GONE);
                    return;
                }

                if (pbSearchLoading != null) pbSearchLoading.setVisibility(android.view.View.VISIBLE);
                if (svSearchResults != null) svSearchResults.setVisibility(android.view.View.GONE);
                if (tvNoSearchRes != null) tvNoSearchRes.setVisibility(android.view.View.GONE);

                searchRunnable = () -> {
                    ApiClient.student(AdminPreparationDashboardActivity.this)
                            .searchStudents(keyword, 0, 10).enqueue(new Callback<ApiResponse<com.example.campuslife.api.StudentApi.StudentSearchResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<com.example.campuslife.api.StudentApi.StudentSearchResponse>> call, Response<ApiResponse<com.example.campuslife.api.StudentApi.StudentSearchResponse>> response) {
                            if (pbSearchLoading != null) pbSearchLoading.setVisibility(android.view.View.GONE);
                            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                                java.util.List<com.example.campuslife.entity.Student> currentSearchResult = new java.util.ArrayList<>();
                                if (response.body().getData() != null && response.body().getData().content != null) {
                                    currentSearchResult.addAll(response.body().getData().content);
                                }
                                
                                if (llSearchResults != null) llSearchResults.removeAllViews();
                                if (currentSearchResult.isEmpty()) {
                                    if (tvNoSearchRes != null) tvNoSearchRes.setVisibility(android.view.View.VISIBLE);
                                    if (svSearchResults != null) svSearchResults.setVisibility(android.view.View.GONE);
                                } else {
                                    if (tvNoSearchRes != null) tvNoSearchRes.setVisibility(android.view.View.GONE);
                                    if (svSearchResults != null) svSearchResults.setVisibility(android.view.View.VISIBLE);
                                    for (com.example.campuslife.entity.Student student : currentSearchResult) {
                                        android.widget.LinearLayout itemLayout = new android.widget.LinearLayout(AdminPreparationDashboardActivity.this);
                                        itemLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
                                        itemLayout.setPadding(40, 32, 40, 32);

                                        android.widget.TextView tvName = new android.widget.TextView(AdminPreparationDashboardActivity.this);
                                        tvName.setText(student.getFullName());
                                        tvName.setTextSize(14f);
                                        tvName.setTextColor(android.graphics.Color.parseColor("#111827"));
                                        tvName.setTypeface(null, android.graphics.Typeface.BOLD);

                                        android.widget.TextView tvDesc = new android.widget.TextView(AdminPreparationDashboardActivity.this);
                                        String email = (student.getEmail() != null && !student.getEmail().isEmpty()) ? student.getEmail() : "Chưa cập nhật email";
                                        tvDesc.setText(student.getStudentCode() + " • " + email);
                                        tvDesc.setTextSize(12f);
                                        tvDesc.setTextColor(android.graphics.Color.parseColor("#6B7280"));
                                        tvDesc.setPadding(0, 4, 0, 0);

                                        itemLayout.addView(tvName);
                                        itemLayout.addView(tvDesc);

                                        android.util.TypedValue outValue = new android.util.TypedValue();
                                        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                                        itemLayout.setBackgroundResource(outValue.resourceId);

                                        itemLayout.setClickable(true);
                                        itemLayout.setOnClickListener((android.view.View v) -> {
                                            String currentFullText = etStudentId.getText().toString();
                                            int lastComma = currentFullText.lastIndexOf(',');
                                            String prefix = lastComma == -1 ? "" : currentFullText.substring(0, lastComma + 1) + " ";
                                            etStudentId.setText(prefix + student.getStudentCode() + ", ");
                                            etStudentId.setSelection(etStudentId.getText().length());
                                            if (svSearchResults != null) svSearchResults.setVisibility(android.view.View.GONE);
                                        });
                                        if (llSearchResults != null) llSearchResults.addView(itemLayout);
                                        
                                        android.view.View divider = new android.view.View(AdminPreparationDashboardActivity.this);
                                        divider.setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1));
                                        divider.setBackgroundColor(android.graphics.Color.parseColor("#E5E7EB"));
                                        if (llSearchResults != null) llSearchResults.addView(divider);
                                    }
                                }
                            } else {
                                if (tvNoSearchRes != null) tvNoSearchRes.setVisibility(android.view.View.VISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<com.example.campuslife.api.StudentApi.StudentSearchResponse>> call, Throwable t) {
                            if (pbSearchLoading != null) pbSearchLoading.setVisibility(android.view.View.GONE);
                            if (tvNoSearchRes != null) tvNoSearchRes.setVisibility(android.view.View.VISIBLE);
                        }
                    });
                };
                searchHandler.postDelayed(searchRunnable, 400);
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(v -> {
            String input = etStudentId.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập MSSV", Toast.LENGTH_SHORT).show();
                return;
            }

            // Split by comma or space
            String[] rawIds = input.split("[,\\s]+");
            java.util.List<String> validIds = new java.util.ArrayList<>();
            for (String split : rawIds) {
                if (!split.trim().isEmpty()) {
                    validIds.add(split.trim());
                }
            }

            if (validIds.isEmpty()) {
                Toast.makeText(this, "Danh sách MSSV không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            pbSearchLoading.setVisibility(android.view.View.VISIBLE);
            tvStatusMessage.setVisibility(android.view.View.VISIBLE);
            tvStatusMessage.setText("Đang xử lý " + validIds.size() + " sinh viên...");
            etStudentId.setEnabled(false);
            btnAdd.setEnabled(false);
            btnCancel.setEnabled(false);

            processMultipleOrganizers(validIds, 0, new java.util.ArrayList<>(), new java.util.ArrayList<>(), dialog);
        });

        dialog.show();
    }

    private void processMultipleOrganizers(java.util.List<String> studentIds, int index, 
                                          java.util.List<String> successList, java.util.List<String> failList, 
                                          android.app.Dialog dialog) {
        if (index >= studentIds.size()) {
            // Processing done
            android.widget.ProgressBar pbSearchLoading = dialog.findViewById(R.id.pbSearchLoading);
            android.widget.TextView tvStatusMessage = dialog.findViewById(R.id.tvStatusMessage);
            android.widget.EditText etStudentId = dialog.findViewById(R.id.etStudentId);
            android.view.View btnAdd = dialog.findViewById(R.id.btnAddOrganizer);
            android.view.View btnCancel = dialog.findViewById(R.id.btnCancelOrganizer);

            if (pbSearchLoading != null) pbSearchLoading.setVisibility(android.view.View.GONE);
            if (tvStatusMessage != null) {
                String summary = "Hoàn thành! Thành công: " + successList.size() + ", Lỗi: " + failList.size();
                tvStatusMessage.setText(summary);
                if (!failList.isEmpty()) {
                    tvStatusMessage.append("\nChi tiết lỗi: " + String.join(", ", failList));
                    if (etStudentId != null) {
                        etStudentId.setEnabled(true);
                        // Reset field to only contain failed ones so user can retry
                        etStudentId.setText(String.join(", ", failList).replaceAll(" \\(.*?\\)", ""));
                    }
                    if (btnAdd != null) btnAdd.setEnabled(true);
                    if (btnCancel != null) btnCancel.setEnabled(true);
                    
                    if (!successList.isEmpty()) loadOrganizersData();
                } else {
                    Toast.makeText(this, "Đã thêm thành công tất cả", Toast.LENGTH_SHORT).show();
                    loadOrganizersData();
                    dialog.dismiss();
                }
            }
            return;
        }

        String currentMssv = studentIds.get(index);
        android.widget.TextView tvStatusMessage = dialog.findViewById(R.id.tvStatusMessage);
        if (tvStatusMessage != null) {
            tvStatusMessage.setText("Đang tìm " + currentMssv + " (" + (index+1) + "/" + studentIds.size() + ")");
        }

        // Step 1: Search by MSSV
        ApiClient.student(this).searchStudents(currentMssv, 0, 10).enqueue(new Callback<ApiResponse<com.example.campuslife.api.StudentApi.StudentSearchResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<com.example.campuslife.api.StudentApi.StudentSearchResponse>> call, Response<ApiResponse<com.example.campuslife.api.StudentApi.StudentSearchResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus() 
                        && response.body().getData() != null && response.body().getData().content != null) {
                    
                    java.util.List<com.example.campuslife.entity.Student> results = response.body().getData().content;
                    com.example.campuslife.entity.Student targetStudent = null;
                    
                    // Strictly match studentCode ignoring case
                    for (com.example.campuslife.entity.Student s : results) {
                        if (s.getStudentCode() != null && s.getStudentCode().equalsIgnoreCase(currentMssv)) {
                            targetStudent = s;
                            break;
                        }
                    }
                    
                    if (targetStudent == null && !results.isEmpty()) {
                        // Fallback to first if strict match fails but results exist
                        targetStudent = results.get(0);
                    }

                    if (targetStudent != null) {
                        if (tvStatusMessage != null) tvStatusMessage.setText("Đang thêm " + targetStudent.getStudentCode() + "...");
                        // Step 2: Add Organizer
                        ApiClient.preparation(AdminPreparationDashboardActivity.this).addOrganizer(activityId, targetStudent.getId()).enqueue(new Callback<ApiResponse<Object>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> res) {
                                if (res.isSuccessful() && res.body() != null && res.body().isStatus()) {
                                    successList.add(currentMssv);
                                } else {
                                    String err = res.body() != null && res.body().getMessage() != null ? res.body().getMessage() : "Lỗi thêm";
                                    failList.add(currentMssv + " (" + err + ")");
                                }
                                processMultipleOrganizers(studentIds, index + 1, successList, failList, dialog);
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                                failList.add(currentMssv + " (Mất kết nối)");
                                processMultipleOrganizers(studentIds, index + 1, successList, failList, dialog);
                            }
                        });
                    } else {
                        // Not found
                        failList.add(currentMssv + " (Không tìm thấy)");
                        processMultipleOrganizers(studentIds, index + 1, successList, failList, dialog);
                    }
                } else {
                    failList.add(currentMssv + " (Lỗi Server)");
                    processMultipleOrganizers(studentIds, index + 1, successList, failList, dialog);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<com.example.campuslife.api.StudentApi.StudentSearchResponse>> call, Throwable t) {
                failList.add(currentMssv + " (Lỗi mạng)");
                processMultipleOrganizers(studentIds, index + 1, successList, failList, dialog);
            }
        });
    }

    private void showAssignTaskDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.layout_dialog_assign_task, null);
        dialog.setContentView(view);

        android.widget.EditText etTaskTitle = view.findViewById(R.id.etTaskTitle);
        android.widget.EditText etTaskAssigneeId = view.findViewById(R.id.etTaskAssigneeId);
        android.widget.EditText etTaskDescription = view.findViewById(R.id.etTaskDescription);
        android.widget.EditText etTaskDeadline = view.findViewById(R.id.etTaskDeadline);
        android.widget.CheckBox cbIsFinancial = view.findViewById(R.id.cbIsFinancial);

        etTaskAssigneeId.setFocusable(false);
        etTaskAssigneeId.setClickable(true);
        etTaskAssigneeId.setLongClickable(false);
        etTaskAssigneeId.setOnClickListener(v -> {
            if (organizerList == null || organizerList.isEmpty()) {
                Toast.makeText(this, "Chưa có ban tổ chức. Vui lòng thêm ban tổ chức trước.", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] orgNames = new String[organizerList.size()];
            for(int i=0; i<organizerList.size(); i++) {
                orgNames[i] = organizerList.get(i).getFullName();
            }
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Chọn người thực hiện")
                .setItems(orgNames, (d, which) -> {
                    selectedAssigneeId = organizerList.get(which).getStudentId();
                    etTaskAssigneeId.setText(organizerList.get(which).getFullName());
                })
                .show();
        });

        etTaskDeadline.setFocusable(false);
        etTaskDeadline.setClickable(true);
        etTaskDeadline.setLongClickable(false);
        etTaskDeadline.setOnClickListener(v -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int year = calendar.get(java.util.Calendar.YEAR);
            int month = calendar.get(java.util.Calendar.MONTH);
            int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

            new android.app.DatePickerDialog(this, (view1, selectedYear, selectedMonth, selectedDay) -> {
                String dateString = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                etTaskDeadline.setText(dateString);
            }, year, month, day).show();
        });

        view.findViewById(R.id.btnCancelTask).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnAssignTask).setOnClickListener(v -> {
            if (etTaskTitle.getText().toString().isEmpty()) return;
            
            if (selectedAssigneeId == null) {
                Toast.makeText(this, "Vui lòng chọn người thực hiện", Toast.LENGTH_SHORT).show();
                return;
            }

            String rawDate = etTaskDeadline.getText().toString();
            String formattedDeadline = null;
            if (!rawDate.isEmpty() && rawDate.length() == 10) {
                formattedDeadline = rawDate + "T23:59:59";
            }

            ApiClient.preparation(this).assignTask(activityId, 
                    new com.example.campuslife.entity.preparation.CreatePreparationTaskRequest(
                            activityId,
                            selectedAssigneeId, 
                            etTaskTitle.getText().toString(), 
                            etTaskDescription.getText().toString(), 
                            formattedDeadline,
                            cbIsFinancial.isChecked()))
                    .enqueue(new Callback<ApiResponse<PreparationTaskDto>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<PreparationTaskDto>> call, Response<ApiResponse<PreparationTaskDto>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                                Toast.makeText(AdminPreparationDashboardActivity.this, "Task Assigned", Toast.LENGTH_SHORT).show();
                                loadDashboardData();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(AdminPreparationDashboardActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<ApiResponse<PreparationTaskDto>> call, Throwable t) {}
                    });
        });

        dialog.show();
    }
}
