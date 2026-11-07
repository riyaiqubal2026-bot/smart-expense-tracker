package com.shawbin.expense_service.service;

import com.shawbin.expense_service.client.UserServiceClient;
import com.shawbin.expense_service.dto.ExpenseRequest;
import com.shawbin.expense_service.dto.ExpenseResponse;
import com.shawbin.expense_service.entity.Expense;
import com.shawbin.expense_service.exception.UserNotFoundException;
import com.shawbin.expense_service.mapper.ExpenseMapper;
import com.shawbin.expense_service.repository.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {

    public final ExpenseRepository expenseRepository;
    public final UserServiceClient userServiceClient;

    public ExpenseService(ExpenseRepository expenseRepository,UserServiceClient userServiceClient) {
        this.expenseRepository = expenseRepository;
        this.userServiceClient=userServiceClient;
    }

    public ExpenseResponse createExpense(ExpenseRequest expenseRequest, Long userId) {
        userServiceClient.validateUser(userId);

        Expense expenseEntity = ExpenseMapper.toEntity(expenseRequest);
        expenseEntity.setUserId(userId);

        Expense savedExpense = expenseRepository.save(expenseEntity);

        return ExpenseMapper.toResponse(savedExpense);
    }

    public Page<ExpenseResponse> getAllExpenses(Long userId, Pageable pageable) {
        return expenseRepository.findByUserId(userId, pageable)
                .map(ExpenseMapper::toResponse);
    }

    public Optional<ExpenseResponse> getExpenseById(Long id, Long userId) {
        return expenseRepository.findByIdAndUserId(id, userId)
                .map(ExpenseMapper::toResponse);
    }

    public Optional<ExpenseResponse> updateExpense(
            Long id, ExpenseRequest expenseRequest, Long userId) {

        return expenseRepository.findByIdAndUserId(id, userId)
                .map(existingExpense -> {
                    ExpenseMapper.updateEntity(existingExpense, expenseRequest);

                    Expense updatedExpense = expenseRepository.save(existingExpense);

                    return ExpenseMapper.toResponse(updatedExpense);
                });
    }

    public Boolean deleteExpense(Long id, Long userId) {

        Optional<Expense> expense =
                expenseRepository.findByIdAndUserId(id, userId);

        if (expense.isEmpty()) {
            return false;
        }

        expenseRepository.delete(expense.get());
        return true;
    }

    public Page<ExpenseResponse> filterExpensesByCategory(
            Long userId, Pageable pageable, String category) {

        return expenseRepository
                .findByUserIdAndCategoryIgnoreCase(userId, category, pageable)
                .map(ExpenseMapper::toResponse);
    }

    public Page<ExpenseResponse> filterByStartDateAndEndDate(
            Long userId, Pageable pageable,
            LocalDate startDate, LocalDate endDate) {

        return expenseRepository
                .findByUserIdAndExpenseDateBetween(
                        userId, startDate, endDate, pageable)
                .map(ExpenseMapper::toResponse);
    }

}
