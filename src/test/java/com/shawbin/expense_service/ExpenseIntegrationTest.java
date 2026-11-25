
package com.shawbin.expense_service;

import com.shawbin.expense_service.dto.ExpenseRequest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
})
@AutoConfigureMockMvc
class ExpenseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String SECRET =
            "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    private String generateToken(Long userId) {

        SecretKey key = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(SECRET));

        return Jwts.builder()
                .subject(userId.toString())
                .expiration(Date.from(
                        Instant.now().plusSeconds(600)))
                .signWith(key)
                .compact();
    }

    private ExpenseRequest createRequest(
            Long userId,
            String title,
            String category,
            String amount) {

        ExpenseRequest request = new ExpenseRequest();

        request.setTitle(title);
        request.setCategory(category);
        request.setAmount(new BigDecimal(amount));
        request.setExpenseDate(LocalDate.now());

        return request;
    }

    private MvcResult createExpense(
            Long userId,
            String title) throws Exception {

        ExpenseRequest request = createRequest(
                userId, title, "Food", "25.50");

        return mockMvc.perform(post("/api/expenses/saveExpense")
                        .header("Authorization",
                                "Bearer " + generateToken(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private Long extractExpenseId(MvcResult result)
            throws Exception {

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString());

        return response.get("id").asLong();
    }

    // 1. Create expense using a real JWT

    @Test
    void createExpense_withValidJwt_shouldReturn201()
            throws Exception {

        MvcResult result = createExpense(
                4L, "Integration Test Expense");

        mockMvc.perform(get("/api/expenses/fetchExpense/{id}",
                        extractExpenseId(result))
                        .header("Authorization",
                                "Bearer " + generateToken(4L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("Integration Test Expense"))
                .andExpect(jsonPath("$.userId").value(4));
    }

    // 2. Retrieve saved expense

    @Test
    void fetchExpense_withValidJwt_shouldReturnSavedExpense()
            throws Exception {

        MvcResult result = createExpense(
                4L, "Retrieve Integration Expense");

        Long expenseId = extractExpenseId(result);

        mockMvc.perform(get("/api/expenses/fetchExpense/{id}",
                        expenseId)
                        .header("Authorization",
                                "Bearer " + generateToken(4L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId))
                .andExpect(jsonPath("$.title")
                        .value("Retrieve Integration Expense"));
    }

    // 3. Update saved expense

    @Test
    void updateExpense_withValidJwt_shouldUpdateExpense()
            throws Exception {

        MvcResult result = createExpense(
                4L, "Original Expense");

        Long expenseId = extractExpenseId(result);

        ExpenseRequest updatedRequest = createRequest(
                4L, "Updated Expense", "Transport", "50.00");

        mockMvc.perform(put("/api/expenses/updateExpense/{id}",
                        expenseId)
                        .header("Authorization",
                                "Bearer " + generateToken(4L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                updatedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("Updated Expense"))
                .andExpect(jsonPath("$.category")
                        .value("Transport"));
    }

    // 4. Delete saved expense

    @Test
    void deleteExpense_withValidJwt_shouldDeleteExpense()
            throws Exception {

        MvcResult result = createExpense(
                4L, "Expense To Delete");

        Long expenseId = extractExpenseId(result);

        mockMvc.perform(delete(
                        "/api/expenses/deleteExpense/{id}",
                        expenseId)
                        .header("Authorization",
                                "Bearer " + generateToken(4L)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/expenses/fetchExpense/{id}",
                        expenseId)
                        .header("Authorization",
                                "Bearer " + generateToken(4L)))
                .andExpect(status().isNotFound());
    }

    // 5. Prevent another user from accessing the expense

    @Test
    void fetchExpense_withDifferentUser_shouldReturn404()
            throws Exception {

        MvcResult result = createExpense(
                4L, "Private User Expense");

        Long expenseId = extractExpenseId(result);

        mockMvc.perform(get("/api/expenses/fetchExpense/{id}",
                        expenseId)
                        .header("Authorization",
                                "Bearer " + generateToken(5L)))
                .andExpect(status().isNotFound());
    }

    // 6. Reject request without JWT

    @Test
    void protectedEndpoint_withoutToken_shouldReturn401()
            throws Exception {

        mockMvc.perform(get("/api/expenses/total"))
                .andExpect(status().isUnauthorized());
    }

    // 7. Reject request with invalid JWT

    @Test
    void protectedEndpoint_withInvalidToken_shouldReturn401()
            throws Exception {

        mockMvc.perform(get("/api/expenses/total")
                        .header("Authorization",
                                "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}
