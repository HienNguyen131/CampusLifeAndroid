package com.example.campuslife.entity.preparation;

import java.io.Serializable;
import java.math.BigDecimal;

public class FundAdvanceSourceSuggestionDto implements Serializable {
    public Long categoryId;
    public String categoryName;
    public BigDecimal allocationRemainingAmount;
    public BigDecimal cashAvailableAmount;
    public BigDecimal maxAdvanceAmount;
}
