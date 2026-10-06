package io.github.kjovik.expenseplatform.context;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Sets the Postgres session variable that RLS policies read.
 * Must be called inside an active transaction; SET LOCAL is transaction-scoped.
 */
@Component
public class RlsContext {

    @PersistenceContext
    private EntityManager entityManager;

    public void setTenant(UUID tenantId) {
        if (tenantId == null) {
            return;
        }
        // NOTE: Postgres does not allow bind parameters in SET statements.
        // The value is a UUID (canonical hex form only), so inlining is safe
        // from SQL injection — no quotes or special characters can appear.
        entityManager
                .createNativeQuery("SET LOCAL app.tenant_id = '" + tenantId + "'")
                .executeUpdate();
    }
}