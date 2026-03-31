package com.example.campuslife.entity.preparation;

import java.io.Serializable;
import java.math.BigDecimal;

public class InvoiceStatusSummaryDto implements Serializable {
    public String status;
    public Long count;
    public BigDecimal totalAmount;
}
