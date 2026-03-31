**Báo cáo FE Android Native Java (Student Leader/Member) - Module Chuẩn Bị Sự Kiện**

## 1. Mục tiêu triển khai
Tài liệu này mô tả UI và luồng thao tác cho phía Student (Leader/Member) trong module chuẩn bị sự kiện, kèm API + DTO để đội Android Native Java có thể triển khai trực tiếp theo nhóm chức năng.

## 2. Vai trò và phạm vi
- Member:
  - Xem nhiệm vụ của mình.
  - Nhận nhiệm vụ.
  - Tạo chi phí cho task tài chính.
  - Xem trạng thái chi phí.
  - Xem tổng tạm ứng đang giữ và lịch sử tạm ứng của chính mình trong tab Chi phí.
- Leader:
  - Tất cả quyền của Member.
  - Duyệt chi phí cấp 1.
  - Gửi yêu cầu hoàn thành task.
  - Tạo yêu cầu tạm ứng cho thành viên.
  - Xem lịch sử tạm ứng toàn task trong tab Tạm ứng.
  - Tạo yêu cầu bổ sung cấp phát.

## 3. Đề xuất cấu trúc màn hình Android
- Screen 1: Danh sách nhiệm vụ của tôi (RecyclerView + filter trạng thái).
- Screen 2: Chi tiết nhiệm vụ (BottomSheet/Fullscreen Dialog với tab):
  - Tab Chi tiết
  - Tab Chi phí (task tài chính)
  - Tab Duyệt cấp 1 (chỉ leader)
  - Tab Tạm ứng (chỉ leader)
- Dialog con:
  - Dialog yêu cầu bổ sung cấp phát.
  - Dialog xem ảnh minh chứng.
  - Dialog/BottomSheet chọn nguồn cấp phát (nếu cần tùy UX).

## 4. Nhóm chức năng và API/DTO

### A. Nạp dữ liệu tổng quan và nhiệm vụ của user
1. Lấy activity mà user tham gia preparation
- API: `GET /api/preparation/my/activity-ids`
- Response: `number[]`
- Mục đích UI: quyết định hiển thị module preparation theo activity.

2. Lấy dashboard activity
- API: `GET /api/preparation/activities/{activityId}/dashboard`
- Response DTO: `PreparationDashboardDto`
public class PreparationDashboardDto {
    private Long activityId;
    private boolean hasPreparation;
    private List<PreparationTaskDto> tasks;
    private ActivityBudgetDto activityBudget;
    private String financeMessage;
}

- Dùng để render danh sách task, trạng thái bật/tắt preparation, budget sơ bộ.

3. Lấy nhiệm vụ của chính user theo activity
- API: `GET /api/preparation/my/activities/tasks?activityId={activityId}`
- Response DTO: `MyPreparationTaskDto[]`
public class MyPreparationTaskDto {
    private Long id;
    private Long activityId;
    private Long ownerId;
    private String ownerName;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private BigDecimal allocatedAmount;
    private boolean isFinancial;
    private PreparationTaskStatus status;
    private PreparationTaskMemberRole myRole;
}

- Dùng cho màn “Nhiệm vụ của tôi”.

### B. Chi tiết nhiệm vụ và workflow trạng thái
1. Lấy chi tiết task
- API: `GET /api/preparation/detail/{taskId}`
- Response DTO: `PreparationTaskDto`
public class PreparationTaskDto {
    private Long id;
    private Long activityId;
    private Long ownerId;
    private String ownerName;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private BigDecimal allocatedAmount;
    private Boolean isFinancial;
    private PreparationTaskStatus status;


}


2. Lấy member của task
- API: `GET /api/preparation/tasks/{taskId}/members`
- Response DTO: `PreparationTaskMemberDto[]`
public class PreparationTaskMemberDto {
    private Long studentId;
    private String studentName;
    private PreparationTaskMemberRole role;
}


3. Nhận nhiệm vụ (PENDING -> ACCEPTED)
- API: `PUT /api/preparation/tasks/{taskId}/accept`
- Response DTO: `PreparationTaskDto`
- UI: nút “Nhận nhiệm vụ” trong tab Chi tiết.

