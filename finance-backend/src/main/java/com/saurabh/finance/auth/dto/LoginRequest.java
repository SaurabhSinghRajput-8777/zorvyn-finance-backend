package com.saurabh.finance.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Incoming request payload for the login endpoint.
 * Jakarta Validation enforced before reaching the service layer.
 */
public record LoginRequest(

                @NotBlank(message = "Username is required and must not be blank") String username,

                @NotBlank(message = "Password is required and must not be blank") String password) {
}
