package com.saurabh.finance.user.service;

import com.saurabh.finance.common.enums.UserStatus;
import com.saurabh.finance.user.dto.CreateUserRequest;
import com.saurabh.finance.user.dto.UserResponse;

import java.util.List;
import java.util.UUID;


/**
 * Contract for user management operations.
 *
 * <p>
 * This interface is the only permitted communication boundary for other domains
 * that need to interact with user data. No cross-domain repository access is
 * allowed.
 */
public interface UserService {

    /**
     * Creates a new user with an encoded password.
     * Throws {@link IllegalArgumentException} if username is already taken.
     *
     * @param request validated creation payload
     * @return the persisted user as a sanitized DTO
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Retrieves all users in the system.
     *
     * @return list of user DTOs (passwords excluded)
     */
    List<UserResponse> getAllUsers();

    /**
     * Resolves a username to its full user profile.
     * Used by the analytics domain to convert the authenticated principal
     * (username string from JWT) into a UUID for repository queries.
     *
     * @param username the login username
     * @return the user DTO
     * @throws com.saurabh.finance.common.exception.ResourceNotFoundException if not found
     */
    UserResponse getUserByUsername(String username);

    /**
     * Updates the status of an existing user.
     *
     * @param id the user ID
     * @param status the new status to set
     * @return the updated user DTO
     * @throws com.saurabh.finance.common.exception.ResourceNotFoundException if user doesn't exist
     */
    UserResponse updateUserStatus(UUID id, UserStatus status);
}
