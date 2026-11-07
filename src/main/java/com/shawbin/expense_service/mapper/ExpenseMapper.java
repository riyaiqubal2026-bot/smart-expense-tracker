package com.shawbin.expense_service.mapper;

import com.shawbin.expense_service.dto.ExpenseRequest;
import com.shawbin.expense_service.dto.ExpenseResponse;
import com.shawbin.expense_service.entity.Expense;

public class ExpenseMapper {

    public static Expense toEntity(ExpenseRequest request) {
        Expense expenseEntity = new Expense();
        expenseEntity.setTitle(request.getTitle());
        expenseEntity.setAmount(request.getAmount());
        expenseEntity.setCategory(request.getCategory());
        expenseEntity.setExpenseDate(request.getExpenseDate());

        return expenseEntity;
    }

    public static ExpenseResponse toResponse(Expense expenseEntity){
        return new ExpenseResponse(
                expenseEntity.getId(),
                expenseEntity.getUserId(),
                expenseEntity.getTitle(),
                expenseEntity.getCategory(),
                expenseEntity.getAmount(),
                expenseEntity.getExpenseDate()

        );
    }
    public static void updateEntity(
            Expense expense, ExpenseRequest request) {

        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setExpenseDate(request.getExpenseDate());
    }
}
