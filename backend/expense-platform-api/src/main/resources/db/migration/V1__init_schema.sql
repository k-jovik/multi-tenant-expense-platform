-- Tenants (companies using the SaaS)
CREATE TABLE tenants (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         name VARCHAR(255) NOT NULL UNIQUE,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                         metadata JSONB
);

-- Users (people who log in)
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                       email VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL CHECK (role IN ('EMPLOYEE','MANAGER','ADMIN')),
                       full_name VARCHAR(255),
                       created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                       UNIQUE(tenant_id, email)
);
CREATE INDEX idx_users_tenant ON users(tenant_id);

-- Accounts (chart of accounts: buckets money flows through)
CREATE TABLE accounts (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                          name VARCHAR(255) NOT NULL,
                          type VARCHAR(50) NOT NULL CHECK (type IN ('CASH','PAYABLE','EXPENSE')),
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                          UNIQUE(tenant_id, name)
);
CREATE INDEX idx_accounts_tenant ON accounts(tenant_id);

-- Expenses (claims submitted by employees)
CREATE TABLE expenses (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                          user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          amount_minor BIGINT NOT NULL CHECK (amount_minor > 0),
                          currency CHAR(3) NOT NULL DEFAULT 'USD',
                          fx_rate_at_submission NUMERIC(18,8) NOT NULL DEFAULT 1.0,
                          category VARCHAR(100) NOT NULL,
                          description VARCHAR(500),
                          receipt_url VARCHAR(500),
                          receipt_text VARCHAR(5000),
                          status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                              CHECK (status IN ('DRAFT','SUBMITTED','APPROVED','REJECTED')),
                          approved_by_id UUID REFERENCES users(id),
                          rejection_reason VARCHAR(500),
                          anomaly_flags JSONB NOT NULL DEFAULT '{}'::jsonb,
                          idempotency_key VARCHAR(255),
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                          submitted_at TIMESTAMPTZ,
                          approved_at TIMESTAMPTZ,
                          UNIQUE(tenant_id, idempotency_key)
);
CREATE INDEX idx_expenses_tenant_status ON expenses(tenant_id, status);
CREATE INDEX idx_expenses_user ON expenses(user_id);

-- Journal Entries (header of each financial event; append-only)
CREATE TABLE journal_entries (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                                 expense_id UUID UNIQUE REFERENCES expenses(id) ON DELETE CASCADE,
                                 entry_type VARCHAR(20) NOT NULL CHECK (entry_type IN ('APPROVAL','REVERSAL','PAYMENT')),
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_journal_entries_tenant ON journal_entries(tenant_id);

-- Journal Lines (debits/credits; SIGNED: debit positive, credit negative)
CREATE TABLE journal_lines (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               journal_entry_id UUID NOT NULL REFERENCES journal_entries(id) ON DELETE CASCADE,
                               account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,
                               amount_minor BIGINT NOT NULL CHECK (amount_minor <> 0),
                               created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_journal_lines_entry ON journal_lines(journal_entry_id);
CREATE INDEX idx_journal_lines_account ON journal_lines(account_id);

-- Audit Log (append-only trail)
CREATE TABLE audit_logs (
                            id BIGSERIAL PRIMARY KEY,
                            tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
                            user_id UUID REFERENCES users(id) ON DELETE SET NULL,
                            action VARCHAR(100) NOT NULL,
                            resource_type VARCHAR(50) NOT NULL,
                            resource_id VARCHAR(100),
                            details JSONB,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_tenant_time ON audit_logs(tenant_id, created_at DESC);