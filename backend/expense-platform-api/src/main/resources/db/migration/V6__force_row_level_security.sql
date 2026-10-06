-- V6: Force RLS to apply to the table owner (the app connects as
-- the owning role, so without FORCE the policies are silently skipped).

ALTER TABLE users           FORCE ROW LEVEL SECURITY;
ALTER TABLE accounts        FORCE ROW LEVEL SECURITY;
ALTER TABLE expenses        FORCE ROW LEVEL SECURITY;
ALTER TABLE journal_entries FORCE ROW LEVEL SECURITY;
ALTER TABLE audit_logs      FORCE ROW LEVEL SECURITY;