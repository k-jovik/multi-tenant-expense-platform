package io.github.kjovik.expenseplatform.entity;

import io.github.kjovik.expenseplatform.enums.Type;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table ( name = "accounts")
public class Account {
    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private UUID tenantId;

    @Column (nullable = false)
    private String name;

    @EnumeratedValue
    @Column (nullable = false)
    private Type type;

    @Column (nullable = false, insertable = false, updatable = false)
    private Instant createdAt;
}
