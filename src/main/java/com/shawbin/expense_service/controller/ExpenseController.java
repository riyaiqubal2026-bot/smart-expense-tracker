package com.shawbin.expense_service.controller;

import com.shawbin.expense_service.dto.ExpenseRequest;
import com.shawbin.expense_service.dto.ExpenseResponse;
import com.shawbin.expense_service.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Tag(
        name ="Expense Management",
        description = "APIs for creating, retrieving, updating and deleting expenses"
)
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    public final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService){
        this.expenseService=expenseService;
    }
    private Long getLoggedInUserId() {
        return (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }



    @Operation(
            summary = "Create a new expense",
            description = "Creates and saves a new expense with its title, category, amount and date"
    )
    @ApiResponses({
            @ApiResponse (responseCode="201",description="Expense created successfully"),
            @ApiResponse(responseCode = "400",description = "Invalid Expense Data")

    })
    @PostMapping("/saveExpense")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(
            @Valid @RequestBody ExpenseRequest expenseRequest) {

        Long userId = getLoggedInUserId();

        return expenseService.createExpense(expenseRequest, userId);
    }



    @Operation(
            summary = "Retrieve all the expenses",
            description = "Retrieve the expenses with pagination and sorting, default pagination size is 10"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200" , description = "Successsfully Retrived Expenses")
    }
    )
    @GetMapping("/fetchAllExpenses")
    public Page<ExpenseResponse> getAllExpenses(
            @PageableDefault(size = 10) Pageable pageable) {

        Long userId = getLoggedInUserId();

        return expenseService.getAllExpenses(userId, pageable);
    }



    @Operation(
            summary = "Retrieve expense by ID",
            description = "Retrieve expense which matches with the given ID (Unique)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the Expense"),
            @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @GetMapping("/fetchExpense/{id}")
    public ExpenseResponse getExpenseById(@PathVariable Long id) {

        Long userId = getLoggedInUserId();

        return expenseService.getExpenseById(id, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Expense not found"));
    }



    @Operation(
            summary = "Update an Expense",
            description = "update  expense which matches with given ID"
    )
    @ApiResponses({
           @ApiResponse(responseCode = "200",description = "Expense updated successfully"),
            @ApiResponse(responseCode = "400",description = "Invalid expense data"),
            @ApiResponse(responseCode = "404",description = "Expense not found")

    }
    )
    @PutMapping("/updateExpense/{id}")
    public ExpenseResponse updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest expenseRequest) {

        Long userId = getLoggedInUserId();

        return expenseService.updateExpense(id, expenseRequest, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Expense not found"));
    }



    @Operation(
            summary = "Delete an expense",
            description = "Deletes an existing expense using its ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @DeleteMapping("/deleteExpense/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable Long id) {

        Long userId = getLoggedInUserId();

        if (!expenseService.deleteExpense(id, userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Expense not found"
            );
        }
    }



    @Operation(
            summary = "Filter expenses by category",
            description = "Retrieves expenses matching the specified category with pagination support."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    })
    @GetMapping("/getExpenseByCategory/{category}")
    public Page<ExpenseResponse> getExpenseByCategory(
            @PathVariable String category,
            @PageableDefault(size = 10) Pageable pageable) {

        Long userId = getLoggedInUserId();

        return expenseService.filterExpensesByCategory(
                userId, pageable, category);
    }



    @Operation(
            summary = "Filter expenses by date range",
            description = "Retrieves expenses between the specified start date and end date, inclusive."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range or date format")
    })
    @GetMapping("/dateRange")
    public Page<ExpenseResponse> getExpensesByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @PageableDefault(size = 10) Pageable pageable) {

        Long userId = getLoggedInUserId();

        return expenseService.filterByStartDateAndEndDate(
                userId, pageable, startDate, endDate);
    }

}
