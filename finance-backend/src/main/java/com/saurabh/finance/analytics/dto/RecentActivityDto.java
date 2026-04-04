package com.saurabh.finance.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO representing a recent transaction for the dashboard's "Recent Activity" feed.
 * 
 * <p>Created specifically for the analytics domain to prevent leaking
 * transaction domain DTOs across bounded contexts.
 */
public record RecentActivityDto(
        UUID id,
        BigDecimal amount,
        String type,
        String category,
        LocalDate date
) {}
