
package com.shawbin.expense_service.config;

import com.shawbin.expense_service.controller.ExpenseController;
import com.shawbin.expense_service.security.JwtAuthenticationFilter;
import com.shawbin.expense_service.service.ExpenseService;
import com.shawbin.expense_service.service.JwtService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;

import java.time.Instant;
import java.util.Date;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = ExpenseController.class,
        properties = "jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

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

    @Test
    void protectedEndpoint_withoutToken_shouldReturn401()
            throws Exception {

        mockMvc.perform(get("/api/expenses/total"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withInvalidToken_shouldReturn401()
            throws Exception {

        mockMvc.perform(get("/api/expenses/total")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withValidToken_shouldReturn200()
            throws Exception {

        when(expenseService.getTotalExpenses(1L))
                .thenReturn(new java.math.BigDecimal("1500.00"));

        String token = generateToken(1L);

        mockMvc.perform(get("/api/expenses/total")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("1500.00"));
    }
}
