package com.shawbin.expense_service.repository;

import com.shawbin.expense_service.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long>{

}
