package com.saurabh.finance.analytics.dto;

import java.math.BigDecimal;

/**
 * Aggregated total for a single expense category.
 * Used in the dashboard summary to show category-level spending breakdowns.
 */
public record CategoryTotal(
        String category,
        BigDecimal total
) {}
