package io.github.kjovik.expenseplatform.entity;

import io.github.kjovik.expenseplatform.enums.EntryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "journal_entries")
public class JournalEntry {
    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column (nullable = false)
    private UUID tenantId;

    @Column (unique = true)
    private UUID expenseId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EntryType entryType;

    @Column (nullable = false, insertable = false,updatable = false)
    private Instant createdAt;
}
