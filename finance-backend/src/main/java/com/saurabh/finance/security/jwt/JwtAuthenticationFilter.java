package com.saurabh.finance.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.saurabh.finance.user.service.CustomUserDetailsService;

import java.io.IOException;

/**
 * Stateless JWT authentication filter.
 *
 * <p>
 * Runs once per request ({@link OncePerRequestFilter}). Extracts the Bearer
 * token from the {@code Authorization} header, validates it via
 * {@link JwtUtil},
 * and populates the {@link SecurityContextHolder} so that downstream
 * {@code @PreAuthorize} annotations function correctly.
 *
 * <p>
 * Any malformed or expired token is silently swallowed — the request proceeds
 * without authentication, and Spring Security's authorization layer will reject
 * it
 * with a 403 if the endpoint requires authentication.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER = "Authorization";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTH_HEADER);

        // Skip filter if no valid Bearer token header present
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX.length());

        try {
            final String username = jwtUtil.extractUsername(jwt);

            // Only proceed if username resolved and no existing auth in context
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("[JWT FILTER] Authenticated '{}' for request: {} {}",
                            username, request.getMethod(), request.getRequestURI());
                } else {
                    log.warn("[JWT FILTER] Invalid token presented for user: '{}'", username);
                }
            }
        } catch (Exception e) {
            // Silently log — Spring Security will deny access downstream
            log.warn("[JWT FILTER] Token processing error on {} {}: {}",
                    request.getMethod(), request.getRequestURI(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
