
package com.shawbin.expense_service.service;

import com.shawbin.expense_service.dto.BudgetRequest;
import com.shawbin.expense_service.dto.BudgetResponse;
import com.shawbin.expense_service.entity.Budget;
import com.shawbin.expense_service.entity.Expense;
import com.shawbin.expense_service.repository.BudgetRepository;
import com.shawbin.expense_service.repository.ExpenseRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
    }

    public List<BudgetResponse> getBudgets(
            Long userId, LocalDate selectedMonth) {

        LocalDate month = YearMonth.from(selectedMonth).atDay(1);

        List<Budget> budgets =
                budgetRepository.findByUserIdAndBudgetMonth(userId, month);

        return budgets.stream()
                .map(budget -> calculateBudgetResponse(budget, userId))
                .toList();
    }

    public BudgetResponse createBudget(
            Long userId, BudgetRequest request) {

        LocalDate month =
                YearMonth.from(request.budgetMonth()).atDay(1);

        if (budgetRepository.existsByUserIdAndCategoryAndBudgetMonth(
                userId, request.category(), month)) {
            throw new IllegalArgumentException(
                    "A budget already exists for this category and month");
        }

        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setCategory(request.category());
        budget.setBudgetAmount(request.budgetAmount());
        budget.setBudgetMonth(month);

        Budget savedBudget = budgetRepository.save(budget);

        return calculateBudgetResponse(savedBudget, userId);
    }

    @Transactional
    public BudgetResponse updateBudget(
            Long userId, Long budgetId, BudgetRequest request) {

        Budget budget = budgetRepository.findByIdAndUserId(
                budgetId, userId
        ).orElseThrow(() ->
                new IllegalArgumentException("Budget not found"));

        LocalDate month =
                YearMonth.from(request.budgetMonth()).atDay(1);

        List<Budget> existingBudgets =
                budgetRepository.findByUserIdAndBudgetMonth(userId, month);

        boolean duplicate = existingBudgets.stream()
                .anyMatch(existing ->
                        !existing.getId().equals(budgetId)
                                && existing.getCategory().equalsIgnoreCase(
                                request.category()));

        if (duplicate) {
            throw new IllegalArgumentException(
                    "A budget already exists for this category and month");
        }

        budget.setCategory(request.category());
        budget.setBudgetAmount(request.budgetAmount());
        budget.setBudgetMonth(month);

        Budget updatedBudget = budgetRepository.save(budget);

        return calculateBudgetResponse(updatedBudget, userId);
    }

    public void deleteBudget(Long userId, Long budgetId) {

        Budget budget = budgetRepository.findByIdAndUserId(
                budgetId, userId
        ).orElseThrow(() ->
                new IllegalArgumentException("Budget not found"));

        budgetRepository.delete(budget);
    }

    private BudgetResponse calculateBudgetResponse(
            Budget budget, Long userId) {

        YearMonth yearMonth = YearMonth.from(budget.getBudgetMonth());

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Expense> expenses =
                expenseRepository
                        .findByUserIdAndCategoryIgnoreCaseAndExpenseDateBetween(
                                userId,
                                budget.getCategory(),
                                startDate,
                                endDate
                        );

        BigDecimal totalSpent = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingAmount =
                budget.getBudgetAmount().subtract(totalSpent);

        double percentageUsed = totalSpent
                .multiply(BigDecimal.valueOf(100))
                .divide(budget.getBudgetAmount(), 2, RoundingMode.HALF_UP)
                .doubleValue();

        return new BudgetResponse(
                budget.getId(),
                budget.getCategory(),
                budget.getBudgetAmount(),
                budget.getBudgetMonth(),
                totalSpent,
                remainingAmount,
                percentageUsed
        );
    }
}
