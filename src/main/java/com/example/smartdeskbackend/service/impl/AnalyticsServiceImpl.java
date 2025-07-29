// src/main/java/com/example/smartdeskbackend/service/impl/AnalyticsServiceImpl.java
package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.repository.TicketRepository;
import com.example.smartdeskbackend.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public List<Object[]> getDailyTicketCreationTrend(Long companyId, LocalDate startDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        return ticketRepository.getDailyTicketCreationTrend(companyId, startDateTime);
    }

    @Override
    public Double getAverageCustomerSatisfaction(Long companyId, LocalDate startDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        return ticketRepository.getAverageCustomerSatisfaction(companyId, startDateTime);
    }

    @Override
    public List<Object[]> getMostUsedCategories(Long companyId) {
        return ticketRepository.getMostUsedCategories(companyId);
    }

    @Override
    public Map<String, Long> getTicketStatsByStatus(Long companyId) {
        // TicketRepository'den gelen Object[] listesini Map'e dönüştürür.
        // Object[0] -> Status (String), Object[1] -> Count (Long)
        return ticketRepository.getTicketStatsByStatus(companyId)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0], // Durum adı
                        arr -> (Long) arr[1]    // Sayı
                ));
    }

    @Override
    public Map<String, Long> getTicketStatsByPriority(Long companyId) {
        // TicketRepository'den gelen Object[] listesini Map'e dönüştürür.
        // Object[0] -> Priority (String), Object[1] -> Count (Long)
        return ticketRepository.getTicketStatsByPriority(companyId)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0], // Öncelik adı
                        arr -> (Long) arr[1]    // Sayı
                ));
    }
}