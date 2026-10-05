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
        String rejectionReason,
        Instant submittedAt,
        Instant approvedAt,
        Instant reversedAt,
        UUID reversedById,
        String reversalReason
) {
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmountMinor(),
                expense.getCurrency(),
                expense.getCategory(),
                expense.getDescription(),
                expense.getStatus().name(),
                expense.getRejectionReason(),
                expense.getSubmittedAt(),
                expense.getApprovedAt(),
                expense.getReversedAt(),
                expense.getReversedById(),
                expense.getReversalReason()
        );
    }
}