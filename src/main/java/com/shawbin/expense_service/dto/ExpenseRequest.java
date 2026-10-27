package com.shawbin.expense_service.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ExpenseRequest {

    @NotBlank(message = "Title is Required")
    private String title;

    @NotBlank(message = "Category is Required")
    private String category;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Column(nullable = false, precision = 12, scale = 2)
    @NotNull(message = "Amount is Required")
    private BigDecimal amount;

    @NotNull(message = "Expense Date is Required")
    private LocalDate expenseDate;


}
