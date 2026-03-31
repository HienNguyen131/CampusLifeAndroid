package com.example.campuslife.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import com.bumptech.glide.Glide;
import com.example.campuslife.R;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.AllocationAdjustmentRequestDto;
import com.example.campuslife.entity.preparation.CreateAllocationAdjustmentRequest;
import com.example.campuslife.entity.preparation.CreateExpenseRequest;
import com.example.campuslife.entity.preparation.ExpenseCategorySuggestionDto;
import com.example.campuslife.entity.preparation.ExpenseDto;
import com.example.campuslife.entity.preparation.FundAdvanceDto;
import com.example.campuslife.entity.preparation.UploadResultDto;
import com.example.campuslife.utils.FileUtils;
import com.example.campuslife.utils.ImageCompressUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskExpensesFragment extends Fragment {

    private static final String ARG_TASK_ID = "taskId";
    private static final String ARG_ACTIVITY_ID = "activityId";
    private static final String ARG_ROLE = "myRole";

    private long taskId;
    private long activityId;
    private String myRole;

    private final DecimalFormat df = new DecimalFormat("#,###");

    private ProgressBar progressExpenses;
    private TextView tvEmpty, tvMyAdvanceAmount, tvViewHistory;
    private RecyclerView rvExpenses;
    private MaterialButton btnAddExpense, btnRequestAllocation;

    private final List<ExpenseDto> expenseList = new ArrayList<>();
    private TaskExpenseAdapter expenseAdapter;

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

    public static TaskExpensesFragment newInstance(long taskId, long activityId, String myRole) {
        TaskExpensesFragment f = new TaskExpensesFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_TASK_ID, taskId);
        b.putLong(ARG_ACTIVITY_ID, activityId);
        b.putString(ARG_ROLE, myRole);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskId = getArguments().getLong(ARG_TASK_ID, -1);
            activityId = getArguments().getLong(ARG_ACTIVITY_ID, -1);
            myRole = getArguments().getString(ARG_ROLE, "MEMBER");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_expenses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressExpenses = view.findViewById(R.id.progressExpenses);
        tvEmpty = view.findViewById(R.id.tvEmptyExpenses);
        tvMyAdvanceAmount = view.findViewById(R.id.tvMyAdvanceAmount);
        tvViewHistory = view.findViewById(R.id.tvViewMyAdvanceHistory);
        rvExpenses = view.findViewById(R.id.rvTaskExpenses);
        btnAddExpense = view.findViewById(R.id.btnAddExpense);
        btnRequestAllocation = view.findViewById(R.id.btnRequestAllocation);

        expenseAdapter = new TaskExpenseAdapter(requireContext(), expenseList);
        rvExpenses.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExpenses.setAdapter(expenseAdapter);

        if ("LEADER".equalsIgnoreCase(myRole)) {
            btnRequestAllocation.setVisibility(View.VISIBLE);
            btnRequestAllocation.setOnClickListener(v -> showCreateAllocationDialog());
        }

        btnAddExpense.setOnClickListener(v -> showCreateExpenseDialog());
        tvViewHistory.setOnClickListener(v -> showMyAdvanceHistory());

        loadExpenses();
        loadMyAdvances();
    }

    private void loadExpenses() {
        progressExpenses.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.preparation(requireContext())
                .listExpenses(activityId, null)
                .enqueue(new Callback<ApiResponse<List<ExpenseDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ExpenseDto>>> call,
                            Response<ApiResponse<List<ExpenseDto>>> resp) {
                        progressExpenses.setVisibility(View.GONE);
                        if (!isAdded()) return;
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            List<ExpenseDto> all = resp.body().getData();
                            expenseList.clear();
                            if (all != null) {
                                for (ExpenseDto e : all) {
                                    if (e.taskId != null && e.taskId == taskId) {
                                        expenseList.add(e);
                                    }
                                }
                            }
                            expenseAdapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(expenseList.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<List<ExpenseDto>>> call, Throwable t) {
                        if (!isAdded()) return;
                        progressExpenses.setVisibility(View.GONE);
                        toast("Lỗi tải chi phí");
                    }
                });
    }

    private void loadMyAdvances() {
        ApiClient.preparation(requireContext())
                .getMyFundAdvances(activityId, taskId)
                .enqueue(new Callback<ApiResponse<List<FundAdvanceDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<FundAdvanceDto>>> call,
                            Response<ApiResponse<List<FundAdvanceDto>>> resp) {
                        if (!isAdded()) return;
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            List<FundAdvanceDto> advances = resp.body().getData();
                            BigDecimal total = BigDecimal.ZERO;
                            if (advances != null) {
                                for (FundAdvanceDto a : advances) {
                                    if ("HOLDING".equals(a.status) && a.remainingAmount != null) {
                                        total = total.add(a.remainingAmount);
                                    }
                                }
                            }
                            tvMyAdvanceAmount.setText(df.format(total) + " VNĐ");
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<List<FundAdvanceDto>>> call, Throwable t) {}
                });
    }

    private void showMyAdvanceHistory() {
        ApiClient.preparation(requireContext())
                .getMyFundAdvances(activityId, taskId)
                .enqueue(new Callback<ApiResponse<List<FundAdvanceDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<FundAdvanceDto>>> call,
                            Response<ApiResponse<List<FundAdvanceDto>>> resp) {
                        if (!isAdded()) return;
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            showAdvanceHistoryDialog(resp.body().getData());
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<List<FundAdvanceDto>>> call, Throwable t) {
                        if (!isAdded()) return;
                        toast("Lỗi mạng");
                    }
                });
    }

    private void showAdvanceHistoryDialog(List<FundAdvanceDto> list) {
        if (!isAdded()) return;
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_simple_list_sheet, null, false);
        dialog.setContentView(v);

        TextView tvTitle = v.findViewById(R.id.tvSheetTitle);
        RecyclerView rv = v.findViewById(R.id.rvSheetList);
        if (tvTitle != null) tvTitle.setText("Lịch sử tạm ứng của tôi");
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(requireContext()));
            rv.setAdapter(new MyAdvanceHistoryAdapter(
                    list != null ? list : new ArrayList<>(), df));
        }
        dialog.show();
    }

    // Handler for debounce expense suggestion
    private final Handler suggestionHandler = new Handler(Looper.getMainLooper());
    private Runnable suggestionRunnable;
    private List<ExpenseCategorySuggestionDto> currentSuggestions = new ArrayList<>();
    private Long selectedCategoryId = null;

    private void showCreateExpenseDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_bottom_sheet_create_expense_task, null, false);
        dialog.setContentView(v);

        TextInputEditText etAmount = v.findViewById(R.id.etExpenseAmount);
        TextInputEditText etDesc = v.findViewById(R.id.etExpenseDescription);
        AutoCompleteTextView actCategory = v.findViewById(R.id.actCategory);
        LinearLayout llUpload = v.findViewById(R.id.llUploadEvidence);
        activePreviewImage = v.findViewById(R.id.ivEvidencePreview);
        TextView tvSuggestionInfo = v.findViewById(R.id.tvSuggestionInfo);
        MaterialButton btnSubmit = v.findViewById(R.id.btnSubmitExpense);
        MaterialButton btnCancel = v.findViewById(R.id.btnCancelExpense);

        selectedEvidenceUri = null;
        selectedCategoryId = null;
        currentSuggestions = new ArrayList<>();

        dialog.setOnDismissListener(d -> activePreviewImage = null);

        if (llUpload != null) {
            llUpload.setOnClickListener(x -> pickImageLauncher.launch("image/*"));
        }
        if (btnCancel != null) btnCancel.setOnClickListener(x -> dialog.dismiss());

        if (etAmount != null) {
            etAmount.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                    if (suggestionRunnable != null) suggestionHandler.removeCallbacks(suggestionRunnable);
                    suggestionRunnable = () -> {
                        String amtStr = s.toString().trim();
                        if (!amtStr.isEmpty() && !amtStr.equals("0")) {
                            fetchCategorySuggestions(amtStr, actCategory, tvSuggestionInfo, btnSubmit);
                        }
                    };
                    suggestionHandler.postDelayed(suggestionRunnable, 400);
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(x -> {
                String amtStr = etAmount != null ? etAmount.getText().toString().trim() : "";
                String desc = etDesc != null ? etDesc.getText().toString().trim() : "";
                if (amtStr.isEmpty() || selectedCategoryId == null) {
                    toast("Vui lòng nhập số tiền và chọn ví");
                    return;
                }
                if (selectedEvidenceUri != null) {
                    uploadEvidenceThenCreate(dialog, amtStr, desc);
                } else {
                    submitExpense(dialog, amtStr, desc, null);
                }
            });
        }
        dialog.show();
    }

    private void fetchCategorySuggestions(String amount, AutoCompleteTextView actCategory,
            TextView tvInfo, MaterialButton btnSubmit) {
        ApiClient.preparation(requireContext())
                .suggestExpenseCategories(taskId, amount)
                .enqueue(new Callback<ApiResponse<List<ExpenseCategorySuggestionDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ExpenseCategorySuggestionDto>>> call,
                            Response<ApiResponse<List<ExpenseCategorySuggestionDto>>> resp) {
                        if (!isAdded()) return;
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            currentSuggestions = resp.body().getData();
                            if (currentSuggestions == null) currentSuggestions = new ArrayList<>();
                            autoPickCategory(currentSuggestions, actCategory, tvInfo, btnSubmit);
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<List<ExpenseCategorySuggestionDto>>> call, Throwable t) {}
                });
    }

    private void autoPickCategory(List<ExpenseCategorySuggestionDto> suggestions,
            AutoCompleteTextView actCategory, TextView tvInfo, MaterialButton btnSubmit) {
        // Empty = backend đã lọc: không có ví nào có tạm ứng khả dụng của user này
        if (suggestions.isEmpty()) {
            if (tvInfo != null) {
                tvInfo.setText("⚠️ Chưa có tạm ứng khả dụng. Vui lòng xin tạm ứng trước.");
                tvInfo.setTextColor(android.graphics.Color.parseColor("#B45309"));
            }
            if (btnSubmit != null) btnSubmit.setEnabled(false);
            return;
        }

        // Tất cả maxExpenseAmount = 0 → allocation/wallet không đủ (dù đã có tạm ứng)
        boolean allZero = true;
        for (ExpenseCategorySuggestionDto s : suggestions) {
            if (s.maxExpenseAmount != null && s.maxExpenseAmount.compareTo(BigDecimal.ZERO) > 0) {
                allZero = false;
                break;
            }
        }
        if (allZero) {
            if (tvInfo != null) {
                tvInfo.setText("⚠️ Quota cấp phát hoặc ví không đủ để tạo chi phí này.");
                tvInfo.setTextColor(android.graphics.Color.parseColor("#B45309"));
            }
            if (btnSubmit != null) btnSubmit.setEnabled(false);
            return;
        }
        if (btnSubmit != null) btnSubmit.setEnabled(true);

        // Chỉ hiển thị các ví có maxExpenseAmount > 0
        List<ExpenseCategorySuggestionDto> validSugg = new ArrayList<>();
        for (ExpenseCategorySuggestionDto s : suggestions) {
            if (s.maxExpenseAmount != null && s.maxExpenseAmount.compareTo(BigDecimal.ZERO) > 0) {
                validSugg.add(s);
            }
        }

        String[] names = new String[validSugg.size()];
        for (int i = 0; i < validSugg.size(); i++) names[i] = validSugg.get(i).categoryName;
        ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, names);
        actCategory.setAdapter(ad);

        // Auto-pick ví có maxExpenseAmount lớn nhất
        ExpenseCategorySuggestionDto best = validSugg.get(0);
        for (ExpenseCategorySuggestionDto s : validSugg) {
            if (s.maxExpenseAmount != null && best.maxExpenseAmount != null
                    && s.maxExpenseAmount.compareTo(best.maxExpenseAmount) > 0) {
                best = s;
            }
        }
        selectedCategoryId = best.categoryId;
        actCategory.setText(best.categoryName, false);

        // Hiển thị thông tin tạm ứng còn lại
        String advanceInfo = best.myFundAdvanceRemainingAmount != null
                ? " | Tạm ứng còn: " + df.format(best.myFundAdvanceRemainingAmount) + " đ"
                : "";
        String maxInfo = best.maxExpenseAmount != null
                ? " (tối đa " + df.format(best.maxExpenseAmount) + " đ)" : "";
        if (tvInfo != null) {
            tvInfo.setText("✅ Gợi ý: " + best.categoryName + maxInfo + advanceInfo);
            tvInfo.setTextColor(android.graphics.Color.parseColor("#065F46"));
        }

        final List<ExpenseCategorySuggestionDto> finalSugg = validSugg;
        actCategory.setOnItemClickListener((parent, view, position, id) -> {
            if (position < finalSugg.size()) {
                selectedCategoryId = finalSugg.get(position).categoryId;
                ExpenseCategorySuggestionDto picked = finalSugg.get(position);
                String advInfo = picked.myFundAdvanceRemainingAmount != null
                        ? " | Tạm ứng còn: " + df.format(picked.myFundAdvanceRemainingAmount) + " đ"
                        : "";
                String mxInfo = picked.maxExpenseAmount != null
                        ? " (tối đa " + df.format(picked.maxExpenseAmount) + " đ)" : "";
                if (tvInfo != null) {
                    tvInfo.setText("✅ " + picked.categoryName + mxInfo + advInfo);
                    tvInfo.setTextColor(android.graphics.Color.parseColor("#065F46"));
                }
            }
        });
    }

    private void uploadEvidenceThenCreate(BottomSheetDialog dialog, String amount, String desc) {
        try {
            String path = FileUtils.getPath(requireContext(), selectedEvidenceUri);
            if (path == null) { submitExpense(dialog, amount, desc, null); return; }
            File compressed = ImageCompressUtil.compressToJpeg(requireContext(), path);
            RequestBody reqBody = RequestBody.create(MediaType.parse("image/jpeg"), compressed);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", compressed.getName(), reqBody);
            ApiClient.preparation(requireContext()).uploadEvidence(taskId, part)
                    .enqueue(new Callback<ApiResponse<UploadResultDto>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<UploadResultDto>> call,
                                Response<ApiResponse<UploadResultDto>> resp) {
                            if (!isAdded()) return;
                            String url = (resp.isSuccessful() && resp.body() != null
                                    && resp.body().getData() != null)
                                    ? resp.body().getData().url : null;
                            submitExpense(dialog, amount, desc, url);
                        }
                        @Override
                        public void onFailure(Call<ApiResponse<UploadResultDto>> call, Throwable t) {
                            if (!isAdded()) return;
                            submitExpense(dialog, amount, desc, null);
                        }
                    });
        } catch (Exception e) {
            submitExpense(dialog, amount, desc, null);
        }
    }

    private void submitExpense(BottomSheetDialog dialog, String amount, String desc, String evidenceUrl) {
        CreateExpenseRequest req = new CreateExpenseRequest();
        req.categoryId = selectedCategoryId;
        req.amount = amount;
        req.description = desc;
        req.evidenceUrl = evidenceUrl;

        ApiClient.preparation(requireContext()).createExpense(taskId, req)
                .enqueue(new Callback<ApiResponse<ExpenseDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ExpenseDto>> call,
                            Response<ApiResponse<ExpenseDto>> resp) {
                        if (!isAdded()) return;
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            toast("Đã tạo chi phí thành công");
                            dialog.dismiss();
                            loadExpenses();
                        } else {
                            toast(resp.body() != null ? resp.body().getMessage() : "Lỗi khi tạo chi phí");
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<ExpenseDto>> call, Throwable t) {
                        if (!isAdded()) return;
                        toast("Lỗi mạng: " + t.getMessage());
                    }
                });
    }

    private void showCreateAllocationDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_dialog_allocation_adjustment, null, false);
        dialog.setContentView(v);

        TextInputEditText etAmount = v.findViewById(R.id.etAdjAmount);
        TextInputEditText etDesc = v.findViewById(R.id.etAdjDescription);
        MaterialButton btnSubmit = v.findViewById(R.id.btnSubmitAdj);
        MaterialButton btnCancel = v.findViewById(R.id.btnCancelAdj);

        if (btnCancel != null) btnCancel.setOnClickListener(x -> dialog.dismiss());
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(x -> {
                String amtStr = etAmount != null ? etAmount.getText().toString().trim() : "";
                String descStr = etDesc != null ? etDesc.getText().toString().trim() : "";
                if (amtStr.isEmpty() || descStr.isEmpty()) {
                    toast("Vui lòng nhập đầy đủ thông tin");
                    return;
                }
                CreateAllocationAdjustmentRequest req = new CreateAllocationAdjustmentRequest();
                req.amount = amtStr;
                req.description = descStr;
                ApiClient.preparation(requireContext())
                        .createAllocationAdjustment(taskId, req)
                        .enqueue(new Callback<ApiResponse<AllocationAdjustmentRequestDto>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<AllocationAdjustmentRequestDto>> call,
                                    Response<ApiResponse<AllocationAdjustmentRequestDto>> resp) {
                                if (!isAdded()) return;
                                if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                                    toast("Đã gửi yêu cầu bổ sung cấp phát");
                                    dialog.dismiss();
                                } else {
                                    toast(resp.body() != null ? resp.body().getMessage() : "Lỗi");
                                }
                            }
                            @Override
                            public void onFailure(Call<ApiResponse<AllocationAdjustmentRequestDto>> call, Throwable t) {
                                if (!isAdded()) return;
                                toast("Lỗi mạng: " + t.getMessage());
                            }
                        });
            });
        }
        dialog.show();
    }

    private void toast(String msg) {
        if (!isAdded()) return;
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    // --- Inner TaskExpenseAdapter ---
    private static class TaskExpenseAdapter extends RecyclerView.Adapter<TaskExpenseAdapter.VH> {
        private final android.content.Context ctx;
        private final List<ExpenseDto> list;
        private final DecimalFormat df = new DecimalFormat("#,###");

        TaskExpenseAdapter(android.content.Context ctx, List<ExpenseDto> list) {
            this.ctx = ctx;
            this.list = list;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(ctx).inflate(R.layout.item_my_expense, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            ExpenseDto e = list.get(position);
            h.tvDesc.setText(e.description != null ? e.description : "Chi phí");
            try { h.tvAmount.setText(df.format(new BigDecimal(e.amount != null ? e.amount : "0")) + " đ"); }
            catch (Exception ex) { h.tvAmount.setText(e.amount + " đ"); }
            h.tvCategory.setText(e.categoryName != null ? "Ví: " + e.categoryName : "");
            bindStatus(h.tvStatus, e.status);
            h.tvDate.setText(e.createdAt != null ? e.createdAt.split("T")[0] : "");
            if (h.tvEvidence != null) {
                h.tvEvidence.setVisibility(
                        (e.evidenceUrl != null && !e.evidenceUrl.isEmpty()) ? View.VISIBLE : View.GONE);
            }
        }

        private void bindStatus(TextView tv, String status) {
            com.example.campuslife.utils.StatusBadgeHelper.applyExpenseStatus(tv, status != null ? status : "PENDING_LEADER");
        }

        @Override public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvDesc, tvAmount, tvCategory, tvStatus, tvDate, tvEvidence;
            VH(View v) {
                super(v);
                tvDesc = v.findViewById(R.id.tvExpenseDesc);
                tvAmount = v.findViewById(R.id.tvExpenseAmount);
                tvCategory = v.findViewById(R.id.tvExpenseCategory);
                tvStatus = v.findViewById(R.id.tvExpenseStatus);
                tvDate = v.findViewById(R.id.tvExpenseDate);
                tvEvidence = v.findViewById(R.id.tvEvidenceIndicator);
            }
        }
    }

    // --- Inner adapter for advance history ---
    private static class MyAdvanceHistoryAdapter extends RecyclerView.Adapter<MyAdvanceHistoryAdapter.VH> {
        private final List<FundAdvanceDto> list;
        private final DecimalFormat df;

        MyAdvanceHistoryAdapter(List<FundAdvanceDto> list, DecimalFormat df) {
            this.list = list;
            this.df = df;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            FundAdvanceDto a = list.get(position);
            String amount = a.amount != null ? df.format(a.amount) + " đ" : "0 đ";
            h.text1.setText(a.categoryName != null ? a.categoryName + " – " + amount : amount);
            h.text2.setText(mapStatus(a.status) + (a.createdAt != null ? " | " + a.createdAt.split("T")[0] : ""));
        }

        private String mapStatus(String s) {
            if (s == null) return "";
            switch (s) {
                case "HOLDING": return "Đang giữ tiền";
                case "SETTLED": return "Đã tất toán";
                case "REJECTED": return "Từ chối";
                default: return "Chờ duyệt";
            }
        }

        @Override public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView text1, text2;
            VH(View v) {
                super(v);
                text1 = v.findViewById(android.R.id.text1);
                text2 = v.findViewById(android.R.id.text2);
            }
        }
    }

}
