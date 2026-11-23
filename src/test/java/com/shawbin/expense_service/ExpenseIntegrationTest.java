
package com.shawbin.expense_service;

import com.shawbin.expense_service.dto.ExpenseRequest;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ExpenseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private RequestPostProcessor authenticatedAs(Long userId) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );

        return authentication(authenticationToken);
    }

    @Test
    void createExpense_withAuthenticatedUser_shouldReturn201()
            throws Exception {

        ExpenseRequest request = new ExpenseRequest();
        request.setTitle("Integration Test Expense");
        request.setCategory("Food");
        request.setAmount(new BigDecimal("25.50"));
        request.setExpenseDate(LocalDate.now());

        mockMvc.perform(post("/api/expenses/saveExpense")
                        .with(authenticatedAs(4L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title")
                        .value("Integration Test Expense"))
                .andExpect(jsonPath("$.userId").value(4));
    }
}
