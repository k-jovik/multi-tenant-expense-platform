-- V8: make RLS policies tolerant of unset app.tenant_id.
--
-- current_setting('app.tenant_id', true) returns '' (not NULL) in some
-- Postgres contexts. Without NULLIF, ''::uuid throws
-- "invalid input syntax for type uuid" as a 500 rather than filtering.
--
-- With NULLIF, an unset tenant becomes NULL, and the policy filters all
-- rows without erroring — the safer failure mode.

ALTER POLICY tenant_isolation_users ON users
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER POLICY tenant_isolation_accounts ON accounts
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER POLICY tenant_isolation_expenses ON expenses
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER POLICY tenant_isolation_journal_entries ON journal_entries
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);

ALTER POLICY tenant_isolation_audit_logs ON audit_logs
    USING (tenant_id = NULLIF(current_setting('app.tenant_id', true), '')::uuid);