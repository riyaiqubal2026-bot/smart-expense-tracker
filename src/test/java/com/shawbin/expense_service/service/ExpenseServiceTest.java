
package com.shawbin.expense_service.service;

import com.shawbin.expense_service.client.UserServiceClient;
import com.shawbin.expense_service.dto.ExpenseRequest;
import com.shawbin.expense_service.dto.ExpenseResponse;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Mock
    private UserServiceClient userServiceClient;

    private ExpenseRequest expenseRequest;
    private Expense expense;

    @BeforeEach
    void setUp() {
        expenseRequest = new ExpenseRequest();
        expenseRequest.setTitle("Groceries");
        expenseRequest.setAmount(new BigDecimal("45.50"));
        expenseRequest.setCategory("Food");
        expenseRequest.setExpenseDate(LocalDate.of(2026, 10, 26));

        expense = new Expense();
        expense.setUserId(1L);
        expense.setId(1L);
        expense.setTitle("Groceries");
        expense.setAmount(new BigDecimal("45.50"));
        expense.setCategory("Food");
        expense.setExpenseDate(LocalDate.of(2026, 10, 26));
    }

    @Test
    public void createExpense_shouldReturnSavedExpense() {

        doNothing().when(userServiceClient).validateUser(1L);

        when(expenseRepository.save(any(Expense.class)))
                .thenReturn(expense);

        ExpenseResponse response =
                expenseService.createExpense(expenseRequest, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Groceries", response.getTitle());
        assertEquals(new BigDecimal("45.50"), response.getAmount());
        assertEquals("Food", response.getCategory());
        assertEquals(LocalDate.of(2026, 10, 26), response.getExpenseDate());

        verify(userServiceClient).validateUser(1L);
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    public void fetchAllExpenses_shouldReturnAllExpenses() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Expense> expensePage =
                new PageImpl<>(List.of(expense), pageable, 1);

        when(expenseRepository.findByUserId(1L, pageable))
                .thenReturn(expensePage);

        Page<ExpenseResponse> result =
                expenseService.getAllExpenses(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Groceries", result.getContent().get(0).getTitle());

        verify(expenseRepository).findByUserId(1L, pageable);
    }

    @Test
    public void getExpenseById_whenFound_shouldReturnExpense() {

        when(expenseRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(expense));

        Optional<ExpenseResponse> response =
                expenseService.getExpenseById(1L, 1L);

        assertNotNull(response);
        assertTrue(response.isPresent());
        assertEquals("Groceries", response.get().getTitle());

        verify(expenseRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    public void getExpenseById_whenNotFound_shouldReturnEmpty() {

        when(expenseRepository.findByIdAndUserId(99L, 1L))
                .thenReturn(Optional.empty());

        Optional<ExpenseResponse> result =
                expenseService.getExpenseById(99L, 1L);

        assertTrue(result.isEmpty());

        verify(expenseRepository).findByIdAndUserId(99L, 1L);
    }

    @Test
    public void updateExpense_whenFound_shouldUpdateAndReturnExpense() {

        ExpenseRequest updatedExpense = new ExpenseRequest();
        updatedExpense.setTitle("Updated Groceries");
        updatedExpense.setAmount(new BigDecimal("45.50"));
        updatedExpense.setCategory("Food");
        updatedExpense.setExpenseDate(LocalDate.of(2026, 10, 26));

        when(expenseRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(expense));

        when(expenseRepository.save(expense))
                .thenReturn(expense);

        Optional<ExpenseResponse> result =
                expenseService.updateExpense(1L, updatedExpense, 1L);

        assertTrue(result.isPresent());
        assertEquals("Updated Groceries", result.get().getTitle());
        assertEquals(new BigDecimal("45.50"), result.get().getAmount());
        assertEquals("Food", result.get().getCategory());
        assertEquals(
                LocalDate.of(2026, 10, 26),
                result.get().getExpenseDate()
        );

        verify(expenseRepository).findByIdAndUserId(1L, 1L);
        verify(expenseRepository).save(expense);
    }

    @Test
    public void updateExpense_whenNotFound_shouldReturnEmpty() {

        ExpenseRequest updatedExpense = new ExpenseRequest();
        updatedExpense.setTitle("Updated Groceries");

        when(expenseRepository.findByIdAndUserId(99L, 1L))
                .thenReturn(Optional.empty());

        Optional<ExpenseResponse> result =
                expenseService.updateExpense(99L, updatedExpense, 1L);

        assertTrue(result.isEmpty());

        verify(expenseRepository).findByIdAndUserId(99L, 1L);
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    public void deleteExpense_whenFound_shouldDeleteAndReturnTrue() {

        when(expenseRepository.findByIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(expense));

        Boolean result = expenseService.deleteExpense(1L, 1L);

        assertTrue(result);

        verify(expenseRepository).findByIdAndUserId(1L, 1L);
        verify(expenseRepository).delete(expense);
    }

    @Test
    public void deleteExpense_whenNotFound_shouldReturnFalse() {

        when(expenseRepository.findByIdAndUserId(99L, 1L))
                .thenReturn(Optional.empty());

        Boolean result = expenseService.deleteExpense(99L, 1L);

        assertFalse(result);

        verify(expenseRepository).findByIdAndUserId(99L, 1L);
        verify(expenseRepository, never()).delete(any(Expense.class));
    }
}
