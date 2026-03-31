BÁO CÁO FE ANDROID NATIVE JAVA  
Phạm vi: Giao diện Admin/Manager cho phân hệ Chuẩn bị sự kiện (Preparation)

1. Mục tiêu triển khai trên Android
- Tái hiện đúng 2 màn chính của web:
- Màn danh sách hoạt động Preparation (Preparation Management).
- Màn chi tiết Preparation của 1 activity (Preparation Detail) với 2 tab:
- Tổng quan.
- Trung tâm nhiệm vụ.
- Mỗi thao tác UI map 1-1 với API, dùng DTO rõ ràng để dễ code Retrofit + model Java.

2. Chuẩn dữ liệu và xử lý chung
- Hầu hết API trả theo wrapper:
- status: boolean.
- message: string.
- body: object hoặc list.
- Android nên parse wrapper chung, sau đó map body vào DTO cụ thể.
- Các trường tiền (amount, allocatedAmount, remainingAmount...) đang đi dưới dạng string trong FE hiện tại.
- Khuyến nghị Android Java parse sang BigDecimal tại tầng domain để tính toán an toàn.

3. Nhóm chức năng A: Quản lý danh sách hoạt động Preparation
Chức năng A1: Lấy danh sách hoạt động
- UI: Màn danh sách.
- API: GET /api/activities
- Request DTO: không có.
- Response DTO chính: ActivityResponse[]
public class ActivityResponse {
    private Long id;
    private String name;
    private ActivityType type;
    private ScoreType scoreType;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private boolean requiresSubmission;
    private BigDecimal maxPoints;

    private LocalDateTime registrationStartDate;
    private LocalDateTime registrationDeadline;

    private String shareLink;
    private boolean isImportant;
    private boolean isDraft;
    private String bannerUrl;
    private String location;

    private Integer ticketQuantity;
    private String benefits;
    private String requirements;
    private String contactInfo;
    private String checkInCode;
    private boolean requiresApproval;
    private boolean mandatoryForFacultyStudents;
    private BigDecimal penaltyPointsIncomplete;

    private List<Long> organizerIds;

    private Long seriesId;
    private Integer seriesOrder;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String lastModifiedBy;
}

- Field cần hiển thị: id, name, startDate, endDate.

Chức năng A2: Lấy thống kê Preparation từng activity
- UI: Hiển thị Pending task, Expense chờ duyệt, Remaining.
- API:
- GET /api/preparation/activities/{activityId}/dashboard
- GET /api/preparation/activities/{activityId}/expenses?status=PENDING_ADMIN
- GET /api/preparation/activities/{activityId}/financial-report
- Request DTO: không có.
- Response DTO:
- PreparationDashboardDto
public class PreparationDashboardDto {
    private Long activityId;
    private boolean hasPreparation;
    private List<PreparationTaskDto> tasks;
    private ActivityBudgetDto activityBudget;
    private String financeMessage;
}

- ExpenseDto[]
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

- FinancialReportDto
public class FinancialReportDto {
    private Long activityId;
    private BigDecimal totalBudget;
    private List<BudgetCategoryDto> categories;
    private List<TaskOverBudgetDto> overBudgetTasks;
}

Chức năng A3: Bật/tắt Preparation
- UI: Nút bật/tắt trên từng dòng activity.
- API: PUT /api/preparation/activities/{activityId}/toggle?enabled=true|false
- Request DTO: không có body.
- Response DTO: không dùng body (thành công/thất bại theo status).

4. Nhóm chức năng B: Màn chi tiết activity (Header + điều hướng)
Chức năng B1: Lấy thông tin activity và dữ liệu lõi
- UI: Header tên sự kiện + trạng thái chuẩn bị.
- API:
- GET /api/activities/{activityId}
- GET /api/preparation/activities/{activityId}/dashboard
- GET /api/preparation/activities/{activityId}/organizers
- GET /api/preparation/activities/{activityId}/workload-warnings
- GET /api/preparation/activities/{activityId}/budget
- Response DTO:
- ActivityResponse
- PreparationDashboardDto
- OrganizerDto[]
public class OrganizerDto {
    private Long studentId;
    private String fullName;
}

- WorkloadWarningDto[]
public class WorkloadWarningDto {
    private Long studentId;
    private String studentName;
    private Long taskCount;
    private WorkloadWarningType type;
}

