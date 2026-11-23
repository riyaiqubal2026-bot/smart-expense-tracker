package com.shawbin.expense_service.repository;

import com.shawbin.expense_service.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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

    @Query("""
    SELECT e FROM Expense e
    WHERE e.userId = :userId
    AND (:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%')))
    AND (:category IS NULL OR LOWER(e.category) = LOWER(:category))
    AND (:startDate IS NULL OR e.expenseDate >= :startDate)
    AND (:endDate IS NULL OR e.expenseDate <= :endDate)
    """)
    Page<Expense> searchExpenses(
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.userId = :userId")
    BigDecimal calculateTotalExpensesByUserId(@Param("userId") Long userId);

    @Query("""
    SELECT COALESCE(SUM(e.amount), 0)
    FROM Expense e
    WHERE e.userId = :userId
    AND e.expenseDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal calculateMonthlyExpenses(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    List<Expense> findByUserIdAndExpenseDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    long countByUserId(Long userId);

    @Query("""
    SELECT e.category
    FROM Expense e
    WHERE e.userId = :userId
    GROUP BY e.category
    ORDER BY SUM(e.amount) DESC
    """)
    List<String> findHighestSpendingCategory(@Param("userId") Long userId);

    @Query("""
    SELECT MONTH(e.expenseDate), SUM(e.amount)
    FROM Expense e
    WHERE e.userId = :userId
    AND YEAR(e.expenseDate) = :year
    GROUP BY MONTH(e.expenseDate)
    ORDER BY MONTH(e.expenseDate)
    """)
    List<Object[]> findMonthlySpending(
            @Param("userId") Long userId,
            @Param("year") int year
    );

    @Query("""
    SELECT
        e.category,
        SUM(e.amount),
        COUNT(e),
        AVG(e.amount)
    FROM Expense e
    WHERE e.userId = :userId
    AND (:startDate IS NULL OR e.expenseDate >= :startDate)
    AND (:endDate IS NULL OR e.expenseDate <= :endDate)
    GROUP BY e.category
    ORDER BY SUM(e.amount) DESC
    """)
    List<Object[]> findCategorySummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    List<Expense> findByUserIdAndCategoryIgnoreCaseAndExpenseDateBetween(
            Long userId,
            String category,
            LocalDate startDate,
            LocalDate endDate
    );

}
