package com.saurabh.finance.transaction.controller;

import com.saurabh.finance.common.dto.ApiResponse;
import com.saurabh.finance.common.enums.RecordType;
import com.saurabh.finance.transaction.dto.CreateRecordRequest;
import com.saurabh.finance.transaction.dto.FinancialRecordResponse;
import com.saurabh.finance.transaction.dto.UpdateRecordRequest;
import com.saurabh.finance.transaction.service.FinancialRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for the transaction domain.
 *
 * <p>Base path: {@code /api/v1/transactions}
 *
 * <p><strong>RBAC enforcement:</strong>
 * <ul>
 *   <li>POST, PUT, DELETE — ADMIN only</li>
 *   <li>GET — ADMIN and ANALYST</li>
 * </ul>
 * Method-level security via {@code @PreAuthorize} uses roles mapped with
 * the {@code ROLE_} prefix by {@link com.saurabh.finance.user.service.CustomUserDetailsService}.
 *
 * <p><strong>Pagination standard:</strong> The GET endpoint accepts {@code page},
 * {@code size}, {@code sortBy}, and {@code sortDir} parameters explicitly so that the
 * Swagger UI can document them. Spring's {@code Pageable} argument resolver is not used
 * directly to keep the API contract explicit and version-stable.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Financial Records", description = "CRUD operations and filtering for financial ledger entries")
@SecurityRequirement(name = "bearerAuth")
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    // ── POST / — Create Record (ADMIN only) ────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a financial record",
            description = "Creates a new ledger entry. Restricted to ADMIN role."
    )
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> createRecord(
            @Valid @RequestBody CreateRecordRequest request) {

        log.info("[CONTROLLER] POST /api/v1/transactions — userId={}, type={}",
                request.userId(), request.type());

        FinancialRecordResponse response = recordService.createRecord(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Financial record created successfully"));
    }

    // ── GET / — List Records (ADMIN + ANALYST) ─────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(
            summary = "Retrieve financial records",
            description = "Returns a paginated list of records. Supports optional filtering by userId, type, and category."
    )
    public ResponseEntity<ApiResponse<Page<FinancialRecordResponse>>> getRecords(
            @RequestParam(defaultValue = "0")          int page,
            @RequestParam(defaultValue = "10")         int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "DESC")       String sortDir,
            @RequestParam(required = false)            UUID userId,
            @RequestParam(required = false)            RecordType type,
            @RequestParam(required = false)            String category) {

        log.debug("[CONTROLLER] GET /api/v1/transactions — page={}, size={}, userId={}, type={}, category={}",
                page, size, userId, type, category);

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PageRequest pageable = PageRequest.of(page, size, sort);
        Page<FinancialRecordResponse> records = recordService.getRecords(userId, type, category, pageable);

        return ResponseEntity.ok(ApiResponse.success(records,
                "Records retrieved successfully. Total: " + records.getTotalElements()));
    }

    // ── PUT /{id} — Update Record (ADMIN only) ─────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update a financial record",
            description = "Updates an existing, non-deleted record. Restricted to ADMIN role."
    )
    public ResponseEntity<ApiResponse<FinancialRecordResponse>> updateRecord(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRecordRequest request) {

        log.info("[CONTROLLER] PUT /api/v1/transactions/{} — type={}, amount={}", id, request.type(), request.amount());

        FinancialRecordResponse response = recordService.updateRecord(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Financial record updated successfully"));
    }

    // ── DELETE /{id} — Soft Delete (ADMIN only) ────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Soft-delete a financial record",
            description = "Marks a record as deleted (is_deleted=true). The record is preserved in the database for audit purposes."
    )
    public ResponseEntity<ApiResponse<Void>> deleteRecord(@PathVariable UUID id) {
        log.info("[CONTROLLER] DELETE /api/v1/transactions/{}", id);

        recordService.deleteRecord(id);
        return ResponseEntity.ok(ApiResponse.success("Financial record deleted successfully"));
    }
}
