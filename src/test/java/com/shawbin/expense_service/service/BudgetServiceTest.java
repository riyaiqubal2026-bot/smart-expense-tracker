
package com.shawbin.expense_service.service;

import com.shawbin.expense_service.dto.BudgetRequest;
import com.shawbin.expense_service.dto.BudgetResponse;
import com.shawbin.expense_service.entity.Budget;
import com.shawbin.expense_service.entity.Expense;
import com.shawbin.expense_service.repository.BudgetRepository;
import com.shawbin.expense_service.repository.ExpenseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private BudgetService budgetService;

    private Long userId;
    private LocalDate month;

    @BeforeEach
    void setUp() {
        userId = 1L;
        month = LocalDate.of(2026, 11, 1);
    }

    private Budget createBudget(
            Long id,
            String category,
            String amount,
            LocalDate budgetMonth) {

        Budget budget = new Budget();
        budget.setId(id);
        budget.setUserId(userId);
        budget.setCategory(category);
        budget.setBudgetAmount(new BigDecimal(amount));
        budget.setBudgetMonth(budgetMonth);

        return budget;
    }

    private BudgetRequest createRequest(
            String category,
            String amount,
            LocalDate budgetMonth) {

        return new BudgetRequest(
                category,
                new BigDecimal(amount),
                budgetMonth
        );
    }

    private Expense createExpense(
            String category,
            String amount,
            LocalDate expenseDate) {

        Expense expense = new Expense();
        expense.setUserId(userId);
        expense.setCategory(category);
        expense.setAmount(new BigDecimal(amount));
        expense.setExpenseDate(expenseDate);

        return expense;
    }

    // --------------------------------------------------
    // GET BUDGETS
    // --------------------------------------------------

    @Test
    void getBudgets_shouldReturnBudgetsForSelectedMonth() {

        Budget budget = createBudget(
                1L, "Food", "1000",
                LocalDate.of(2026, 11, 1)
        );

        when(budgetRepository.findByUserIdAndBudgetMonth(
                userId, month
        )).thenReturn(List.of(budget));

        when(expenseRepository
                .findByUserIdAndCategoryIgnoreCaseAndExpenseDateBetween(
                        eq(userId),
                        eq("Food"),
                        eq(LocalDate.of(2026, 11, 1)),
                        eq(LocalDate.of(2026, 11, 30))
                )).thenReturn(List.of(
                createExpense(
                        "Food", "300",
                        LocalDate.of(2026, 11, 10)
                )
        ));

        List<BudgetResponse> result =
                budgetService.getBudgets(
                        userId,
                        LocalDate.of(2026, 11, 20)
                );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).category()).isEqualTo("Food");
        assertThat(result.get(0).totalSpent())
                .isEqualByComparingTo("300");
        assertThat(result.get(0).remainingAmount())
                .isEqualByComparingTo("700");
        assertThat(result.get(0).percentageUsed())
                .isEqualTo(30.0);
    }

    @Test
    void getBudgets_shouldReturnEmptyListWhenNoBudgetsExist() {

        when(budgetRepository.findByUserIdAndBudgetMonth(
                userId, month
        )).thenReturn(List.of());

        List<BudgetResponse> result =
                budgetService.getBudgets(userId, month);

        assertThat(result).isEmpty();

        verifyNoInteractions(expenseRepository);
    }

    // --------------------------------------------------
    // CREATE BUDGET
    // --------------------------------------------------

    @Test
    void createBudget_shouldCreateBudgetSuccessfully() {

        BudgetRequest request = createRequest(
                "Food",
                "1000",
                LocalDate.of(2026, 11, 15)
        );

        when(budgetRepository
                .existsByUserIdAndCategoryAndBudgetMonth(
                        userId,
                        "Food",
                        month
                )).thenReturn(false);

        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(invocation -> {
                    Budget saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        when(expenseRepository
                .findByUserIdAndCategoryIgnoreCaseAndExpenseDateBetween(
                        eq(userId),
                        eq("Food"),
                        eq(LocalDate.of(2026, 11, 1)),
                        eq(LocalDate.of(2026, 11, 30))
                )).thenReturn(List.of());

        BudgetResponse result =
                budgetService.createBudget(userId, request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.category()).isEqualTo("Food");
        assertThat(result.budgetAmount())
                .isEqualByComparingTo("1000");
        assertThat(result.totalSpent())
                .isEqualByComparingTo("0");
        assertThat(result.remainingAmount())
                .isEqualByComparingTo("1000");
        assertThat(result.percentageUsed()).isEqualTo(0.0);

        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void createBudget_shouldThrowExceptionWhenDuplicateExists() {

        BudgetRequest request = createRequest(
                "Food",
                "1000",
                month
        );

        when(budgetRepository
                .existsByUserIdAndCategoryAndBudgetMonth(
                        userId,
                        "Food",
                        month
                )).thenReturn(true);

        assertThatThrownBy(() ->
                budgetService.createBudget(userId, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "A budget already exists for this category and month"
                );

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    // --------------------------------------------------
    // UPDATE BUDGET
    // --------------------------------------------------

    @Test
    void updateBudget_shouldUpdateBudgetSuccessfully() {

        Budget budget = createBudget(
                1L, "Food", "1000", month
        );

        BudgetRequest request = createRequest(
                "Groceries",
                "1500",
                LocalDate.of(2026, 12, 15)
        );

        when(budgetRepository.findByIdAndUserId(
                1L, userId
        )).thenReturn(Optional.of(budget));

        when(budgetRepository.findByUserIdAndBudgetMonth(
                userId,
                LocalDate.of(2026, 12, 1)
        )).thenReturn(List.of());

        when(budgetRepository.save(budget))
                .thenReturn(budget);

        when(expenseRepository
                .findByUserIdAndCategoryIgnoreCaseAndExpenseDateBetween(
                        eq(userId),
                        eq("Groceries"),
                        eq(LocalDate.of(2026, 12, 1)),
                        eq(LocalDate.of(2026, 12, 31))
                )).thenReturn(List.of());

        BudgetResponse result =
                budgetService.updateBudget(userId, 1L, request);

        assertThat(result.category()).isEqualTo("Groceries");
        assertThat(result.budgetAmount())
                .isEqualByComparingTo("1500");
        assertThat(result.budgetMonth())
                .isEqualTo(LocalDate.of(2026, 12, 1));

        verify(budgetRepository).save(budget);
    }

    @Test
    void updateBudget_shouldThrowExceptionWhenBudgetNotFound() {

        BudgetRequest request = createRequest(
                "Food", "1000", month
        );

        when(budgetRepository.findByIdAndUserId(
                1L, userId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                budgetService.updateBudget(userId, 1L, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Budget not found");

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void updateBudget_shouldThrowExceptionWhenDuplicateCategoryExists() {

        Budget budget = createBudget(
                1L, "Food", "1000", month
        );

        Budget existingBudget = createBudget(
                2L, "Food", "500", month
        );

        BudgetRequest request = createRequest(
                "Food",
                "1500",
                month
        );

        when(budgetRepository.findByIdAndUserId(
                1L, userId
        )).thenReturn(Optional.of(budget));

        when(budgetRepository.findByUserIdAndBudgetMonth(
                userId, month
        )).thenReturn(List.of(budget, existingBudget));

        assertThatThrownBy(() ->
                budgetService.updateBudget(userId, 1L, request)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "A budget already exists for this category and month"
                );

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void updateBudget_shouldAllowUpdatingSameBudgetCategory() {

        Budget budget = createBudget(
                1L, "Food", "1000", month
        );

        BudgetRequest request = createRequest(
                "Food",
                "1200",
                month
        );

        when(budgetRepository.findByIdAndUserId(
                1L, userId
        )).thenReturn(Optional.of(budget));

        when(budgetRepository.findByUserIdAndBudgetMonth(
                userId, month
        )).thenReturn(List.of(budget));

        when(budgetRepository.save(budget))
                .thenReturn(budget);

        when(expenseRepository
                .findByUserIdAndCategoryIgnoreCaseAndExpenseDateBetween(
                        eq(userId),
                        eq("Food"),
                        eq(LocalDate.of(2026, 11, 1)),
                        eq(LocalDate.of(2026, 11, 30))
                )).thenReturn(List.of());

        BudgetResponse result =
                budgetService.updateBudget(userId, 1L, request);

        assertThat(result.budgetAmount())
                .isEqualByComparingTo("1200");

        verify(budgetRepository).save(budget);
    }

    // --------------------------------------------------
    // DELETE BUDGET
    // --------------------------------------------------

    @Test
    void deleteBudget_shouldDeleteBudgetSuccessfully() {

        Budget budget = createBudget(
                1L, "Food", "1000", month
        );

        when(budgetRepository.findByIdAndUserId(
                1L, userId
        )).thenReturn(Optional.of(budget));

        budgetService.deleteBudget(userId, 1L);

        verify(budgetRepository).delete(budget);
    }

    @Test
    void deleteBudget_shouldThrowExceptionWhenBudgetNotFound() {

        when(budgetRepository.findByIdAndUserId(
                1L, userId
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                budgetService.deleteBudget(userId, 1L)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Budget not found");

        verify(budgetRepository, never()).delete(any(Budget.class));
    }

    // --------------------------------------------------
    // BUDGET CALCULATION
    // --------------------------------------------------

    @Test
    void getBudgets_shouldCalculatePercentageAbove100() {

        Budget budget = createBudget(
                1L, "Food", "1000", month
        );

        when(budgetRepository.findByUserIdAndBudgetMonth(
                userId, month
        )).thenReturn(List.of(budget));

        when(expenseRepository
                .findByUserIdAndCategoryIgnoreCaseAndExpenseDateBetween(
                        eq(userId),
                        eq("Food"),
                        eq(LocalDate.of(2026, 11, 1)),
                        eq(LocalDate.of(2026, 11, 30))
                )).thenReturn(List.of(
                createExpense(
                        "Food", "1200",
                        LocalDate.of(2026, 11, 10)
                )
        ));

        List<BudgetResponse> result =
                budgetService.getBudgets(userId, month);

        assertThat(result.get(0).totalSpent())
                .isEqualByComparingTo("1200");
        assertThat(result.get(0).remainingAmount())
                .isEqualByComparingTo("-200");
        assertThat(result.get(0).percentageUsed())
                .isEqualTo(120.0);
    }

    @Test
    void getBudgets_shouldAddMultipleExpenses() {

        Budget budget = createBudget(
                1L, "Food", "1000", month
        );

        when(budgetRepository.findByUserIdAndBudgetMonth(
                userId, month
        )).thenReturn(List.of(budget));

        when(expenseRepository
                .findByUserIdAndCategoryIgnoreCaseAndExpenseDateBetween(
                        eq(userId),
                        eq("Food"),
                        eq(LocalDate.of(2026, 11, 1)),
                        eq(LocalDate.of(2026, 11, 30))
                )).thenReturn(List.of(
                createExpense(
                        "Food", "200",
                        LocalDate.of(2026, 11, 5)
                ),
                createExpense(
                        "Food", "300",
                        LocalDate.of(2026, 11, 15)
                )
        ));

        List<BudgetResponse> result =
                budgetService.getBudgets(userId, month);

        assertThat(result.get(0).totalSpent())
                .isEqualByComparingTo("500");
        assertThat(result.get(0).remainingAmount())
                .isEqualByComparingTo("500");
        assertThat(result.get(0).percentageUsed())
                .isEqualTo(50.0);
    }
}
