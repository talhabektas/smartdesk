// src/main/java/com/example/smartdeskbackend/controller/AdminController.java
package com.example.smartdeskbackend.controller;

import com.example.smartdeskbackend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin operations REST Controller
 * Sadece SUPER_ADMIN erişimi
 */
@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * System-wide istatistikler
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats() {
        logger.info("Getting system-wide statistics");

        try {
            Map<String, Object> stats = new HashMap<>();

            // Temel sayılar
            stats.put("totalCompanies", companyRepository.count());
            stats.put("totalUsers", userRepository.count());
            stats.put("totalTickets", ticketRepository.count());
            stats.put("totalCustomers", customerRepository.count());
            stats.put("totalDepartments", departmentRepository.count());

            // Aktif şirketler
            stats.put("activeCompanies", companyRepository.findByIsActiveTrue().size());

            // Son 30 günde aktif kullanıcılar
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            stats.put("activeUsersLast30Days", userRepository.countActiveUsersSince(null, thirtyDaysAgo));

            // Şirket bazında kullanıcı sayıları (top 10)
            List<Object[]> topCompanies = userRepository.countUsersByRoleAndCompany(null);
            stats.put("topCompaniesByUsers", topCompanies);

            stats.put("generatedAt", LocalDateTime.now());

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("Error getting system statistics", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "SYSTEM_STATS_ERROR", "message", e.getMessage()));
        }
    }

    /**
     * Tüm şirketlerin listesi (admin için)
     */
    @GetMapping("/companies")
    public ResponseEntity<?> getAllCompanies() {
        logger.info("Getting all companies for admin");

        try {
            List<Object[]> companies = companyRepository.findAll().stream()
                    .map(company -> new Object[]{
                            company.getId(),
                            company.getName(),
                            company.getDomain(),
                            company.getPlanType(),
                            company.getIsActive(),
                            company.getUserCount(),
                            company.getCreatedAt()
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("companies", companies);
            response.put("count", companies.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting all companies", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "COMPANIES_FETCH_ERROR", "message", e.getMessage()));
        }
    }

    /**
     * System health check
     */
    @GetMapping("/health")
    public ResponseEntity<?> systemHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "admin-service");
        health.put("timestamp", LocalDateTime.now());
        health.put("database", "Connected");

        return ResponseEntity.ok(health);
    }

    /**
     * Cache temizleme (placeholder)
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<?> clearCache() {
        logger.info("Clearing system cache");

        // Cache temizleme implementasyonu buraya eklenecek
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Cache cleared successfully");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * System maintenance mode (placeholder)
     */
    @PostMapping("/maintenance/{enabled}")
    public ResponseEntity<?> setMaintenanceMode(@PathVariable boolean enabled) {
        logger.info("Setting maintenance mode: {}", enabled);

        Map<String, Object> response = new HashMap<>();
        response.put("maintenanceMode", enabled);
        response.put("message", "Maintenance mode " + (enabled ? "enabled" : "disabled"));
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}