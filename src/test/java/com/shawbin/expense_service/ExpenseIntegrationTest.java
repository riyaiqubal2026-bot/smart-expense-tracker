
package com.shawbin.expense_service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.shawbin.expense_service.dto.ExpenseRequest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

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

    static WireMockServer wireMockServer =
            new WireMockServer(options().dynamicPort());

    static {
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("user.service.url", wireMockServer::baseUrl);
    }

    @BeforeEach
    void configureWireMockResponses() {

        wireMockServer.resetAll();

        // User 4 exists
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/users/fetchUser/4"))
                        .willReturn(aResponse().withStatus(200))
        );

        // User 999 does not exist
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/users/fetchUser/999"))
                        .willReturn(aResponse().withStatus(404))
        );

        // Simulate User Service returning an error
        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/users/fetchUser/500"))
                        .willReturn(aResponse().withStatus(503))
        );
    }

    // Creates an authenticated request for a particular user
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
    void createExpense_withValidUser_shouldReturn201() throws Exception {

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

    @Test
    void createExpense_withInvalidUser_shouldReturn404() throws Exception {

        ExpenseRequest request = new ExpenseRequest();
        request.setTitle("Invalid User Expense");
        request.setCategory("Food");
        request.setAmount(new BigDecimal("25.50"));
        request.setExpenseDate(LocalDate.now());

        mockMvc.perform(post("/api/expenses/saveExpense")
                        .with(authenticatedAs(999L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createExpense_whenUserServiceUnavailable_shouldReturn503()
            throws Exception {

        ExpenseRequest request = new ExpenseRequest();
        request.setTitle("Service Unavailable Expense");
        request.setCategory("Food");
        request.setAmount(new BigDecimal("25.50"));
        request.setExpenseDate(LocalDate.now());

        mockMvc.perform(post("/api/expenses/saveExpense")
                        .with(authenticatedAs(500L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable());
    }
}
