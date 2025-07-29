// src/main/java/com/example/smartdeskbackend/service/impl/AgentPerformanceServiceImpl.java
package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.entity.AgentPerformance;
import com.example.smartdeskbackend.entity.Company;
import com.example.smartdeskbackend.entity.User;
import com.example.smartdeskbackend.exception.ResourceNotFoundException;
import com.example.smartdeskbackend.repository.AgentPerformanceRepository;
import com.example.smartdeskbackend.repository.CompanyRepository;
import com.example.smartdeskbackend.repository.TicketRepository;
import com.example.smartdeskbackend.repository.UserRepository; // User entity'sine erişim için
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
    private UserRepository userRepository; // Agent (User) bilgilerini almak için
    @Autowired
    private CompanyRepository companyRepository; // Company bilgilerini almak için

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
    public List<AgentPerformance> getCompanyPerformanceMetrics(Long companyId, LocalDate startDate, LocalDate endDate) {
        // Bu metot, TicketRepository'deki native sorgudan alınan Object[]'leri işler.
        // Daha iyi bir yaklaşım, bu Object[]'leri özel bir DTO'ya dönüştürüp döndürmektir.
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        List<Object[]> rawMetrics = ticketRepository.getAgentPerformanceMetrics(companyId, startDateTime, endDateTime);

        // Bu rawMetrics'i AgentPerformance DTO'larına veya daha anlamlı bir yapıya dönüştürme mantığı eklenebilir.
        // Şimdilik doğrudan listeyi döndürmek yerine, daha ileriye dönük bir yaklaşımla DTO'ya dönüşümü burada yapmalıyız.
        // Örnek bir dönüşüm:
        /*
        return rawMetrics.stream().map(metric -> {
            AgentPerformanceDto dto = new AgentPerformanceDto();
            dto.setAgentId((Long) metric[0]);
            dto.setAgentFirstName((String) metric[1]);
            dto.setAgentLastName((String) metric[2]);
            dto.setTicketCount(((Number) metric[3]).intValue());
            dto.setAvgResolutionTime(((Number) metric[4]).doubleValue());
            dto.setResolvedCount(((Number) metric[5]).intValue());
            return dto;
        }).collect(Collectors.toList());
        */
        // Geçici olarak null dönüyorum, ancak gerçek uygulamada DTO dönüşümü yapılmalı.
        return null;
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
            int totalTickets = ((Number) metric[3]).intValue();
            double avgResolutionTime = ((Number) metric[4]).doubleValue(); // Object tipinden dönüştürme
            int resolvedCount = ((Number) metric[5]).intValue();

            // Performans kaydının zaten var olup olmadığını kontrol et (günlük olduğu için)
            Optional<AgentPerformance> existingPerformance = agentPerformanceRepository.findByAgentIdAndReportDate(agentId, date);
            AgentPerformance performance = existingPerformance.orElseGet(AgentPerformance::new);

            User agent = userRepository.findById(agentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + agentId));

            performance.setAgent(agent);
            performance.setCompany(company);
            performance.setReportDate(date);
            performance.setTotalTicketsHandled(totalTickets);
            performance.setAverageResolutionTimeHours(avgResolutionTime);
            performance.setResolvedTickets(resolvedCount);
            // ReopenedTickets, AverageFirstResponseTimeHours, CustomerSatisfactionScore gibi diğer alanlar da burada hesaplanıp set edilebilir.

            performance.setUpdatedAt(LocalDateTime.now());
            if (performance.getId() == null) { // Yeni kayıt ise CreatedAt'i ayarla
                performance.setCreatedAt(LocalDateTime.now());
            }
            agentPerformanceRepository.save(performance);
        }
    }
}