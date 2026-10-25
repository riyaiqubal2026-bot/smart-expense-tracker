package com.shawbin.expense_service.service;

import com.shawbin.expense_service.entity.Expense;
import com.shawbin.expense_service.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {

    public final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public Expense createExpense(Expense expense){
        return expenseRepository.save(expense);
    }

    public List<Expense> getAllExpenses(){
        return expenseRepository.findAll();
    }

    public Optional<Expense> getExpenseById(Long id){
        return expenseRepository.findById(id);
    }

    public Optional<Expense> updateExpense(Long id,Expense updatedExpense){
        return expenseRepository.findById(id).map(existigExpense -> {
            existigExpense.setTitle(updatedExpense.getTitle());
            existigExpense.setAmount(updatedExpense.getAmount());
            existigExpense.setCategory(updatedExpense.getCategory());
            existigExpense.setExpenseDate(updatedExpense.getExpenseDate());
            return expenseRepository.save(existigExpense);
        });
    }

    public Boolean deleteExpense(Long id){
        if(!expenseRepository.existsById(id)){
            return false;
        }
        expenseRepository.deleteById(id);
        return true;
    }

}
