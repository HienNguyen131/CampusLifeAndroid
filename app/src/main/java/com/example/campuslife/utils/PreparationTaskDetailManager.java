package com.example.campuslife.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campuslife.R;
import com.example.campuslife.adapter.FundAdvanceAdapter;
import com.example.campuslife.adapter.PreparationTaskMemberAdapter;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.preparation.ActivityBudgetDto;
import com.example.campuslife.entity.preparation.AllocateTaskAmountRequest;
import com.example.campuslife.entity.preparation.ApproveTaskCompletionRequest;
import com.example.campuslife.entity.preparation.BudgetCategoryDto;
import com.example.campuslife.entity.preparation.CreateFundAdvanceRequest;
import com.example.campuslife.entity.preparation.FundAdvanceDto;
import com.example.campuslife.entity.preparation.FundAdvanceSourceSuggestionDto;
import com.example.campuslife.entity.preparation.OrganizerDto;
import com.example.campuslife.entity.preparation.PreparationTaskDto;
import com.example.campuslife.entity.preparation.PreparationTaskMemberDto;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreparationTaskDetailManager {

    private final Context context;
    private final long activityId;
    private final long currentStudentId; // -1L means Admin Mode
    private final PreparationTaskDto task;
    private final Runnable onDataChanged;

    private BottomSheetDialog detailDialog;
    private PreparationTaskMemberAdapter memberAdapter;
    private final List<PreparationTaskMemberDto> membersList = new ArrayList<>();
    
    // For Add Member Dialog
    private RecyclerView rvAvailableOrganizers;
    private TextView tvNoOrganizersLeft;
    private List<OrganizerDto> allOrganizers = new ArrayList<>();
    private List<OrganizerDto> filteredOrganizers = new ArrayList<>();
    private com.example.campuslife.adapter.PreparationTaskAddMemberAdapter addMemberAdapter;

    public PreparationTaskDetailManager(Context context, long activityId, long currentStudentId, PreparationTaskDto task, Runnable onDataChanged) {
        this.context = context;
        this.activityId = activityId;
        this.currentStudentId = currentStudentId;
        this.task = task;
        this.onDataChanged = onDataChanged;
    }

    public void show() {
        detailDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_task_detail, null);
        detailDialog.setContentView(view);

        // Bind Basic Task Data
        TextView tvTitle = view.findViewById(R.id.tvTaskTitle);
        TextView tvDesc = view.findViewById(R.id.tvTaskDescription);
        TextView tvStatus = view.findViewById(R.id.tvTaskStatus);
        TextView tvDeadline = view.findViewById(R.id.tvTaskDeadline);
        TextView tvFinancial = view.findViewById(R.id.tvFinancialTag);

        tvTitle.setText(task.title != null ? task.title : "Nhiệm vụ");
        tvDesc.setText(task.description != null ? task.description : "Chưa có mô tả.");
        
        String status = task.status != null ? task.status : "PENDING";
        StatusBadgeHelper.applyTaskStatus(tvStatus, status);

        if (task.deadline != null) {
            try {
                LocalDateTime dt = LocalDateTime.parse(task.deadline);
                tvDeadline.setText(dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } catch (Exception e) {
                tvDeadline.setText(task.deadline);
            }
        } else {
            tvDeadline.setText("Chưa có hạn");
        }

        if (task.isFinancial != null && task.isFinancial) {
            tvFinancial.setVisibility(View.VISIBLE);
            tvFinancial.setText(task.allocatedAmount != null ? task.allocatedAmount + " đ" : "Cần ngân sách");
        } else {
            tvFinancial.setVisibility(View.GONE);
        }

        // Setup Members Recycler
        RecyclerView rvTaskMembers = view.findViewById(R.id.rvTaskMembers);
        rvTaskMembers.setLayoutManager(new LinearLayoutManager(context));
        
        boolean isAdmin = (currentStudentId == -1L);
        memberAdapter = new PreparationTaskMemberAdapter(context, membersList, isAdmin, new PreparationTaskMemberAdapter.OnMemberActionListener() {
            @Override
            public void onPromoteLeader(PreparationTaskMemberDto member) {
                setLeaderAPI(member.studentId);
            }

            @Override
            public void onDemoteLeader(PreparationTaskMemberDto member) {
                demoteLeaderAPI(member.studentId);
            }

            @Override
            public void onRemoveMember(PreparationTaskMemberDto member) {
                removeMemberAPI(member.studentId);
            }
        });
        rvTaskMembers.setAdapter(memberAdapter);

        // Load actual members
        loadMembers();

        // Add Member Button
        ImageButton btnAddMember = view.findViewById(R.id.btnAddTaskMember);
        if (isAdmin) {
            btnAddMember.setVisibility(View.VISIBLE);
            btnAddMember.setOnClickListener(v -> showAddMemberDialog());
        } else {
            btnAddMember.setVisibility(View.GONE);
        }

        // Finance Section
        LinearLayout llFinanceSection = view.findViewById(R.id.llFinanceSection);
        if (Boolean.TRUE.equals(task.isFinancial)) {
            llFinanceSection.setVisibility(View.VISIBLE);
            TextView tvAllocatedAmount = view.findViewById(R.id.tvAllocatedAmount);
            MaterialButton btnAllocate = view.findViewById(R.id.btnAllocate);
            MaterialButton btnViewFundAdvances = view.findViewById(R.id.btnViewFundAdvances);
            
            String formattedAllocated = task.allocatedAmount != null ? String.format("%,dđ", task.allocatedAmount.longValue()) : "0đ";
            tvAllocatedAmount.setText(formattedAllocated);
            
            if (isAdmin) {
                btnAllocate.setVisibility(View.VISIBLE);
                btnAllocate.setOnClickListener(v -> showAllocateDialog());
                
                if (btnViewFundAdvances != null) {
                    btnViewFundAdvances.setVisibility(View.VISIBLE);
                    btnViewFundAdvances.setOnClickListener(v -> showFundAdvancesListDialog(isAdmin));
                }
            } else {
                btnAllocate.setVisibility(View.GONE);
                if (btnViewFundAdvances != null) {
                    btnViewFundAdvances.setVisibility(View.GONE);
                }
            }
        } else {
            llFinanceSection.setVisibility(View.GONE);
        }

        // Bottom Actions (Primary / Secondary)
        MaterialButton btnPrimary = view.findViewById(R.id.btnTaskActionPrimary);
        MaterialButton btnSecondary = view.findViewById(R.id.btnTaskActionSecondary);
        btnSecondary.setOnClickListener(v -> detailDialog.dismiss());

        setupActionButtons(btnPrimary, status, isAdmin);

        detailDialog.show();
    }

    private void setupActionButtons(MaterialButton btnPrimary, String status, boolean isAdmin) {
        if (isAdmin) {
            btnPrimary.setVisibility(View.VISIBLE);
            if ("COMPLETION_REQUESTED".equals(status)) {
                btnPrimary.setText("Phê duyệt");
                btnPrimary.setOnClickListener(v -> showApproveDialog());
            } else {
                btnPrimary.setVisibility(View.GONE);
            }
        } else {
            // Start by hiding, we'll update in loadMembers or if they are the explicit owner
            btnPrimary.setVisibility(View.GONE);
            boolean isOwner = task.assigneeId != null && task.assigneeId.equals(currentStudentId);
            if (isOwner) {
                if ("PENDING".equals(status)) {
                    btnPrimary.setVisibility(View.VISIBLE);
                    btnPrimary.setText("Nhận việc");
                    btnPrimary.setOnClickListener(v -> {
                        ApiClient.preparation(context).acceptTask(task.id).enqueue(new StatusCallback());
                    });
                } else if ("ACCEPTED".equals(status)) {
                    btnPrimary.setVisibility(View.VISIBLE);
                    btnPrimary.setText("Báo cáo hoàn thành");
                    btnPrimary.setOnClickListener(v -> {
                        ApiClient.preparation(context).requestCompleteTask(task.id).enqueue(new StatusCallback());
                    });
                }
            }
        }
    }

    private void updateMemberActionButtons(MaterialButton btnPrimary, String status, List<PreparationTaskMemberDto> members) {
        boolean isAdmin = (currentStudentId == -1L);
        boolean isOwner = task.assigneeId != null && task.assigneeId.equals(currentStudentId);
        boolean isMember = false;
        boolean isLeader = false;

        if (members != null) {
            for (PreparationTaskMemberDto m : members) {
                if (m.studentId != null && m.studentId.equals(currentStudentId)) {
                    isMember = true;
                    if ("LEADER".equalsIgnoreCase(m.role)) {
                        isLeader = true;
                    }
                    break;
                }
            }
        }

        if (!isAdmin && Boolean.TRUE.equals(task.isFinancial) && detailDialog != null) {
            MaterialButton btnFundAdvance = detailDialog.findViewById(R.id.btnFundAdvance);
            MaterialButton btnViewFundAdvances = detailDialog.findViewById(R.id.btnViewFundAdvances);
            if (btnFundAdvance != null) {
                if (isLeader) {
                    btnFundAdvance.setVisibility(View.VISIBLE);
                    btnFundAdvance.setOnClickListener(v -> showFundAdvanceDialog());
                    if (btnViewFundAdvances != null) {
                        btnViewFundAdvances.setVisibility(View.VISIBLE);
                        btnViewFundAdvances.setOnClickListener(v -> showFundAdvancesListDialog(isAdmin));
                    }
                } else {
                    btnFundAdvance.setVisibility(View.GONE);
                    if (btnViewFundAdvances != null) {
                        btnViewFundAdvances.setVisibility(View.GONE);
                    }
                }
            }
        }

        if (isAdmin) return; 

        if ("PENDING".equals(status)) {
            if (isOwner || isMember) {
                btnPrimary.setVisibility(View.VISIBLE);
                btnPrimary.setText(isOwner ? "Nhận việc" : "Xin tham gia");
                btnPrimary.setOnClickListener(v -> {
                    ApiClient.preparation(context).acceptTask(task.id).enqueue(new StatusCallback());
                });
            } else {
                btnPrimary.setVisibility(View.GONE);
            }
        } else if ("ACCEPTED".equals(status)) {
            if (isOwner || isLeader) {
                btnPrimary.setVisibility(View.VISIBLE);
                btnPrimary.setText("Báo cáo hoàn thành");
                btnPrimary.setOnClickListener(v -> {
                    ApiClient.preparation(context).requestCompleteTask(task.id).enqueue(new StatusCallback());
                });
            } else {
                btnPrimary.setVisibility(View.GONE);
            }
        } else {
            btnPrimary.setVisibility(View.GONE);
        }
    }

    private void loadMembers() {
        ApiClient.preparation(context).getTaskMembers(task.id).enqueue(new Callback<ApiResponse<List<PreparationTaskMemberDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PreparationTaskMemberDto>>> call, Response<ApiResponse<List<PreparationTaskMemberDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<PreparationTaskMemberDto> fetched = response.body().getData();
                    memberAdapter.updateData(fetched);
                    MaterialButton btnPrimary = detailDialog.findViewById(R.id.btnTaskActionPrimary);
                    if (btnPrimary != null) {
                        updateMemberActionButtons(btnPrimary, task.status, fetched);
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<PreparationTaskMemberDto>>> call, Throwable t) {}
        });
    }

    // ========== API CALLS FOR MEMBERS ==========
    private void setLeaderAPI(long studentId) {
        ApiClient.preparation(context).setTaskLeader(task.id, studentId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    loadMembers();
                    Toast.makeText(context, "Đã set Leader", Toast.LENGTH_SHORT).show();
                    if (onDataChanged != null) onDataChanged.run();
                } else {
                    Toast.makeText(context, "Lỗi phân quyền", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
        });
    }

    private void demoteLeaderAPI(long studentId) {
        ApiClient.preparation(context).demoteTaskLeader(task.id, studentId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    loadMembers();
                    Toast.makeText(context, "Đã tước quyền Leader", Toast.LENGTH_SHORT).show();
                    if (onDataChanged != null) onDataChanged.run();
                } else {
                    Toast.makeText(context, "Lỗi phân quyền", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
        });
    }

    private void removeMemberAPI(long studentId) {
        ApiClient.preparation(context).removeTaskMember(task.id, studentId).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    loadMembers();
                    Toast.makeText(context, "Đã xóa thành viên", Toast.LENGTH_SHORT).show();
                    if (onDataChanged != null) onDataChanged.run();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
        });
    }

    private void showAddMemberDialog() {
        BottomSheetDialog addDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_add_task_member, null);
        addDialog.setContentView(view);
        if (addDialog.getWindow() != null) {
            addDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextInputEditText etSearch = view.findViewById(R.id.etSearch);
        rvAvailableOrganizers = view.findViewById(R.id.rvAvailableOrganizers);
        tvNoOrganizersLeft = view.findViewById(R.id.tvNoOrganizersLeft);
        MaterialButton btnClose = view.findViewById(R.id.btnClose);

        rvAvailableOrganizers.setLayoutManager(new LinearLayoutManager(context));
        addMemberAdapter = new com.example.campuslife.adapter.PreparationTaskAddMemberAdapter(context, filteredOrganizers, organizer -> {
            ApiClient.preparation(context).addTaskMember(task.id, organizer.getStudentId()).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        Toast.makeText(context, "Đã thêm thành viên", Toast.LENGTH_SHORT).show();
                        loadMembers();
                        addDialog.dismiss();
                    } else {
                        Toast.makeText(context, "Thêm lỗi", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {}
            });
        });
        rvAvailableOrganizers.setAdapter(addMemberAdapter);

        // Fetch Organizers from EVENT, then filter out those already in task
        ApiClient.preparation(context).getOrganizers(activityId).enqueue(new Callback<ApiResponse<List<OrganizerDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<OrganizerDto>>> call, Response<ApiResponse<List<OrganizerDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    allOrganizers.clear();
                    List<OrganizerDto> APIList = response.body().getData();
                    // Filter logic: Only keep organizers NOT in membersList
                    if (APIList != null) {
                        for (OrganizerDto org : APIList) {
                            boolean exists = false;
                            for (PreparationTaskMemberDto mem : membersList) {
                                if (mem.studentId != null && mem.studentId.equals(org.getStudentId())) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) allOrganizers.add(org);
                        }
                    }
                    filterAddMember("");
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<OrganizerDto>>> call, Throwable t) {}
        });

        // Add search logic
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterAddMember(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnClose.setOnClickListener(v -> addDialog.dismiss());
        addDialog.show();
    }

    private void filterAddMember(String query) {
        filteredOrganizers.clear();
        String q = query.toLowerCase();
        for (OrganizerDto org : allOrganizers) {
            String name = org.getFullName() != null ? org.getFullName().toLowerCase() : "";
            if (name.contains(q)) {
                filteredOrganizers.add(org);
            }
        }
        addMemberAdapter.notifyDataSetChanged();
        if (filteredOrganizers.isEmpty()) {
            tvNoOrganizersLeft.setVisibility(View.VISIBLE);
        } else {
            tvNoOrganizersLeft.setVisibility(View.GONE);
        }
    }

    // ========== APPROVE TASK DIALOG ==========
    private void showApproveDialog() {
        Dialog approveDialog = new Dialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_approve_task, null);
        approveDialog.setContentView(view);
        if (approveDialog.getWindow() != null) {
            approveDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        MaterialButton btnApprove = view.findViewById(R.id.btnApproveTask);
        MaterialButton btnReject = view.findViewById(R.id.btnRejectTask);

        TextInputEditText etApprovalNote = view.findViewById(R.id.etApprovalNote);

        btnApprove.setOnClickListener(v -> {
            ApproveTaskCompletionRequest req = new ApproveTaskCompletionRequest();
            req.approved = true;
            if (etApprovalNote != null && etApprovalNote.getText() != null) {
                req.approvalNote = etApprovalNote.getText().toString();
            }
            ApiClient.preparation(context).approveTaskCompletion(task.id, req).enqueue(new StatusCallback());
            approveDialog.dismiss();
            detailDialog.dismiss();
        });

        btnReject.setOnClickListener(v -> {
            ApproveTaskCompletionRequest req = new ApproveTaskCompletionRequest();
            req.approved = false;
            if (etApprovalNote != null && etApprovalNote.getText() != null) {
                req.approvalNote = etApprovalNote.getText().toString();
            }
            ApiClient.preparation(context).approveTaskCompletion(task.id, req).enqueue(new StatusCallback());
            approveDialog.dismiss();
            detailDialog.dismiss();
        });

        approveDialog.show();
    }

    private class StatusCallback implements Callback<ApiResponse<PreparationTaskDto>> {
        @Override
        public void onResponse(Call<ApiResponse<PreparationTaskDto>> call, Response<ApiResponse<PreparationTaskDto>> response) {
            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                Toast.makeText(context, "Thao tác thành công", Toast.LENGTH_SHORT).show();
                if (detailDialog != null) detailDialog.dismiss();
                if (onDataChanged != null) onDataChanged.run();
            } else {
                Toast.makeText(context, "Lỗi từ máy chủ", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<ApiResponse<PreparationTaskDto>> call, Throwable t) {
            Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
        }
    }

    // ========== FUND ADVANCE (TẠM ỨNG) ==========
    private void showFundAdvanceDialog() {
        BottomSheetDialog advanceDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dialog_fund_advance, null);
        advanceDialog.setContentView(view);

        AutoCompleteTextView actStudent = view.findViewById(R.id.actStudent);
        AutoCompleteTextView actAdvanceCategory = view.findViewById(R.id.actAdvanceCategory);
        TextInputEditText etAdvanceAmount = view.findViewById(R.id.etAdvanceAmount);
        MaterialButton btnSubmitAdvance = view.findViewById(R.id.btnSubmitAdvance);
        MaterialButton btnCancelAdvance = view.findViewById(R.id.btnCancelAdvance);

        btnCancelAdvance.setOnClickListener(v -> advanceDialog.dismiss());

        // 1. Setup Student Dropdown (From membersList)
        List<String> studentNames = new ArrayList<>();
        List<Long> studentIds = new ArrayList<>();
        if (membersList != null) {
            for (PreparationTaskMemberDto m : membersList) {
                studentNames.add(m.studentName != null ? m.studentName : "Không rõ");
                studentIds.add(m.studentId);
            }
        }
        ArrayAdapter<String> studentAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, studentNames);
        actStudent.setAdapter(studentAdapter);

        // 2. Setup Category Suggestion Dropdown (API Call)
        List<FundAdvanceSourceSuggestionDto> categorySuggestions = new ArrayList<>();
        List<String> categoryDisplays = new ArrayList<>();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, categoryDisplays);
        actAdvanceCategory.setAdapter(categoryAdapter);

        ApiClient.preparation(context).suggestFundAdvanceSources(task.id, null).enqueue(new Callback<ApiResponse<List<FundAdvanceSourceSuggestionDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FundAdvanceSourceSuggestionDto>>> call, Response<ApiResponse<List<FundAdvanceSourceSuggestionDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<FundAdvanceSourceSuggestionDto> data = response.body().getData();
                    if (data != null) {
                        for (FundAdvanceSourceSuggestionDto dto : data) {
                            categorySuggestions.add(dto);
                            String display = dto.categoryName + " (Còn " + String.format("%,dđ", dto.maxAdvanceAmount.longValue()) + ")";
                            categoryDisplays.add(display);
                        }
                        categoryAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<FundAdvanceSourceSuggestionDto>>> call, Throwable t) {
            }
        });

        btnSubmitAdvance.setOnClickListener(v -> {
            int selectedStudentIdx = -1;
            for (int i=0; i<studentNames.size(); i++) {
                if (studentNames.get(i).equals(actStudent.getText().toString())) {
                    selectedStudentIdx = i; break;
                }
            }
            
            int selectedCategoryIdx = -1;
            for (int i=0; i<categoryDisplays.size(); i++) {
                if (categoryDisplays.get(i).equals(actAdvanceCategory.getText().toString())) {
                    selectedCategoryIdx = i; break;
                }
            }

            if (selectedStudentIdx == -1 || selectedCategoryIdx == -1) {
                Toast.makeText(context, "Vui lòng chọn đầy đủ người nhận và nguồn tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            String amountStr = etAdvanceAmount.getText() != null ? etAdvanceAmount.getText().toString() : "";
            if (amountStr.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            Long studentId = studentIds.get(selectedStudentIdx);
            Long categoryId = categorySuggestions.get(selectedCategoryIdx).categoryId;

            CreateFundAdvanceRequest req = new CreateFundAdvanceRequest(studentId, categoryId, amountStr);
            ApiClient.preparation(context).requestFundAdvance(task.id, req).enqueue(new Callback<ApiResponse<com.example.campuslife.entity.preparation.FundAdvanceDto>>() {
                @Override
                public void onResponse(Call<ApiResponse<com.example.campuslife.entity.preparation.FundAdvanceDto>> call, Response<ApiResponse<com.example.campuslife.entity.preparation.FundAdvanceDto>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        Toast.makeText(context, "Đã gửi Yêu cầu Ứng tiền", Toast.LENGTH_SHORT).show();
                        advanceDialog.dismiss();
                    } else {
                        Toast.makeText(context, "Lỗi ứng tiền. Có thể còn nợ cũ chưa đối soát.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<com.example.campuslife.entity.preparation.FundAdvanceDto>> call, Throwable t) {}
            });
        });

        advanceDialog.show();
    }

    private void showFundAdvancesListDialog(boolean isAdmin) {
        BottomSheetDialog bsDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_fund_advances, null);
        bsDialog.setContentView(view);
        
        RecyclerView rv = view.findViewById(R.id.rvFundAdvances);
        rv.setLayoutManager(new LinearLayoutManager(context));
        
        ApiClient.preparation(context).listFundAdvances(task.id).enqueue(new Callback<ApiResponse<List<FundAdvanceDto>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FundAdvanceDto>>> call, Response<ApiResponse<List<FundAdvanceDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus() && response.body().getData() != null) {
                    List<FundAdvanceDto> funds = response.body().getData();
                    FundAdvanceAdapter adapter = new FundAdvanceAdapter(context, funds, isAdmin);
                    rv.setAdapter(adapter);
                } else {
                    Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<FundAdvanceDto>>> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
        
        bsDialog.show();
    }

    private void showAllocateDialog() {
        BottomSheetDialog allocateDialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_allocate_budget, null);
        allocateDialog.setContentView(view);
        
        AutoCompleteTextView actCategory = view.findViewById(R.id.actCategory);
        TextInputEditText etAllocatedAmount = view.findViewById(R.id.etAllocatedAmount);
        MaterialButton btnSubmitAllocate = view.findViewById(R.id.btnSubmitAllocate);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(v -> allocateDialog.dismiss());

        List<BudgetCategoryDto> categories = new ArrayList<>();
        List<String> categoryDisplays = new ArrayList<>();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, categoryDisplays);
        actCategory.setAdapter(categoryAdapter);

        // We fetch budget based on ActivityId so we know what categories we have to allocate from.
        ApiClient.preparation(context).getActivityBudget(activityId).enqueue(new Callback<ApiResponse<ActivityBudgetDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<ActivityBudgetDto>> call, Response<ApiResponse<ActivityBudgetDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    ActivityBudgetDto budget = response.body().getData();
                    if (budget != null && budget.categories != null) {
                        for (BudgetCategoryDto cat : budget.categories) {
                            categories.add(cat);
                            categoryDisplays.add(cat.name);
                        }
                        categoryAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<ActivityBudgetDto>> call, Throwable t) {}
        });

        btnSubmitAllocate.setOnClickListener(v -> {
            int selectedCategoryIdx = -1;
            for (int i=0; i<categoryDisplays.size(); i++) {
                if (categoryDisplays.get(i).equals(actCategory.getText().toString())) {
                    selectedCategoryIdx = i; break;
                }
            }
            if (selectedCategoryIdx == -1) {
                Toast.makeText(context, "Vui lòng chọn hạng mục ví lớn", Toast.LENGTH_SHORT).show();
                return;
            }

            String amountStr = etAllocatedAmount.getText() != null ? etAllocatedAmount.getText().toString() : "";
            if (amountStr.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            Long categoryId = categories.get(selectedCategoryIdx).id;
            AllocateTaskAmountRequest req = new AllocateTaskAmountRequest(categoryId, amountStr);
            
            ApiClient.preparation(context).allocateTaskAmount(task.id, req).enqueue(new Callback<ApiResponse<PreparationTaskDto>>() {
                @Override
                public void onResponse(Call<ApiResponse<PreparationTaskDto>> call, Response<ApiResponse<PreparationTaskDto>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        Toast.makeText(context, "Cấp vốn thành công", Toast.LENGTH_SHORT).show();
                        allocateDialog.dismiss();
                        if (onDataChanged != null) onDataChanged.run();
                        if (detailDialog != null) detailDialog.dismiss();
                    } else {
                        Toast.makeText(context, "Cấp vốn thất bại (Tiền quỹ không đủ?)", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse<PreparationTaskDto>> call, Throwable t) {}
            });
        });

        allocateDialog.show();
    }
}
