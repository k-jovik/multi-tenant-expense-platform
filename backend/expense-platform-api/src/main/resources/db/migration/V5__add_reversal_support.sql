-- V5: Add reversal support to expenses and journal entries
--
-- Rationale: ledger is append-only. A reversal does not delete or modify the
-- original journal entry — it creates a new entry with opposite-signed lines
-- that nets the effect to zero while preserving the audit trail.

-- 1) Extend expenses.status to allow REVERSED
ALTER TABLE expenses DROP CONSTRAINT IF EXISTS expenses_status_check;
ALTER TABLE expenses ADD CONSTRAINT expenses_status_check
    CHECK (status IN ('DRAFT','SUBMITTED','APPROVED','REJECTED','REVERSED'));

-- 2) Reversal tracking columns on expenses
ALTER TABLE expenses
    ADD COLUMN reversed_at TIMESTAMPTZ,
    ADD COLUMN reversed_by_id UUID REFERENCES users(id),
    ADD COLUMN reversal_reason VARCHAR(500);

-- 3) Allow multiple journal entries per expense (original + reversal).
--    The "one approval per expense" invariant is enforced in the service
--    layer by requiring status = SUBMITTED before approval.
ALTER TABLE journal_entries DROP CONSTRAINT IF EXISTS journal_entries_expense_id_key;

-- 4) Link a reversal entry back to the entry it reverses
ALTER TABLE journal_entries
    ADD COLUMN reverses_entry_id UUID REFERENCES journal_entries(id);

-- 5) Helpful indexes
CREATE INDEX idx_expenses_reversed_by ON expenses(reversed_by_id)
    WHERE reversed_by_id IS NOT NULL;

CREATE INDEX idx_journal_entries_expense ON journal_entries(expense_id);
CREATE INDEX idx_journal_entries_reverses ON journal_entries(reverses_entry_id)
    WHERE reverses_entry_id IS NOT NULL;