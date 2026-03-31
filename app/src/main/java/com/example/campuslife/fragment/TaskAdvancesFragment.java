package com.example.campuslife.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.CreateFundAdvanceRequest;
import com.example.campuslife.entity.preparation.FundAdvanceDebtDto;
import com.example.campuslife.entity.preparation.FundAdvanceDto;
import com.example.campuslife.entity.preparation.FundAdvanceSourceSuggestionDto;
import com.example.campuslife.entity.preparation.PreparationTaskMemberDto;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskAdvancesFragment extends Fragment {

    private static final String ARG_TASK_ID = "taskId";
    private static final String ARG_ACTIVITY_ID = "activityId";

    private long taskId;
    private long activityId;

    private final DecimalFormat df = new DecimalFormat("#,###");

    private ProgressBar progressAdvances;
    private TextView tvEmpty;
    private RecyclerView rvAdvances;
    private AdvancesAdapter adapter;
    private final List<FundAdvanceDto> advanceList = new ArrayList<>();

    public static TaskAdvancesFragment newInstance(long taskId, long activityId) {
        TaskAdvancesFragment f = new TaskAdvancesFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_TASK_ID, taskId);
        b.putLong(ARG_ACTIVITY_ID, activityId);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskId = getArguments().getLong(ARG_TASK_ID, -1);
            activityId = getArguments().getLong(ARG_ACTIVITY_ID, -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_advances, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressAdvances = view.findViewById(R.id.progressAdvances);
        tvEmpty = view.findViewById(R.id.tvEmptyAdvances);
        rvAdvances = view.findViewById(R.id.rvAdvances);

        adapter = new AdvancesAdapter(advanceList, df);
        rvAdvances.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAdvances.setAdapter(adapter);

        view.findViewById(R.id.btnCreateAdvance).setOnClickListener(v -> loadMembersAndShowDialog());

        load();
    }

    private void load() {
        progressAdvances.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.preparation(requireContext()).listFundAdvances(taskId)
                .enqueue(new Callback<ApiResponse<List<FundAdvanceDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<FundAdvanceDto>>> call,
                            Response<ApiResponse<List<FundAdvanceDto>>> resp) {
                        progressAdvances.setVisibility(View.GONE);
                        if (!isAdded()) return;
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            advanceList.clear();
                            List<FundAdvanceDto> data = resp.body().getData();
                            if (data != null) advanceList.addAll(data);
                            adapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(advanceList.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<List<FundAdvanceDto>>> call, Throwable t) {
                        progressAdvances.setVisibility(View.GONE);
                        if (!isAdded()) return;
                        toast("Lỗi mạng");
                    }
                });
    }

    private void loadMembersAndShowDialog() {
        ApiClient.preparation(requireContext()).getTaskMembers(taskId)
                .enqueue(new Callback<ApiResponse<List<PreparationTaskMemberDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<PreparationTaskMemberDto>>> call,
                            Response<ApiResponse<List<PreparationTaskMemberDto>>> resp) {
                        if (!isAdded()) return;
                        List<PreparationTaskMemberDto> members = new ArrayList<>();
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()
                                && resp.body().getData() != null) {
                            members = resp.body().getData();
                        }
                        showCreateAdvanceDialog(members);
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<List<PreparationTaskMemberDto>>> call, Throwable t) {
                        if (!isAdded()) return;
                        showCreateAdvanceDialog(new ArrayList<>());
                    }
                });
    }

    private void showCreateAdvanceDialog(List<PreparationTaskMemberDto> members) {
        if (!isAdded()) return;
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_dialog_create_fund_advance, null, false);
        dialog.setContentView(v);

        Spinner spMember = v.findViewById(R.id.spMember);
        TextInputEditText etAmount = v.findViewById(R.id.etAdvanceAmount);
        TextView tvDebtWarning = v.findViewById(R.id.tvDebtWarning);
        TextView tvSourceInfo = v.findViewById(R.id.tvSourceInfo);
        MaterialButton btnSubmit = v.findViewById(R.id.btnSubmitAdvance);
        MaterialButton btnCancel = v.findViewById(R.id.btnCancelAdvance);

        if (btnCancel != null) btnCancel.setOnClickListener(x -> dialog.dismiss());

        // Setup member spinner
        final List<PreparationTaskMemberDto> finalMembers = members;
        String[] memberNames = new String[members.size()];
        for (int i = 0; i < members.size(); i++) memberNames[i] = members.get(i).studentName;

        if (spMember != null && memberNames.length > 0) {
            ArrayAdapter<String> ad = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, memberNames);
            spMember.setAdapter(ad);

            spMember.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    Long studentId = finalMembers.get(position).studentId;
                    if (studentId != null) checkDebt(studentId, tvDebtWarning);
                }
                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        }

        // Amount change → suggest source
        final Long[] selectedCategoryId = {null};
        if (etAmount != null) {
            etAmount.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                    String amtStr = s.toString().trim();
                    if (!amtStr.isEmpty()) {
                        suggestSource(amtStr, tvSourceInfo, selectedCategoryId);
                    }
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(x -> {
                String amtStr = etAmount != null ? etAmount.getText().toString().trim() : "";
                int memberPos = spMember != null ? spMember.getSelectedItemPosition() : -1;
                if (amtStr.isEmpty() || memberPos < 0 || selectedCategoryId[0] == null) {
                    toast("Vui lòng nhập đầy đủ thông tin");
                    return;
                }
                Long studentId = finalMembers.get(memberPos).studentId;
                CreateFundAdvanceRequest req = new CreateFundAdvanceRequest();
                req.studentId = studentId;
                req.categoryId = selectedCategoryId[0];
                req.amount = amtStr;
                submitAdvance(dialog, req);
            });
        }
        dialog.show();
    }

    private void checkDebt(Long studentId, TextView tvDebtWarning) {
        ApiClient.preparation(requireContext()).listFundAdvanceDebts(activityId, studentId)
                .enqueue(new Callback<ApiResponse<List<FundAdvanceDebtDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<FundAdvanceDebtDto>>> call,
                            Response<ApiResponse<List<FundAdvanceDebtDto>>> resp) {
                        if (!isAdded()) return;
                        if (tvDebtWarning == null) return;
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()
                                && resp.body().getData() != null) {
                            List<FundAdvanceDebtDto> debts = resp.body().getData();
                            BigDecimal total = BigDecimal.ZERO;
                            for (FundAdvanceDebtDto d : debts) {
                                if (d.holdingAmount != null) {
                                    try {
                                        total = total.add(new BigDecimal(d.holdingAmount));
                                    } catch (Exception ignored) {}
                                }
                            }
                            if (total.compareTo(BigDecimal.ZERO) > 0) {
                                tvDebtWarning.setVisibility(View.VISIBLE);
                                tvDebtWarning.setText("⚠️ Thành viên đang giữ " + df.format(total) + " đ chưa hoàn");
                            } else {
                                tvDebtWarning.setVisibility(View.GONE);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<List<FundAdvanceDebtDto>>> call, Throwable t) {}
                });
    }

    private void suggestSource(String amount, TextView tvInfo, Long[] selectedCategoryId) {
        ApiClient.preparation(requireContext()).suggestFundAdvanceSources(taskId, amount)
                .enqueue(new Callback<ApiResponse<List<FundAdvanceSourceSuggestionDto>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<FundAdvanceSourceSuggestionDto>>> call,
                            Response<ApiResponse<List<FundAdvanceSourceSuggestionDto>>> resp) {
                        if (!isAdded() || tvInfo == null) return;
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            List<FundAdvanceSourceSuggestionDto> list = resp.body().getData();
                            if (list == null || list.isEmpty()) {
                                tvInfo.setText("Không có nguồn ứng khả dụng");
                                selectedCategoryId[0] = null;
                                return;
                            }
                            FundAdvanceSourceSuggestionDto best = list.get(0);
                            for (FundAdvanceSourceSuggestionDto s : list) {
                                if (s.maxAdvanceAmount != null && best.maxAdvanceAmount != null
                                        && s.maxAdvanceAmount.compareTo(best.maxAdvanceAmount) > 0) {
                                    best = s;
                                }
                            }
                            selectedCategoryId[0] = best.categoryId;
                            tvInfo.setText("Nguồn gợi ý: " + best.categoryName
                                    + " (tối đa " + df.format(best.maxAdvanceAmount) + " đ)");
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<List<FundAdvanceSourceSuggestionDto>>> call, Throwable t) {}
                });
    }

    private void submitAdvance(BottomSheetDialog dialog, CreateFundAdvanceRequest req) {
        ApiClient.preparation(requireContext()).requestFundAdvance(taskId, req)
                .enqueue(new Callback<ApiResponse<FundAdvanceDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<FundAdvanceDto>> call,
                            Response<ApiResponse<FundAdvanceDto>> resp) {
                        if (!isAdded()) return;
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isStatus()) {
                            toast("Đã tạo yêu cầu tạm ứng");
                            dialog.dismiss();
                            load();
                        } else {
                            toast(resp.body() != null ? resp.body().getMessage() : "Lỗi khi tạo tạm ứng");
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<FundAdvanceDto>> call, Throwable t) {
                        if (!isAdded()) return;
                        toast("Lỗi mạng: " + t.getMessage());
                    }
                });
    }

    private void toast(String msg) {
        if (!isAdded()) return;
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    // --- Inner adapter ---
    private static class AdvancesAdapter extends RecyclerView.Adapter<AdvancesAdapter.VH> {
        private final List<FundAdvanceDto> list;
        private final DecimalFormat df;

        AdvancesAdapter(List<FundAdvanceDto> list, DecimalFormat df) {
            this.list = list;
            this.df = df;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_fund_advance, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            FundAdvanceDto a = list.get(position);
            if (h.tvStudentName != null) {
                String amt = a.amount != null ? df.format(a.amount) + " đ" : "0 đ";
                h.tvStudentName.setText((a.studentName != null ? a.studentName : "") + " • " + amt);
            }
            if (h.tvCategoryTaskInfo != null) {
                h.tvCategoryTaskInfo.setText(
                        "Ví: " + (a.categoryName != null ? a.categoryName : "") +
                        " • Người tạo: " + (a.requestedByName != null ? a.requestedByName : ""));
            }
            if (h.tvDate != null) {
                h.tvDate.setText(a.createdAt != null ? a.createdAt.split("T")[0] : "");
            }
            if (h.tvAmountInfo != null) {
                String rem = a.remainingAmount != null ? df.format(a.remainingAmount) + " đ" : "0 đ";
                h.tvAmountInfo.setText("Còn lại: " + rem);
            }
            // Hide admin-only action buttons
            if (h.layoutRequestedActions != null) h.layoutRequestedActions.setVisibility(View.GONE);
            if (h.layoutHoldingActions != null) h.layoutHoldingActions.setVisibility(View.GONE);

            // Status badge
            if (h.tvStatus != null) {
                h.tvStatus.setVisibility(View.VISIBLE);
                com.example.campuslife.utils.StatusBadgeHelper.applyFundAdvanceStatus(h.tvStatus, a.status != null ? a.status : "REQUESTED");
            }
        }


        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvStudentName, tvCategoryTaskInfo, tvDate, tvAmountInfo, tvStatus;
            View layoutRequestedActions, layoutHoldingActions;

            VH(View v) {
                super(v);
                tvStudentName = v.findViewById(R.id.tvStudentName);
                tvCategoryTaskInfo = v.findViewById(R.id.tvCategoryTaskInfo);
                tvDate = v.findViewById(R.id.tvDate);
                tvAmountInfo = v.findViewById(R.id.tvAmountInfo);
                tvStatus = v.findViewById(R.id.tvDebtWarning);
                layoutRequestedActions = v.findViewById(R.id.layoutRequestedActions);
                layoutHoldingActions = v.findViewById(R.id.layoutHoldingActions);
            }
        }
    }
}
