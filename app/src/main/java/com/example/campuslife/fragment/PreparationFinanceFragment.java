package com.example.campuslife.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campuslife.BuildConfig;
import com.example.campuslife.R;
import com.example.campuslife.adapter.ExpenseAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.ActivityBudgetDto;
import com.example.campuslife.entity.preparation.ApproveExpenseRequest;
import com.example.campuslife.entity.preparation.BudgetCategoryDto;
import com.example.campuslife.entity.preparation.CreateExpenseRequest;
import com.example.campuslife.entity.preparation.ExpenseDto;
import com.example.campuslife.entity.preparation.FinanceOverviewReportDto;
import com.example.campuslife.entity.preparation.PreparationTaskDto;
import com.example.campuslife.entity.preparation.UploadResultDto;
import com.example.campuslife.utils.FileUtils;
import com.example.campuslife.utils.ImageCompressUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreparationFinanceFragment extends Fragment implements ExpenseAdapter.OnExpenseClickListener {

    private long activityId;
    private String currentQueryStatus = "ALL";

    private TextView tvTotal, tvSpent, tvRemaining, tvEmpty;
    private Spinner spFilter;
    private ProgressBar progress;
    private RecyclerView rvExpenses;
    private ExpenseAdapter adapter;

    private Uri selectedEvidenceUri;
    private ImageView activePreviewImage;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedEvidenceUri = uri;
                    if (activePreviewImage != null) {
                        activePreviewImage.setVisibility(View.VISIBLE);
                        Glide.with(this).load(uri).into(activePreviewImage);
                    }
                }
            });

    public PreparationFinanceFragment() {
    }

    public static PreparationFinanceFragment newInstance(long maxTaskActivityId) {
        PreparationFinanceFragment fragment = new PreparationFinanceFragment();
        Bundle args = new Bundle();
        args.putLong("activityId", maxTaskActivityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preparation_finance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        activityId = args != null ? args.getLong("activityId", -1) : -1;

        tvTotal = view.findViewById(R.id.tvTotal);
        tvSpent = view.findViewById(R.id.tvSpent);
        tvRemaining = view.findViewById(R.id.tvRemaining);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        spFilter = view.findViewById(R.id.spFilter);
        progress = view.findViewById(R.id.progress);
        rvExpenses = view.findViewById(R.id.rvExpenses);

        adapter = new ExpenseAdapter(this);
        rvExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExpenses.setAdapter(adapter);

        setupFilter();
        view.findViewById(R.id.btnAdd).setOnClickListener(v -> openAddExpense());

        loadDashboard();
        loadExpenses();
    }

    private void setupFilter() {
        String[] labels = new String[] { "Tất cả", "Chờ duyệt", "Đã duyệt", "Từ chối" };
        ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels);
        spFilter.setAdapter(ad);
        spFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) currentQueryStatus = "ALL";
                else if (position == 1) currentQueryStatus = "PENDING";
                else if (position == 2) currentQueryStatus = "APPROVED";
                else currentQueryStatus = "REJECTED";
                loadExpenses();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void loadDashboard() {
        ApiClient.preparation(requireContext()).getFinanceOverview(activityId).enqueue(new Callback<ApiResponse<FinanceOverviewReportDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<FinanceOverviewReportDto>> call, Response<ApiResponse<FinanceOverviewReportDto>> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                    FinanceOverviewReportDto d = resp.body().getData();
                    if (d != null) {
                        tvTotal.setText("Total: " + formatMoney(d.totalBudget));
                        tvSpent.setText("Spent: " + formatMoney(d.totalApprovedSpent));
                        try {
                            BigDecimal total = new BigDecimal(d.totalBudget != null ? d.totalBudget : "0");
                            BigDecimal spent = new BigDecimal(d.totalApprovedSpent != null ? d.totalApprovedSpent : "0");
                            tvRemaining.setText("Remaining: " + formatMoney(total.subtract(spent).toString()));
                        } catch (Exception ignored) {}
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<FinanceOverviewReportDto>> call, Throwable t) {}
        });
    }

    private void loadExpenses() {
        showLoading(true);
        tvEmpty.setVisibility(View.GONE);
        ApiClient.preparation(requireContext()).listExpenses(activityId, currentQueryStatus).enqueue(new Callback<ApiResponse<List<ExpenseDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ExpenseDto>>> call, Response<ApiResponse<List<ExpenseDto>>> resp) {
                showLoading(false);
                if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus() && resp.body().getData() != null) {
                    List<ExpenseDto> data = resp.body().getData();
                    adapter.submit(data);
                    tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    adapter.submit(new ArrayList<>());
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<ExpenseDto>>> call, Throwable t) {
                showLoading(false);
                toast("Lỗi tải danh sách chi phí");
            }
        });
    }

    private Long selectedTaskId = null;
    private Long selectedCategoryId = null;

    private void openAddExpense() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(R.layout.layout_bottom_sheet_create_expense);

        AutoCompleteTextView actTask = dialog.findViewById(R.id.actTask);
        AutoCompleteTextView actCategory = dialog.findViewById(R.id.actCategory);
        TextInputEditText etAmount = dialog.findViewById(R.id.etExpenseAmount);
        TextInputEditText etDesc = dialog.findViewById(R.id.etExpenseDescription);
        
        LinearLayout llUpload = dialog.findViewById(R.id.llUploadEvidence);
        activePreviewImage = dialog.findViewById(R.id.ivEvidencePreview);
        View btnSubmit = dialog.findViewById(R.id.btnSubmitExpense);
        View btnCancel = dialog.findViewById(R.id.btnCancelExpense);
        
        selectedEvidenceUri = null;
        selectedTaskId = null;
        selectedCategoryId = null;

        dialog.setOnDismissListener(d -> activePreviewImage = null);

        llUpload.setOnClickListener(x -> pickImageLauncher.launch("image/*"));
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Load Categories
        ApiClient.preparation(requireContext()).getActivityBudget(activityId).enqueue(new Callback<ApiResponse<ActivityBudgetDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<ActivityBudgetDto>> call, Response<ApiResponse<ActivityBudgetDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ActivityBudgetDto data = response.body().getData();
                    List<BudgetCategoryDto> categories = data.categories;
                    if (categories != null && !categories.isEmpty()) {
                        String[] names = new String[categories.size()];
                        for (int i = 0; i < categories.size(); i++) names[i] = categories.get(i).name;
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names);
                        actCategory.setAdapter(adapter);
                        actCategory.setOnItemClickListener((parent, view, position, id) -> selectedCategoryId = categories.get(position).id);
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<ActivityBudgetDto>> call, Throwable t) {}
        });

        // Load Tasks
        ApiClient.preparation(requireContext()).getDashboard(activityId).enqueue(new Callback<ApiResponse<com.example.campuslife.entity.preparation.PreparationDashboardDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<com.example.campuslife.entity.preparation.PreparationDashboardDto>> call, Response<ApiResponse<com.example.campuslife.entity.preparation.PreparationDashboardDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<PreparationTaskDto> tasks = response.body().getData().tasks;
                    if (tasks != null && !tasks.isEmpty()) {
                        String[] names = new String[tasks.size()];
                        for (int i = 0; i < tasks.size(); i++) names[i] = tasks.get(i).title;
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names);
                        actTask.setAdapter(adapter);
                        actTask.setOnItemClickListener((parent, view, position, id) -> selectedTaskId = tasks.get(position).id);
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<com.example.campuslife.entity.preparation.PreparationDashboardDto>> call, Throwable t) {}
        });

        btnSubmit.setOnClickListener(x -> {
            String amount = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
            
            if (selectedCategoryId == null) {
                toast("Vui lòng chọn Hạng mục"); return;
            }
            if (amount.isEmpty()) {
                toast("Nhập số tiền"); return;
            }

            btnSubmit.setEnabled(false);
            showLoading(true);

            if (selectedEvidenceUri == null) {
                createExpense(selectedTaskId, selectedCategoryId, amount, desc, null, dialog, btnSubmit);
                return;
            }

            String path = FileUtils.getPath(requireContext(), selectedEvidenceUri);
            if (path == null) {
                btnSubmit.setEnabled(true); showLoading(false);
                toast("Không đọc được ảnh"); return;
            }

            File file;
            try { file = ImageCompressUtil.compressToJpeg(requireContext(), path); }
            catch (Exception e) {
                btnSubmit.setEnabled(true); showLoading(false);
                toast("Nén ảnh thất bại"); return;
            }

            RequestBody rb = RequestBody.create(file, MediaType.parse("image/jpeg"));
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), rb);

            ApiClient.preparation(requireContext()).uploadEvidence(activityId, part).enqueue(new Callback<ApiResponse<UploadResultDto>>() {
                @Override
                public void onResponse(Call<ApiResponse<UploadResultDto>> call, Response<ApiResponse<UploadResultDto>> resp) {
                    if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus() && resp.body().getData() != null) {
                        createExpense(selectedTaskId, selectedCategoryId, amount, desc, resp.body().getData().url, dialog, btnSubmit);
                    } else {
                        btnSubmit.setEnabled(true); showLoading(false);
                        toast(resp.body() != null ? resp.body().getMessage() : "HTTP " + resp.code());
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse<UploadResultDto>> call, Throwable t) {
                    btnSubmit.setEnabled(true); showLoading(false);
                    toast("Lỗi upload: " + t.getMessage());
                }
            });
        });

        dialog.show();
    }

    private void createExpense(Long taskId, Long categoryId, String amount, String desc, String url, BottomSheetDialog dialog, View btnSubmit) {
        ApiClient.preparation(requireContext())
            .createExpense(activityId, new CreateExpenseRequest(taskId, categoryId, amount, desc, url))
            .enqueue(new Callback<ApiResponse<ExpenseDto>>() {
                @Override
                public void onResponse(Call<ApiResponse<ExpenseDto>> call, Response<ApiResponse<ExpenseDto>> resp) {
                    showLoading(false); btnSubmit.setEnabled(true);
                    if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                        toast("Đã gửi chi phí");
                        dialog.dismiss();
                        loadExpenses(); loadDashboard();
                    } else {
                        toast(resp.body() != null ? resp.body().getMessage() : "HTTP " + resp.code());
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse<ExpenseDto>> call, Throwable t) {
                    showLoading(false); btnSubmit.setEnabled(true);
                    toast("Lỗi mạng: " + t.getMessage());
                }
            });
    }

    @Override
    public void onExpenseClick(ExpenseDto expense) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(R.layout.layout_dialog_approve_expense);

        ImageView ivDetail = dialog.findViewById(R.id.ivExpenseEvidenceDetail);
        TextView tvAmount = dialog.findViewById(R.id.tvDetailAmount);
        TextView tvDesc = dialog.findViewById(R.id.tvDetailDescription);
        TextView tvCreator = dialog.findViewById(R.id.tvDetailCreator);
        View btnApprove = dialog.findViewById(R.id.btnApproveExpense);
        View btnReject = dialog.findViewById(R.id.btnRejectExpense);
        View btnClose = dialog.findViewById(R.id.btnCloseDetail);

        tvAmount.setText(formatMoney(expense.amount) + "đ");
        tvDesc.setText("Mô tả: " + (expense.description != null ? expense.description : "Không có"));
        tvCreator.setText("Người tạo: " + expense.createdByName);

        if (expense.evidenceUrl != null && !expense.evidenceUrl.isEmpty()) {
            String fullUrl = expense.evidenceUrl.replace("http://localhost:8080", "http://10.0.2.2:8080");
            if (!fullUrl.startsWith("http")) fullUrl = BuildConfig.BASE_URL + (fullUrl.startsWith("/") ? fullUrl.substring(1) : fullUrl);
            Glide.with(this).load(fullUrl).into(ivDetail);
        }

        // Handle Approval logic: Wait, we also need to check role. For now we just show.
        // If not PENDING and not Admin/Leader, maybe we hide buttons. Let Backend reject if unauthorized.
        if ("APPROVED".equals(expense.status) || "REJECTED".equals(expense.status)) {
            btnApprove.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
        }

        btnApprove.setOnClickListener(v -> submitApproval(expense.id, true, dialog));
        btnReject.setOnClickListener(v -> submitApproval(expense.id, false, dialog));
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void submitApproval(Long expenseId, boolean approve, BottomSheetDialog dialog) {
        showLoading(true);
        ApiClient.preparation(requireContext()).approveExpense(expenseId, new ApproveExpenseRequest(approve)).enqueue(new Callback<ApiResponse<ExpenseDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<ExpenseDto>> call, Response<ApiResponse<ExpenseDto>> resp) {
                showLoading(false);
                if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                    toast(approve ? "Đã duyệt!" : "Đã từ chối!");
                    dialog.dismiss();
                    loadExpenses(); loadDashboard();
                } else {
                    toast(resp.body() != null ? resp.body().getMessage() : "Lỗi xét duyệt");
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<ExpenseDto>> call, Throwable t) {
                showLoading(false);
                toast("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        if (progress != null) progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    private void toast(String msg) {
        if (!isAdded()) return;
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
    private String formatMoney(String amount) {
        if (amount == null || amount.trim().isEmpty()) return "0";
        try { return String.format("%,d", new BigDecimal(amount).longValue()); } 
        catch (Exception e) { return amount; }
    }
}
