package com.saurabh.finance.analytics.service;

import com.saurabh.finance.analytics.dto.CategoryTotal;
import com.saurabh.finance.analytics.dto.DashboardSummaryResponse;
import com.saurabh.finance.analytics.dto.MonthlyTrend;
import com.saurabh.finance.analytics.repository.AnalyticsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void shouldCorrectlyCalculateNetBalance() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(analyticsRepository.getTotalAmountByType(userId, "INCOME")).thenReturn(new BigDecimal("5000.00"));
        when(analyticsRepository.getTotalAmountByType(userId, "EXPENSE")).thenReturn(new BigDecimal("2000.00"));
        when(analyticsRepository.getExpensesByCategory(userId)).thenReturn(List.of(new CategoryTotal("Housing", new BigDecimal("1000.00"))));
        when(analyticsRepository.getMonthlyTrends(userId)).thenReturn(List.of(new MonthlyTrend("2023-10", new BigDecimal("5000.00"), new BigDecimal("2000.00"))));

        // Act
        DashboardSummaryResponse response = dashboardService.getDashboardSummary(userId);

        // Assert
        assertEquals(new BigDecimal("5000.00"), response.totalIncome());
        assertEquals(new BigDecimal("2000.00"), response.totalExpense());
        assertEquals(new BigDecimal("3000.00"), response.netBalance());
        
        verify(analyticsRepository).getTotalAmountByType(userId, "INCOME");
        verify(analyticsRepository).getTotalAmountByType(userId, "EXPENSE");
        verify(analyticsRepository).getExpensesByCategory(userId);
        verify(analyticsRepository).getMonthlyTrends(userId);
    }

    @Test
    void shouldHandleEmptyDatasetGracefully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(analyticsRepository.getTotalAmountByType(userId, "INCOME")).thenReturn(null);
        when(analyticsRepository.getTotalAmountByType(userId, "EXPENSE")).thenReturn(null);
        when(analyticsRepository.getExpensesByCategory(userId)).thenReturn(Collections.emptyList());
        when(analyticsRepository.getMonthlyTrends(userId)).thenReturn(Collections.emptyList());

        // Act
        DashboardSummaryResponse response = dashboardService.getDashboardSummary(userId);

        // Assert
        assertEquals(BigDecimal.ZERO, response.totalIncome());
        assertEquals(BigDecimal.ZERO, response.totalExpense());
        assertEquals(BigDecimal.ZERO, response.netBalance());
        
        verify(analyticsRepository).getTotalAmountByType(userId, "INCOME");
        verify(analyticsRepository).getTotalAmountByType(userId, "EXPENSE");
    }
}
