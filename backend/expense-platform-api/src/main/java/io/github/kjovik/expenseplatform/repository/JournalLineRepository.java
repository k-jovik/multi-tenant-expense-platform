package io.github.kjovik.expenseplatform.repository;

import io.github.kjovik.expenseplatform.entity.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JournalLineRepository extends JpaRepository<JournalLine, UUID> {

}
