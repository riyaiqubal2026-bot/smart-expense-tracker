package com.shawbin.expense_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionhandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionhandler.class);

    /*
    @RestControllerAdvice lets this class handle exceptions from your controllers across the application.
    @ExceptionHandler(MethodArgumentNotValidException.class) handles validation failures triggered by @Valid.
    getFieldErrors() retrieves the fields that failed validation.
    getDefaultMessage() retrieves messages you wrote in annotations such as @NotBlank.
    ResponseEntity lets you return both the JSON response and the HTTP status code.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidationErrors(MethodArgumentNotValidException ex ){
        List<String> messages = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error->error.getDefaultMessage())
                .distinct()
                .toList();
        Map<String,Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error","Validation Errors");
        response.put("message",messages);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,Object>> handleResponseStatusException(ResponseStatusException exception){

        Map<String,Object> response = new LinkedHashMap<>();
        response.put("status",exception.getStatusCode().value());
        response.put("error",exception.getStatusCode().toString());
        response.put("message",exception.getReason());

        return ResponseEntity.status(exception.getStatusCode()).body(response);

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleUnexpectedExceptions(Exception ex){
        logger.error("An unexpected error occurred", ex);
        Map<String,Object> response = new LinkedHashMap<>();
        response.put("status",500);
        response.put("error","Internal Server Error Occured");
        response.put("message","An unexpected error occurred");

        return ResponseEntity.status(500).body(response);
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(
            UserNotFoundException ex) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(UserServiceUnavailableException.class)
    public ResponseEntity<Map<String,Object>> handleUserServiceUnavailable(UserServiceUnavailableException ex){
        Map<String,Object> response = new LinkedHashMap<>();
        response.put("status",HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error","Service Unavailable");
        response.put("message",ex.getMessage());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }



}