4. Gửi yêu cầu hoàn thành (leader)
- API: `PUT /api/preparation/tasks/{taskId}/request-complete`
- Response DTO: `PreparationTaskDto`
- UI: nút “Yêu cầu hoàn thành”.

### C. Chi phí (member + leader)
1. Lấy danh sách chi phí theo activity + status
- API: `GET /api/preparation/activities/{activityId}/expenses?status={status}`
- Response DTO: `ExpenseDto[]`
public class ExpenseDto {
    private Long id;
    private Long activityId;
    private Long taskId;
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String description;
    private String evidenceUrl;
    private ExpenseStatus status;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}

- FE lọc theo `taskId` hiện tại.
- UI: danh sách chi phí trong tab Chi phí.

2. Upload minh chứng ảnh chi phí
- API: `POST /api/preparation/tasks/{taskId}/expenses/evidence` (multipart file)
- Response DTO: `UploadResultDto` (`url`)
public class UploadResultDto {
    private String url;
}


3. Tạo chi phí
- API: `POST /api/preparation/tasks/{taskId}/expenses`
- Request DTO: `CreateExpenseRequest`
public class CreateExpenseRequest {
    private Long taskId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Amount is required")
    @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "Amount must be a positive number")
    private String amount;

    private String description;

    private String evidenceUrl;
}

- Response DTO: `ExpenseDto`
- Rule UI:
  - Bắt buộc `amount > 0`, có `categoryId`.
  - Có thể gửi `description`, `evidenceUrl`.

4. Gợi ý ví chi tiêu theo task + amount
- API: `GET /api/preparation/tasks/{taskId}/expense-category-suggestions?amount={amount}`
- Response DTO: `ExpenseCategorySuggestionDto[]`
public class ExpenseCategorySuggestionDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal allocationRemainingAmount;
    private BigDecimal walletRemainingAmount;
    private BigDecimal maxExpenseAmount;
}

- Rule UI đang dùng:
  - Nếu 1 ví: auto chọn category.
  - Nếu nhiều ví: mặc định ví `maxExpenseAmount` cao nhất, vẫn cho user đổi.
  - Nếu tất cả `maxExpenseAmount <= 0`: chặn submit sớm.

5. Nguồn cấp phát theo task (để hiển thị quota thực tế)
- API: `GET /api/preparation/tasks/{taskId}/allocation-sources`
- Response DTO: `TaskAllocationSourceDto[]`
public class TaskAllocationSourceDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal allocatedAmount;
    private BigDecimal holdingAdvanceAmount;
    private BigDecimal approvedSpentAmount;
    private BigDecimal allocationRemainingAmount;
}

- Dùng cho hiển thị “đã cấp phát/đang tạm ứng/đã chi duyệt/còn lại”.

### D. Duyệt chi phí cấp 1 (leader)
1. Duyệt/Từ chối expense ở cấp leader
- API: `PUT /api/preparation/expenses/{expenseId}/leader-decision`
- Request DTO: `ApproveExpenseRequest` (`approved: boolean`)
public class ApproveExpenseRequest {
    @NotNull(message = "Approved is required")
    private Boolean approved;
}

- Response DTO: `ExpenseDto`
- UI: tab Duyệt cấp 1, chỉ leader thấy và thao tác.

### E. Tạm ứng
1. Leader xem lịch sử tạm ứng toàn task
- API: `GET /api/preparation/tasks/{taskId}/fund-advances`
- Response DTO: `FundAdvanceDto[]`
public class FundAdvanceDto {
    private Long id;
    private Long taskId;
    private Long categoryId;
    private String categoryName;
    private Long studentId;
    private String studentName;
    private Long requestedById;
    private String requestedByName;
    private BigDecimal amount;
    private BigDecimal remainingAmount;
    private FundAdvanceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
}

- UI: tab Tạm ứng (chỉ leader).

