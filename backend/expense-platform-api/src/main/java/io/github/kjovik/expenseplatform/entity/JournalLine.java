package io.github.kjovik.expenseplatform.entity;

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
@Table(name = "journal_lines")
public class JournalLine {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column (nullable = false)
    private UUID journalEntryId;

    @Column (nullable = false)
    private UUID accountId;

    @Column (nullable = false)
    private Long amountMinor;

    @Column (nullable = false, insertable = false, updatable = false)
    private Instant createdAt;
}
