package io.github.kjovik.expenseplatform.repository;

import io.github.kjovik.expenseplatform.entity.JournalEntry;
import io.github.kjovik.expenseplatform.enums.EntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    Optional<JournalEntry> findByExpenseIdAndEntryType(UUID expenseId, EntryType entryType);
}
