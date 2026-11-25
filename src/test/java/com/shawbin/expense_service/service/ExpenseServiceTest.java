
package com.shawbin.expense_service.service;

import com.shawbin.expense_service.dto.ExpenseRequest;
import com.shawbin.expense_service.dto.ExpenseResponse;
import com.shawbin.expense_service.dto.CategorySummaryResponse;
import com.shawbin.expense_service.entity.Expense;
import com.shawbin.expense_service.repository.ExpenseRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private ExpenseRequest expenseRequest;
    private Expense expense;

    private final Long userId = 1L;

    private final LocalDate expenseDate =
            LocalDate.of(2026, 10, 26);

    @BeforeEach
    void setUp() {

        expenseRequest = new ExpenseRequest();
        expenseRequest.setTitle("Groceries");
        expenseRequest.setAmount(new BigDecimal("45.50"));
        expenseRequest.setCategory("Food");
        expenseRequest.setExpenseDate(expenseDate);

        expense = new Expense();
        expense.setId(1L);
        expense.setUserId(userId);
        expense.setTitle("Groceries");
        expense.setAmount(new BigDecimal("45.50"));
        expense.setCategory("Food");
        expense.setExpenseDate(expenseDate);
    }

    // ------------------------------------------------
    // CREATE EXPENSE TESTS
    // ------------------------------------------------

    @Test
    void createExpense_shouldReturnSavedExpense() {

        when(expenseRepository.save(any(Expense.class)))
                .thenReturn(expense);

        ExpenseResponse response =
                expenseService.createExpense(expenseRequest, userId);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Groceries", response.getTitle());
        assertEquals(new BigDecimal("45.50"), response.getAmount());
        assertEquals("Food", response.getCategory());
        assertEquals(expenseDate, response.getExpenseDate());

        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void createExpense_shouldSetCorrectUserId() {

        when(expenseRepository.save(any(Expense.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        expenseService.createExpense(expenseRequest, 5L);

        verify(expenseRepository).save(argThat(savedExpense ->
                savedExpense.getUserId().equals(5L)
        ));
    }

    // ------------------------------------------------
    // FETCH ALL EXPENSES TESTS
    // ------------------------------------------------

    @Test
    void fetchAllExpenses_shouldReturnAllExpenses() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Expense> expensePage =
                new PageImpl<>(List.of(expense), pageable, 1);

        when(expenseRepository.findByUserId(userId, pageable))
                .thenReturn(expensePage);

        Page<ExpenseResponse> result =
                expenseService.getAllExpenses(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Groceries", result.getContent().get(0).getTitle());

        verify(expenseRepository).findByUserId(userId, pageable);
    }

    @Test
    void fetchAllExpenses_whenNoExpenses_shouldReturnEmptyPage() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Expense> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(expenseRepository.findByUserId(userId, pageable))
                .thenReturn(emptyPage);

        Page<ExpenseResponse> result =
                expenseService.getAllExpenses(userId, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(expenseRepository).findByUserId(userId, pageable);
    }

    @Test
    void fetchAllExpenses_shouldPreservePaginationDetails() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Expense> expensePage =
                new PageImpl<>(List.of(expense), pageable, 11);

        when(expenseRepository.findByUserId(userId, pageable))
                .thenReturn(expensePage);

        Page<ExpenseResponse> result =
                expenseService.getAllExpenses(userId, pageable);

        assertEquals(11, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());

        verify(expenseRepository).findByUserId(userId, pageable);
    }

    // ------------------------------------------------
    // FETCH EXPENSE BY ID TESTS
    // ------------------------------------------------

    @Test
    void getExpenseById_whenFound_shouldReturnExpense() {

        when(expenseRepository.findByIdAndUserId(1L, userId))
                .thenReturn(Optional.of(expense));

        Optional<ExpenseResponse> response =
                expenseService.getExpenseById(1L, userId);

        assertTrue(response.isPresent());
        assertEquals("Groceries", response.get().getTitle());

        verify(expenseRepository).findByIdAndUserId(1L, userId);
    }

    @Test
    void getExpenseById_whenNotFound_shouldReturnEmpty() {

        when(expenseRepository.findByIdAndUserId(99L, userId))
                .thenReturn(Optional.empty());

        Optional<ExpenseResponse> result =
                expenseService.getExpenseById(99L, userId);

        assertTrue(result.isEmpty());

        verify(expenseRepository).findByIdAndUserId(99L, userId);
    }

    // ------------------------------------------------
    // UPDATE EXPENSE TESTS
    // ------------------------------------------------

    @Test
    void updateExpense_whenFound_shouldUpdateAndReturnExpense() {

        ExpenseRequest updatedExpense = new ExpenseRequest();
        updatedExpense.setTitle("Updated Groceries");
        updatedExpense.setAmount(new BigDecimal("60.00"));
        updatedExpense.setCategory("Food");
        updatedExpense.setExpenseDate(expenseDate);

        when(expenseRepository.findByIdAndUserId(1L, userId))
                .thenReturn(Optional.of(expense));

        when(expenseRepository.save(expense))
                .thenReturn(expense);

        Optional<ExpenseResponse> result =
                expenseService.updateExpense(1L, updatedExpense, userId);

        assertTrue(result.isPresent());
        assertEquals("Updated Groceries", result.get().getTitle());
        assertEquals(new BigDecimal("60.00"), result.get().getAmount());

        verify(expenseRepository).findByIdAndUserId(1L, userId);
        verify(expenseRepository).save(expense);
    }

    @Test
    void updateExpense_whenNotFound_shouldReturnEmpty() {

        ExpenseRequest updatedExpense = new ExpenseRequest();
        updatedExpense.setTitle("Updated Groceries");

        when(expenseRepository.findByIdAndUserId(99L, userId))
                .thenReturn(Optional.empty());

        Optional<ExpenseResponse> result =
                expenseService.updateExpense(99L, updatedExpense, userId);

        assertTrue(result.isEmpty());

        verify(expenseRepository).findByIdAndUserId(99L, userId);
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    // ------------------------------------------------
    // DELETE EXPENSE TESTS
    // ------------------------------------------------

    @Test
    void deleteExpense_whenFound_shouldDeleteAndReturnTrue() {

        when(expenseRepository.findByIdAndUserId(1L, userId))
                .thenReturn(Optional.of(expense));

        Boolean result = expenseService.deleteExpense(1L, userId);

        assertTrue(result);

        verify(expenseRepository).findByIdAndUserId(1L, userId);
        verify(expenseRepository).delete(expense);
    }

    @Test
    void deleteExpense_whenNotFound_shouldReturnFalse() {

        when(expenseRepository.findByIdAndUserId(99L, userId))
                .thenReturn(Optional.empty());

        Boolean result = expenseService.deleteExpense(99L, userId);

        assertFalse(result);

        verify(expenseRepository).findByIdAndUserId(99L, userId);
        verify(expenseRepository, never()).delete(any(Expense.class));
    }

    // ------------------------------------------------
    // FILTER BY CATEGORY TESTS
    // ------------------------------------------------

    @Test
    void filterExpensesByCategory_shouldReturnMatchingExpenses() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Expense> expensePage =
                new PageImpl<>(List.of(expense), pageable, 1);

        when(expenseRepository.findByUserIdAndCategoryIgnoreCase(
                userId, "Food", pageable))
                .thenReturn(expensePage);

        Page<ExpenseResponse> result =
                expenseService.filterExpensesByCategory(
                        userId, pageable, "Food");

        assertEquals(1, result.getContent().size());
        assertEquals("Food", result.getContent().get(0).getCategory());

        verify(expenseRepository)
                .findByUserIdAndCategoryIgnoreCase(userId, "Food", pageable);
    }

    @Test
    void filterExpensesByCategory_whenNoMatch_shouldReturnEmptyPage() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Expense> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(expenseRepository.findByUserIdAndCategoryIgnoreCase(
                userId, "Travel", pageable))
                .thenReturn(emptyPage);

        Page<ExpenseResponse> result =
                expenseService.filterExpensesByCategory(
                        userId, pageable, "Travel");

        assertTrue(result.isEmpty());

        verify(expenseRepository)
                .findByUserIdAndCategoryIgnoreCase(userId, "Travel", pageable);
    }

    // ------------------------------------------------
    // FILTER BY DATE RANGE TESTS
    // ------------------------------------------------

    @Test
    void filterByDateRange_shouldReturnMatchingExpenses() {

        Pageable pageable = PageRequest.of(0, 10);

        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        Page<Expense> expensePage =
                new PageImpl<>(List.of(expense), pageable, 1);

        when(expenseRepository.findByUserIdAndExpenseDateBetween(
                userId, startDate, endDate, pageable))
                .thenReturn(expensePage);

        Page<ExpenseResponse> result =
                expenseService.filterByStartDateAndEndDate(
                        userId, pageable, startDate, endDate);

        assertEquals(1, result.getContent().size());
        assertEquals(expenseDate, result.getContent().get(0).getExpenseDate());

        verify(expenseRepository).findByUserIdAndExpenseDateBetween(
                userId, startDate, endDate, pageable);
    }

    @Test
    void filterByDateRange_whenNoMatch_shouldReturnEmptyPage() {

        Pageable pageable = PageRequest.of(0, 10);

        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        when(expenseRepository.findByUserIdAndExpenseDateBetween(
                userId, startDate, endDate, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        Page<ExpenseResponse> result =
                expenseService.filterByStartDateAndEndDate(
                        userId, pageable, startDate, endDate);

        assertTrue(result.isEmpty());

        verify(expenseRepository).findByUserIdAndExpenseDateBetween(
                userId, startDate, endDate, pageable);
    }

    // ------------------------------------------------
    // DASHBOARD CALCULATION TESTS
    // ------------------------------------------------

    @Test
    void getTotalExpenses_shouldReturnTotalAmount() {

        BigDecimal expectedTotal = new BigDecimal("500.00");

        when(expenseRepository.calculateTotalExpensesByUserId(userId))
                .thenReturn(expectedTotal);

        BigDecimal result = expenseService.getTotalExpenses(userId);

        assertEquals(expectedTotal, result);

        verify(expenseRepository).calculateTotalExpensesByUserId(userId);
    }

    @Test
    void getMonthlyExpense_shouldReturnMonthlyTotal() {

        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        BigDecimal expectedTotal = new BigDecimal("250.00");

        when(expenseRepository.calculateMonthlyExpenses(
                userId, startDate, endDate))
                .thenReturn(expectedTotal);

        BigDecimal result =
                expenseService.getMonthlyExpense(userId, startDate, endDate);

        assertEquals(expectedTotal, result);

        verify(expenseRepository).calculateMonthlyExpenses(
                userId, startDate, endDate);
    }

    @Test
    void getTotalTransactions_shouldReturnTransactionCount() {

        when(expenseRepository.countByUserId(userId))
                .thenReturn(5L);

        Long result = expenseService.getTotalTransactions(userId);

        assertEquals(5L, result);

        verify(expenseRepository).countByUserId(userId);
    }

    // ------------------------------------------------
    // REPORTING TESTS
    // ------------------------------------------------

    @Test
    void getHighestSpendingCategory_whenFound_shouldReturnCategory() {

        when(expenseRepository.findHighestSpendingCategory(userId))
                .thenReturn(List.of("Food", "Shopping"));

        String result =
                expenseService.getHighestSpendingCategory(userId);

        assertEquals("Food", result);

        verify(expenseRepository).findHighestSpendingCategory(userId);
    }

    @Test
    void getHighestSpendingCategory_whenEmpty_shouldReturnNA() {

        when(expenseRepository.findHighestSpendingCategory(userId))
                .thenReturn(List.of());

        String result =
                expenseService.getHighestSpendingCategory(userId);

        assertEquals("N/A", result);

        verify(expenseRepository).findHighestSpendingCategory(userId);
    }

    @Test
    void getMonthlySpending_shouldReturnMonthlyData() {

        List<Object[]> monthlyData = List.of(
                new Object[]{10, new BigDecimal("500.00")},
                new Object[]{11, new BigDecimal("750.00")}
        );

        when(expenseRepository.findMonthlySpending(userId, 2026))
                .thenReturn(monthlyData);

        List<Object[]> result =
                expenseService.getMonthlySpending(userId, 2026);

        assertEquals(2, result.size());
        assertEquals(10, result.get(0)[0]);
        assertEquals(new BigDecimal("500.00"), result.get(0)[1]);
        assertEquals(11, result.get(1)[0]);
        assertEquals(new BigDecimal("750.00"), result.get(1)[1]);

        verify(expenseRepository).findMonthlySpending(userId, 2026);
    }

    // ------------------------------------------------
    // SEARCH EXPENSE TESTS
    // ------------------------------------------------


    @Test
    void searchExpenses_shouldReturnMatchingExpenses() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Expense> expensePage =
                new PageImpl<>(List.of(expense), pageable, 1);

        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        when(expenseRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(expensePage);

        Page<ExpenseResponse> result = expenseService.searchExpenses(
                userId,
                "Groceries",
                "Food",
                startDate,
                endDate,
                pageable
        );

        assertEquals(1, result.getContent().size());
        assertEquals("Groceries", result.getContent().get(0).getTitle());

        verify(expenseRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }



    @Test
    void searchExpenses_whenNoMatch_shouldReturnEmptyPage() {

        Pageable pageable = PageRequest.of(0, 10);

        when(expenseRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(Page.empty(pageable));

        Page<ExpenseResponse> result = expenseService.searchExpenses(
                userId,
                "Unknown",
                null,
                null,
                null,
                pageable
        );

        assertTrue(result.isEmpty());

        verify(expenseRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }


    // ------------------------------------------------
    // CATEGORY SUMMARY TESTS
    // ------------------------------------------------

    @Test
    void getCategorySummary_shouldReturnCategoryData() {

        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        List<Object[]> summaryData = java.util.Collections.singletonList(
                new Object[]{
                        "Food",
                        new BigDecimal("90.00"),
                        2L,
                        new BigDecimal("45.00")
                }
        );

        when(expenseRepository.findCategorySummary(
                userId, startDate, endDate))
                .thenReturn(summaryData);

        List<CategorySummaryResponse> result =
                expenseService.getCategorySummary(
                        userId, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());

        CategorySummaryResponse response = result.get(0);

        assertEquals("Food", response.category());
        assertEquals(new BigDecimal("90.00"), response.totalAmount());
        assertEquals(2L, response.transactionCount());
        assertEquals(
                0,
                new BigDecimal("45.00").compareTo(response.averageAmount())
        );

        verify(expenseRepository).findCategorySummary(
                userId, startDate, endDate);
    }

    @Test
    void getCategorySummary_whenNoExpenses_shouldReturnEmptyList() {

        when(expenseRepository.findCategorySummary(
                userId, null, null))
                .thenReturn(List.of());

        List<CategorySummaryResponse> result =
                expenseService.getCategorySummary(userId, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(expenseRepository).findCategorySummary(
                userId, null, null);
    }
}
