package com.saurabh.finance.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT infrastructure utility for the security domain.
 *
 * <p>
 * Uses JJWT 0.12.5 API. The signing key is a HMAC-SHA256 key derived
 * from a base64-encoded secret configured via {@code jwt.secret}.
 *
 * <p>
 * Token payload:
 * <ul>
 * <li>{@code sub} — username</li>
 * <li>{@code role} — Spring Security authority string (e.g. "ROLE_ADMIN")</li>
 * <li>{@code iat} — issued-at timestamp</li>
 * <li>{@code exp} — expiration timestamp</li>
 * </ul>
 *
 * <p>
 * <strong>Default secret fallback:</strong> Suitable for development only.
 * Override {@code jwt.secret} in production via environment variable.
 */
@Slf4j
@Component
public class JwtUtil {

    // 64-char base64 key = 48 decoded bytes → 384-bit key (well above HS256 minimum
    // of 256 bits)
    @Value("${jwt.secret:Wm9ydnluRmluYW5jZUVudGVycHJpc2VKV1RTZWNyZXRLZXkyMDI0U2VjdXJlVG9rZW4xMjM0NTY=}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // default: 24 hours in ms
    private long expirationMs;

    // ── Key Resolution ─────────────────────────────────────────────────────

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ── Token Generation ───────────────────────────────────────────────────

    /**
     * Generates a signed JWT for the authenticated user.
     * The first granted authority is encoded as the {@code role} claim.
     */
    public String generateToken(UserDetails userDetails) {
        String role = userDetails.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");

        String token = Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();

        log.debug("[JWT] Generated token for user '{}' with role '{}'", userDetails.getUsername(), role);
        return token;
    }

    // ── Claim Extraction ───────────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    // ── Validation ─────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the token subject matches the UserDetails username
     * and the token has not expired.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        final boolean valid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        log.debug("[JWT] Token validation result for '{}': {}", username, valid);
        return valid;
    }

    // ── Private Helpers ────────────────────────────────────────────────────

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
