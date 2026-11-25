
package com.shawbin.expense_service.controller;

import com.shawbin.expense_service.dto.CategorySummaryResponse;
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
import static org.mockito.Mockito.verify;
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

    private final Long userId = 1L;

    private final LocalDate expenseDate = LocalDate.of(2026, 10, 26);

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
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

    private ExpenseRequest createRequest() {
        ExpenseRequest request = new ExpenseRequest();
        request.setTitle("Groceries");
        request.setCategory("Food");
        request.setAmount(new BigDecimal("45.50"));
        request.setExpenseDate(expenseDate);
        return request;
    }

    private ExpenseResponse createResponse() {
        return new ExpenseResponse(
                1L,
                userId,
                "Groceries",
                "Food",
                new BigDecimal("45.50"),
                expenseDate
        );
    }

    // CREATE EXPENSE

    @Test
    void createExpense_shouldReturn201() throws Exception {

        when(expenseService.createExpense(
                any(ExpenseRequest.class), eq(userId)))
                .thenReturn(createResponse());

        mockMvc.perform(post("/api/expenses/saveExpense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.title").value("Groceries"))
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.amount").value(45.50));

        verify(expenseService).createExpense(
                any(ExpenseRequest.class), eq(userId));
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
    void createExpense_withInvalidUser_shouldReturn404() throws Exception {

        when(expenseService.createExpense(
                any(ExpenseRequest.class), eq(userId)))
                .thenThrow(new UserNotFoundException(
                        "User not found with id: 1"
                ));

        mockMvc.perform(post("/api/expenses/saveExpense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void createExpense_whenUserServiceUnavailable_shouldReturn503()
            throws Exception {

        when(expenseService.createExpense(
                any(ExpenseRequest.class), eq(userId)))
                .thenThrow(new UserServiceUnavailableException(
                        "User Service is currently unavailable"
                ));

        mockMvc.perform(post("/api/expenses/saveExpense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service Unavailable"));
    }

    // FETCH ALL EXPENSES

    @Test
    void getAllExpenses_shouldReturn200AndExpenseList() throws Exception {

        Pageable pageable = PageRequest.of(0, 10);

        when(expenseService.getAllExpenses(
                eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(createResponse()), pageable, 1));

        mockMvc.perform(get("/api/expenses/fetchAllExpenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Groceries"))
                .andExpect(jsonPath("$.content[0].category").value("Food"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAllExpenses_whenEmpty_shouldReturnEmptyPage() throws Exception {

        when(expenseService.getAllExpenses(
                eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/expenses/fetchAllExpenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getAllExpenses_shouldSupportPagination() throws Exception {

        when(expenseService.getAllExpenses(
                eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(createResponse()),
                        PageRequest.of(1, 5),
                        6
                ));

        mockMvc.perform(get("/api/expenses/fetchAllExpenses")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(6));
    }

    // FETCH EXPENSE BY ID

    @Test
    void getExpenseById_whenFound_shouldReturn200() throws Exception {

        when(expenseService.getExpenseById(1L, userId))
                .thenReturn(Optional.of(createResponse()));

        mockMvc.perform(get("/api/expenses/fetchExpense/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Groceries"));
    }

    @Test
    void getExpenseById_whenNotFound_shouldReturn404() throws Exception {

        when(expenseService.getExpenseById(999L, userId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/expenses/fetchExpense/999"))
                .andExpect(status().isNotFound());
    }

    // UPDATE EXPENSE

    @Test
    void updateExpense_shouldReturn200AndUpdatedExpense() throws Exception {

        ExpenseRequest request = createRequest();
        request.setTitle("Transport");
        request.setCategory("Travel");
        request.setAmount(new BigDecimal("20.00"));

        ExpenseResponse response = new ExpenseResponse(
                1L,
                userId,
                "Transport",
                "Travel",
                new BigDecimal("20.00"),
                expenseDate
        );

        when(expenseService.updateExpense(
                eq(1L), any(ExpenseRequest.class), eq(userId)))
                .thenReturn(Optional.of(response));

        mockMvc.perform(put("/api/expenses/updateExpense/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Transport"))
                .andExpect(jsonPath("$.category").value("Travel"))
                .andExpect(jsonPath("$.amount").value(20.00));
    }

    @Test
    void updateExpense_whenNotFound_shouldReturn404() throws Exception {

        when(expenseService.updateExpense(
                eq(999L), any(ExpenseRequest.class), eq(userId)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/expenses/updateExpense/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateExpense_withBlankTitle_shouldReturn400() throws Exception {

        ExpenseRequest request = createRequest();
        request.setTitle("");

        mockMvc.perform(put("/api/expenses/updateExpense/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // DELETE EXPENSE

    @Test
    void deleteExpense_shouldReturn204() throws Exception {

        when(expenseService.deleteExpense(1L, userId))
                .thenReturn(true);

        mockMvc.perform(delete("/api/expenses/deleteExpense/1"))
                .andExpect(status().isNoContent());

        verify(expenseService).deleteExpense(1L, userId);
    }

    @Test
    void deleteExpense_whenNotFound_shouldReturn404() throws Exception {

        when(expenseService.deleteExpense(999L, userId))
                .thenReturn(false);

        mockMvc.perform(delete("/api/expenses/deleteExpense/999"))
                .andExpect(status().isNotFound());
    }

    // FILTER BY CATEGORY

    @Test
    void getExpenseByCategory_shouldReturnMatchingExpenses() throws Exception {

        when(expenseService.filterExpensesByCategory(
                eq(userId), any(Pageable.class), eq("Food")))
                .thenReturn(new PageImpl<>(
                        List.of(createResponse())));

        mockMvc.perform(get("/api/expenses/getExpenseByCategory/Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].category").value("Food"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getExpenseByCategory_whenNoMatch_shouldReturnEmptyPage()
            throws Exception {

        when(expenseService.filterExpensesByCategory(
                eq(userId), any(Pageable.class), eq("Travel")))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/expenses/getExpenseByCategory/Travel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    // FILTER BY DATE RANGE

    @Test
    void getExpensesByDateRange_shouldReturnMatchingExpenses()
            throws Exception {

        LocalDate startDate = LocalDate.of(2026, 10, 1);
        LocalDate endDate = LocalDate.of(2026, 10, 31);

        when(expenseService.filterByStartDateAndEndDate(
                eq(userId),
                any(Pageable.class),
                eq(startDate),
                eq(endDate)))
                .thenReturn(new PageImpl<>(
                        List.of(createResponse())));

        mockMvc.perform(get("/api/expenses/dateRange")
                        .param("startDate", "2026-10-01")
                        .param("endDate", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Groceries"));
    }

    @Test
    void getExpensesByDateRange_withInvalidDate_shouldReturn400()
            throws Exception {

        mockMvc.perform(get("/api/expenses/dateRange")
                        .param("startDate", "invalid-date")
                        .param("endDate", "2026-10-31"))
                .andExpect(status().isBadRequest());
    }

    // TOTAL EXPENSES

    @Test
    void getTotalExpenses_shouldReturnTotal() throws Exception {

        when(expenseService.getTotalExpenses(userId))
                .thenReturn(new BigDecimal("1500.50"));

        mockMvc.perform(get("/api/expenses/total"))
                .andExpect(status().isOk())
                .andExpect(content().json("1500.50"));
    }

    // MONTHLY EXPENSES

    @Test
    void getMonthlyExpenses_shouldReturnMonthlyTotal() throws Exception {

        when(expenseService.getMonthlyExpense(
                eq(userId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("750.00"));

        mockMvc.perform(get("/api/expenses/monthly-total"))
                .andExpect(status().isOk())
                .andExpect(content().json("750.00"));
    }

    // TOTAL TRANSACTIONS

    @Test
    void getTotalTransaction_shouldReturnCount() throws Exception {

        when(expenseService.getTotalTransactions(userId))
                .thenReturn(15L);

        mockMvc.perform(get("/api/expenses/total-transaction"))
                .andExpect(status().isOk())
                .andExpect(content().json("15"));
    }

    // HIGHEST SPENDING CATEGORY

    @Test
    void getHighestSpendingCategory_shouldReturnCategory()
            throws Exception {

        when(expenseService.getHighestSpendingCategory(userId))
                .thenReturn("Food");

        mockMvc.perform(get("/api/expenses/highest-spending-category"))
                .andExpect(status().isOk())
                .andExpect(content().string("Food"));
    }

    // MONTHLY SPENDING

    @Test
    void getMonthlySpending_shouldReturnMonthlyData() throws Exception {

        List<Object[]> monthlyData = Collections.singletonList(
                new Object[]{10, new BigDecimal("500.00")}
        );

        when(expenseService.getMonthlySpending(userId, 2026))
                .thenReturn(monthlyData);

        mockMvc.perform(get("/api/expenses/monthly-spending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0][0]").value(10))
                .andExpect(jsonPath("$[0][1]").value(500.00));
    }

    @Test
    void getMonthlySpending_shouldAcceptCustomYear() throws Exception {

        when(expenseService.getMonthlySpending(userId, 2025))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/expenses/monthly-spending")
                        .param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // SEARCH EXPENSES

    @Test
    void searchExpenses_shouldReturnMatchingExpenses() throws Exception {

        when(expenseService.searchExpenses(
                eq(userId),
                eq("Groc"),
                eq("Food"),
                eq(expenseDate),
                eq(expenseDate),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(createResponse())));

        mockMvc.perform(get("/api/expenses/search")
                        .param("title", "Groc")
                        .param("category", "Food")
                        .param("startDate", "2026-10-26")
                        .param("endDate", "2026-10-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Groceries"));
    }

    @Test
    void searchExpenses_withoutFilters_shouldReturnResults()
            throws Exception {

        when(expenseService.searchExpenses(
                eq(userId),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(createResponse())));

        mockMvc.perform(get("/api/expenses/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void searchExpenses_withInvalidDate_shouldReturn400()
            throws Exception {

        mockMvc.perform(get("/api/expenses/search")
                        .param("startDate", "not-a-date"))
                .andExpect(status().isBadRequest());
    }

    // CATEGORY SUMMARY

    @Test
    void getCategorySummary_shouldReturnCategoryData() throws Exception {

        List<CategorySummaryResponse> summary = List.of(
                new CategorySummaryResponse(
                        "Food",
                        new BigDecimal("500.00"),
                        10L,
                        new BigDecimal("50.00")
                )
        );

        when(expenseService.getCategorySummary(
                userId,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)))
                .thenReturn(summary);

        mockMvc.perform(get("/api/expenses/category-summary")
                        .param("startDate", "2026-10-01")
                        .param("endDate", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Food"))
                .andExpect(jsonPath("$[0].totalAmount").value(500.00))
                .andExpect(jsonPath("$[0].transactionCount").value(10))
                .andExpect(jsonPath("$[0].averageAmount").value(50.00));
    }

    @Test
    void getCategorySummary_withoutDates_shouldReturnSummary()
            throws Exception {

        when(expenseService.getCategorySummary(
                userId, null, null))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/expenses/category-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCategorySummary_withInvalidDate_shouldReturn400()
            throws Exception {

        mockMvc.perform(get("/api/expenses/category-summary")
                        .param("startDate", "invalid-date"))
                .andExpect(status().isBadRequest());
    }
}
