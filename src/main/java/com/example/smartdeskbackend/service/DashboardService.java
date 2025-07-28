
package com.example.smartdeskbackend.service;

import java.util.Map;

public interface DashboardService {
    Map<String, Object> getDashboardStats(Long companyId);
    Map<String, Object> getAgentDashboard(Long agentId);
    Map<String, Object> getCustomerDashboard(Long customerId);
}
