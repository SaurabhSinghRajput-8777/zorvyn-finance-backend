package com.saurabh.finance.analytics.dto;

import java.math.BigDecimal;

/**
 * Income vs. expense breakdown for a single calendar month.
 *
 * <p>{@code month} is formatted as {@code YYYY-MM} by a PostgreSQL {@code TO_CHAR}
 * function in the native query.
 */
public record MonthlyTrend(
        String month,
        BigDecimal income,
        BigDecimal expense
) {}
