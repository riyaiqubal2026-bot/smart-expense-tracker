package com.shawbin.expense_service.service;

import com.shawbin.expense_service.dto.ExpenseRequest;
import com.shawbin.expense_service.dto.ExpenseResponse;
import com.shawbin.expense_service.entity.Expense;
import com.shawbin.expense_service.mapper.ExpenseMapper;
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

    public ExpenseResponse createExpense(ExpenseRequest expenseRequest){
        Expense expenseEntity = ExpenseMapper.toEntity(expenseRequest);
        Expense savedExpense = expenseRepository.save(expenseEntity);
        return ExpenseMapper.toResponse(savedExpense);
    }

    public List<ExpenseResponse> getAllExpenses(){
        return expenseRepository.findAll().
                stream().map(ExpenseMapper::toResponse).toList();
    }

    public Optional<ExpenseResponse> getExpenseById(Long id){
        return expenseRepository.findById(id).map(ExpenseMapper::toResponse);
    }

    public Optional<ExpenseResponse> updateExpense(Long id,ExpenseRequest expenseRequest){

        return expenseRepository.findById(id)
                .map(existigExpense -> {
                    ExpenseMapper.updateEntity(existigExpense,expenseRequest);
                    Expense updatedExpense= expenseRepository.save(existigExpense);
                    return ExpenseMapper.toResponse(updatedExpense);
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
