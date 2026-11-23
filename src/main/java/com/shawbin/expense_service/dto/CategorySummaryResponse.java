package com.shawbin.expense_service.dto;

import java.math.BigDecimal;

public record CategorySummaryResponse(
        String category,
        BigDecimal totalAmount,
        Long transactionCount,
        BigDecimal averageAmount
) {
}