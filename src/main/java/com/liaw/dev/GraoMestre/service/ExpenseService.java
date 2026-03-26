package com.liaw.dev.GraoMestre.service;

import com.liaw.dev.GraoMestre.dto.request.ExpeneRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.ExpenseResponseDTO;
import com.liaw.dev.GraoMestre.entity.Expense;
import com.liaw.dev.GraoMestre.enums.TimePeriod;
import com.liaw.dev.GraoMestre.exception.exceptions.EntityNotFoundException;
import com.liaw.dev.GraoMestre.mapper.ExpenseMapper;
import com.liaw.dev.GraoMestre.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    public ExpenseResponseDTO createExpense(ExpeneRequestDTO request){
        Expense expense = expenseMapper.toEntity(request);
        expense = expenseRepository.save(expense);
        return expenseMapper.toResponse(expense);
    }

    public List<ExpenseResponseDTO> getAllExpenses(){
        List<Expense> expenses = expenseRepository.findAll();
        return expenses.stream().map(expenseMapper::toResponse).collect(Collectors.toList());
    }

    public ExpenseResponseDTO findById(Long id){
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despesa não encontrada com ID: " + id));
        return expenseMapper.toResponse(expense);
    }

    public ExpenseResponseDTO updateExpense(Long id, ExpeneRequestDTO request){
        Expense existingExpense = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despesa não encontrada com ID: " + id));

        existingExpense.setName(request.getName());
        existingExpense.setPrice(request.getPrice());
        expenseRepository.save(existingExpense);
        return expenseMapper.toResponse(existingExpense);
    }

    public List<ExpenseResponseDTO> findExpensesForToday(){
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDate = today.atTime(23, 59, 59);

        List<Expense> expenses = expenseRepository.findByDateRange(startOfDay, endOfDate);
        return expenses.stream().map(expenseMapper::toResponse).collect(Collectors.toList());
    }

    public List<ExpenseResponseDTO> findExpensesForYesterday(){
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.atTime(23, 59, 59);

        List<Expense> expenses = expenseRepository.findByDateRange(startOfDay, endOfDay);
        return expenses.stream().map(expenseMapper::toResponse).collect(Collectors.toList());
    }

    public List<ExpenseResponseDTO> findExpensesForThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime start = startOfWeek.atStartOfDay();
        LocalDateTime end = endOfWeek.atTime(23, 59, 59);

        List<Expense> expenses = expenseRepository.findByDateRange(start, end);
        return expenses.stream().map(expenseMapper::toResponse).collect(Collectors.toList());
    }

    public List<ExpenseResponseDTO> findExpensesForLastWeek() {
        LocalDate today = LocalDate.now();
        LocalDate endOfLastWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).minusWeeks(1);
        LocalDate startOfLastWeek = endOfLastWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDateTime start = startOfLastWeek.atStartOfDay();
        LocalDateTime end = endOfLastWeek.atTime(23, 59, 59);

        List<Expense> expenses = expenseRepository.findByDateRange(start, end);
        return expenses.stream().map(expenseMapper::toResponse).collect(Collectors.toList());
    }

    public List<ExpenseResponseDTO> findExpensesForThisMonth() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());

        LocalDateTime start = startOfMonth.atStartOfDay();
        LocalDateTime end = endOfMonth.atTime(23, 59, 59);

        List<Expense> expenses = expenseRepository.findByDateRange(start, end);
        return expenses.stream().map(expenseMapper::toResponse).collect(Collectors.toList());
    }

    public List<ExpenseResponseDTO> findExpensesForLastMonth() {
        LocalDate today = LocalDate.now();
        LocalDate startOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
        LocalDate endOfLastMonth = startOfLastMonth.with(TemporalAdjusters.lastDayOfMonth());

        LocalDateTime start = startOfLastMonth.atStartOfDay();
        LocalDateTime end = endOfLastMonth.atTime(23, 59, 59);

        List<Expense> expenses = expenseRepository.findByDateRange(start, end);
        return expenses.stream().map(expenseMapper::toResponse).collect(Collectors.toList());
    }

    public List<ExpenseResponseDTO> findExpensesForCustomPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Data inicial não pode ser posterior à data final");
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Expense> expenses = expenseRepository.findByDateRange(start, end);
        return expenses.stream().map(expenseMapper::toResponse).collect(Collectors.toList());
    }

    public List<ExpenseResponseDTO> findExpensesByTimePeriod(TimePeriod timePeriod, LocalDate startDate, LocalDate endDate) {
        switch (timePeriod) {
            case TODAY:
                return findExpensesForToday();
            case YESTERDAY:
                return findExpensesForYesterday();
            case THIS_WEEK:
                return findExpensesForThisWeek();
            case LAST_WEEK:
                return findExpensesForLastWeek();
            case THIS_MONTH:
                return findExpensesForThisMonth();
            case LAST_MONTH:
                return findExpensesForLastMonth();
            case CUSTOM:
                if (startDate == null || endDate == null) {
                    throw new IllegalArgumentException("Para período CUSTOM, startDate e endDate são obrigatórios");
                }
                return findExpensesForCustomPeriod(startDate, endDate);
            default:
                throw new IllegalArgumentException("Período de tempo inválido");
        }
    }

    /**
     * Deletes an expense by its ID.
     * @param id The ID of the expense to delete.
     * @throws EntityNotFoundException if no expense is found with the given ID.
     */
    public void deleteExpense(Long id){
        Expense existingExpense = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despesa não encontrada com ID: " + id));
        expenseRepository.delete(existingExpense);
    }

}