package com.saurabh.finance.analytics.service;

import com.saurabh.finance.analytics.dto.DashboardSummaryResponse;
import com.saurabh.finance.analytics.dto.RecentActivityDto;

import java.util.List;
import java.util.UUID;

/**
 * Contract for the analytics (read) side of the system.
 *
 * <p>This interface is the only permitted API boundary for the analytics domain.
 * All callers must go through this interface — no direct access to
 * {@link com.saurabh.finance.analytics.repository.AnalyticsRepository} from outside
 * this package.
 */
public interface DashboardService {

    /**
     * Assembles a complete dashboard summary for the given user.
     *
     * <p>Internally calls three {@link com.saurabh.finance.analytics.repository.AnalyticsRepository}
     * methods and combines the results into a single response. If the user has no
     * financial records, all monetary totals will be {@code 0.00} and the lists will be empty.
     *
     * @param userId the authenticated user's UUID
     * @return the fully assembled {@link DashboardSummaryResponse}
     */
    DashboardSummaryResponse getDashboardSummary(UUID userId);

    /**
     * Retrieves the most recent financial activities for the user.
     *
     * @param userId the authenticated user's UUID
     * @return list of {@link RecentActivityDto} (up to 5 items)
     */
    List<RecentActivityDto> getRecentActivity(UUID userId);
}
