package com.saurabh.finance.auth.dto;

/**
 * Response payload returned upon successful authentication.
 *
 * <p>
 * Contains the JWT bearer token and the authenticated user's identity/role
 * so the frontend can make immediate role-based decisions without decoding the
 * token.
 */
public record AuthResponse(
                String token,
                String username,
                String role) {
}
