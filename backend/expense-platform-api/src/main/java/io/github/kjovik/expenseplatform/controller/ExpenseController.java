package io.github.kjovik.expenseplatform.controller;

import io.github.kjovik.expenseplatform.dto.ExpenseRequest;
import io.github.kjovik.expenseplatform.dto.ExpenseResponse;
import io.github.kjovik.expenseplatform.entity.Expense;
import io.github.kjovik.expenseplatform.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/expenses")
    public ResponseEntity<ExpenseResponse> submit(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/expenses")
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.list());
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @GetMapping("/expenses/pending")
    public ResponseEntity<List<ExpenseResponse>> getPendingExpenses() {
        return ResponseEntity.ok(expenseService.listPendingExpenses());
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @PatchMapping("/expenses/{id}/approve")
    public ResponseEntity<ExpenseResponse> approveExpense(@PathVariable UUID id) {
        ExpenseResponse resp = expenseService.approve(id);
        return ResponseEntity.ok(resp);
    }
}
