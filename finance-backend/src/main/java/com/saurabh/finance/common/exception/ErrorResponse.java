package com.saurabh.finance.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Structured error payload returned inside ApiResponse<ErrorResponse> on failures.
 * The 'details' map carries per-field validation errors from MethodArgumentNotValidException.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> details
) {

    /**
     * Factory for simple single-message errors (non-validation).
     */
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, null);
    }

    /**
     * Factory for validation errors with per-field detail map.
     */
    public static ErrorResponse ofValidation(int status, String error, String message,
                                             Map<String, String> details) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, details);
    }
}
