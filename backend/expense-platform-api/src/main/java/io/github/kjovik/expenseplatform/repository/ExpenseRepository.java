package io.github.kjovik.expenseplatform.repository;

import io.github.kjovik.expenseplatform.entity.Expense;
import io.github.kjovik.expenseplatform.enums.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByTenantId(UUID tenantId);
    List<Expense> findByTenantIdAndStatus(UUID tenantId, ExpenseStatus status);
}
