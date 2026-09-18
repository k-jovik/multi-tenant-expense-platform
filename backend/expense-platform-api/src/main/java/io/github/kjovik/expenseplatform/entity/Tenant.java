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
@Table (name = "tenants")
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column (nullable = false, unique = true, length = 255)
    private String name;

    @Column (nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column (columnDefinition = "jsonb")
    private String metadata;
}
