package com.example.campuslife.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    
    // RecyclerViews
    private RecyclerView rvOrganizers, rvTasks, rvExpenses;
    
    // Adapters & Data
    private PreparationOrganizerAdapter organizerAdapter;
    private List<OrganizerDto> organizerList;
    
    private PreparationTaskAdapter taskAdapter;
    private List<PreparationTaskDto> taskList;
    
    private PreparationExpenseAdapter expenseAdapter;
    private List<ExpenseDto> expenseList;
    
    private final DecimalFormat df = new DecimalFormat("#,###");
    private Long selectedAssigneeId = null;

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
        loadOrganizersData();
        loadExpensesData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvSpentAmount = findViewById(R.id.tvSpentAmount);
        tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
        
        rvOrganizers = findViewById(R.id.rvOrganizers);
        rvTasks = findViewById(R.id.rvTasks);
        rvExpenses = findViewById(R.id.rvExpenses);
    }

    private void setupRecyclerViews() {
        organizerList = new ArrayList<>();
        organizerAdapter = new PreparationOrganizerAdapter(this, organizerList, activityId, this::loadOrganizersData);
        rvOrganizers.setLayoutManager(new LinearLayoutManager(this));
        rvOrganizers.setAdapter(organizerAdapter);
        
        taskList = new ArrayList<>();
        taskAdapter = new PreparationTaskAdapter(this, taskList, -1L, (task, newStatus) -> {
            ApiClient.preparation(this).updateTaskStatus(task.id, new com.example.campuslife.entity.preparation.UpdateTaskStatusRequest(newStatus))
                    .enqueue(new Callback<ApiResponse<PreparationTaskDto>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<PreparationTaskDto>> call, Response<ApiResponse<PreparationTaskDto>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                                Toast.makeText(AdminPreparationDashboardActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                loadDashboardData();
                            } else {
                                Toast.makeText(AdminPreparationDashboardActivity.this, "Cập nhật lỗi", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<ApiResponse<PreparationTaskDto>> call, Throwable t) {}
                    });
        });
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(taskAdapter);
        
        expenseList = new ArrayList<>();
        expenseAdapter = new PreparationExpenseAdapter(this, expenseList);
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvExpenses.setAdapter(expenseAdapter);
    }

    private void loadDashboardData() {
        ApiClient.preparation(this).getDashboard(activityId).enqueue(new Callback<ApiResponse<PreparationDashboardDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<PreparationDashboardDto>> call, Response<ApiResponse<PreparationDashboardDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    PreparationDashboardDto dto = response.body().getData();
                    
                    if (dto != null) {
                        // Budget binding
                        if (dto.budget != null) {
                            try {
                                tvTotalAmount.setText(dto.budget.totalAmount != null ? df.format(Double.parseDouble(dto.budget.totalAmount)) + " VNĐ" : "0 VNĐ");
                            } catch (Exception e) { tvTotalAmount.setText(dto.budget.totalAmount + " VNĐ"); }

                            try {
                                tvSpentAmount.setText(dto.budget.spentAmount != null ? df.format(Double.parseDouble(dto.budget.spentAmount)) + " VNĐ" : "0 VNĐ");
                            } catch (Exception e) { tvSpentAmount.setText(dto.budget.spentAmount + " VNĐ"); }

                            try {
                                tvRemainingAmount.setText(dto.budget.remainingAmount != null ? df.format(Double.parseDouble(dto.budget.remainingAmount)) + " VNĐ" : "0 VNĐ");
                            } catch (Exception e) { tvRemainingAmount.setText(dto.budget.remainingAmount + " VNĐ"); }
                        }
                    
                        // Tasks binding
                        taskList.clear();
                        if (dto.tasks != null && !dto.tasks.isEmpty()) {
                            taskList.addAll(dto.tasks);
                        }
                        taskAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(AdminPreparationDashboardActivity.this, "Lỗi tải Dashboard", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PreparationDashboardDto>> call, Throwable t) {
                Toast.makeText(AdminPreparationDashboardActivity.this, "Lỗi mạng Dashboard", Toast.LENGTH_SHORT).show();
            }
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

    private void loadExpensesData() {
        ApiClient.preparation(this).listExpenses(activityId, "ALL").enqueue(new Callback<ApiResponse<List<ExpenseDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ExpenseDto>>> call, Response<ApiResponse<List<ExpenseDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<ExpenseDto> data = response.body().getData();
                    expenseList.clear();
                    if (data != null && !data.isEmpty()) {
                        expenseList.addAll(data);
                    }
                    expenseAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ExpenseDto>>> call, Throwable t) {}
        });
    }

    private void setupFabListener() {
        android.view.View btnUpdateBudget = findViewById(R.id.btnUpdateBudget);
        if (btnUpdateBudget != null) {
            btnUpdateBudget.setOnClickListener(v -> showUpdateBudgetDialog());
        }

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
        findViewById(R.id.btnUpdateBudget).setVisibility(position == 1 ? android.view.View.VISIBLE : android.view.View.GONE);

        findViewById(R.id.tvViewAllOrganizers).setVisibility(showAll ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.btnAddOrganizer).setVisibility(position == 2 ? android.view.View.VISIBLE : android.view.View.GONE);

        findViewById(R.id.tvViewAllTasks).setVisibility(showAll ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.btnAssignTask).setVisibility(position == 3 ? android.view.View.VISIBLE : android.view.View.GONE);

        findViewById(R.id.tvViewAllExpenses).setVisibility(showAll ? android.view.View.VISIBLE : android.view.View.GONE);
        
        // Overview = 0, Budget = 1, Organizers = 2, Tasks = 3, Expenses = 4
        findViewById(R.id.cvBudget).setVisibility(showAll || position == 1 ? android.view.View.VISIBLE : android.view.View.GONE);
        
        findViewById(R.id.layoutOrganizersHeader).setVisibility(showAll || position == 2 ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.rvOrganizers).setVisibility(showAll || position == 2 ? android.view.View.VISIBLE : android.view.View.GONE);
        
        findViewById(R.id.layoutTasksHeader).setVisibility(showAll || position == 3 ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.rvTasks).setVisibility(showAll || position == 3 ? android.view.View.VISIBLE : android.view.View.GONE);
        
        findViewById(R.id.layoutExpensesHeader).setVisibility(showAll || position == 4 ? android.view.View.VISIBLE : android.view.View.GONE);
        findViewById(R.id.rvExpenses).setVisibility(showAll || position == 4 ? android.view.View.VISIBLE : android.view.View.GONE);
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
                        new com.example.campuslife.entity.preparation.UpsertBudgetRequest(Double.parseDouble(amountStr), desc))
                        .enqueue(new Callback<ApiResponse<com.example.campuslife.entity.preparation.BudgetDto>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<com.example.campuslife.entity.preparation.BudgetDto>> call, Response<ApiResponse<com.example.campuslife.entity.preparation.BudgetDto>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                                    Toast.makeText(AdminPreparationDashboardActivity.this, "Budget Updated", Toast.LENGTH_SHORT).show();
                                    loadDashboardData();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(AdminPreparationDashboardActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<ApiResponse<com.example.campuslife.entity.preparation.BudgetDto>> call, Throwable t) {}
                        });
            } else {
                Toast.makeText(this, "Amount is required", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private Long selectedStudentIdForOrganizer = null;
    private android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;

    private void showAddOrganizerDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.layout_dialog_add_organizer);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        android.widget.EditText etStudentId = dialog.findViewById(R.id.etStudentId);
        android.widget.LinearLayout llSearchResults = dialog.findViewById(R.id.llSearchResults);
        android.widget.ScrollView svSearchResults = dialog.findViewById(R.id.svSearchResults);
        android.widget.ProgressBar pbSearchLoading = dialog.findViewById(R.id.pbSearchLoading);
        android.widget.TextView tvNoSearchRes = dialog.findViewById(R.id.tvNoSearchRes);
        selectedStudentIdForOrganizer = null;

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
                
                String keyword = s.toString().trim();
                if (keyword.length() < 2) {
                    svSearchResults.setVisibility(android.view.View.GONE);
                    pbSearchLoading.setVisibility(android.view.View.GONE);
                    tvNoSearchRes.setVisibility(android.view.View.GONE);
                    selectedStudentIdForOrganizer = null;
                    return;
                }
                
                if (selectedStudentIdForOrganizer != null && keyword.contains("-")) {
                    return;
                }
                selectedStudentIdForOrganizer = null;

                pbSearchLoading.setVisibility(android.view.View.VISIBLE);
                svSearchResults.setVisibility(android.view.View.GONE);
                tvNoSearchRes.setVisibility(android.view.View.GONE);

                searchRunnable = () -> {
                    ApiClient.student(AdminPreparationDashboardActivity.this)
                            .searchStudents(keyword, 0, 10).enqueue(new Callback<ApiResponse<com.example.campuslife.api.StudentApi.StudentSearchResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<com.example.campuslife.api.StudentApi.StudentSearchResponse>> call, Response<ApiResponse<com.example.campuslife.api.StudentApi.StudentSearchResponse>> response) {
                            pbSearchLoading.setVisibility(android.view.View.GONE);
                            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                                java.util.List<com.example.campuslife.entity.Student> currentSearchResult = new java.util.ArrayList<>();
                                if (response.body().getData() != null && response.body().getData().content != null) {
                                    currentSearchResult.addAll(response.body().getData().content);
                                }
                                
                                llSearchResults.removeAllViews();
                                if (currentSearchResult.isEmpty()) {
                                    tvNoSearchRes.setVisibility(android.view.View.VISIBLE);
                                    svSearchResults.setVisibility(android.view.View.GONE);
                                } else {
                                    tvNoSearchRes.setVisibility(android.view.View.GONE);
                                    svSearchResults.setVisibility(android.view.View.VISIBLE);
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
                                        itemLayout.setOnClickListener(v -> {
                                            selectedStudentIdForOrganizer = student.getId();
                                            etStudentId.setText(student.getStudentCode() + " - " + student.getFullName());
                                            etStudentId.setSelection(etStudentId.getText().length());
                                            svSearchResults.setVisibility(android.view.View.GONE);
                                            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                                            if (imm != null) {
                                                imm.hideSoftInputFromWindow(etStudentId.getWindowToken(), 0);
                                            }
                                        });
                                        llSearchResults.addView(itemLayout);
                                        
                                        android.view.View divider = new android.view.View(AdminPreparationDashboardActivity.this);
                                        divider.setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1));
                                        divider.setBackgroundColor(android.graphics.Color.parseColor("#E5E7EB"));
                                        llSearchResults.addView(divider);
                                    }
                                }
                            } else {
                                tvNoSearchRes.setVisibility(android.view.View.VISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<com.example.campuslife.api.StudentApi.StudentSearchResponse>> call, Throwable t) {
                            pbSearchLoading.setVisibility(android.view.View.GONE);
                            tvNoSearchRes.setVisibility(android.view.View.VISIBLE);
                        }
                    });
                };
                searchHandler.postDelayed(searchRunnable, 500); // 500ms debounce
            }
        });

        dialog.findViewById(R.id.btnCancelOrganizer).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnAddOrganizer).setOnClickListener(v -> {
            if (selectedStudentIdForOrganizer != null) {
                ApiClient.preparation(this).addOrganizer(activityId, selectedStudentIdForOrganizer)
                        .enqueue(new Callback<ApiResponse<Object>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                                    Toast.makeText(AdminPreparationDashboardActivity.this, "Organizer Added", Toast.LENGTH_SHORT).show();
                                    loadOrganizersData();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(AdminPreparationDashboardActivity.this, "Error or Invalid User", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
                        });
            } else {
                Toast.makeText(this, "Vui lòng chọn sinh viên từ danh sách gợi ý", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showAssignTaskDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.layout_dialog_assign_task, null);
        dialog.setContentView(view);

        android.widget.EditText etTaskTitle = view.findViewById(R.id.etTaskTitle);
        android.widget.EditText etTaskAssigneeId = view.findViewById(R.id.etTaskAssigneeId);
        android.widget.EditText etTaskDescription = view.findViewById(R.id.etTaskDescription);
        android.widget.EditText etTaskDeadline = view.findViewById(R.id.etTaskDeadline);

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
                            formattedDeadline))
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
