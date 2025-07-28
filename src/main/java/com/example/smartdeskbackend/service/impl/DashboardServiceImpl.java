
// DashboardService Implementation
package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.repository.*;
import com.example.smartdeskbackend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public Map<String, Object> getDashboardStats(Long companyId) {
        Map<String, Object> stats = new HashMap<>();

        // Ticket istatistikleri
        Map<String, Object> ticketStats = new HashMap<>();
        ticketStats.put("total", ticketRepository.countByCompanyId(companyId));
        ticketStats.put("active", ticketRepository.findActiveTickets(companyId, null).getTotalElements());
        ticketStats.put("unassigned", ticketRepository.findUnassignedTickets(companyId).size());

        stats.put("tickets", ticketStats);

        // Kullanıcı istatistikleri
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("total", userRepository.countByCompanyId(companyId));
        userStats.put("active", userRepository.countActiveUsersSince(companyId, LocalDateTime.now().minusDays(30)));

        stats.put("users", userStats);

        // Müşteri istatistikleri
        Map<String, Object> customerStats = new HashMap<>();
        customerStats.put("total", customerRepository.countByCompanyId(companyId));
        customerStats.put("vip", customerRepository.findVipCustomers(companyId).size());

        stats.put("customers", customerStats);

        return stats;
    }

    @Override
    public Map<String, Object> getAgentDashboard(Long agentId) {
        Map<String, Object> dashboard = new HashMap<>();

        // Agent'ın ticketları
        long assignedTickets = ticketRepository.countByAssignedAgentId(agentId);
        dashboard.put("assignedTickets", assignedTickets);

        return dashboard;
    }

    @Override
    public Map<String, Object> getCustomerDashboard(Long customerId) {
        Map<String, Object> dashboard = new HashMap<>();

        // Customer'ın ticketları
        long totalTickets = ticketRepository.countByCustomerId(customerId);
        dashboard.put("totalTickets", totalTickets);

        return dashboard;
    }
}
