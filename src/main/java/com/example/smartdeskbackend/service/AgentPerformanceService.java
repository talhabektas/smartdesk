// src/main/java/com/example/smartdeskbackend/service/AgentPerformanceService.java
package com.example.smartdeskbackend.service;

import com.example.smartdeskbackend.entity.AgentPerformance;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AgentPerformanceService {
    AgentPerformance createAgentPerformance(AgentPerformance performance);
    Optional<AgentPerformance> getAgentPerformanceById(Long id);
    List<AgentPerformance> getAgentPerformanceByAgentIdAndDateRange(Long agentId, LocalDate startDate, LocalDate endDate);
    // Dönüş tipi List<Object[]> olarak belirtildi, çünkü repository'den ham veri geliyor.
    List<Object[]> getCompanyPerformanceMetrics(Long companyId, LocalDate startDate, LocalDate endDate);
    void calculateAndSaveDailyAgentPerformance(Long companyId, LocalDate date);
}