2. Leader lấy gợi ý ví nguồn cho tạm ứng
- API: `GET /api/preparation/tasks/{taskId}/fund-advance-source-suggestions?amount={amount}`
- Response DTO: `FundAdvanceSourceSuggestionDto[]`
public class FundAdvanceSourceSuggestionDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal allocationRemainingAmount;
    private BigDecimal cashAvailableAmount;
    private BigDecimal maxAdvanceAmount;
}


3. Leader kiểm tra công nợ member trước khi tạo yêu cầu
- API: `GET /api/preparation/activities/{activityId}/fund-advance-debts?studentId={studentId}`
- Response DTO: `FundAdvanceDebtDto[]`
public class FundAdvanceDebtDto {
    private Long studentId;
    private String studentName;
    private BigDecimal holdingAmount;
}


4. Leader tạo yêu cầu tạm ứng
- API: `POST /api/preparation/tasks/{taskId}/fund-advances`
- Request DTO: `CreateFundAdvanceRequest`
public class CreateFundAdvanceRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Amount is required")
    @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "Amount must be a positive number")
    private String amount;
}

- Response DTO: `FundAdvanceDto`

5. Member/Leader xem tạm ứng của chính mình trong card tab Chi phí
- API: `GET /api/preparation/my/fund-advances?activityId={activityId}&taskId={taskId}`
- Response DTO: `FundAdvanceDto[]`
- UI:
  - Card “Tạm ứng tôi đang giữ” = sum `remainingAmount` của status `HOLDING`.
  - Có nút nhỏ “Xem lịch sử của tôi”.

### F. Bổ sung cấp phát (leader)
1. Tạo request bổ sung cấp phát
- API: `POST /api/preparation/tasks/{taskId}/allocation-adjustments`
- Request DTO: `CreateAllocationAdjustmentRequest`
public class CreateAllocationAdjustmentRequest {
    @NotBlank(message = "Amount is required")
    @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "Amount must be a non-negative number")
    private String amount;

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;
}

- Response DTO: `AllocationAdjustmentRequestDto`
public class AllocationAdjustmentRequestDto {
    private Long id;
    private Long activityId;
    private Long taskId;
    private BigDecimal amount;
    private String description;
    private AllocationAdjustmentStatus status;
    private Long requestedById;
    private String requestedByName;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
    private Long decidedById;
}

- UI: nút “Bổ sung cấp phát” trong tab Chi phí.

## 5. DTO cần map sang Java model

1. `PreparationTaskDto`
- `id, activityId, ownerId, ownerName, title, description, deadline, allocatedAmount, isFinancial, status, assigneeId, assigneeName`

2. `MyPreparationTaskDto`
- như `PreparationTaskDto` + `myRole`

3. `PreparationTaskMemberDto`
- `studentId, studentName, role`

4. `ExpenseDto`
- `id, activityId, taskId, categoryId, categoryName, amount, description, evidenceUrl, status, createdById, createdByName, createdAt`

5. `CreateExpenseRequest`
- `categoryId, amount, description, evidenceUrl`

6. `ExpenseCategorySuggestionDto`
- `categoryId, categoryName, allocationRemainingAmount, walletRemainingAmount, maxExpenseAmount`

7. `TaskAllocationSourceDto`
- `categoryId, categoryName, allocatedAmount, holdingAdvanceAmount, approvedSpentAmount, allocationRemainingAmount`

8. `FundAdvanceDto`
- `id, taskId, categoryId, categoryName, studentId, studentName, requestedById, requestedByName, amount, remainingAmount, status, createdAt, decidedAt`

9. `FundAdvanceSourceSuggestionDto`
- `categoryId, categoryName, allocationRemainingAmount, cashAvailableAmount, maxAdvanceAmount`

10. `FundAdvanceDebtDto`
- `studentId, studentName, holdingAmount`

11. `UploadResultDto`
- `url`

## 6. Mapping trạng thái để hiển thị UI
- `PreparationTaskStatus`:
  - `PENDING`, `ACCEPTED`, `COMPLETION_REQUESTED`, `COMPLETED`
