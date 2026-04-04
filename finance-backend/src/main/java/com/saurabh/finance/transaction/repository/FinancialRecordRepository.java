package com.saurabh.finance.transaction.repository;

import com.saurabh.finance.common.enums.RecordType;
import com.saurabh.finance.transaction.entity.FinancialRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for the transaction domain.
 *
 * <p>All query methods here automatically respect the {@code @SQLRestriction("is_deleted = false")}
 * defined on {@link FinancialRecord} — no manual {@code isDeleted = false} predicates needed.
 *
 * <p>All list-returning methods accept {@link Pageable} to enforce our pagination standard.
 * Unconstrained {@code findAll()} is intentionally not exposed from the service layer.
 */
@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, UUID> {

    /** All records for a specific user, paginated. */
    Page<FinancialRecord> findByUserId(UUID userId, Pageable pageable);

    /** All records of a given type (INCOME/EXPENSE), paginated. */
    Page<FinancialRecord> findByType(RecordType type, Pageable pageable);

    /** All records for a given category, paginated. */
    Page<FinancialRecord> findByCategory(String category, Pageable pageable);

    /** Combined filter: type + category, paginated. */
    Page<FinancialRecord> findByTypeAndCategory(RecordType type, String category, Pageable pageable);

    /** Combined filter: userId + type, paginated. */
    Page<FinancialRecord> findByUserIdAndType(UUID userId, RecordType type, Pageable pageable);

    /** Combined filter: userId + category, paginated. */
    Page<FinancialRecord> findByUserIdAndCategory(UUID userId, String category, Pageable pageable);

    /** Full filter: userId + type + category, paginated. */
    Page<FinancialRecord> findByUserIdAndTypeAndCategory(UUID userId, RecordType type,
                                                          String category, Pageable pageable);
}
