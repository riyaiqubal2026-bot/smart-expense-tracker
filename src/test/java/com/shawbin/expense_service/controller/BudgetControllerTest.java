
package com.shawbin.expense_service.controller;

import com.shawbin.expense_service.dto.BudgetRequest;
import com.shawbin.expense_service.dto.BudgetResponse;
import com.shawbin.expense_service.service.BudgetService;
import com.shawbin.expense_service.service.JwtService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetController.class)
@AutoConfigureMockMvc(addFilters = false)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BudgetService budgetService;

    @MockitoBean
    private JwtService jwtService;

    private final Long userId = 1L;

    private final LocalDate month =
            LocalDate.of(2026, 11, 1);

    private BudgetResponse createResponse() {
        return new BudgetResponse(
                1L,
                "Food",
                new BigDecimal("1000.00"),
                month,
                new BigDecimal("300.00"),
                new BigDecimal("700.00"),
                30.0
        );
    }

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of()
                )
        );
    }

    // --------------------------------------------------
    // GET BUDGETS
    // --------------------------------------------------

    @Test
    void getBudgets_shouldReturnBudgetsSuccessfully()
            throws Exception {

        when(budgetService.getBudgets(userId, month))
                .thenReturn(List.of(createResponse()));

        mockMvc.perform(get("/api/budgets")
                        .param("month", "2026-11-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].category").value("Food"))
                .andExpect(jsonPath("$[0].budgetAmount").value(1000.00))
                .andExpect(jsonPath("$[0].totalSpent").value(300.00))
                .andExpect(jsonPath("$[0].remainingAmount").value(700.00))
                .andExpect(jsonPath("$[0].percentageUsed").value(30.0));

        verify(budgetService).getBudgets(userId, month);
    }

    @Test
    void getBudgets_shouldReturnEmptyList()
            throws Exception {

        when(budgetService.getBudgets(userId, month))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/budgets")
                        .param("month", "2026-11-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(budgetService).getBudgets(userId, month);
    }

    @Test
    void getBudgets_shouldReturnBadRequestForInvalidMonth()
            throws Exception {

        mockMvc.perform(get("/api/budgets")
                        .param("month", "invalid-date"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(budgetService);
    }

    @Test
    void getBudgets_shouldReturnBadRequestWhenMonthIsMissing()
            throws Exception {

        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(budgetService);
    }

    // --------------------------------------------------
    // CREATE BUDGET
    // --------------------------------------------------

    @Test
    void createBudget_shouldReturnCreatedSuccessfully()
            throws Exception {

        when(budgetService.createBudget(
                eq(userId),
                any(BudgetRequest.class)
        )).thenReturn(createResponse());

        String requestJson = """
                {
                    "category": "Food",
                    "budgetAmount": 1000.00,
                    "budgetMonth": "2026-11-01"
                }
                """;

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.budgetAmount").value(1000.00));

        verify(budgetService).createBudget(
                eq(userId),
                any(BudgetRequest.class)
        );
    }

    @Test
    void createBudget_shouldReturnBadRequestWhenCategoryIsBlank()
            throws Exception {

        String requestJson = """
                {
                    "category": "",
                    "budgetAmount": 1000.00,
                    "budgetMonth": "2026-11-01"
                }
                """;

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(budgetService);
    }

    @Test
    void createBudget_shouldReturnBadRequestWhenAmountIsZero()
            throws Exception {

        String requestJson = """
                {
                    "category": "Food",
                    "budgetAmount": 0,
                    "budgetMonth": "2026-11-01"
                }
                """;

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(budgetService);
    }

    @Test
    void createBudget_shouldReturnBadRequestWhenAmountIsMissing()
            throws Exception {

        String requestJson = """
                {
                    "category": "Food",
                    "budgetMonth": "2026-11-01"
                }
                """;

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(budgetService);
    }

    @Test
    void createBudget_shouldReturnBadRequestWhenMonthIsMissing()
            throws Exception {

        String requestJson = """
                {
                    "category": "Food",
                    "budgetAmount": 1000.00
                }
                """;

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(budgetService);
    }

    // --------------------------------------------------
    // UPDATE BUDGET
    // --------------------------------------------------

    @Test
    void updateBudget_shouldReturnUpdatedBudgetSuccessfully()
            throws Exception {

        when(budgetService.updateBudget(
                eq(userId),
                eq(1L),
                any(BudgetRequest.class)
        )).thenReturn(createResponse());

        String requestJson = """
                {
                    "category": "Food",
                    "budgetAmount": 1200.00,
                    "budgetMonth": "2026-11-01"
                }
                """;

        mockMvc.perform(put("/api/budgets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Food"))
                .andExpect(jsonPath("$.id").value(1));

        verify(budgetService).updateBudget(
                eq(userId),
                eq(1L),
                any(BudgetRequest.class)
        );
    }

    @Test
    void updateBudget_shouldReturnBadRequestWhenCategoryIsBlank()
            throws Exception {

        String requestJson = """
                {
                    "category": "",
                    "budgetAmount": 1200.00,
                    "budgetMonth": "2026-11-01"
                }
                """;

        mockMvc.perform(put("/api/budgets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(budgetService);
    }

    // --------------------------------------------------
    // DELETE BUDGET
    // --------------------------------------------------

    @Test
    void deleteBudget_shouldReturnNoContentSuccessfully()
            throws Exception {

        doNothing().when(budgetService)
                .deleteBudget(userId, 1L);

        mockMvc.perform(delete("/api/budgets/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(budgetService).deleteBudget(userId, 1L);
    }
}
