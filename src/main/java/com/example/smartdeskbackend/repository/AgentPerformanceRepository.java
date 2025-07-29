// src/main/java/com/example/smartdeskbackend/repository/AgentPerformanceRepository.java
package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.AgentPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgentPerformanceRepository extends JpaRepository<AgentPerformance, Long> {
    List<AgentPerformance> findByCompanyId(Long companyId);
    Optional<AgentPerformance> findByAgentIdAndReportDate(Long agentId, LocalDate reportDate);
    List<AgentPerformance> findByAgentIdAndReportDateBetween(Long agentId, LocalDate startDate, LocalDate endDate);
}