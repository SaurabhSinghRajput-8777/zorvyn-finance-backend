package com.saurabh.finance.transaction.entity;

import com.saurabh.finance.common.enums.RecordType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core financial ledger entry entity.
 *
 * <p><strong>Bounded Context Isolation:</strong> {@code userId} is stored as a raw
 * {@link UUID} rather than a {@code @ManyToOne User} reference. This enforces strict
 * domain separation — the transaction domain must never directly query the user table.
 *
 * <p><strong>Soft Deletion Strategy:</strong>
 * <ul>
 *   <li>{@link SQLDelete} intercepts all {@code DELETE} calls and converts them to
 *       an {@code UPDATE} that sets {@code is_deleted = true}, preserving the audit trail.</li>
 *   <li>{@link SQLRestriction} appends {@code is_deleted = false} to every generated
 *       SQL query at the Hibernate level, making deleted records invisible without
 *       any application-layer filtering.</li>
 * </ul>
 */
@Entity
@Table(name = "financial_records")
@SQLDelete(sql = "UPDATE financial_records SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    /**
     * References the owning user by ID only — no JPA join to the users table.
     * Enforces bounded context isolation as per our DDD standards.
     */
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    /**
     * Monetary amount. Using BigDecimal with precision 19, scale 4 for financial accuracy.
     * Never use float/double for money.
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordType type;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(length = 500)
    private String notes;

    /**
     * Soft-delete flag. Default {@code false} — never hard-deleted to preserve audit trail.
     * Managed by {@link SQLDelete}; never set manually in business logic.
     */
    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
