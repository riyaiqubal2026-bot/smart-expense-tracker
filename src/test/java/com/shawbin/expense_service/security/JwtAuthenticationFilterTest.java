
package com.shawbin.expense_service.security;

import com.shawbin.expense_service.service.JwtService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtService);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void executeFilter(
            MockHttpServletRequest request)
            throws ServletException, IOException {

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        jwtAuthenticationFilter.doFilter(
                request, response, chain);
    }

    @Test
    void doFilter_withoutAuthorizationHeader_shouldContinueChain()
            throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        executeFilter(request);

        assertThat(SecurityContextHolder.getContext()
                .getAuthentication()).isNull();

        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilter_withNonBearerHeader_shouldContinueChain()
            throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader("Authorization", "Basic abc123");

        executeFilter(request);

        assertThat(SecurityContextHolder.getContext()
                .getAuthentication()).isNull();

        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilter_withValidToken_shouldAuthenticateUser()
            throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.extractUserId("valid-token"))
                .thenReturn(1L);

        executeFilter(request);

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(1L);
        assertThat(authentication.isAuthenticated()).isTrue();

        verify(jwtService).extractUserId("valid-token");
    }

    @Test
    void doFilter_withInvalidToken_shouldClearAuthentication()
            throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader("Authorization", "Bearer invalid-token");

        when(jwtService.extractUserId("invalid-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        executeFilter(request);

        assertThat(SecurityContextHolder.getContext()
                .getAuthentication()).isNull();

        verify(jwtService).extractUserId("invalid-token");
    }

    @Test
    void doFilter_withValidToken_shouldSetCorrectUserId()
            throws ServletException, IOException {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader("Authorization", "Bearer another-token");

        when(jwtService.extractUserId("another-token"))
                .thenReturn(25L);

        executeFilter(request);

        assertThat(SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal())
                .isEqualTo(25L);
    }
}
