package com.liaw.dev.GraoMestre.service;

import com.liaw.dev.GraoMestre.dto.request.ExpeneRequest;
import com.liaw.dev.GraoMestre.dto.response.ExpenseResponse;
import com.liaw.dev.GraoMestre.entity.Expense;
import com.liaw.dev.GraoMestre.exception.exceptions.EntityNotFoundException;
import com.liaw.dev.GraoMestre.mapper.ExpenseMapper;
import com.liaw.dev.GraoMestre.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseMapper expenseMapper;

    public ExpenseResponse createExpense(ExpeneRequest request){
        Expense expense = expenseMapper.toEntity(request);
        expenseRepository.save(expense);
        return expenseMapper.toResponse(expense);
    }

    public List<ExpenseResponse> listExpenses(){
        List<Expense> expenses = expenseRepository.findAll();
        return expenses.stream().map(expenseMapper::toResponse).toList();
    }

    public ExpenseResponse update(Long id, ExpeneRequest request){
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Dispesa não encontrada com id: " + id));
        expense.setName(request.getName());
        expense.setPrice(request.getPrice());
        expenseRepository.save(expense);
        return expenseMapper.toResponse(expense);
    }

    public void delete(Long id){
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Dispesa não encontrada com id: " + id));
        expenseRepository.delete(expense);
    }

}
