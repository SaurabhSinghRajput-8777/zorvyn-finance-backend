package com.saurabh.finance.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Unified API response wrapper used across ALL endpoints in the system.
 * Contract: every HTTP response body is of type ApiResponse<T>.
 *
 * @param <T> the type of the data payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        String message
) {

    // ── Static factory: success with data and message ──────────────────────
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    // ── Static factory: success with data only ─────────────────────────────
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    // ── Static factory: success with message only (no body, e.g. DELETE) ───
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, null, message);
    }

    // ── Static factory: error with message only ────────────────────────────
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }

    // ── Static factory: error with error-detail payload ────────────────────
    public static <T> ApiResponse<T> error(T data, String message) {
        return new ApiResponse<>(false, data, message);
    }
}
