package com.saurabh.finance.transaction.service;

import com.saurabh.finance.common.enums.RecordType;
import com.saurabh.finance.common.exception.ResourceNotFoundException;
import com.saurabh.finance.transaction.dto.CreateRecordRequest;
import com.saurabh.finance.transaction.dto.FinancialRecordResponse;
import com.saurabh.finance.transaction.dto.UpdateRecordRequest;
import com.saurabh.finance.transaction.entity.FinancialRecord;
import com.saurabh.finance.transaction.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of {@link FinancialRecordService}.
 *
 * <p><strong>Soft Deletion:</strong> Hibernate's {@code @SQLDelete} on {@link FinancialRecord}
 * converts {@code repository.deleteById(id)} into an {@code UPDATE SET is_deleted=true}.
 * The service never manually sets {@code isDeleted} — that would bypass the Hibernate hook.
 *
 * <p><strong>Filter Routing:</strong> The {@link #getRecords} method selects the most
 * specific repository query based on which optional filter parameters are present,
 * reducing result-set size at the database level rather than in memory.
 *
 * <p><strong>Transaction Boundaries:</strong> The class-level {@code @Transactional}
 * covers all mutating operations. Read operations override with {@code readOnly = true}
 * to enable connection-pool and Hibernate optimizations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FinancialRecordServiceImpl implements FinancialRecordService {

    private final FinancialRecordRepository recordRepository;

    // ── Create ─────────────────────────────────────────────────────────────

    @Override
    public FinancialRecordResponse createRecord(CreateRecordRequest request) {
        log.info("[TRANSACTION] Creating record for userId='{}', type={}, amount={}",
                request.userId(), request.type(), request.amount());

        FinancialRecord record = FinancialRecord.builder()
                .userId(request.userId())
                .amount(request.amount())
                .type(request.type())
                .category(request.category())
                .transactionDate(request.transactionDate())
                .notes(request.notes())
                .build();

        FinancialRecord saved = recordRepository.save(record);
        log.info("[TRANSACTION] Record created with id={}", saved.getId());
        return FinancialRecordResponse.from(saved);
    }

    // ── Read (Paginated + Filtered) ─────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<FinancialRecordResponse> getRecords(UUID userId, RecordType type,
                                                    String category, Pageable pageable) {
        log.debug("[TRANSACTION] Fetching records — userId={}, type={}, category={}, page={}",
                userId, type, category, pageable.getPageNumber());

        Page<FinancialRecord> page = routeQuery(userId, type, category, pageable);
        return page.map(FinancialRecordResponse::from);
    }

    /**
     * Routes to the most precise repository method based on which filters are active.
     * All 7 combinations of (userId, type, category) are covered.
     */
    private Page<FinancialRecord> routeQuery(UUID userId, RecordType type,
                                             String category, Pageable pageable) {
        boolean hasUser     = userId != null;
        boolean hasType     = type != null;
        boolean hasCategory = category != null && !category.isBlank();

        if (hasUser && hasType && hasCategory) {
            return recordRepository.findByUserIdAndTypeAndCategory(userId, type, category, pageable);
        } else if (hasUser && hasType) {
            return recordRepository.findByUserIdAndType(userId, type, pageable);
        } else if (hasUser && hasCategory) {
            return recordRepository.findByUserIdAndCategory(userId, category, pageable);
        } else if (hasType && hasCategory) {
            return recordRepository.findByTypeAndCategory(type, category, pageable);
        } else if (hasUser) {
            return recordRepository.findByUserId(userId, pageable);
        } else if (hasType) {
            return recordRepository.findByType(type, pageable);
        } else if (hasCategory) {
            return recordRepository.findByCategory(category, pageable);
        } else {
            return recordRepository.findAll(pageable);
        }
    }

    // ── Update ─────────────────────────────────────────────────────────────

    @Override
    public FinancialRecordResponse updateRecord(UUID id, UpdateRecordRequest request) {
        log.info("[TRANSACTION] Updating record id={}", id);

        FinancialRecord record = findActiveRecordOrThrow(id);

        record.setAmount(request.amount());
        record.setType(request.type());
        record.setCategory(request.category());
        record.setTransactionDate(request.transactionDate());
        record.setNotes(request.notes());

        FinancialRecord updated = recordRepository.save(record);
        log.info("[TRANSACTION] Record id={} updated successfully", id);
        return FinancialRecordResponse.from(updated);
    }

    // ── Soft Delete ────────────────────────────────────────────────────────

    @Override
    public void deleteRecord(UUID id) {
        log.info("[TRANSACTION] Soft-deleting record id={}", id);

        // Verify the record exists and is not already deleted before issuing the @SQLDelete
        findActiveRecordOrThrow(id);
        recordRepository.deleteById(id);
        // Hibernate's @SQLDelete converts the above to: UPDATE financial_records SET is_deleted=true WHERE id=?

        log.info("[TRANSACTION] Record id={} soft-deleted successfully", id);
    }

    // ── Private Helpers ────────────────────────────────────────────────────

    /**
     * Fetches a record by ID, leveraging Hibernate's @SQLRestriction to ensure
     * soft-deleted records are not returned. Throws {@link ResourceNotFoundException}
     * if the ID doesn't correspond to an active record.
     */
    private FinancialRecord findActiveRecordOrThrow(UUID id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[TRANSACTION] Record not found or already deleted: id={}", id);
                    return new ResourceNotFoundException("Financial Record", "id", id);
                });
    }
}
