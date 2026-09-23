package io.github.kjovik.expenseplatform.repository;

import io.github.kjovik.expenseplatform.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {

}
