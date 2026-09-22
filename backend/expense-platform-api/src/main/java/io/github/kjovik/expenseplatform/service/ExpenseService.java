package io.github.kjovik.expenseplatform.service;

import io.github.kjovik.expenseplatform.context.TenantContext;
import io.github.kjovik.expenseplatform.dto.ExpenseRequest;
import io.github.kjovik.expenseplatform.dto.ExpenseResponse;
import io.github.kjovik.expenseplatform.entity.Expense;
import io.github.kjovik.expenseplatform.entity.Tenant;
import io.github.kjovik.expenseplatform.enums.ExpenseStatus;
import io.github.kjovik.expenseplatform.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
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

    @Transactional (readOnly = true)
    public List<ExpenseResponse> list(){
        UUID tenant = TenantContext.getTenantId();
        return expenseRepository.findByTenantId(tenant).stream().map(ExpenseResponse::from).toList();
    }

    @Transactional (readOnly = true)
    public List<ExpenseResponse> listPendingExpenses(){
        UUID tenantId = TenantContext.getTenantId();
        List<Expense> expenses = expenseRepository.findByTenantIdAndStatus(tenantId,ExpenseStatus.SUBMITTED);
        return expenses.stream().map(ExpenseResponse::from).toList();
    }
}
