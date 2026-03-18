package com.liaw.dev.GraoMestre.controller;

import com.liaw.dev.GraoMestre.dto.request.ExpeneRequestDTO;
import com.liaw.dev.GraoMestre.dto.response.ExpenseResponseDTO;
import com.liaw.dev.GraoMestre.enums.TimePeriod;
import com.liaw.dev.GraoMestre.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<ExpenseResponseDTO> createExpense(@Valid @RequestBody ExpeneRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createExpense(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<List<ExpenseResponseDTO>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<ExpenseResponseDTO> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<ExpenseResponseDTO> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpeneRequestDTO request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/period")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesByPeriod(
            @RequestParam TimePeriod timePeriod,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(expenseService.findExpensesByTimePeriod(timePeriod, startDate, endDate));
    }

    // Endpoints específicos para períodos comuns, se preferir ter URLs mais diretas
    @GetMapping("/today")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesForToday() {
        return ResponseEntity.ok(expenseService.findExpensesForToday());
    }

    @GetMapping("/yesterday")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesForYesterday() {
        return ResponseEntity.ok(expenseService.findExpensesForYesterday());
    }

    @GetMapping("/this-week")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesForThisWeek() {
        return ResponseEntity.ok(expenseService.findExpensesForThisWeek());
    }

    @GetMapping("/last-week")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesForLastWeek() {
        return ResponseEntity.ok(expenseService.findExpensesForLastWeek());
    }

    @GetMapping("/this-month")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesForThisMonth() {
        return ResponseEntity.ok(expenseService.findExpensesForThisMonth());
    }

    @GetMapping("/last-month")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_MANAGER')")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesForLastMonth() {
        return ResponseEntity.ok(expenseService.findExpensesForLastMonth());
    }
}