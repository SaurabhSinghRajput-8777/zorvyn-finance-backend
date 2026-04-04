package com.saurabh.finance.user.service;

import com.saurabh.finance.common.enums.UserStatus;
import com.saurabh.finance.user.entity.User;
import com.saurabh.finance.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security integration service for the user domain.
 *
 * <p>
 * Bridges our {@link User} entity to Spring Security's {@link UserDetails}.
 * The role is mapped with the "ROLE_" prefix, which is required by Spring
 * Security
 * for {@code hasRole()} checks to function correctly with
 * {@code @PreAuthorize}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("[AUTH] Loading UserDetails for username: '{}'", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("[AUTH] Authentication failed — user not found: '{}'", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        if (user.getStatus() == UserStatus.INACTIVE) {
            log.warn("[AUTH] Authentication failed — account is INACTIVE: '{}'", username);
            throw new UsernameNotFoundException("Account is deactivated: " + username);
        }

        log.debug("[AUTH] UserDetails loaded successfully for '{}', role: {}", username, user.getRole());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }
}
