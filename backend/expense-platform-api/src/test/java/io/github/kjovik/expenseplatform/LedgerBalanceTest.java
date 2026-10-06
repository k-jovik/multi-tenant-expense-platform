package io.github.kjovik.expenseplatform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerBalanceTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private UUID seedTenantAndAccounts() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO tenants (id, name) VALUES (?, ?)",
                tenantId, "test-tenant-" + tenantId);
        jdbcTemplate.update(
                "INSERT INTO users (id, tenant_id, email, password_hash, role, full_name) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                userId, tenantId, "u@x.com", "hash", "ADMIN", "User");
        jdbcTemplate.update(
                "INSERT INTO accounts (id, tenant_id, name, type) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), tenantId, "Expense:Travel", "EXPENSE");
        jdbcTemplate.update(
                "INSERT INTO accounts (id, tenant_id, name, type) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), tenantId, "Payable:" + userId, "PAYABLE");

        return tenantId;
    }

    @Test
    void rejects_entry_whose_lines_do_not_sum_to_zero() {
        UUID tenantId = seedTenantAndAccounts();

        UUID entryId = UUID.randomUUID();
        UUID accountA = jdbcTemplate.queryForObject(
                "SELECT id FROM accounts WHERE tenant_id = ? AND name = 'Expense:Travel'",
                UUID.class, tenantId);
        UUID accountB = jdbcTemplate.queryForObject(
                "SELECT id FROM accounts WHERE tenant_id = ? AND name LIKE 'Payable:%'",
                UUID.class, tenantId);

        // Insert an UNBALANCED entry: +100 and -90 → sum = +10
        assertThatThrownBy(() ->
                transactionTemplate.executeWithoutResult(status -> {
                    jdbcTemplate.update(
                            "INSERT INTO journal_entries (id, tenant_id, entry_type) VALUES (?, ?, ?)",
                            entryId, tenantId, "APPROVAL");
                    jdbcTemplate.update(
                            "INSERT INTO journal_lines (id, journal_entry_id, account_id, amount_minor) " +
                                    "VALUES (?, ?, ?, ?)",
                            UUID.randomUUID(), entryId, accountA, 100L);
                    jdbcTemplate.update(
                            "INSERT INTO journal_lines (id, journal_entry_id, account_id, amount_minor) " +
                                    "VALUES (?, ?, ?, ?)",
                            UUID.randomUUID(), entryId, accountB, -90L);
                })
        ).hasStackTraceContaining("is not balanced");

        // Confirm nothing persisted (rolled back)
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM journal_entries WHERE id = ?",
                Integer.class, entryId);
        assertThat(count).isZero();
    }

    @Test
    void accepts_entry_whose_lines_sum_to_zero() {
        UUID tenantId = seedTenantAndAccounts();

        UUID entryId = UUID.randomUUID();
        UUID accountA = jdbcTemplate.queryForObject(
                "SELECT id FROM accounts WHERE tenant_id = ? AND name = 'Expense:Travel'",
                UUID.class, tenantId);
        UUID accountB = jdbcTemplate.queryForObject(
                "SELECT id FROM accounts WHERE tenant_id = ? AND name LIKE 'Payable:%'",
                UUID.class, tenantId);

        transactionTemplate.executeWithoutResult(status -> {
            jdbcTemplate.update(
                    "INSERT INTO journal_entries (id, tenant_id, entry_type) VALUES (?, ?, ?)",
                    entryId, tenantId, "APPROVAL");
            jdbcTemplate.update(
                    "INSERT INTO journal_lines (id, journal_entry_id, account_id, amount_minor) " +
                            "VALUES (?, ?, ?, ?)",
                    UUID.randomUUID(), entryId, accountA, 100L);
            jdbcTemplate.update(
                    "INSERT INTO journal_lines (id, journal_entry_id, account_id, amount_minor) " +
                            "VALUES (?, ?, ?, ?)",
                    UUID.randomUUID(), entryId, accountB, -100L);
        });

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM journal_lines WHERE journal_entry_id = ?",
                Integer.class, entryId);
        assertThat(count).isEqualTo(2);
    }
}