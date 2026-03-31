package com.example.campuslife.entity.preparation;

import java.io.Serializable;
import java.math.BigDecimal;

public class ExpenseCategorySuggestionDto implements Serializable {
    public Long categoryId;
    public String categoryName;
    public BigDecimal allocationRemainingAmount;
    public BigDecimal walletRemainingAmount;
    public BigDecimal myFundAdvanceRemainingAmount;
    public BigDecimal maxExpenseAmount;
}
