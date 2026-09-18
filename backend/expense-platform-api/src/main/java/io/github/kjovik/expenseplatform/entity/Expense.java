package io.github.kjovik.expenseplatform.entity;


import io.github.kjovik.expenseplatform.enums.ExpenseStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table (name = "expenses")
public class Expense {
    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID userId;

    @Column (nullable = false)
    private Long amountMinor;

    @Column (nullable = false, length = 3)
    private String currency;

    @Column (nullable = false, precision = 18, scale = 8)
    private BigDecimal fxRateAtSubmission;

    @Column (nullable = false, length = 100)
    private String category;

    @Column (length = 500)
    private String description;

    @Column (length = 500)
    private String receiptUrl;

    @Column (length = 5000)
    private String receiptText;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false, length = 20)
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    @Column
    private UUID approvedById;

    @Column (length = 500)
    private String rejectionReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column (nullable = false, columnDefinition = "jsonb")
    private String anomalyFlags = "{}";

    @Column ( length = 255)
    private String idempotencyKey;

    @Column ( nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant submittedAt;
    @Column
    private Instant approvedAt;

}