- `ExpenseStatus`:
  - `PENDING_LEADER`, `PENDING_ADMIN`, `APPROVED`, `REJECTED`
- `FundAdvanceStatus`:
  - `REQUESTED`, `HOLDING`, `SETTLED`, `REJECTED`

Khuyến nghị Android FE map sang label tiếng Việt + badge màu thống nhất:
- `REQUESTED`: Chờ duyệt
- `HOLDING`: Đang giữ tiền
- `SETTLED`: Đã tất toán
- `REJECTED`: Từ chối

## 7. Checklist triển khai Android
1. Dùng Retrofit + Gson/Moshi cho API.
2. Dùng MVVM: `Repository -> UseCase/ViewModel -> Fragment/Dialog`.
3. Chuẩn hóa parse decimal bằng `BigDecimal` cho amount.
4. Upload ảnh minh chứng qua multipart trước, lấy URL rồi gọi create expense.
5. Với expense suggestion:
- debounce input amount 300-500ms
- auto-pick category theo rule
- block submit khi quota không khả dụng.
6. Phân quyền UI theo role:
- member không thấy tab Tạm ứng leader.
- leader có thêm tab Duyệt cấp 1 + Tạm ứng.

**Phần 2 - Đặc tả triển khai Android Native Java**

## 1. API Contract chi tiết theo use-case

### UC1 - Mở màn Nhiệm vụ của tôi
- Input:
1. activityId
- Calls:
1. GET /api/preparation/my/activities/tasks?activityId={activityId}
- Output UI:
1. RecyclerView danh sách task
2. Filter theo status ALL, PENDING, ACCEPTED, COMPLETION_REQUESTED, COMPLETED
- DTO:
1. MyPreparationTaskDto[]

### UC2 - Mở Chi tiết nhiệm vụ
- Input:
1. taskId, activityId
- Calls song song:
1. GET /api/preparation/detail/{taskId}
2. GET /api/preparation/tasks/{taskId}/members
3. Nếu task tài chính và user là leader: GET /api/preparation/tasks/{taskId}/fund-advances
4. Nếu task tài chính: GET /api/preparation/my/fund-advances?activityId={activityId}&taskId={taskId}
- Output UI:
1. Tab Chi tiết luôn có
2. Tab Chi phí nếu task tài chính
3. Tab Duyệt cấp 1 + Tạm ứng nếu leader

### UC3 - Member/Leader tạo chi phí
- Precondition:
1. task.isFinancial = true
2. task.status khác PENDING
- Calls:
1. GET /api/preparation/tasks/{taskId}/expense-category-suggestions?amount={amount}
2. POST /api/preparation/tasks/{taskId}/expenses/evidence (optional)
3. POST /api/preparation/tasks/{taskId}/expenses
- Request:
1. CreateExpenseRequest
- Response:
1. ExpenseDto
- Rule:
1. Nếu 1 suggestion thì auto chọn category
2. Nếu nhiều suggestion thì default maxExpenseAmount lớn nhất
3. Nếu tất cả maxExpenseAmount <= 0 thì chặn submit local

### UC4 - Leader duyệt chi phí cấp 1
- Calls:
1. GET /api/preparation/activities/{activityId}/expenses?status=PENDING_LEADER rồi filter taskId
2. PUT /api/preparation/expenses/{expenseId}/leader-decision
- Request:
1. ApproveExpenseRequest
- Response:
1. ExpenseDto

### UC5 - Leader tạo tạm ứng
- Calls:
1. GET /api/preparation/tasks/{taskId}/fund-advance-source-suggestions?amount={amount}
2. GET /api/preparation/activities/{activityId}/fund-advance-debts?studentId={studentId}
3. POST /api/preparation/tasks/{taskId}/fund-advances
- Request:
1. CreateFundAdvanceRequest
- Response:
1. FundAdvanceDto

### UC6 - Leader xem lịch sử tạm ứng toàn task
- Calls:
1. GET /api/preparation/tasks/{taskId}/fund-advances
- Response:
1. FundAdvanceDto[]

