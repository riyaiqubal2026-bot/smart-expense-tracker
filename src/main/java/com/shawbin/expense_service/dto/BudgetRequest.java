
package com.shawbin.expense_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetRequest(

        @NotBlank(message = "Category is required")
        String category,

        @NotNull(message = "Budget amount is required")
        @DecimalMin(value = "0.01", message = "Budget must be greater than zero")
        BigDecimal budgetAmount,

        @NotNull(message = "Budget month is required")
        LocalDate budgetMonth

) {}
