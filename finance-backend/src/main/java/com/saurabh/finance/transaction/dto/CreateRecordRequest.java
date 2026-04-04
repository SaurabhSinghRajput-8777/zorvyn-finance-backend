package com.saurabh.finance.transaction.dto;

import com.saurabh.finance.common.enums.RecordType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Validated request payload for creating a new financial record.
 *
 * <p>All Jakarta Validation constraints are applied at the controller layer via
 * {@code @Valid}, before execution reaches the service.
 */
public record CreateRecordRequest(

        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be a positive value")
        BigDecimal amount,

        @NotNull(message = "Record type is required (INCOME or EXPENSE)")
        RecordType type,

        @NotBlank(message = "Category is required and must not be blank")
        @Size(max = 100, message = "Category must not exceed 100 characters")
        String category,

        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {}
