package com.saurabh.finance.analytics.service;

import com.saurabh.finance.analytics.dto.CategoryTotal;
import com.saurabh.finance.analytics.dto.DashboardSummaryResponse;
import com.saurabh.finance.analytics.dto.MonthlyTrend;
import com.saurabh.finance.analytics.dto.RecentActivityDto;
import com.saurabh.finance.analytics.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link DashboardService}.
 *
 * <p><strong>Assembly logic:</strong>
 * <ul>
 *   <li>Calls each {@link AnalyticsRepository} method independently.</li>
 *   <li>Calculates {@code netBalance} here (Income − Expense) rather than in SQL,
 *       keeping business logic out of the persistence layer.</li>
 *   <li>Null safety is guaranteed by the {@code COALESCE} in all SQL queries —
 *       but we defensively default to {@link BigDecimal#ZERO} here as well.</li>
 * </ul>
 *
 * <p>{@code @Transactional(readOnly = true)} signals to the connection pool that
 * this transaction will not mutate data, enabling optimizations in both Spring
 * and the PostgreSQL connection pool (HikariCP).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final AnalyticsRepository analyticsRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(UUID userId) {
        log.info("[ANALYTICS] Assembling dashboard summary for userId={}", userId);

        // ── Monetary Aggregations ──────────────────────────────────────────
        BigDecimal totalIncome = safeAmount(
                analyticsRepository.getTotalAmountByType(userId, "INCOME"));

        BigDecimal totalExpense = safeAmount(
                analyticsRepository.getTotalAmountByType(userId, "EXPENSE"));

        // Net balance calculated at the service layer — business logic stays out of SQL
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        // ── Category Breakdown ─────────────────────────────────────────────
        List<CategoryTotal> expensesByCategory =
                analyticsRepository.getExpensesByCategory(userId);

        // ── Monthly Trends (last 12 months) ───────────────────────────────
        List<MonthlyTrend> monthlyTrends =
                analyticsRepository.getMonthlyTrends(userId);

        log.info("[ANALYTICS] Summary assembled — income={}, expense={}, net={}",
                totalIncome, totalExpense, netBalance);

        return new DashboardSummaryResponse(
                totalIncome,
                totalExpense,
                netBalance,
                expensesByCategory,
                monthlyTrends
        );
    }

    /**
     * Defensive null guard. Although {@code COALESCE} in SQL should always return
     * a non-null value, this protects against unexpected driver behaviour.
     */
    private BigDecimal safeAmount(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecentActivityDto> getRecentActivity(UUID userId) {
        log.info("[ANALYTICS] Fetching recent activity for userId={}", userId);
        return analyticsRepository.getRecentActivity(userId);
    }
}
