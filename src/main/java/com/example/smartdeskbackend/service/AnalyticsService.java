// src/main/java/com/example/smartdeskbackend/service/AnalyticsService.java
package com.example.smartdeskbackend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AnalyticsService {
    // AnalyticsServiceImpl içinde override edilen tüm metod imzaları buraya eklendi
    List<Object[]> getDailyTicketCreationTrend(Long companyId, LocalDate startDate);
    Double getAverageCustomerSatisfaction(Long companyId, LocalDate startDate);
    List<Object[]> getMostUsedCategories(Long companyId);
    Map<String, Long> getTicketStatsByStatus(Long companyId);
    Map<String, Long> getTicketStatsByPriority(Long companyId);
}