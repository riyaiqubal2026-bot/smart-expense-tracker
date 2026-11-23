
package com.shawbin.expense_service.repository;

import com.shawbin.expense_service.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserIdAndBudgetMonth(
            Long userId,
            LocalDate budgetMonth
    );

    Optional<Budget> findByIdAndUserId(
            Long id,
            Long userId
    );

    boolean existsByUserIdAndCategoryAndBudgetMonth(
            Long userId,
            String category,
            LocalDate budgetMonth
    );
}
