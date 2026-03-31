package com.example.campuslife.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.utils.StatusBadgeHelper;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.AdminDecisionAllocationAdjustmentRequest;
import com.example.campuslife.entity.preparation.AllocationAdjustmentRequestDto;
import com.example.campuslife.entity.preparation.AllocationAdjustmentSourcePlanDto;
import com.example.campuslife.entity.preparation.AllocationAdjustmentSourceRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllocationAdjustmentAdapter extends RecyclerView.Adapter<AllocationAdjustmentAdapter.ViewHolder> {
    private final Context context;
    private final List<AllocationAdjustmentRequestDto> list;
    private final NumberFormat currencyFormatter;
    private final boolean isAdmin;
    private final long activityId;

    public AllocationAdjustmentAdapter(Context context, List<AllocationAdjustmentRequestDto> list, boolean isAdmin, long activityId) {
        this.context = context;
        this.list = list;
        this.isAdmin = isAdmin;
        this.activityId = activityId;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_allocation_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AllocationAdjustmentRequestDto item = list.get(position);

        holder.tvTaskTitle.setText(item.taskTitle != null ? item.taskTitle : "Task #" + item.taskId);
        String byName = item.requestedByName != null ? item.requestedByName : item.createdByName;
        holder.tvCreatedByName.setText(byName != null ? "Bởi: " + byName : "Khuyết danh");
        holder.tvDescription.setText(item.description != null ? item.description : "Không có lý do.");

        if (item.createdAt != null) {
            String dateFormatted = item.createdAt;
            try {
                java.util.Date date = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).parse(item.createdAt);
                dateFormatted = new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy", java.util.Locale.getDefault()).format(date);
            } catch (Exception ignored) {}
            holder.tvDate.setText(dateFormatted);
        } else {
            holder.tvDate.setText("");
        }

        if (item.amount != null) {
            try {
                holder.tvAmount.setText("+ " + currencyFormatter.format(item.amount.doubleValue()));
            } catch (Exception e) {
                holder.tvAmount.setText("+ " + item.amount + "đ");
            }
        } else {
            holder.tvAmount.setText("0đ");
        }

        String status = item.status != null ? item.status : "UNKNOWN";
        StatusBadgeHelper.applyAllocationAdjStatus(holder.tvStatusBadge, status);
        holder.layoutActions.setVisibility("PENDING".equals(status) && isAdmin ? View.VISIBLE : View.GONE);

        holder.btnApproveAllocation.setOnClickListener(v -> {
            if (!isAdmin || !"PENDING".equals(item.status)) return;
            fetchAutoSplitAndShowDialog(item, position);
        });

        holder.btnRejectAllocation.setOnClickListener(v -> {
            if (!isAdmin || !"PENDING".equals(item.status)) return;
            AdminDecisionAllocationAdjustmentRequest body = new AdminDecisionAllocationAdjustmentRequest(false, new ArrayList<>());
            submitDecision(item.id, body, position, "REJECTED", null);
        });
    }

    private void fetchAutoSplitAndShowDialog(AllocationAdjustmentRequestDto item, int position) {
        ApiClient.preparation(context).getAllocationAdjustmentSourcePlan(item.id).enqueue(new Callback<ApiResponse<List<AllocationAdjustmentSourcePlanDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AllocationAdjustmentSourcePlanDto>>> call, Response<ApiResponse<List<AllocationAdjustmentSourcePlanDto>>> autoSplitResponse) {
                ApiClient.preparation(context).getActivityBudget(activityId).enqueue(new Callback<ApiResponse<com.example.campuslife.entity.preparation.ActivityBudgetDto>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<com.example.campuslife.entity.preparation.ActivityBudgetDto>> call2, Response<ApiResponse<com.example.campuslife.entity.preparation.ActivityBudgetDto>> budgetResponse) {
                        List<AllocationAdjustmentSourcePlanDto> plan = new ArrayList<>();
                        if (autoSplitResponse.isSuccessful() && autoSplitResponse.body() != null && autoSplitResponse.body().isStatus() && autoSplitResponse.body().getData() != null) {
                            plan = autoSplitResponse.body().getData();
                        }

                        List<com.example.campuslife.entity.preparation.BudgetCategoryDto> categories = new ArrayList<>();
                        if (budgetResponse.isSuccessful() && budgetResponse.body() != null && budgetResponse.body().isStatus() && budgetResponse.body().getData() != null) {
                            if (budgetResponse.body().getData().categories != null) {
                                categories = budgetResponse.body().getData().categories;
                            }
                        }

                        showAdminDecisionDialog(item, position, plan, categories);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<com.example.campuslife.entity.preparation.ActivityBudgetDto>> call2, Throwable t) {
                        Toast.makeText(context, "Lỗi mạng lấy danh sách ví", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AllocationAdjustmentSourcePlanDto>>> call, Throwable t) {
                Toast.makeText(context, "Lỗi mạng khi gọi Auto Split", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAdminDecisionDialog(AllocationAdjustmentRequestDto item, int position, List<AllocationAdjustmentSourcePlanDto> plan, List<com.example.campuslife.entity.preparation.BudgetCategoryDto> categories) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_admin_allocation_adj, null);
        dialog.setContentView(view);
        
        dialog.setOnShowListener(d -> {
            com.google.android.material.bottomsheet.BottomSheetDialog bsd = (com.google.android.material.bottomsheet.BottomSheetDialog) d;
            View bottomSheetInternal = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetInternal != null) {
                com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheetInternal).setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        TextView tvDetailReason = view.findViewById(R.id.tvDetailReason);
        RecyclerView rvSourcePlan = view.findViewById(R.id.rvSourcePlan);
        MaterialButton btnApproveSplit = view.findViewById(R.id.btnApproveSplit);
        MaterialButton btnRejectAdj = view.findViewById(R.id.btnRejectAdj);
        android.widget.ImageButton btnClose = view.findViewById(R.id.btnClose);

        android.widget.RadioGroup rgApprovalMode = view.findViewById(R.id.rgApprovalMode);
        android.widget.RadioButton rbAutoSplit = view.findViewById(R.id.rbAutoSplit);
        android.widget.RadioButton rbManual = view.findViewById(R.id.rbManual);
        LinearLayout layoutAutoSplit = view.findViewById(R.id.layoutAutoSplit);
        LinearLayout layoutManualSelect = view.findViewById(R.id.layoutManualSelect);
        LinearLayout containerManualWallets = view.findViewById(R.id.containerManualWallets);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        if (item.description != null && tvDetailReason != null) {
            tvDetailReason.setText(item.description);
        }

        // 1. Setup Auto Split Layout
        if (plan == null || plan.isEmpty()) {
            if (rvSourcePlan != null) rvSourcePlan.setVisibility(View.GONE);
            if (rbAutoSplit != null) rbAutoSplit.setEnabled(false);
            if (rgApprovalMode != null && rbManual != null) {
                rgApprovalMode.check(R.id.rbManual);
            }
        } else {
            if (rvSourcePlan != null) {
                rvSourcePlan.setVisibility(View.VISIBLE);
                rvSourcePlan.setLayoutManager(new LinearLayoutManager(context));
                rvSourcePlan.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    @NonNull
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        TextView tv = new TextView(context);
                        tv.setLayoutParams(new RecyclerView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        tv.setPadding(0, 16, 0, 16);
                        tv.setTextColor(Color.parseColor("#0F172A"));
                        tv.setTextSize(16);
                        return new RecyclerView.ViewHolder(tv) {};
                    }
    
                    @Override
                    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
                        AllocationAdjustmentSourcePlanDto p = plan.get(pos);
                        String amtStr = p.amount != null ? currencyFormatter.format(p.amount.doubleValue()) : "0đ";
                        ((TextView) holder.itemView).setText("- Rút từ ví " + p.categoryName + ": " + amtStr);
                    }
    
                    @Override
                    public int getItemCount() {
                        return plan.size();
                    }
                });
            }
        }

        // 2. Setup Manual Select Layout
        List<com.example.campuslife.entity.preparation.BudgetCategoryDto> finalCategories = categories != null ? categories : new ArrayList<>();
        List<android.util.Pair<android.widget.Spinner, android.widget.EditText>> manualInputFields = new ArrayList<>();
        TextView btnAddWalletRow = view.findViewById(R.id.btnAddWalletRow);

        if (containerManualWallets != null && !finalCategories.isEmpty()) {
            containerManualWallets.removeAllViews();
            
            List<String> categoryDisplayNames = new ArrayList<>();
            for (com.example.campuslife.entity.preparation.BudgetCategoryDto c : finalCategories) {
                String remaining = "0đ";
                if (c.remainingAmount != null) {
                    try { remaining = currencyFormatter.format(Double.parseDouble(c.remainingAmount)); } catch (Exception e) {}
                }
                categoryDisplayNames.add(c.name + " (" + remaining + ")");
            }

            addManualWalletRow(context, containerManualWallets, categoryDisplayNames, manualInputFields);

            if (btnAddWalletRow != null) {
                btnAddWalletRow.setOnClickListener(v -> {
                    addManualWalletRow(context, containerManualWallets, categoryDisplayNames, manualInputFields);
                });
            }
        }

        if (finalCategories.isEmpty() && rbManual != null) {
            rbManual.setEnabled(false);
        }

        // 3. Setup Toggle Logic
        if (rgApprovalMode != null) {
            rgApprovalMode.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbAutoSplit) {
                    if (layoutAutoSplit != null) layoutAutoSplit.setVisibility(View.VISIBLE);
                    if (layoutManualSelect != null) layoutManualSelect.setVisibility(View.GONE);
                    if (btnApproveSplit != null) btnApproveSplit.setEnabled(plan != null && !plan.isEmpty());
                } else if (checkedId == R.id.rbManual) {
                    if (layoutAutoSplit != null) layoutAutoSplit.setVisibility(View.GONE);
                    if (layoutManualSelect != null) layoutManualSelect.setVisibility(View.VISIBLE);
                    if (btnApproveSplit != null) btnApproveSplit.setEnabled(!finalCategories.isEmpty());
                }
            });

            // Trigger initial state
            int initialCheck = rgApprovalMode.getCheckedRadioButtonId();
            if (initialCheck == R.id.rbAutoSplit) {
                if (btnApproveSplit != null) btnApproveSplit.setEnabled(plan != null && !plan.isEmpty());
                if (layoutAutoSplit != null) layoutAutoSplit.setVisibility(View.VISIBLE);
                if (layoutManualSelect != null) layoutManualSelect.setVisibility(View.GONE);
            } else {
                if (btnApproveSplit != null) btnApproveSplit.setEnabled(!finalCategories.isEmpty());
                if (layoutAutoSplit != null) layoutAutoSplit.setVisibility(View.GONE);
                if (layoutManualSelect != null) layoutManualSelect.setVisibility(View.VISIBLE);
            }
        }

        if (btnApproveSplit != null) {
            btnApproveSplit.setOnClickListener(v -> {
                boolean isAuto = rbAutoSplit != null && rbAutoSplit.isChecked();
                List<AllocationAdjustmentSourceRequest> sources = new ArrayList<>();
                
                if (isAuto) {
                    for (AllocationAdjustmentSourcePlanDto p : plan) {
                        sources.add(new AllocationAdjustmentSourceRequest(p.categoryId, p.amount.toString()));
                    }
                } else {
                    double totalEntered = 0;
                    java.util.Map<Long, Double> groupedSources = new java.util.HashMap<>();
                    for (android.util.Pair<android.widget.Spinner, android.widget.EditText> pair : manualInputFields) {
                        String val = pair.second.getText().toString().trim();
                        if (!val.isEmpty()) {
                            try {
                                double amt = Double.parseDouble(val);
                                if (amt > 0) {
                                    int pos = pair.first.getSelectedItemPosition();
                                    if (pos >= 0 && pos < finalCategories.size()) {
                                        Long catId = finalCategories.get(pos).id;
                                        groupedSources.put(catId, groupedSources.getOrDefault(catId, 0.0) + amt);
                                        totalEntered += amt;
                                    }
                                }
                            } catch (Exception e) {}
                        }
                    }
                    if (groupedSources.isEmpty() || totalEntered <= 0) {
                        Toast.makeText(context, "Vui lòng nhập số tiền hợp lệ cho ít nhất 1 ví", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (java.util.Map.Entry<Long, Double> entry : groupedSources.entrySet()) {
                        sources.add(new AllocationAdjustmentSourceRequest(entry.getKey(), String.format(java.util.Locale.US, "%.0f", entry.getValue())));
                    }
                }
                
                AdminDecisionAllocationAdjustmentRequest body = new AdminDecisionAllocationAdjustmentRequest(true, sources);
                submitDecision(item.id, body, position, "APPROVED", dialog);
            });
        }

        if (btnRejectAdj != null) {
            btnRejectAdj.setOnClickListener(v -> {
                AdminDecisionAllocationAdjustmentRequest body = new AdminDecisionAllocationAdjustmentRequest(false, new ArrayList<>());
                submitDecision(item.id, body, position, "REJECTED", dialog);
            });
        }

        dialog.show();
    }

    private void submitDecision(Long id, AdminDecisionAllocationAdjustmentRequest body, int position, String newStatus, BottomSheetDialog dialog) {
        ApiClient.preparation(context).adminDecisionAllocationAdjustment(id, body).enqueue(new Callback<ApiResponse<AllocationAdjustmentRequestDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<AllocationAdjustmentRequestDto>> call, Response<ApiResponse<AllocationAdjustmentRequestDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    list.get(position).status = newStatus;
                    notifyItemChanged(position);
                    if (dialog != null) dialog.dismiss();
                    Toast.makeText(context, "Đã xử lý quyết định Cấp bù", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Lỗi server!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AllocationAdjustmentRequestDto>> call, Throwable t) {
                Toast.makeText(context, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addManualWalletRow(Context context, LinearLayout container, List<String> displayNames, List<android.util.Pair<android.widget.Spinner, android.widget.EditText>> manualInputFields) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setBackgroundResource(R.drawable.bg_reason_bordered);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 0, 0, 24);
        row.setLayoutParams(rowParams);
        row.setPadding(32, 24, 32, 32);

        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        android.widget.Spinner spManualCategory = new android.widget.Spinner(context);
        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(context, android.R.layout.simple_spinner_item, displayNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spManualCategory.setAdapter(spinnerAdapter);
        header.addView(spManualCategory, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        android.widget.ImageButton btnRemove = new android.widget.ImageButton(context);
        btnRemove.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        btnRemove.setBackgroundColor(Color.TRANSPARENT);
        btnRemove.setColorFilter(Color.parseColor("#EF4444"));
        int btnSize = (int) (32 * context.getResources().getDisplayMetrics().density);
        header.addView(btnRemove, new LinearLayout.LayoutParams(btnSize, btnSize));

        android.widget.EditText etAmount = new android.widget.EditText(context);
        etAmount.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        etAmount.setHint("Nhập số tiền rút (VNĐ)");
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etAmount.setTextSize(14f);
        etAmount.setPadding(0, 16, 0, 0);
        etAmount.setBackground(null);

        row.addView(header);
        row.addView(etAmount);
        container.addView(row);

        android.util.Pair<android.widget.Spinner, android.widget.EditText> pair = new android.util.Pair<>(spManualCategory, etAmount);
        manualInputFields.add(pair);

        btnRemove.setOnClickListener(v -> {
            if (manualInputFields.size() > 1) {
                container.removeView(row);
                manualInputFields.remove(pair);
            } else {
                Toast.makeText(context, "Phải có ít nhất 1 ví", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle, tvAmount, tvDescription, tvCreatedByName, tvDate, tvStatusBadge;
        LinearLayout layoutActions;
        MaterialButton btnApproveAllocation, btnRejectAllocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCreatedByName = itemView.findViewById(R.id.tvCreatedByName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnApproveAllocation = itemView.findViewById(R.id.btnApproveAllocation);
            btnRejectAllocation = itemView.findViewById(R.id.btnRejectAllocation);
        }
    }
}
