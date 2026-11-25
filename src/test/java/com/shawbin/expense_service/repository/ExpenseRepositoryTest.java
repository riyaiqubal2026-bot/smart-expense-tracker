
package com.shawbin.expense_service.repository;

import com.shawbin.expense_service.entity.Expense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private EntityManager entityManager;

    private final Long userId = 1L;
    private final Long anotherUserId = 2L;

    private final LocalDate januaryDate = LocalDate.of(2026, 1, 10);
    private final LocalDate februaryDate = LocalDate.of(2026, 2, 15);

    @BeforeEach
    void setUp() {

        expenseRepository.save(createExpense(
                userId, "Grocery shopping", "Food",
                "100.00", januaryDate));

        expenseRepository.save(createExpense(
                userId, "Weekly groceries", "Food",
                "50.00", februaryDate));

        expenseRepository.save(createExpense(
                userId, "Bus ticket", "Transport",
                "30.00", LocalDate.of(2026, 1, 20)));

        expenseRepository.save(createExpense(
                anotherUserId, "Other user expense", "Food",
                "999.00", januaryDate));

        expenseRepository.flush();
        entityManager.clear();
    }

    private Expense createExpense(
            Long userId,
            String title,
            String category,
            String amount,
            LocalDate date) {

        Expense expense = new Expense();
        expense.setUserId(userId);
        expense.setTitle(title);
        expense.setCategory(category);
        expense.setAmount(new BigDecimal(amount));
        expense.setExpenseDate(date);

        return expense;
    }

    // 1. Find expenses belonging to a specific user

    @Test
    void findByUserId_shouldReturnOnlyUsersExpenses() {

        Page<Expense> result = expenseRepository.findByUserId(
                userId, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .allMatch(expense -> expense.getUserId().equals(userId));
    }

    // 2. Find expense by ID and user ID

    @Test
    void findByIdAndUserId_whenFound_shouldReturnExpense() {

        Expense saved = expenseRepository.save(
                createExpense(userId, "Coffee", "Food",
                        "10.00", januaryDate));

        Optional<Expense> result =
                expenseRepository.findByIdAndUserId(saved.getId(), userId);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Coffee");
    }

    // 3. Prevent one user from accessing another user's expense

    @Test
    void findByIdAndUserId_whenWrongUser_shouldReturnEmpty() {

        Expense otherUserExpense = expenseRepository.findByUserId(
                        anotherUserId, PageRequest.of(0, 10))
                .getContent().get(0);

        Optional<Expense> result =
                expenseRepository.findByIdAndUserId(
                        otherUserExpense.getId(), userId);

        assertThat(result).isEmpty();
    }

    // 4. Filter expenses by category

    @Test
    void findByUserIdAndCategoryIgnoreCase_shouldReturnMatchingExpenses() {

        Page<Expense> result =
                expenseRepository.findByUserIdAndCategoryIgnoreCase(
                        userId, "food", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(expense -> expense.getCategory().equalsIgnoreCase("Food"));
    }

    // 5. Filter expenses by date range

    @Test
    void findByUserIdAndExpenseDateBetween_shouldReturnMatchingExpenses() {

        Page<Expense> result =
                expenseRepository.findByUserIdAndExpenseDateBetween(
                        userId,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31),
                        PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    // 6. Search using multiple filters

    @Test
    void searchExpenses_withFilters_shouldReturnMatchingExpenses() {

        Page<Expense> result = expenseRepository.searchExpenses(
                userId,
                "grocery",
                "Food",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle())
                .isEqualTo("Grocery shopping");
    }

    // 7. Search without optional filters

    @Test
    void searchExpenses_withoutFilters_shouldReturnAllUserExpenses() {

        Page<Expense> result = expenseRepository.searchExpenses(
                userId, null, null, null, null,
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
    }

    // 8. Calculate total expenses

    @Test
    void calculateTotalExpensesByUserId_shouldReturnCorrectTotal() {

        BigDecimal total =
                expenseRepository.calculateTotalExpensesByUserId(userId);

        assertThat(total.compareTo(new BigDecimal("180.00"))).isZero();
    }

    // 9. Calculate monthly expenses

    @Test
    void calculateMonthlyExpenses_shouldReturnCorrectTotal() {

        BigDecimal total = expenseRepository.calculateMonthlyExpenses(
                userId,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31));

        assertThat(total.compareTo(new BigDecimal("130.00"))).isZero();
    }

    // 10. Count transactions

    @Test
    void countByUserId_shouldReturnCorrectCount() {

        long count = expenseRepository.countByUserId(userId);

        assertThat(count).isEqualTo(3);
    }

    // 11. Find highest spending category

    @Test
    void findHighestSpendingCategory_shouldReturnHighestCategory() {

        List<String> result =
                expenseRepository.findHighestSpendingCategory(userId);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0)).isEqualTo("Food");
    }

    // 12. Find monthly spending

    @Test
    void findMonthlySpending_shouldReturnMonthlyTotals() {

        List<Object[]> result =
                expenseRepository.findMonthlySpending(userId, 2026);

        assertThat(result).hasSize(2);

        assertThat(((Number) result.get(0)[0]).intValue()).isEqualTo(1);
        assertThat(((BigDecimal) result.get(0)[1])
                .compareTo(new BigDecimal("130.00"))).isZero();

        assertThat(((Number) result.get(1)[0]).intValue()).isEqualTo(2);
        assertThat(((BigDecimal) result.get(1)[1])
                .compareTo(new BigDecimal("50.00"))).isZero();
    }

    // 13. Find category summary

    @Test
    void findCategorySummary_shouldReturnCorrectCategoryData() {

        List<Object[]> result =
                expenseRepository.findCategorySummary(userId, null, null);

        assertThat(result).hasSize(2);

        Object[] foodSummary = result.stream()
                .filter(row -> row[0].equals("Food"))
                .findFirst()
                .orElseThrow();

        assertThat(foodSummary[1]).isEqualTo(new BigDecimal("150.00"));
        assertThat(((Number) foodSummary[2]).longValue()).isEqualTo(2);
        assertThat(((Number) foodSummary[3]).doubleValue())
                .isEqualTo(75.00);
    }

    // 14. Find category summary within a date range

    @Test
    void findCategorySummary_withDateRange_shouldReturnFilteredData() {

        List<Object[]> result =
                expenseRepository.findCategorySummary(
                        userId,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31));

        assertThat(result).hasSize(2);

        BigDecimal foodTotal = result.stream()
                .filter(row -> row[0].equals("Food"))
                .map(row -> (BigDecimal) row[1])
                .findFirst()
                .orElseThrow();

        assertThat(foodTotal).isEqualByComparingTo("100.00");
    }

    // 15. Filter by category and date range

    @Test
    void findByUserIdAndCategoryIgnoreCaseAndExpenseDateBetween_shouldReturnMatchingExpenses() {

        List<Expense> result =
                expenseRepository.findByUserIdAndCategoryIgnoreCaseAndExpenseDateBetween(
                        userId,
                        "Food",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Grocery shopping");
    }
}
