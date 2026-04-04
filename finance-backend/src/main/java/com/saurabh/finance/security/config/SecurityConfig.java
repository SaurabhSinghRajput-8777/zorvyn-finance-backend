package com.saurabh.finance.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.saurabh.finance.security.jwt.JwtAuthenticationFilter;

/**
 * Central Spring Security configuration for the finance backend.
 *
 * <p>
 * Design decisions:
 * <ul>
 * <li><strong>Stateless sessions:</strong> No HttpSession is created or used.
 * Authentication state is carried entirely within the JWT.</li>
 * <li><strong>CSRF disabled:</strong> Not required for stateless REST APIs
 * that do not use browser cookies for authentication.</li>
 * <li><strong>Method security enabled:</strong> {@code @PreAuthorize} works at
 * the controller and service level for fine-grained RBAC enforcement.</li>
 * <li><strong>PasswordEncoder as a standalone bean:</strong> Defined here to
 * avoid circular dependency with UserDetailsService implementations.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        private static final String[] PUBLIC_ENDPOINTS = {
                        "/",
                        "/api/v1/auth/**",
                        "/v3/api-docs/**",
                        "/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
        };

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * BCrypt password encoder with default strength (10 rounds).
         * Strength 10 is the industry-standard balance between security and
         * performance.
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        /**
         * Exposes the {@link AuthenticationManager} from Spring Boot's
         * auto-configuration.
         * This absorbs the
         * {@link com.saurabh.finance.user.service.CustomUserDetailsService} and
         * {@link PasswordEncoder} beans automatically via
         * {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}.
         */
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
                        throws Exception {
                return config.getAuthenticationManager();
        }
}
