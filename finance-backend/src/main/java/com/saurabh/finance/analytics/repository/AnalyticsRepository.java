package com.saurabh.finance.analytics.repository;

import com.saurabh.finance.analytics.dto.CategoryTotal;
import com.saurabh.finance.analytics.dto.MonthlyTrend;
import com.saurabh.finance.analytics.dto.RecentActivityDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Analytics read repository using native PostgreSQL queries via {@link JdbcTemplate}.
 *
 * <p><strong>Architectural Pattern — Lightweight CQRS Read Side:</strong>
 * This class executes raw SQL directly against the {@code financial_records} table
 * without importing any class from the {@code transaction} domain. This satisfies
 * our strict "no cross-domain repository access" rule while enabling highly
 * optimised aggregation queries using PostgreSQL's native SQL capabilities.
 *
 * <p><strong>Soft-deletion awareness:</strong> Every query manually enforces
 * {@code is_deleted = false} because {@code @SQLRestriction} only applies to
 * JPA/Hibernate queries. JdbcTemplate bypasses Hibernate entirely and executes
 * SQL directly, so the filter must be explicit.
 *
 * <p><strong>Null safety:</strong> All aggregate queries use {@code COALESCE(SUM(...), 0)}
 * so that empty result sets return {@code 0.00} instead of {@code null}.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AnalyticsRepository {

    private final JdbcTemplate jdbcTemplate;

    // ── Total by Type ──────────────────────────────────────────────────────

    /**
     * Returns the sum of all non-deleted records matching {@code type} for a user.
     *
     * @param userId the user UUID
     * @param type   "INCOME" or "EXPENSE"
     * @return the total amount, or {@code BigDecimal.ZERO} if no records exist
     */
    public BigDecimal getTotalAmountByType(UUID userId, String type) {
        log.debug("[ANALYTICS] Calculating total for userId={}, type={}", userId, type);

        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM financial_records
                WHERE is_deleted = false
                  AND user_id = ?
                  AND type = ?
                """;

        BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class, userId, type);
        return result != null ? result : BigDecimal.ZERO;
    }

    // ── Expenses by Category ───────────────────────────────────────────────

    /**
     * Returns per-category subtotals for EXPENSE records, sorted by total descending.
     *
     * @param userId the user UUID
     * @return list of {@link CategoryTotal} for all expense categories with data
     */
    public List<CategoryTotal> getExpensesByCategory(UUID userId) {
        log.debug("[ANALYTICS] Fetching expense category breakdown for userId={}", userId);

        String sql = """
                SELECT category,
                       COALESCE(SUM(amount), 0) AS total
                FROM financial_records
                WHERE is_deleted = false
                  AND user_id = ?
                  AND type = 'EXPENSE'
                GROUP BY category
                ORDER BY total DESC
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new CategoryTotal(
                        rs.getString("category"),
                        rs.getBigDecimal("total")
                ),
                userId
        );
    }

    // ── Monthly Trends ─────────────────────────────────────────────────────

    /**
     * Returns the last 12 months of income vs. expense data.
     *
     * <p>Uses PostgreSQL's {@code TO_CHAR} to format the month as {@code YYYY-MM}.
     * A conditional {@code SUM(CASE WHEN ...)} computes income and expense totals
     * in a single table scan — more efficient than two separate queries.
     *
     * @param userId the user UUID
     * @return list of {@link MonthlyTrend}, ordered by month descending (most recent first)
     */
    public List<MonthlyTrend> getMonthlyTrends(UUID userId) {
        log.debug("[ANALYTICS] Fetching monthly trends for userId={}", userId);

        String sql = """
                SELECT TO_CHAR(transaction_date, 'YYYY-MM')                        AS month,
                       COALESCE(SUM(CASE WHEN type = 'INCOME'  THEN amount END), 0) AS income,
                       COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount END), 0) AS expense
                FROM financial_records
                WHERE is_deleted = false
                  AND user_id = ?
                GROUP BY TO_CHAR(transaction_date, 'YYYY-MM')
                ORDER BY month DESC
                LIMIT 12
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new MonthlyTrend(
                        rs.getString("month"),
                        rs.getBigDecimal("income"),
                        rs.getBigDecimal("expense")
                ),
                userId
        );
    }

    // ── Recent Activity ────────────────────────────────────────────────────

    /**
     * Returns the 5 most recent transactions for a user.
     *
     * @param userId the user UUID
     * @return list of {@link RecentActivityDto} ordered by transaction_date DESC
     */
    public List<RecentActivityDto> getRecentActivity(UUID userId) {
        log.debug("[ANALYTICS] Fetching recent activity for userId={}", userId);

        String sql = """
                SELECT id, amount, type, category, transaction_date
                FROM financial_records
                WHERE is_deleted = false
                  AND user_id = ?
                ORDER BY transaction_date DESC
                LIMIT 5
                """;

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new RecentActivityDto(
                        rs.getObject("id", UUID.class),
                        rs.getBigDecimal("amount"),
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getDate("transaction_date").toLocalDate()
                ),
                userId
        );
    }
}
