package com.shawbin.expense_service.client;

import com.shawbin.expense_service.exception.UserNotFoundException;
import com.shawbin.expense_service.exception.UserServiceUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class UserServiceClient {

    private final RestClient restClient;
    private final HttpServletRequest request;

    @Autowired
    public UserServiceClient(
            @Value("${user.service.url:http://localhost:8082}") String userServiceUrl,
            HttpServletRequest request) {

        this(
                RestClient.builder()
                        .baseUrl(userServiceUrl)
                        .build(),
                request
        );
    }

    // Used by tests to provide a controlled RestClient
    UserServiceClient(RestClient restClient, HttpServletRequest request) {
        this.restClient = restClient;
        this.request = request;
    }

    public void validateUser(Long userId, String authorizationHeader) {
        try {
            restClient.get()
                    .uri("/api/users/fetchUser/{id}", userId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new UserNotFoundException(
                        "User not found with id: " + userId);
            }

            throw new UserServiceUnavailableException(
                    "User service returned an error: " + ex.getStatusCode());

        } catch (ResourceAccessException ex) {
            throw new UserServiceUnavailableException(
                    "User service is currently unavailable");
        }
    }
}