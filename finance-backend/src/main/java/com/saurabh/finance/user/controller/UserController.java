package com.saurabh.finance.user.controller;

import com.saurabh.finance.common.dto.ApiResponse;
import com.saurabh.finance.common.enums.UserStatus;
import com.saurabh.finance.user.dto.CreateUserRequest;
import com.saurabh.finance.user.dto.UserResponse;
import com.saurabh.finance.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for managing user accounts.
 *
 * <p>All endpoints in this controller are strictly protected by ADMIN role.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for administering users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user", description = "Registers a new user in the system with an active status by default.")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("[CONTROLLER] POST /api/v1/users — creating user '{}'", request.username());
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "User created successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users", description = "Retrieves all users in the system.")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        log.debug("[CONTROLLER] GET /api/v1/users");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user status", description = "Update user status to ACTIVE or INACTIVE.")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable UUID id,
            @RequestParam UserStatus status) {
        log.info("[CONTROLLER] PUT /api/v1/users/{}/status — setting status to {}", id, status);
        UserResponse updatedUser = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User status updated successfully"));
    }
}
