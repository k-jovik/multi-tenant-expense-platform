package io.github.kjovik.expenseplatform.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table (name = "tenants")
@Data
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column (nullable = false, unique = true)
    private String name;

    @Column (nullable = false, insertable = false, updatable = false)
    private Instant createdAt;
}
