package com.example.campuslife.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.campuslife.R;
import com.example.campuslife.adapter.ExpenseAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.BudgetDto;
import com.example.campuslife.entity.preparation.CreateExpenseRequest;
import com.example.campuslife.entity.preparation.ExpenseDto;
import com.example.campuslife.entity.preparation.PreparationDashboardDto;
import com.example.campuslife.entity.preparation.UploadResultDto;
import com.example.campuslife.utils.FileUtils;
import com.example.campuslife.utils.ImageCompressUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreparationFinanceFragment extends Fragment {

    public static PreparationFinanceFragment newInstance(long activityId) {
        PreparationFinanceFragment f = new PreparationFinanceFragment();
        Bundle b = new Bundle();
        b.putLong("activityId", activityId);
        f.setArguments(b);
        return f;
    }

    private long activityId;

    private TextView tvTotal, tvSpent, tvRemaining, tvEmpty;
    private Spinner spFilter;
    private ProgressBar progress;
    private RecyclerView rvExpenses;
    private ExpenseAdapter adapter;

    private String currentQueryStatus = "ALL";

    private ActivityResultLauncher<String> pickImageLauncher;
    private Uri selectedEvidenceUri;
    private ImageView activePreviewImage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null)
                        return;
                    selectedEvidenceUri = uri;
                    if (activePreviewImage != null) {
                        activePreviewImage.setVisibility(View.VISIBLE);
                        Glide.with(requireContext()).load(uri).into(activePreviewImage);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
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

        adapter = new ExpenseAdapter();
        rvExpenses.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        rvExpenses.setAdapter(adapter);

        setupFilter();

        view.findViewById(R.id.btnAdd).setOnClickListener(v -> openAddExpense());

        loadDashboard();
        loadExpenses();
    }

    private void setupFilter() {
        String[] labels = new String[] { "Tất cả", "Chờ duyệt", "Đã duyệt", "Từ chối" };
        ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item,
                labels);
        spFilter.setAdapter(ad);
        spFilter.setSelection(0);
        spFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)
                    currentQueryStatus = "ALL";
                else if (position == 1)
                    currentQueryStatus = "PENDING";
                else if (position == 2)
                    currentQueryStatus = "APPROVED";
                else
                    currentQueryStatus = "REJECTED";
                loadExpenses();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void loadDashboard() {
        ApiClient.preparation(requireContext())
                .getDashboard(activityId)
                .enqueue(new Callback<ApiResponse<PreparationDashboardDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PreparationDashboardDto>> call,
                            Response<ApiResponse<PreparationDashboardDto>> resp) {
                        if (!resp.isSuccessful() || resp.body() == null || !resp.body().isStatus())
                            return;
                        PreparationDashboardDto d = resp.body().getData();
                        BudgetDto b = d != null ? d.budget : null;
                        if (b != null) {
                            tvTotal.setText("Total: " + formatMoney(b.totalAmount));
                            tvSpent.setText("Spent: " + formatMoney(b.spentAmount));
                            tvRemaining.setText("Remaining: " + formatMoney(b.remainingAmount));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PreparationDashboardDto>> call, Throwable t) {
                    }
                });
    }

    private void loadExpenses() {
        showLoading(true);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.preparation(requireContext())
                .listExpenses(activityId, currentQueryStatus)
                .enqueue(new Callback<ApiResponse<List<ExpenseDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ExpenseDto>>> call,
                            Response<ApiResponse<List<ExpenseDto>>> resp) {
                        showLoading(false);
                        if (!resp.isSuccessful() || resp.body() == null || !resp.body().isStatus()) {
                            adapter.submit(null);
                            tvEmpty.setVisibility(View.VISIBLE);
                            return;
                        }

                        List<ExpenseDto> data = resp.body().getData();
                        adapter.submit(data);
                        tvEmpty.setVisibility(data == null || data.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ExpenseDto>>> call, Throwable t) {
                        showLoading(false);
                        toast("Lỗi mạng: " + (t.getMessage() != null ? t.getMessage() : ""));
                    }
                });
    }

    private void openAddExpense() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_add_expense, null, false);
        dialog.setContentView(v);

        EditText etAmount = v.findViewById(R.id.etAmount);
        EditText etDesc = v.findViewById(R.id.etDescription);
        View btnPick = v.findViewById(R.id.btnPick);
        View btnSubmit = v.findViewById(R.id.btnSubmit);
        ProgressBar pb = v.findViewById(R.id.progress);
        ImageView imgPreview = v.findViewById(R.id.imgPreview);

        selectedEvidenceUri = null;
        activePreviewImage = imgPreview;
        dialog.setOnDismissListener(d -> activePreviewImage = null);

        btnPick.setOnClickListener(x -> pickImageLauncher.launch("image/*"));

        btnSubmit.setOnClickListener(x -> {
            String amount = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
            String desc = etDesc.getText() != null ? etDesc.getText().toString().trim() : "";
            if (amount.isEmpty() || "0".equals(amount)) {
                toast("Nhập số tiền");
                return;
            }
            pb.setVisibility(View.VISIBLE);
            btnSubmit.setEnabled(false);

            if (selectedEvidenceUri == null) {
                createExpense(amount, desc, null, dialog, pb, btnSubmit);
                return;
            }

            String path = FileUtils.getPath(requireContext(), selectedEvidenceUri);
            if (path == null) {
                pb.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                toast("Không đọc được ảnh");
                return;
            }

            File file;
            try {
                file = ImageCompressUtil.compressToJpeg(requireContext(), path);
            } catch (Exception e) {
                pb.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                toast("Nén ảnh thất bại");
                return;
            }

            RequestBody rb = RequestBody.create(file, MediaType.parse("image/jpeg"));
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), rb);

            ApiClient.preparation(requireContext())
                    .uploadEvidence(activityId, part)
                    .enqueue(new Callback<ApiResponse<UploadResultDto>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<UploadResultDto>> call,
                                Response<ApiResponse<UploadResultDto>> resp) {
                            if (!resp.isSuccessful() || resp.body() == null || !resp.body().isStatus()) {
                                pb.setVisibility(View.GONE);
                                btnSubmit.setEnabled(true);
                                toast(resp.body() != null ? resp.body().getMessage() : ("HTTP " + resp.code()));
                                return;
                            }

                            UploadResultDto u = resp.body().getData();
                            createExpense(amount, desc, u != null ? u.url : null, dialog, pb, btnSubmit);
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<UploadResultDto>> call, Throwable t) {
                            pb.setVisibility(View.GONE);
                            btnSubmit.setEnabled(true);
                            toast("Lỗi mạng: " + (t.getMessage() != null ? t.getMessage() : ""));
                        }
                    });
        });

        dialog.show();
    }

    private void createExpense(String amount, String desc, String evidenceUrl, BottomSheetDialog dialog,
            ProgressBar pb, View btnSubmit) {
        ApiClient.preparation(requireContext())
                .createExpense(activityId, new CreateExpenseRequest(amount, desc, evidenceUrl))
                .enqueue(new Callback<ApiResponse<ExpenseDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ExpenseDto>> call,
                            Response<ApiResponse<ExpenseDto>> resp) {
                        pb.setVisibility(View.GONE);
                        btnSubmit.setEnabled(true);
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            toast("Đã gửi chi phí");
                            dialog.dismiss();
                            loadExpenses();
                            loadDashboard();
                        } else {
                            toast(resp.body() != null ? resp.body().getMessage() : ("HTTP " + resp.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ExpenseDto>> call, Throwable t) {
                        pb.setVisibility(View.GONE);
                        btnSubmit.setEnabled(true);
                        toast("Lỗi mạng: " + (t.getMessage() != null ? t.getMessage() : ""));
                    }
                });
    }

    private void showLoading(boolean show) {
        if (progress != null)
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void toast(String msg) {
        if (!isAdded())
            return;
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private String formatMoney(String amount) {
        if (amount == null || amount.trim().isEmpty())
            return "-";
        try {
            BigDecimal v = new BigDecimal(amount);
            return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(v);
        } catch (Exception e) {
            return amount;
        }
    }
}