- ActivityBudgetDto
public class ActivityBudgetDto {
    private Long id;
    private Long activityId;
    private BigDecimal totalAmount;
    private List<BudgetCategoryDto> categories;
}

5. Nhóm chức năng C: Tab Tổng quan (Overview)
Chức năng C1: Báo cáo tổng quan tài chính
- API: GET /api/preparation/activities/{activityId}/reports/finance-overview
- Response DTO: FinanceOverviewReportDto
public class FinanceOverviewReportDto {
    private Long activityId;
    private BigDecimal totalBudget;
    private BigDecimal totalAllocatedToTasks;
    private BigDecimal totalApprovedSpent;
    private BigDecimal varianceAllocatedVsApproved;
    private List<BudgetCategoryDto> wallets;
    private List<TaskSpendStatusDto> tasks;
}

- Field chính:
- totalBudget, totalAllocatedToTasks, totalApprovedSpent, varianceAllocatedVsApproved.
- wallets: BudgetCategoryDto[].
public class BudgetCategoryDto {
    private Long id;
    private String name;
    private BigDecimal allocatedAmount;
    private BigDecimal allocatedToTasksAmount;
    private BigDecimal availableToAllocateAmount;
    private BigDecimal cashOutsideAmount;
    private BigDecimal cashAvailableAmount;
    private BigDecimal usedAmount;
    private BigDecimal remainingAmount;
    private Double usedPercent;
}

- tasks: TaskSpendStatusDto[].
public class TaskSpendStatusDto {
    private Long taskId;
    private String title;
    private BigDecimal allocatedAmount;
    private BigDecimal committedAmount;
    private BigDecimal approvedSpent;
    private Double usedPercent;
}

Chức năng C2: Báo cáo dòng tiền và nợ tạm ứng
- API: GET /api/preparation/activities/{activityId}/reports/cash-flow
- Response DTO: CashFlowReportDto
public class CashFlowReportDto {
    private Long activityId;
    private BigDecimal totalBudget;
    private BigDecimal approvedSpent;
    private BigDecimal cashOutsideWallet;
    private BigDecimal cashInsideWallet;
    private List<FundAdvanceDebtDto> advanceDebts;
    private List<InvoiceStatusSummaryDto> invoiceStatusSummary;
}

- Field chính:
- cashOutsideWallet, cashInsideWallet.
- advanceDebts: FundAdvanceDebtDto[].
public class FundAdvanceDebtDto {
    private Long studentId;
    private String studentName;
    private BigDecimal holdingAmount;
}

- invoiceStatusSummary: InvoiceStatusSummaryDto[].
public class InvoiceStatusSummaryDto {
    private ExpenseStatus status;
    private Long count;
    private BigDecimal totalAmount;
}


Chức năng C3: Quản lý Organizer
- Thêm organizer:
- API: POST /api/preparation/activities/{activityId}/organizers/{studentId}
- Request DTO: không có body.
- Response DTO: không có body nghiệp vụ.
- Xóa organizer:
- API: DELETE /api/preparation/activities/{activityId}/organizers/{studentId}
- Tìm sinh viên khi thêm:
- API: GET /api/students/search?keyword={keyword}&page=0&size=20
- Response DTO: StudentListResponse (chứa danh sách sinh viên để chọn).
public class StudentListResponse {
    private List<StudentResponse> content;
    private Long totalElements;
    private Integer totalPages;
    private Integer size;
    private Integer number;
    private Boolean first;
    private Boolean last;
}


Chức năng C4: Thiết lập ngân sách activity
- API:
- GET /api/preparation/activities/{activityId}/budget
- PUT /api/preparation/activities/{activityId}/budget
- Request DTO khi lưu: UpsertActivityBudgetRequest
public class UpsertActivityBudgetRequest {
    @NotBlank(message = "Total amount is required")
    @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "Total amount must be a non-negative number")
    private String totalAmount;

    @Valid
    private List<UpsertBudgetCategoryRequest> categories;
}

- totalAmount: string
- categories: UpsertBudgetCategoryRequest[]
public class UpsertBudgetCategoryRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Allocated amount is required")
    @Pattern(regexp = "^\\d+(\\.\\d{1,2})?$", message = "Allocated amount must be a non-negative number")
    private String allocatedAmount;
}

- name: string
- allocatedAmount: string
- Response DTO: ActivityBudgetDto


