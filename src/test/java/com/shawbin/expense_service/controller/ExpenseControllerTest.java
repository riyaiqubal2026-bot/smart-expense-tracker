
package com.shawbin.expense_service.controller;

import com.shawbin.expense_service.dto.ExpenseRequest;
import com.shawbin.expense_service.dto.ExpenseResponse;
import com.shawbin.expense_service.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseService expenseService;

    @Test
    void createExpense_shouldReturn201() throws Exception {
        ExpenseRequest request = new ExpenseRequest();
        request.setTitle("Groceries");
        request.setCategory("Food");
        request.setAmount(new BigDecimal("45.50"));
        request.setExpenseDate(LocalDate.of(2026, 10, 26));

        ExpenseResponse response = new ExpenseResponse(
                1L,
                "Groceries",
                "Food",
                new BigDecimal("45.50"),
                LocalDate.of(2026, 10, 26)
        );

        when(expenseService.createExpense(any(ExpenseRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/expenses/saveExpense")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Groceries"))
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.amount").value(45.50));
    }


    @Test
    void createExpense_withBlankTitle_shouldReturn400() throws Exception {
        String invalidRequest = """
            {
                "title": "",
                "category": "Food",
                "amount": 45.50,
                "expenseDate": "2026-10-26"
            }
            """;

        mockMvc.perform(post("/api/expenses/saveExpense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getAllExpenses_shouldReturn200AndExpenseList() throws Exception {
        ExpenseResponse expense = new ExpenseResponse(
                1L,
                "Groceries",
                "Food",
                new BigDecimal("45.50"),
                LocalDate.of(2026, 10, 26)
        );

        when(expenseService.getAllExpenses())
                .thenReturn(java.util.List.of(expense));

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/expenses/fetchAllExpenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Groceries"))
                .andExpect(jsonPath("$[0].category").value("Food"))
                .andExpect(jsonPath("$[0].amount").value(45.50));
    }


    @Test
    void getExpenseById_whenNotFound_shouldReturn404() throws Exception {
        when(expenseService.getExpenseById(999L))
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/expenses/fetchExpense/999"))
                .andExpect(status().isNotFound());
    }


    @Test
    void updateExpense_shouldReturn200AndUpdatedExpense() throws Exception {
        ExpenseRequest request = new ExpenseRequest();
        request.setTitle("Transport");
        request.setCategory("Travel");
        request.setAmount(new BigDecimal("20.00"));
        request.setExpenseDate(LocalDate.of(2026, 10, 26));

        ExpenseResponse response = new ExpenseResponse(
                1L,
                "Transport",
                "Travel",
                new BigDecimal("20.00"),
                LocalDate.of(2026, 10, 26)
        );

        when(expenseService.updateExpense(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(ExpenseRequest.class)))
                .thenReturn(java.util.Optional.of(response));

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .put("/api/expenses/updateExpense/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Transport"))
                .andExpect(jsonPath("$.category").value("Travel"))
                .andExpect(jsonPath("$.amount").value(20.00));
    }



    @Test
    void updateExpense_whenNotFound_shouldReturn404() throws Exception {
        ExpenseRequest request = new ExpenseRequest();
        request.setTitle("Transport");
        request.setCategory("Travel");
        request.setAmount(new BigDecimal("20.00"));
        request.setExpenseDate(LocalDate.of(2026, 10, 26));

        when(expenseService.updateExpense(
                org.mockito.ArgumentMatchers.eq(999L),
                org.mockito.ArgumentMatchers.any(ExpenseRequest.class)))
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .put("/api/expenses/updateExpense/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }



    @Test
    void deleteExpense_shouldReturn204() throws Exception {
        when(expenseService.deleteExpense(1L)).thenReturn(true);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/api/expenses/deleteExpense/1"))
                .andExpect(status().isNoContent());
    }


    @Test
    void deleteExpense_whenNotFound_shouldReturn404() throws Exception {
        when(expenseService.deleteExpense(999L)).thenReturn(false);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/api/expenses/deleteExpense/999"))
                .andExpect(status().isNotFound());
    }





}
