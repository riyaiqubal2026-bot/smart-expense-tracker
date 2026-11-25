
package com.shawbin.expense_service.client;

import com.shawbin.expense_service.exception.UserNotFoundException;
import com.shawbin.expense_service.exception.UserServiceUnavailableException;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserServiceClientTest {

    private UserServiceClient userServiceClient;

    private MockRestServiceServer mockServer;

    private static final Long USER_ID = 1L;
    private static final String AUTH_HEADER = "Bearer test-token";

    @BeforeEach
    void setUp() {

        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost:8082");

        mockServer = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.build();

        userServiceClient = new UserServiceClient(
                restClient,
                org.mockito.Mockito.mock(HttpServletRequest.class)
        );
    }

    @Test
    void validateUser_shouldSucceedWhenUserExists() {

        mockServer.expect(requestTo(
                        "http://localhost:8082/api/users/fetchUser/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andRespond(withSuccess());

        assertDoesNotThrow(() ->
                userServiceClient.validateUser(USER_ID, AUTH_HEADER)
        );

        mockServer.verify();
    }

    @Test
    void validateUser_shouldThrowUserNotFoundWhenUserDoesNotExist() {

        mockServer.expect(requestTo(
                        "http://localhost:8082/api/users/fetchUser/1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, AUTH_HEADER))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userServiceClient.validateUser(USER_ID, AUTH_HEADER)
        );

        assertEquals(
                "User not found with id: 1",
                exception.getMessage()
        );

        mockServer.verify();
    }

    @Test
    void validateUser_shouldThrowServiceUnavailableWhenServerReturnsError() {

        mockServer.expect(requestTo(
                        "http://localhost:8082/api/users/fetchUser/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        UserServiceUnavailableException exception = assertThrows(
                UserServiceUnavailableException.class,
                () -> userServiceClient.validateUser(USER_ID, AUTH_HEADER)
        );

        assertEquals(
                "User service returned an error: 500 INTERNAL_SERVER_ERROR",
                exception.getMessage()
        );

        mockServer.verify();
    }

    @Test
    void validateUser_shouldThrowServiceUnavailableWhenConnectionFails() {

        mockServer.expect(requestTo(
                        "http://localhost:8082/api/users/fetchUser/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withException(
                        new IOException("Connection refused")
                ));

        UserServiceUnavailableException exception = assertThrows(
                UserServiceUnavailableException.class,
                () -> userServiceClient.validateUser(USER_ID, AUTH_HEADER)
        );

        assertEquals(
                "User service is currently unavailable",
                exception.getMessage()
        );

        mockServer.verify();
    }
}