6. Nhóm chức năng D: Tab Trung tâm nhiệm vụ (Task Center)
Chức năng D1: Tạo task
- API: POST /api/preparation/activities/{activityId}/tasks
- Request DTO:
- ownerId: long
- title: string
- description: string hoặc null
- deadline: ISO datetime hoặc null
- isFinancial: boolean
- Response DTO: PreparationTaskDto
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


Chức năng D2: Danh sách task và trạng thái
- API nguồn: GET /api/preparation/activities/{activityId}/dashboard
- Response DTO: PreparationDashboardDto.tasks (PreparationTaskDto[])

Chức năng D3: Xem chi tiết task (modal)
- API gọi song song:
- GET /api/preparation/detail/{taskId}
- GET /api/preparation/tasks/{taskId}/members
- GET /api/preparation/tasks/{taskId}/fund-advances
- GET /api/preparation/tasks/{taskId}/allocation-sources
- Response DTO:
- PreparationTaskDto
- PreparationTaskMemberDto[]
public class PreparationTaskMemberDto {
    private Long studentId;
    private String studentName;
    private PreparationTaskMemberRole role;
}

- FundAdvanceDto[]
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

- TaskAllocationSourceDto[]
public class TaskAllocationSourceDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal allocatedAmount;
    private BigDecimal holdingAdvanceAmount;
    private BigDecimal approvedSpentAmount;
    private BigDecimal allocationRemainingAmount;
}

Chức năng D4: Duyệt/từ chối yêu cầu hoàn thành task
- API: PUT /api/preparation/tasks/{taskId}/complete-decision
- Request DTO:
- approved: boolean
- Response DTO: PreparationTaskDto

Chức năng D5: Quản lý member/leader trong task
- API:
- POST /api/preparation/tasks/{taskId}/members/{studentId}
- DELETE /api/preparation/tasks/{taskId}/members/{studentId}
- POST /api/preparation/tasks/{taskId}/leaders/{studentId}
- DELETE /api/preparation/tasks/{taskId}/leaders/{studentId}
- Response DTO: thường không cần body nghiệp vụ, sau đó reload danh sách members.

7. Nhóm chức năng E: Cấp phát ngân sách và yêu cầu bổ sung
Chức năng E1: Cấp phát cho task theo ví nguồn
- API: PUT /api/preparation/tasks/{taskId}/allocation
- Request DTO:
- categoryId: long
- allocatedAmount: string
- Response DTO: PreparationTaskDto

Chức năng E2: Danh sách yêu cầu bổ sung cấp phát
- API: GET /api/preparation/activities/{activityId}/allocation-adjustments?status={optional}
- Response DTO: AllocationAdjustmentRequestDto[]
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


Chức năng E3: Lấy source-plan đề xuất khi duyệt bổ sung
- API: GET /api/preparation/allocation-adjustments/{requestId}/source-plan
- Response DTO: AllocationAdjustmentSourcePlanItemDto[]
public class AllocationAdjustmentSourcePlanDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal amount;
}

- categoryId, categoryName, amount

Chức năng E4: Admin duyệt/từ chối yêu cầu bổ sung
- API: PUT /api/preparation/allocation-adjustments/{requestId}/admin-decision
- Request DTO: AdminDecisionAllocationAdjustmentRequest
public class AdminDecisionAllocationAdjustmentRequest {
    @NotNull(message = "Approved is required")
    private Boolean approved;

    private Long categoryId;

    @Valid
    private List<AllocationAdjustmentSourceRequest> sources;
}

- approved: boolean
- categoryId: long hoặc null (nếu 1 nguồn)
- sources: AllocationAdjustmentDecisionSourceRequest[]
- categoryId: long
- amount: string
- Response DTO: AllocationAdjustmentRequestDto
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


8. Nhóm chức năng F: Duyệt chi phí (Leader/Admin) trong Task Center
Chức năng F1: Lọc và lấy danh sách chi phí
- API: GET /api/preparation/activities/{activityId}/expenses?status={optional}
- Response DTO: ExpenseDto[]

Chức năng F2: Leader duyệt cấp 1
- API: PUT /api/preparation/expenses/{expenseId}/leader-decision
- Request DTO: ApproveExpenseRequest
- approved: boolean
- Response DTO: ExpenseDto

