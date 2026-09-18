package io.github.kjovik.expenseplatform.entity;

import io.github.kjovik.expenseplatform.enums.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table (name = "users")
public class User {
    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column (nullable = false)
    private UUID tenantId;

    @Column (nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false)
    private Role role;

    @Column
    private String fullName;

    @Column (nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

}
