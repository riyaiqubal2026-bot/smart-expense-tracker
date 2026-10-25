package com.shawbin.expense_service.service;

import com.shawbin.expense_service.entity.Expense;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private Expense expense;

    @BeforeEach
    public void setUp(){
        expense = new Expense();
        expense.setTitle("Groceries");
        expense.setCategory("Food");
        expense.setAmount(BigDecimal.valueOf(80));
        expense.setExpenseDate(LocalDate.of(2026, 10, 25));
    }

    @Test
    public void createExpense_shouldReturnSavedExpense(){

        when(expenseRepository.save(expense)).thenReturn(expense);
        Expense result=expenseService.createExpense(expense);
        assertNotNull(result);
        assertEquals("Groceries", result.getTitle());
        verify(expenseRepository).save(expense);

    }

    @Test
    public void fetchAllExpenses_shouldReturnAllExpenses(){
        doReturn(List.of(expense))
                .when(expenseRepository)
                .findAll();

        List<Expense> result=expenseService.getAllExpenses();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Groceries", result.get(0).getTitle());
        verify(expenseRepository).findAll();
    }

    @Test
    public void getExpenseById_whenFound_shouldReturnExpense(){
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        Optional<Expense> result= expenseService.getExpenseById(1L);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals("Groceries", result.get().getTitle());
        verify(expenseRepository).findById(1L);
    }

    @Test
    public void getExpenseById_whenNotFound_shouldReturnEmpty(){
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<Expense> result=expenseService.getExpenseById(99L);
        assertTrue(result.isEmpty());
    }


    @Test
    public void updateExpense_whenFound_shouldUpdateAndReturnExpense() {
        Expense updatedExpense = new Expense();
        updatedExpense.setTitle("Updated Groceries");
        updatedExpense.setAmount(new java.math.BigDecimal("45.50"));
        updatedExpense.setCategory("Food");
        updatedExpense.setExpenseDate(
                java.time.LocalDate.of(2026, 10, 26)
        );

        when(expenseRepository.findById(1L))
                .thenReturn(Optional.of(expense));
        when(expenseRepository.save(expense))
                .thenReturn(expense);

        Optional<Expense> result =
                expenseService.updateExpense(1L, updatedExpense);

        assertTrue(result.isPresent());
        assertEquals("Updated Groceries", result.get().getTitle());
        assertEquals(
                new java.math.BigDecimal("45.50"),
                result.get().getAmount()
        );
        assertEquals("Food", result.get().getCategory());
        assertEquals(
                java.time.LocalDate.of(2026, 10, 26),
                result.get().getExpenseDate()
        );

        verify(expenseRepository).findById(1L);
        verify(expenseRepository).save(expense);
    }

    @Test
    public void updateExpense_whenNotFound_shouldReturnEmpty() {
        Expense updatedExpense = new Expense();
        updatedExpense.setTitle("Updated Groceries");

        when(expenseRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<Expense> result =
                expenseService.updateExpense(99L, updatedExpense);

        assertTrue(result.isEmpty());
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    public void deleteExpense_whenFound_shouldDeleteAndReturnTrue() {
        when(expenseRepository.existsById(1L)).thenReturn(true);

        Boolean result = expenseService.deleteExpense(1L);

        assertTrue(result);
        verify(expenseRepository).deleteById(1L);
    }

    @Test
    public void deleteExpense_whenNotFound_shouldReturnFalse() {
        when(expenseRepository.existsById(99L)).thenReturn(false);

        Boolean result = expenseService.deleteExpense(99L);

        assertFalse(result);
        verify(expenseRepository, never()).deleteById(anyLong());
    }

}
