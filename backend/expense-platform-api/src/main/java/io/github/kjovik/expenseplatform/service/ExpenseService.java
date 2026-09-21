package io.github.kjovik.expenseplatform.service;

import io.github.kjovik.expenseplatform.context.TenantContext;
import io.github.kjovik.expenseplatform.dto.ExpenseRequest;
import io.github.kjovik.expenseplatform.dto.ExpenseResponse;
import io.github.kjovik.expenseplatform.entity.Expense;
import io.github.kjovik.expenseplatform.enums.ExpenseStatus;
import io.github.kjovik.expenseplatform.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseResponse createExpense(ExpenseRequest expenseRequest) {
        Expense expense = new Expense();
        UUID tenantId = TenantContext.getTenantId();
        expense.setTenantId(tenantId);
        UUID userID = TenantContext.getUserId();
        expense.setUserId(userID);
        expense.setStatus(ExpenseStatus.SUBMITTED);
        expense.setAmountMinor(expenseRequest.amountMinor());
        expense.setCategory(expenseRequest.category());
        if (expenseRequest.description() != null) {
            expense.setDescription(expenseRequest.description());
        }
        expense.setCurrency(expenseRequest.currency());
        expense.setSubmittedAt(Instant.now());

        Expense saved = expenseRepository.save(expense);
        return ExpenseResponse.from(saved);
    }
}
