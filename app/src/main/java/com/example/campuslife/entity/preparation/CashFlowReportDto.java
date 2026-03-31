package com.example.campuslife.entity.preparation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class CashFlowReportDto implements Serializable {
    public Long activityId;
    public BigDecimal totalBudget;
    public BigDecimal approvedSpent;
    public BigDecimal cashOutsideWallet;
    public BigDecimal cashInsideWallet;
    public List<FundAdvanceDebtDto> advanceDebts;
    public List<InvoiceStatusSummaryDto> invoiceStatusSummary;
}
