package com.shawbin.expense_service.repository;

import com.shawbin.expense_service.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long>{

    Page<Expense> findByCategoryIgnoreCase(Pageable pageable,String category);

    Page<Expense> findByExpenseDateBetween(Pageable pageable, LocalDate fromExpenseDate,LocalDate toExpenseDate);

    Page<Expense> findByUserId(Long userId, Pageable pageable);

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    Page<Expense> findByUserIdAndCategoryIgnoreCase(
            Long userId, String category, Pageable pageable);

    Page<Expense> findByUserIdAndExpenseDateBetween(
            Long userId, LocalDate fromDate, LocalDate toDate, Pageable pageable);
}
