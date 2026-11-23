
package com.shawbin.expense_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetResponse(
        Long id,
        String category,
        BigDecimal budgetAmount,
        LocalDate budgetMonth,
        BigDecimal totalSpent,
        BigDecimal remainingAmount,
        double percentageUsed
) {}
