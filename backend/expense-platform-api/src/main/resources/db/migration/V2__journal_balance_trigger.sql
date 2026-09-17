-- Enforces that every journal entry's lines sum to zero.
-- Fires at COMMIT (deferred), so insert the debit and credit lines
-- in any order — the check happens once when the transaction commits.

CREATE OR REPLACE FUNCTION check_journal_balanced()
RETURNS TRIGGER AS $$
DECLARE
total BIGINT;
BEGIN
SELECT COALESCE(SUM(amount_minor), 0) INTO total
FROM journal_lines
WHERE journal_entry_id = NEW.journal_entry_id;

IF total <> 0 THEN
    RAISE EXCEPTION 'Journal entry % is not balanced: sum = %',
      NEW.journal_entry_id, total;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER trg_journal_balanced
AFTER INSERT OR UPDATE ON journal_lines
                              DEFERRABLE INITIALLY DEFERRED
                              FOR EACH ROW
                              EXECUTE FUNCTION check_journal_balanced();