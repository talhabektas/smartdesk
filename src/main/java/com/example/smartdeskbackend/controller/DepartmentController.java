// src/main/java/com/example/smartdeskbackend/controller/DepartmentController.java
package com.example.smartdeskbackend.controller;

import com.example.smartdeskbackend.entity.Department;
import com.example.smartdeskbackend.repository.DepartmentRepository;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Department management REST Controller
 */
@RestController
@RequestMapping("/api/v1/departments")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class DepartmentController {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Şirketteki departmanları getir
     */
    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('MANAGER') and @securityService.isFromSameCompany(#companyId))")
    public ResponseEntity<?> getDepartmentsByCompany(@PathVariable Long companyId) {
        logger.info("Getting departments for company: {}", companyId);

        try {
            List<Department> departments = departmentRepository.findByCompanyIdAndIsActiveTrue(companyId);

            List<Map<String, Object>> departmentList = departments.stream()
                    .map(this::mapDepartmentToResponse)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("departments", departmentList);
            response.put("companyId", companyId);
            response.put("count", departmentList.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting departments for company: {}", companyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("DEPARTMENTS_FETCH_ERROR", e.getMessage()));
        }
    }

    /**
     * Department detaylarını getir
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or @departmentSecurityService.hasAccessToDepartment(#id, authentication.principal.id)")
    public ResponseEntity<?> getDepartmentById(@PathVariable Long id) {
        logger.info("Getting department by id: {}", id);

        try {
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

            Map<String, Object> response = mapDepartmentToDetailResponse(department);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting department by id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("DEPARTMENT_NOT_FOUND", e.getMessage()));
        }
    }

    /**
     * Department istatistikleri
     */
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getDepartmentStats(@PathVariable Long id) {
        logger.info("Getting department statistics: {}", id);

        try {
            Department department = departmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

            Map<String, Object> stats = new HashMap<>();
            stats.put("departmentId", id);
            stats.put("departmentName", department.getName());
            stats.put("userCount", department.getUserCount());
            stats.put("ticketCount", department.getTickets().size());
            stats.put("activeTickets", department.getTickets().stream()
                    .filter(ticket -> ticket.isActive())
                    .count());
            stats.put("generatedAt", LocalDateTime.now());

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("Error getting department stats: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("DEPARTMENT_STATS_ERROR", e.getMessage()));
        }
    }

    // ============ Helper Methods ============

    /**
     * Department entity'sini response map'e çevirir
     */
    private Map<String, Object> mapDepartmentToResponse(Department department) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", department.getId());
        response.put("name", department.getName());
        response.put("description", department.getDescription());
        response.put("email", department.getEmail());
        response.put("isActive", department.getIsActive());
        response.put("userCount", department.getUserCount());

        if (department.getManager() != null) {
            response.put("managerId", department.getManager().getId());
            response.put("managerName", department.getManager().getFullName());
        }

        response.put("createdAt", department.getCreatedAt());
        response.put("updatedAt", department.getUpdatedAt());

        return response;
    }

    /**
     * Department entity'sini detaylı response map'e çevirir
     */
    private Map<String, Object> mapDepartmentToDetailResponse(Department department) {
        Map<String, Object> response = mapDepartmentToResponse(department);

        // Ek detaylar
        response.put("companyId", department.getCompany().getId());
        response.put("companyName", department.getCompany().getName());

        // Users listesi (sadece ID ve name)
        List<Map<String, Object>> users = department.getUsers().stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("fullName", user.getFullName());
                    userMap.put("email", user.getEmail());
                    userMap.put("role", user.getRole().getDisplayName());
                    userMap.put("status", user.getStatus().getDisplayName());
                    return userMap;
                })
                .collect(Collectors.toList());
        response.put("users", users);

        return response;
    }

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