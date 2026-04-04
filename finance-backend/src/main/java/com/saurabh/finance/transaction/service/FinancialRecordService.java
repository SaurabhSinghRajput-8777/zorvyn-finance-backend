package com.saurabh.finance.transaction.service;

import com.saurabh.finance.common.enums.RecordType;
import com.saurabh.finance.transaction.dto.CreateRecordRequest;
import com.saurabh.finance.transaction.dto.FinancialRecordResponse;
import com.saurabh.finance.transaction.dto.UpdateRecordRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Contract for financial record management operations.
 *
 * <p>This is the only permitted entry point for the transaction domain.
 * No cross-domain repository access is allowed — all interactions happen
 * through this service interface.
 */
public interface FinancialRecordService {

    /**
     * Creates a new financial record. The owning user ID must exist.
     *
     * @param request validated creation payload
     * @return the persisted record as a response DTO
     */
    FinancialRecordResponse createRecord(CreateRecordRequest request);

    /**
     * Returns a paginated, filtered list of financial records.
     * All filter parameters are optional and combinable.
     *
     * @param userId   optional — filter by owning user
     * @param type     optional — filter by INCOME or EXPENSE
     * @param category optional — filter by category string
     * @param pageable pagination and sort configuration
     * @return a page of record response DTOs
     */
    Page<FinancialRecordResponse> getRecords(UUID userId, RecordType type,
                                             String category, Pageable pageable);

    /**
     * Updates an existing, non-deleted record.
     *
     * @param id      the record UUID
     * @param request validated update payload
     * @return the updated record as a response DTO
     * @throws com.saurabh.finance.common.exception.ResourceNotFoundException if not found
     */
    FinancialRecordResponse updateRecord(UUID id, UpdateRecordRequest request);

    /**
     * Soft-deletes a record by setting {@code is_deleted = true}.
     * Hibernate's {@code @SQLDelete} handles the actual SQL.
     *
     * @param id the record UUID
     * @throws com.saurabh.finance.common.exception.ResourceNotFoundException if not found
     */
    void deleteRecord(UUID id);
}
