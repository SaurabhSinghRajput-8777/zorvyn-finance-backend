package com.saurabh.finance.user.dto;

import com.saurabh.finance.common.enums.Role;
import com.saurabh.finance.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-only view of a User entity exposed to the web layer.
 * The entity's raw password field is intentionally excluded.
 */
public record UserResponse(
        UUID id,
        String username,
        Role role,
        String status,
        LocalDateTime createdAt) {
    /**
     * Static mapper — keeps mapping logic inside the DTO to avoid a mapper class
     * dependency in this focused domain.
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getStatus().name(),
                user.getCreatedAt());
    }
}
