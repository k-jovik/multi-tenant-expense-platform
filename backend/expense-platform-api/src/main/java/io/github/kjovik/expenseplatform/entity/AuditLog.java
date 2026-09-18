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
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private UUID tenantId;

    @Column
    private UUID userId;

    @Column (length = 100, nullable = false)
    private String action;

    @Column(nullable = false,length = 50)
    private String resourceType;

    @Column(length = 100)
    private String resourceId;

    @Column(columnDefinition = "JSONB")
    private String details;

    @Column (nullable = false,insertable = false,updatable = false)
    private Instant createdAt;
}
