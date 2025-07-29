// src/main/java/com/example/smartdeskbackend/service/impl/AgentPerformanceServiceImpl.java
package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.entity.AgentPerformance;
import com.example.smartdeskbackend.entity.Company;
import com.example.smartdeskbackend.entity.User;
import com.example.smartdeskbackend.exception.ResourceNotFoundException; // Bu sınıfın var olduğundan emin olun
import com.example.smartdeskbackend.repository.AgentPerformanceRepository;
import com.example.smartdeskbackend.repository.CompanyRepository;
import com.example.smartdeskbackend.repository.TicketRepository;
import com.example.smartdeskbackend.repository.UserRepository;
import com.example.smartdeskbackend.service.AgentPerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AgentPerformanceServiceImpl implements AgentPerformanceService {

    @Autowired
    private AgentPerformanceRepository agentPerformanceRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;

    @Override
    @Transactional
    public AgentPerformance createAgentPerformance(AgentPerformance performance) {
        performance.setCreatedAt(LocalDateTime.now());
        performance.setUpdatedAt(LocalDateTime.now());
        return agentPerformanceRepository.save(performance);
    }

    @Override
    public Optional<AgentPerformance> getAgentPerformanceById(Long id) {
        return agentPerformanceRepository.findById(id);
    }

    @Override
    public List<AgentPerformance> getAgentPerformanceByAgentIdAndDateRange(Long agentId, LocalDate startDate, LocalDate endDate) {
        return agentPerformanceRepository.findByAgentIdAndReportDateBetween(agentId, startDate, endDate);
    }

    @Override
    // Bu metodun imzası AgentPerformanceService arayüzündeki ile tam olarak eşleşmeli.
    public List<Object[]> getCompanyPerformanceMetrics(Long companyId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        List<Object[]> rawMetrics = ticketRepository.getAgentPerformanceMetrics(companyId, startDateTime, endDateTime);

        // Not: Bu ham Object[]'leri daha sonra AgentPerformance DTO'larına dönüştürmeyi düşünebilirsiniz.
        return rawMetrics;
    }

    @Override
    @Transactional
    public void calculateAndSaveDailyAgentPerformance(Long companyId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);

        List<Object[]> agentMetricsRaw = ticketRepository.getAgentPerformanceMetrics(companyId, startOfDay, endOfDay);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        for (Object[] metric : agentMetricsRaw) {
            Long agentId = (Long) metric[0];
            // metric[1]: first_name (String), metric[2]: last_name (String)
            int totalTickets = ((Number) metric[3]).intValue(); // Object'ten int'e güvenli dönüştürme
            double avgResolutionTime = ((Number) metric[4]).doubleValue(); // Object'ten double'a güvenli dönüştürme
            int resolvedCount = ((Number) metric[5]).intValue(); // Object'ten int'e güvenli dönüştürme

            Optional<AgentPerformance> existingPerformance = agentPerformanceRepository.findByAgentIdAndReportDate(agentId, date);
            AgentPerformance performance = existingPerformance.orElseGet(AgentPerformance::new);

            User agent = userRepository.findById(agentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Agent (User) not found with id: " + agentId));

            performance.setAgent(agent);
            performance.setCompany(company);
            performance.setReportDate(date);
            performance.setTotalTicketsHandled(totalTickets);
            performance.setAverageResolutionTimeHours(avgResolutionTime);
            performance.setResolvedTickets(resolvedCount);

            // Eğer istersen, burada yeniden açılan biletler (reopenedTickets) veya
            // ilk yanıt süresi (averageFirstResponseTimeHours) gibi diğer alanları da
            // hesaplayıp AgentPerformance entity'sine set edebilirsin.

            performance.setUpdatedAt(LocalDateTime.now());
            if (performance.getId() == null) { // Eğer yeni bir kayıt ise createdAt'i ayarla
                performance.setCreatedAt(LocalDateTime.now());
            }
            agentPerformanceRepository.save(performance);
        }
    }
}