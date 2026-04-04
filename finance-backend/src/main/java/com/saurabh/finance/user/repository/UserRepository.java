package com.saurabh.finance.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saurabh.finance.user.entity.User;
import com.saurabh.finance.user.service.CustomUserDetailsService;
import com.saurabh.finance.user.service.UserServiceImpl;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unused")
/**
 * Data access layer for the User domain.
 *
 * <p>
 * Strictly scoped to this domain — no cross-domain repository access permitted.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Used by {@link CustomUserDetailsService} for authentication.
     */
    Optional<User> findByUsername(String username);

    /**
     * Used by {@link com.saurabh.finance.user.service.UserServiceImpl} to enforce
     * uniqueness before persisting a new user.
     */
    boolean existsByUsername(String username);
}
