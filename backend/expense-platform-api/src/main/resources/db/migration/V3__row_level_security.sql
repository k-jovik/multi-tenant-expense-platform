-- Enable RLS on tenant-owned tables
ALTER TABLE users           ENABLE ROW LEVEL SECURITY;
ALTER TABLE accounts        ENABLE ROW LEVEL SECURITY;
ALTER TABLE expenses        ENABLE ROW LEVEL SECURITY;
ALTER TABLE journal_entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs      ENABLE ROW LEVEL SECURITY;

-- Policies: a row is visible only if its tenant_id matches the
-- current session's app.tenant_id. The `true` argument makes
-- current_setting return NULL (not throw) when unset, so queries
-- without a session tenant return zero rows rather than erroring.

CREATE POLICY tenant_isolation_users ON users
  USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

CREATE POLICY tenant_isolation_accounts ON accounts
  USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

CREATE POLICY tenant_isolation_expenses ON expenses
  USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

CREATE POLICY tenant_isolation_journal_entries ON journal_entries
  USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

CREATE POLICY tenant_isolation_audit_logs ON audit_logs
  USING (tenant_id = current_setting('app.tenant_id', true)::uuid);