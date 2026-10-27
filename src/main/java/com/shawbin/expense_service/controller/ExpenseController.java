package com.shawbin.expense_service.controller;

import com.shawbin.expense_service.dto.ExpenseRequest;
import com.shawbin.expense_service.dto.ExpenseResponse;
import com.shawbin.expense_service.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    public final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService){
        this.expenseService=expenseService;
    }

    @PostMapping("/saveExpense")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(@Valid @RequestBody ExpenseRequest expenseRequest){
        return expenseService.createExpense(expenseRequest);
    }

    @GetMapping("/fetchAllExpenses")
    public Page<ExpenseResponse> getAllExpenses(@PageableDefault(size = 10)Pageable pageable){
        return expenseService.getAllExpenses(pageable);
    }

    @GetMapping("/fetchExpense/{id}")
    public ExpenseResponse getExpenseById(@PathVariable Long id){
        return expenseService.getExpenseById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Expense not found"));
    }

    @PutMapping("/updateExpense/{id}")
    public ExpenseResponse updateExpense(@PathVariable Long id ,@RequestBody ExpenseRequest expenseRequest){

        return expenseService.updateExpense(id,expenseRequest).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Expense not found"));
    }

    @DeleteMapping("/deleteExpense/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable Long id) {
        if (!expenseService.deleteExpense(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "ID not found"
            );
        }
    }




}
