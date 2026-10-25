package com.shawbin.expense_service.controller;

import com.shawbin.expense_service.entity.Expense;
import com.shawbin.expense_service.service.ExpenseService;
import jakarta.validation.Valid;
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
    public Expense createExpense(@Valid @RequestBody Expense expense){
        return expenseService.createExpense(expense);
    }

    @GetMapping("/fetchAllExpenses")
    public List<Expense> getAllExpenses(){
        return expenseService.getAllExpenses();
    }

    @GetMapping("/fetchExpense/{id}")
    public Expense getExpenseById(@PathVariable Long id){
        return expenseService.getExpenseById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Expense not found"));
    }

    @PutMapping("/updateExpense/{id}")
    public Expense updateExpense(@PathVariable Long id ,@RequestBody Expense updatedExpense){
        if(updatedExpense.getId()!=null && !updatedExpense.getId().equals(id)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"URL ID and expense ID must match");
        }
        return expenseService.updateExpense(id,updatedExpense).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Expense not found"));
    }

    @DeleteMapping("/deleteExpense/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Boolean deleteExpense(@PathVariable Long id){
        if(!expenseService.deleteExpense(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"ID not found");
        }
        return expenseService.deleteExpense(id);

    }



}
