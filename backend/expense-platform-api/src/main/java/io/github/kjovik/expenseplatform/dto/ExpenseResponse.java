package io.github.kjovik.expenseplatform.dto;

import io.github.kjovik.expenseplatform.entity.Expense;

import java.time.Instant;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        Long amountMinor,
        String currency,
        String category,
        String description,
        String status,
        String rejectionReason
) {
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmountMinor(),
                expense.getCurrency(),
                expense.getCategory(),
                expense.getDescription(),
                expense.getStatus().name(),
                expense.getRejectionReason()
        );
    }
}