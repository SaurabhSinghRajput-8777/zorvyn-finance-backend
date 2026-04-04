package com.saurabh.finance.common.exception;

/**
 * Thrown when a requested domain entity cannot be found in the database.
 * Handled by GlobalExceptionHandler → mapped to HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super("%s not found with %s: '%s'".formatted(resourceName, fieldName, fieldValue));
    }
}
