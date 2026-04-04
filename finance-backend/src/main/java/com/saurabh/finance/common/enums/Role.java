package com.saurabh.finance.common.enums;

/**
 * Defines the access-control roles available in the system.
 * Used for Spring Security RBAC enforcement via @PreAuthorize.
 */
public enum Role {
    ADMIN,
    ANALYST,
    VIEWER
}