### UC7 - Member xem đang giữ bao nhiêu + lịch sử của tôi
- Calls:
1. GET /api/preparation/my/fund-advances?activityId={activityId}&taskId={taskId}
- Response:
1. FundAdvanceDto[]
- Rule:
1. Tổng giữ = sum remainingAmount với status HOLDING

## 2. State machine đề xuất cho màn Task Detail

### ScreenState
1. Idle
2. Loading
3. Content
4. Error

### TabState
1. DETAIL
2. EXPENSES
3. LEADER_REVIEW (leader)
4. ADVANCE (leader)

### ExpenseCreateState
1. Draft
2. SuggestingCategory
3. UploadingEvidence
4. Submitting
5. Success
6. Failed

### FundAdvanceCreateState (leader)
1. Draft
2. SuggestingSource
3. CheckingDebt
4. Submitting
5. Success
6. Failed

### Các transition chính
1. Open task detail -> Loading -> Content/Error
2. Nhập amount chi phí -> SuggestingCategory -> Draft
3. Gửi chi phí -> UploadingEvidence -> Submitting -> Success/Failed
4. Nhập amount tạm ứng -> SuggestingSource -> Draft
5. Chọn member tạm ứng -> CheckingDebt -> Draft
6. Gửi tạm ứng -> Submitting -> Success/Failed

## 3. Error handling chuẩn hóa (Android)
- 400:
1. Hiển thị message backend nếu có
- 401/403:
1. Force re-auth hoặc về login
- 404:
1. Toast Không tìm thấy dữ liệu
- 409:
1. Dùng cho conflict quota chi phí hoặc quy tắc tài chính, show warning dialog
- 5xx/network:
1. Snackbar lỗi hệ thống, cho phép retry

Khuyến nghị parse lỗi:
1. Ưu tiên message từ response.data.message
2. fallback response.data.body.message
3. fallback lỗi mặc định theo action

## 4. Mapping label tiếng Việt cho Android
- Task:
1. PENDING: Chưa nhận
2. ACCEPTED: Đang làm
3. COMPLETION_REQUESTED: Chờ duyệt hoàn thành
4. COMPLETED: Hoàn thành
- Expense:
1. PENDING_LEADER: Chờ leader duyệt
2. PENDING_ADMIN: Chờ admin duyệt
3. APPROVED: Đã duyệt
4. REJECTED: Từ chối
- FundAdvance:
1. REQUESTED: Chờ duyệt
2. HOLDING: Đang giữ tiền
3. SETTLED: Đã tất toán
4. REJECTED: Từ chối

## 5. Skeleton package cho Android Java

1. data/network
1. PreparationApiService (Retrofit interface)
2. ApiErrorParser
2. data/model/preparation
1. tất cả DTO class
3. data/repository
1. PreparationRepository
4. domain/usecase/preparation
1. GetMyTasksUseCase
2. GetTaskDetailUseCase
3. CreateExpenseUseCase
4. LeaderDecisionExpenseUseCase
5. CreateFundAdvanceUseCase
6. GetMyFundAdvancesUseCase
5. ui/preparation
1. MyTasksActivity/Fragment
2. TaskDetailBottomSheetDialogFragment
3. adapters cho Expense/FundAdvance/TaskMember
6. ui/common
1. LoadingStateView
2. ErrorStateView
3. CurrencyFormatter

## 6. Checklist QA cho Android
1. Member không thấy tab Tạm ứng và Duyệt cấp 1
2. Leader thấy đầy đủ tab
3. Expense auto category theo suggestion đúng rule
4. Expense không submit khi suggestion max = 0
5. Card Tạm ứng tôi đang giữ tính đúng sum HOLDING
6. Xem lịch sử tạm ứng của tôi hoạt động đúng task
7. Leader tạo tạm ứng bị chặn khi có debt warning
8. Duyệt cấp 1 cập nhật list realtime sau action
9. Retry hoạt động khi network lỗi

Nếu bạn muốn, mình viết tiếp bản Part 3: bộ Retrofit interface + Java DTO class mẫu đầy đủ để team Android có thể copy và chạy ngay.
