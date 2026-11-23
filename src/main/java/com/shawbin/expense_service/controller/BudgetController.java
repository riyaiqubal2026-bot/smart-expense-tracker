
package com.shawbin.expense_service.controller;

import com.shawbin.expense_service.dto.BudgetRequest;
import com.shawbin.expense_service.dto.BudgetResponse;
import com.shawbin.expense_service.service.BudgetService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    private Long getLoggedInUserId() {
        return (Long) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgets(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate month) {

        Long userId = getLoggedInUserId();

        return ResponseEntity.ok(
                budgetService.getBudgets(userId, month)
        );
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @Valid @RequestBody BudgetRequest request) {

        Long userId = getLoggedInUserId();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.createBudget(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {

        Long userId = getLoggedInUserId();

        return ResponseEntity.ok(
                budgetService.updateBudget(userId, id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable Long id) {

        Long userId = getLoggedInUserId();

        budgetService.deleteBudget(userId, id);

        return ResponseEntity.noContent().build();
    }
}
