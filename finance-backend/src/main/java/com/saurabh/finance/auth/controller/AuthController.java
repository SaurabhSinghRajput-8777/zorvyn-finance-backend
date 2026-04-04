package com.saurabh.finance.auth.controller;

import com.saurabh.finance.auth.dto.AuthResponse;
import com.saurabh.finance.auth.dto.LoginRequest;
import com.saurabh.finance.common.dto.ApiResponse;
import com.saurabh.finance.security.jwt.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller for the auth domain.
 *
 * <p>
 * Base path: {@code /api/v1/auth} — publicly accessible (no JWT required).
 * Configured in {@link com.saurabh.finance.security.config.SecurityConfig}.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "JWT-based authentication endpoints")
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final JwtUtil jwtUtil;

        /**
         * Authenticates user credentials and returns a signed JWT.
         *
         * <p>
         * Flow:
         * <ol>
         * <li>{@code @Valid} triggers Jakarta Validation on the request body.</li>
         * <li>{@link AuthenticationManager} delegates to
         * {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider},
         * which calls
         * {@link com.saurabh.finance.user.service.CustomUserDetailsService}.</li>
         * <li>On success, a JWT is generated and returned inside
         * {@link ApiResponse}.</li>
         * </ol>
         *
         * @param request validated login credentials
         * @return {@code 200 OK} with JWT and user identity, or {@code 401} on failure
         */
        @PostMapping("/login")
        @Operation(summary = "Authenticate and retrieve JWT", description = "Provide valid credentials to receive a Bearer token for subsequent requests.")
        public ResponseEntity<ApiResponse<AuthResponse>> login(
                        @Valid @RequestBody LoginRequest request) {

                log.info("[AUTH] Login attempt for username: '{}'", request.username());

                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                String role = userDetails.getAuthorities()
                                .stream()
                                .findFirst()
                                .map(GrantedAuthority::getAuthority)
                                .orElse("");

                String token = jwtUtil.generateToken(userDetails);

                AuthResponse authResponse = new AuthResponse(token, userDetails.getUsername(), role);

                log.info("[AUTH] Login successful for username: '{}', role: {}", userDetails.getUsername(), role);

                return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
        }
}
