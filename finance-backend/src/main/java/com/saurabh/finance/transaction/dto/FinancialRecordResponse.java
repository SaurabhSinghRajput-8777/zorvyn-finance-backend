package com.saurabh.finance.transaction.dto;

import com.saurabh.finance.common.enums.RecordType;
import com.saurabh.finance.transaction.entity.FinancialRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-only projection of a {@link FinancialRecord} entity for the web layer.
 *
 * <p>The {@code isDeleted} field is intentionally excluded — by the time a record
 * reaches this DTO, Hibernate's {@code @SQLRestriction} has already guaranteed
 * it is not soft-deleted. Exposing this flag would be misleading.
 *
 * <p>The static {@link #from(FinancialRecord)} factory keeps mapping logic
 * self-contained without requiring a separate mapper class.
 */
public record FinancialRecordResponse(
        UUID id,
        UUID userId,
        BigDecimal amount,
        RecordType type,
        String category,
        LocalDate transactionDate,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Maps a {@link FinancialRecord} entity to this response DTO.
     * Called exclusively from the service layer — never from the controller.
     */
    public static FinancialRecordResponse from(FinancialRecord record) {
        return new FinancialRecordResponse(
                record.getId(),
                record.getUserId(),
                record.getAmount(),
                record.getType(),
                record.getCategory(),
                record.getTransactionDate(),
                record.getNotes(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
