package io.github.kjovik.expenseplatform.repository;

import io.github.kjovik.expenseplatform.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    public List<Expense> findByTenantId(UUID tenantId);
}
