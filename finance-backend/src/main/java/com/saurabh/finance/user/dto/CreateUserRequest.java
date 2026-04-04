package com.saurabh.finance.user.dto;

import com.saurabh.finance.common.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Incoming request payload for creating a new user.
 * Enforces Jakarta Validation constraints before reaching the service layer.
 */
public record CreateUserRequest(

                @NotBlank(message = "Username is required and must not be blank") @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters") String username,

                @NotBlank(message = "Password is required and must not be blank") @Size(min = 8, message = "Password must be at least 8 characters") String password,

                @NotNull(message = "Role is required") Role role) {
}
