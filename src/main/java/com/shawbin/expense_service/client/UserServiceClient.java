package com.shawbin.expense_service.client;

import com.shawbin.expense_service.exception.UserNotFoundException;
import com.shawbin.expense_service.exception.UserServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class UserServiceClient {
    private final RestClient restClient;

    public UserServiceClient(
            @org.springframework.beans.factory.annotation.Value(
                    "${user.service.url:http://localhost:8082}"
            ) String userServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    public void validateUser(Long userId){
        try{
            restClient.get()
                    .uri("/api/users/fetchUser/{id}",userId)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException ex) {
            if(ex.getStatusCode()== HttpStatus.NOT_FOUND){
                throw new UserNotFoundException("User not found with id : "+userId);
            }
            throw new UserServiceUnavailableException(
                    "User service retured an error: "+ex.getStatusCode()
            );
        }catch(ResourceAccessException ex){
            throw new UserServiceUnavailableException("User service is currently unavailable");
        }
    }
}
