package com.saurabh.finance.common.exception;

import com.saurabh.finance.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for the entire application.
 *
 * <p>All handlers return {@code ResponseEntity<ApiResponse<ErrorResponse>>} to
 * enforce the strict API contract — no exception bypasses the standard wrapper.
 *
 * <p>Handler priority (most specific → least specific):
 * <ol>
 *   <li>MethodArgumentNotValidException — Jakarta Validation failures (400)</li>
 *   <li>ResourceNotFoundException      — Domain entity not found (404)</li>
 *   <li>AccessDeniedException          — RBAC authorization failure (403)</li>
 *   <li>Exception                      — Unhandled catch-all (500)</li>
 * </ol>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 1. Validation Errors (400) ─────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - Validation Failed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing   // keep first error per field
                ));

        log.warn("[VALIDATION] Request validation failed with {} error(s): {}",
                fieldErrors.size(), fieldErrors);

        ErrorResponse errorResponse = ErrorResponse.ofValidation(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Request contains invalid field(s). Please review the 'details' map.",
                fieldErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse, "Validation failed"));
    }

    // ── 2. Resource Not Found (404) ────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not Found - Resource does not exist", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<ErrorResponse>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {

        log.warn("[NOT FOUND] {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(errorResponse, ex.getMessage()));
    }

    // ── 3. Access Denied (403) ─────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient privileges", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<ErrorResponse>> handleAccessDeniedException(
            AccessDeniedException ex) {

        log.warn("[ACCESS DENIED] Insufficient privileges: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Access Denied",
                "You do not have permission to perform this action."
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(errorResponse, "Access denied"));
    }

    // ── 3.5. Authentication Failed (401) ───────────────────────────────────

    @ExceptionHandler(AuthenticationException.class)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<ErrorResponse>> handleAuthenticationException(
            AuthenticationException ex) {

        log.warn("[UNAUTHORIZED] Authentication failed: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Invalid username or password"
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(errorResponse, "Authentication failed"));
    }

    // ── 4. Catch-All (500) ─────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGenericException(
            Exception ex) {

        log.error("[INTERNAL ERROR] Unhandled exception: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please contact support."
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(errorResponse, "An unexpected error occurred"));
    }
}
