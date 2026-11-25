
package com.shawbin.expense_service.repository;

import com.shawbin.expense_service.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long>,
        JpaSpecificationExecutor<Expense> {

    // Find expenses by category
    Page<Expense> findByCategoryIgnoreCase(
            String category,
            Pageable pageable
    );

    // Find expenses between two dates
    Page<Expense> findByExpenseDateBetween(
            LocalDate fromExpenseDate,
            LocalDate toExpenseDate,
            Pageable pageable
    );

    // Find all expenses belonging to a user
    Page<Expense> findByUserId(
            Long userId,
            Pageable pageable
    );

    Optional<Expense> findByIdAndUserId(
            Long id,
            Long userId
    );

    // Find a user's expenses by category
    Page<Expense> findByUserIdAndCategoryIgnoreCase(
            Long userId,
            String category,
            Pageable pageable
    );

    // Find a user's expenses between dates
    Page<Expense> findByUserIdAndExpenseDateBetween(
            Long userId,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    );

    // Search expenses with optional filters
    @Query("""
        SELECT e
        FROM Expense e
        WHERE e.userId = :userId
        AND (
            CAST(:title AS string) IS NULL
            OR LOWER(e.title) LIKE LOWER(
                CONCAT('%', CAST(:title AS string), '%')
            )
        )
        AND (
            CAST(:category AS string) IS NULL
            OR LOWER(e.category) = LOWER(CAST(:category AS string))
        )
        AND (
            :startDate IS NULL
            OR e.expenseDate >= :startDate
        )
        AND (
            :endDate IS NULL
            OR e.expenseDate <= :endDate
        )
        """)
    Page<Expense> searchExpenses(
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    // Calculate total expenses for a user
    @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.userId = :userId
        """)
    BigDecimal calculateTotalExpensesByUserId(
            @Param("userId") Long userId
    );

    // Calculate expenses within a date range
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

    // Find the highest spending categories
    @Query("""
        SELECT e.category
        FROM Expense e
        WHERE e.userId = :userId
        GROUP BY e.category
        ORDER BY SUM(e.amount) DESC
        """)
    List<String> findHighestSpendingCategory(
            @Param("userId") Long userId
    );

    // Monthly spending using PostgreSQL EXTRACT
    @Query(
            value = """
            SELECT
                EXTRACT(MONTH FROM expense_date),
                SUM(amount)
            FROM expenses
            WHERE user_id = :userId
            AND EXTRACT(YEAR FROM expense_date) = :year
            GROUP BY EXTRACT(MONTH FROM expense_date)
            ORDER BY EXTRACT(MONTH FROM expense_date)
            """,
            nativeQuery = true
    )
    List<Object[]> findMonthlySpending(
            @Param("userId") Long userId,
            @Param("year") int year
    );

    // Category summary with optional date filters
    @Query("""
        SELECT
            e.category,
            SUM(e.amount),
            COUNT(e),
            AVG(e.amount)
        FROM Expense e
        WHERE e.userId = :userId
        AND e.expenseDate >= COALESCE(:startDate, e.expenseDate)
        AND e.expenseDate <= COALESCE(:endDate, e.expenseDate)
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