Chức năng F3: Admin duyệt cấp cuối
- API: PUT /api/preparation/expenses/{expenseId}/admin-decision
- Request DTO: ApproveExpenseRequest
- approved: boolean
- Response DTO: ExpenseDto

9. Nhóm chức năng G: Quản lý tạm ứng (Fund Advance) cho admin/manager
Chức năng G1: Danh sách tạm ứng theo task
- API: GET /api/preparation/tasks/{taskId}/fund-advances
- Response DTO: FundAdvanceDto[]

Chức năng G2: Admin duyệt/từ chối yêu cầu tạm ứng
- API: PUT /api/preparation/fund-advances/{fundAdvanceId}/admin-decision
- Request DTO: AdminDecideFundAdvanceRequest
- approved: boolean
- Response DTO: FundAdvanceDto
- UI rule quan trọng: có cảnh báo nếu sinh viên còn holding debt.

Chức năng G3: Hoàn ứng (return)
- API: PUT /api/preparation/fund-advances/{fundAdvanceId}/return
- Request DTO: không có body.
- Response DTO: FundAdvanceDto

Chức năng G4: Báo cáo nợ tạm ứng theo activity
- API: GET /api/preparation/activities/{activityId}/fund-advance-debts?studentId={optional}
- Response DTO: FundAdvanceDebtDto[]
- studentId, studentName, holdingAmount

10. DTO cốt lõi cần tạo ở Android (Model Java)
- PreparationTaskDto
- PreparationTaskMemberDto
- PreparationDashboardDto
- ActivityBudgetDto
- BudgetCategoryDto
- ExpenseDto
- FundAdvanceDto
- TaskAllocationSourceDto
- AllocationAdjustmentRequestDto
- AllocationAdjustmentSourcePlanItemDto
- FinanceOverviewReportDto
- CashFlowReportDto
- FundAdvanceDebtDto
- WorkloadWarningDto
- OrganizerDto
- Request DTO:
- UpsertActivityBudgetRequest
- UpsertBudgetCategoryRequest
- Create task request
- Allocate task request
- AdminDecisionAllocationAdjustmentRequest
- AllocationAdjustmentDecisionSourceRequest
- ApproveExpenseRequest
- AdminDecideFundAdvanceRequest

11. Lưu ý mới về API gợi ý ví chi (để Android đồng bộ logic khi mở rộng màn member)
- Endpoint: GET /api/preparation/tasks/{taskId}/expense-category-suggestions?amount=...
- DTO đã có thêm:
public class ExpenseCategorySuggestionDto {
    private Long categoryId;
    private String categoryName;
    private BigDecimal allocationRemainingAmount;
    private BigDecimal walletRemainingAmount;
    private BigDecimal myFundAdvanceRemainingAmount;
    private BigDecimal maxExpenseAmount;
}

- myFundAdvanceRemainingAmount
- maxExpenseAmount mới tính theo:
- min(allocationRemainingAmount, walletRemainingAmount, myFundAdvanceRemainingAmount)
- Nếu user chưa có HOLDING ở ví đó thì myFundAdvanceRemainingAmount = 0, API sẽ không gợi ý ví đó.
- Hàm ý cho UI Android: nếu list gợi ý rỗng sau khi nhập amount thì phải hướng user xin tạm ứng trước, không tự fallback chọn ví.

12. Đề xuất triển khai Android Native Java
1. Chia module theo màn:
- preparation-management
- preparation-detail-overview
- preparation-detail-task-center
2. Tạo lớp BaseApiResponse<T> cho wrapper status/message/body.
3. Toàn bộ amount map BigDecimal ở domain layer, giữ String ở network DTO nếu backend trả string.
4. Với màn Task Detail, gọi song song 4 API để giảm thời gian mở modal chi tiết.
5. Chuẩn hóa enum hiển thị:
- ExpenseStatus, FundAdvanceStatus, PreparationTaskStatus bằng mapper riêng để tránh hard-code ở nhiều nơi.
public enum ExpenseStatus {
    PENDING_LEADER,
    PENDING_ADMIN,
    APPROVED,
    REJECTED
}
public enum FundAdvanceStatus {
    REQUESTED,
    HOLDING,
    SETTLED,
    REJECTED
}
public enum PreparationTaskStatus {
    PENDING,
    ACCEPTED,
    COMPLETION_REQUESTED,
    COMPLETED
}

