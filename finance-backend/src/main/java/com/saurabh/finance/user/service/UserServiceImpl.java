package com.saurabh.finance.user.service;

import com.saurabh.finance.common.enums.UserStatus;
import com.saurabh.finance.common.exception.ResourceNotFoundException;

import com.saurabh.finance.user.dto.CreateUserRequest;
import com.saurabh.finance.user.dto.UserResponse;
import com.saurabh.finance.user.entity.User;
import com.saurabh.finance.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link UserService}.
 *
 * <p>
 * All state-mutating methods are wrapped in {@code @Transactional} for ACID
 * compliance.
 * Passwords are always encoded via {@link PasswordEncoder} — plaintext never
 * reaches the DB.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Create ─────────────────────────────────────────────────────────────

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        log.info("[USER] Creating user with username: '{}', role: {}", request.username(), request.role());

        if (userRepository.existsByUsername(request.username())) {
            log.warn("[USER] Username conflict — '{}' already exists", request.username());
            throw new IllegalArgumentException("Username already exists: " + request.username());
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(UserStatus.ACTIVE)
                .build();

        User saved = userRepository.save(user);
        log.info("[USER] Successfully created user '{}' (id: {}) with role: {}",
                saved.getUsername(), saved.getId(), saved.getRole());

        return UserResponse.from(saved);
    }

    // ── Read ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("[USER] Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    // ── Resolve by Username ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        log.debug("[USER] Resolving user by username: '{}'", username);
        return userRepository.findByUsername(username)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }
    
    // ── Update ─────────────────────────────────────────────────────────────

    @Override
    public UserResponse updateUserStatus(UUID id, UserStatus status) {
        log.info("[USER] Updating status for user id={} to {}", id, status);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
                
        user.setStatus(status);
        User updated = userRepository.save(user);
        
        log.info("[USER] Successfully updated status for user '{}'", updated.getUsername());
        return UserResponse.from(updated);
    }
}
