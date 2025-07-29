// src/main/java/com/example/smartdeskbackend/controller/DashboardController.java
package com.example.smartdeskbackend.controller;

import com.example.smartdeskbackend.service.DashboardService;
import com.example.smartdeskbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard REST Controller
 * Dashboard verileri ve istatistikler için
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Company dashboard istatistikleri
     */
    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('MANAGER') and @securityService.isFromSameCompany(#companyId))")
    public ResponseEntity<?> getCompanyDashboard(@PathVariable Long companyId) {
        logger.info("Getting company dashboard for: {}", companyId);

        try {
            Map<String, Object> dashboardData = dashboardService.getDashboardStats(companyId);
            dashboardData.put("companyId", companyId);
            dashboardData.put("generatedAt", LocalDateTime.now());

            return ResponseEntity.ok(dashboardData);

        } catch (Exception e) {
            logger.error("Error getting company dashboard: {}", companyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("DASHBOARD_ERROR", e.getMessage()));
        }
    }

    /**
     * Agent dashboard
     */
    @GetMapping("/agent")
    @PreAuthorize("hasRole('AGENT') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAgentDashboard(HttpServletRequest request) {
        logger.info("Getting agent dashboard");

        try {
            String token = extractTokenFromRequest(request);
            Long agentId = jwtUtil.getUserIdFromToken(token);

            Map<String, Object> dashboardData = dashboardService.getAgentDashboard(agentId);
            dashboardData.put("agentId", agentId);
            dashboardData.put("generatedAt", LocalDateTime.now());

            return ResponseEntity.ok(dashboardData);

        } catch (Exception e) {
            logger.error("Error getting agent dashboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("DASHBOARD_ERROR", e.getMessage()));
        }
    }

    /**
     * Customer dashboard
     */
    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('AGENT') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getCustomerDashboard(
            @RequestParam(required = false) Long customerId,
            HttpServletRequest request) {

        logger.info("Getting customer dashboard");

        try {
            // Eğer customerId belirtilmemişse, token'dan al
            if (customerId == null) {
                String token = extractTokenFromRequest(request);
                customerId = jwtUtil.getUserIdFromToken(token);
            }

            Map<String, Object> dashboardData = dashboardService.getCustomerDashboard(customerId);
            dashboardData.put("customerId", customerId);
            dashboardData.put("generatedAt", LocalDateTime.now());

            return ResponseEntity.ok(dashboardData);

        } catch (Exception e) {
            logger.error("Error getting customer dashboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("DASHBOARD_ERROR", e.getMessage()));
        }
    }

    /**
     * System-wide dashboard (sadece Super Admin)
     */
    @GetMapping("/system")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getSystemDashboard() {
        logger.info("Getting system dashboard");

        try {
            Map<String, Object> systemStats = new HashMap<>();
            systemStats.put("message", "System dashboard - implement with system-wide statistics");
            systemStats.put("generatedAt", LocalDateTime.now());

            return ResponseEntity.ok(systemStats);

        } catch (Exception e) {
            logger.error("Error getting system dashboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("DASHBOARD_ERROR", e.getMessage()));
        }
    }

    // ============ Helper Methods ============

    /**
     * HTTP request'ten JWT token'ı çıkarır
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Standart hata response'u oluşturur
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());
        return errorResponse;
    }
}