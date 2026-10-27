package com.shawbin.expense_service.repository;

import com.shawbin.expense_service.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ExpenseRepository extends JpaRepository<Expense, Long>{

    Page<Expense> findByCategoryIgnoreCase(Pageable pageable,String category);

    Page<Expense> findByExpenseDateBetween(Pageable pageable, LocalDate fromExpenseDate,LocalDate toExpenseDate);
}
