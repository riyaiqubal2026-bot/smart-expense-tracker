package com.shawbin.expense_service.mapper;

import com.shawbin.expense_service.dto.ExpenseRequest;
import com.shawbin.expense_service.dto.ExpenseResponse;
import com.shawbin.expense_service.entity.Expense;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ExpenseMapperTest {


    @Test
    public void toEntity_shouldCopyRequestFields(){
        ExpenseRequest request =new ExpenseRequest();
        request.setTitle("Groceries");
        request.setAmount(BigDecimal.valueOf(100L));
        request.setCategory("Food");
        request.setExpenseDate(LocalDate.of(2026,8,9));

        Expense expenseEntity=ExpenseMapper.toEntity(request);
        assertEquals("Groceries", expenseEntity.getTitle());
        assertEquals("Food", expenseEntity.getCategory());
        assertEquals(new BigDecimal(100), expenseEntity.getAmount());
        assertEquals(
                LocalDate.of(2026, 8, 9),
                expenseEntity.getExpenseDate()
        );
        assertNull(expenseEntity.getId());
    }

    @Test
    public void toResponse_shouldCopyEntity(){
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setTitle("Utensils");
        expense.setCategory("Kitchen");
        expense.setAmount(BigDecimal.valueOf(230));
        expense.setExpenseDate(LocalDate.of(2026,8,5));

        ExpenseResponse response =ExpenseMapper.toResponse(expense);
        assertEquals(1L, response.getId());
        assertEquals("Utensils", response.getTitle());
        assertEquals("Kitchen", response.getCategory());
        assertEquals(new BigDecimal(230), response.getAmount());
        assertEquals(
                LocalDate.of(2026, 8, 5),
                response.getExpenseDate()
        );

    }

    @Test
    public void updateEntity_shouldUpdateEntityFromRequest(){
        ExpenseRequest request =new ExpenseRequest();
        request.setTitle("Groceries");
        request.setAmount(BigDecimal.valueOf(100L));
        request.setCategory("Food");
        request.setExpenseDate(LocalDate.of(2026,8,9));
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setTitle("Utensils");
        expense.setCategory("Kitchen");
        expense.setAmount(BigDecimal.valueOf(230));
        expense.setExpenseDate(LocalDate.of(2026,8,5));

        ExpenseMapper.updateEntity(expense,request);
        assertEquals(1L, expense.getId());
        assertEquals("Groceries", expense.getTitle());
        assertEquals("Food", expense.getCategory());
        assertEquals(new BigDecimal(100), expense.getAmount());
        assertEquals(
                LocalDate.of(2026, 8, 9),
                expense.getExpenseDate()
        );
    }
}
