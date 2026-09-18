package io.github.kjovik.expenseplatform.entity;

import io.github.kjovik.expenseplatform.enums.AccountType;
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
@Table ( name = "accounts")
public class Account {
    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column (nullable = false)
    private UUID tenantId;

    @Column (nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false, length = 50)
    private AccountType type;

    @Column (nullable = false, insertable = false, updatable = false)
    private Instant createdAt;
}
