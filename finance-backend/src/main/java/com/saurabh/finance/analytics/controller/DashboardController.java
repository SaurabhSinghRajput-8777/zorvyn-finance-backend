package com.saurabh.finance.analytics.controller;

import com.saurabh.finance.analytics.dto.DashboardSummaryResponse;
import com.saurabh.finance.analytics.dto.RecentActivityDto;
import com.saurabh.finance.analytics.service.DashboardService;
import com.saurabh.finance.common.dto.ApiResponse;
import com.saurabh.finance.user.dto.UserResponse;
import com.saurabh.finance.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Analytics controller for the dashboard domain.
 *
 * <p>Base path: {@code /api/v1/analytics}
 *
 * <p><strong>Access control:</strong> All three roles (ADMIN, ANALYST, VIEWER)
 * can access their own dashboard summary. The authenticated user's UUID is resolved
 * by extracting the username from the {@link SecurityContextHolder} and calling
 * {@link UserService#getUserByUsername(String)} — a service-to-service call that
 * obeys the "no cross-domain repository access" rule.
 *
 * <p><strong>VIEWER data isolation:</strong> Since we always resolve the userId
 * from the JWT principal (not from a query parameter), each user can only ever
 * access their own data — role escalation via URL manipulation is not possible.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Dashboard summary and financial analytics endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    /**
     * Returns the complete dashboard summary for the currently authenticated user.
     *
     * <p>Flow:
     * <ol>
     *   <li>Extract username from {@code SecurityContextHolder} (populated by {@link com.saurabh.finance.security.jwt.JwtAuthenticationFilter}).</li>
     *   <li>Resolve username → {@link UserResponse} via {@link UserService}.</li>
     *   <li>Delegate to {@link DashboardService#getDashboardSummary(java.util.UUID)}.</li>
     *   <li>Return result wrapped in {@link ApiResponse}.</li>
     * </ol>
     *
     * @return 200 OK with {@link DashboardSummaryResponse} inside {@link ApiResponse}
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Operation(
            summary = "Get dashboard summary",
            description = "Returns total income, total expense, net balance, category breakdown, " +
                          "and 12-month trends for the authenticated user."
    )
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {

        // Resolve authenticated user's identity from the JWT-populated SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("[ANALYTICS] Dashboard summary requested by: '{}'", username);

        // Cross-domain service call (not repository) — permitted by our DDD standards
        UserResponse user = userService.getUserByUsername(username);

        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(user.id());

        return ResponseEntity.ok(
                ApiResponse.success(summary, "Dashboard summary retrieved successfully")
        );
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Operation(
            summary = "Get recent activity",
            description = "Returns the 5 most recent transactions for the authenticated user."
    )
    public ResponseEntity<ApiResponse<List<RecentActivityDto>>> getRecentActivity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("[ANALYTICS] Recent activity requested by: '{}'", username);
        
        UserResponse user = userService.getUserByUsername(username);
        List<RecentActivityDto> recentActivity = dashboardService.getRecentActivity(user.id());
        
        return ResponseEntity.ok(
                ApiResponse.success(recentActivity, "Recent activity retrieved successfully")
        );
    }
}
