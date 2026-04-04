package com.saurabh.finance.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Top-level dashboard summary response assembled by {@link com.saurabh.finance.analytics.service.DashboardServiceImpl}.
 *
 * <p>All BigDecimal fields default to {@code 0.00} if no records exist —
 * guaranteed by the {@code COALESCE} in the native SQL queries.
 *
 * <p>{@code netBalance} = {@code totalIncome} - {@code totalExpense}, calculated
 * at the service layer to avoid embedding business logic inside SQL.
 */
public record DashboardSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        List<CategoryTotal> expensesByCategory,
        List<MonthlyTrend> monthlyTrends
) {}
