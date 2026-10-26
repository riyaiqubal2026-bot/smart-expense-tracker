package com.shawbin.expense_service.dto;

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

    @NotNull(message = "Amount is Required")
    private BigDecimal Amount;

    @NotNull(message = "Expense Date is Required")
    private LocalDate expenseDate;


}
