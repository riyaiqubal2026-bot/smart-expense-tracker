
package com.shawbin.expense_service.controller;

import com.shawbin.expense_service.dto.ExpenseRequest;
import com.shawbin.expense_service.dto.ExpenseResponse;
import com.shawbin.expense_service.exception.UserNotFoundException;
import com.shawbin.expense_service.exception.UserServiceUnavailableException;
import com.shawbin.expense_service.security.JwtAuthenticationFilter;
import com.shawbin.expense_service.service.ExpenseService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        1L,
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createExpense_shouldReturn201() throws Exception {

        ExpenseRequest request = new ExpenseRequest();
        request.setTitle("Groceries");
        request.setCategory("Food");
        request.setAmount(new BigDecimal("45.50"));
        request.setExpenseDate(LocalDate.of(2026, 10, 26));

        ExpenseResponse response = new ExpenseResponse(
                2L,
                1L,
                "Groceries",
                "Food",
                new BigDecimal("45.50"),
                LocalDate.of(2026, 10, 26)
        );

        when(expenseService.createExpense(
                any(ExpenseRequest.class), eq(1L)))
                .thenReturn(response);

        mockMvc.perform(post("/api/expenses/saveExpense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
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
                1L,
                "Groceries",
                "Food",
                new BigDecimal("45.50"),
                LocalDate.of(2026, 10, 26)
        );

        Pageable pageable = PageRequest.of(0, 10);

        when(expenseService.getAllExpenses(
                eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(expense), pageable, 1));

        mockMvc.perform(get("/api/expenses/fetchAllExpenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Groceries"))
                .andExpect(jsonPath("$.content[0].category").value("Food"))
                .andExpect(jsonPath("$.content[0].amount").value(45.50));
    }

    @Test
    void getExpenseById_whenNotFound_shouldReturn404() throws Exception {

        when(expenseService.getExpenseById(999L, 1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/expenses/fetchExpense/999"))
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
                1L,
                "Transport",
                "Travel",
                new BigDecimal("20.00"),
                LocalDate.of(2026, 10, 26)
        );

        when(expenseService.updateExpense(
                eq(1L),
                any(ExpenseRequest.class),
                eq(1L)))
                .thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/expenses/updateExpense/1")
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
                eq(999L),
                any(ExpenseRequest.class),
                eq(1L)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/expenses/updateExpense/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteExpense_shouldReturn204() throws Exception {

        when(expenseService.deleteExpense(1L, 1L))
                .thenReturn(true);

        mockMvc.perform(delete("/api/expenses/deleteExpense/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteExpense_whenNotFound_shouldReturn404() throws Exception {

        when(expenseService.deleteExpense(999L, 1L))
                .thenReturn(false);

        mockMvc.perform(delete("/api/expenses/deleteExpense/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createExpense_withInvalidUser_shouldReturn404() throws Exception {

        ExpenseRequest request = new ExpenseRequest();
        request.setTitle("Groceries");
        request.setCategory("Food");
        request.setAmount(new BigDecimal("45.50"));
        request.setExpenseDate(LocalDate.of(2026, 10, 26));

        when(expenseService.createExpense(
                any(ExpenseRequest.class), eq(1L)))
                .thenThrow(new UserNotFoundException(
                        "User not found with id: 1"
                ));

        mockMvc.perform(post("/api/expenses/saveExpense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void createExpense_whenUserServiceUnavailable_shouldReturn503()
            throws Exception {

        ExpenseRequest request = new ExpenseRequest();
        request.setTitle("Groceries");
        request.setCategory("Food");
        request.setAmount(new BigDecimal("45.50"));
        request.setExpenseDate(LocalDate.of(2026, 10, 26));

        when(expenseService.createExpense(
                any(ExpenseRequest.class), eq(1L)))
                .thenThrow(new UserServiceUnavailableException(
                        "User Service is currently unavailable"
                ));

        mockMvc.perform(post("/api/expenses/saveExpense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service Unavailable"));
    }
}
