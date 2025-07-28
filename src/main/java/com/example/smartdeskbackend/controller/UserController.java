package com.example.smartdeskbackend.controller;

import com.example.smartdeskbackend.dto.request.user.CreateUserRequest;
import com.example.smartdeskbackend.dto.request.user.UpdateUserRequest;
import com.example.smartdeskbackend.dto.request.user.ChangePasswordRequest;
import com.example.smartdeskbackend.dto.response.user.UserListResponse;
import com.example.smartdeskbackend.dto.response.user.UserProfileResponse;
import com.example.smartdeskbackend.enums.UserRole;
import com.example.smartdeskbackend.enums.UserStatus;
import com.example.smartdeskbackend.service.UserService;
import com.example.smartdeskbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User management REST Controller
 */
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Kullanıcı profil bilgilerini getir
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or #id == authentication.principal.id")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        logger.info("Getting user by id: {}", id);

        try {
            UserProfileResponse user = userService.getUserById(id);
            return ResponseEntity.ok(user);

        } catch (Exception e) {
            logger.error("Error getting user by id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("USER_NOT_FOUND", e.getMessage()));
        }
    }

    /**
     * Şirketteki kullanıcıları getir (sayfalı)
     */
    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('MANAGER') and @securityService.isFromSameCompany(#companyId))")
    public ResponseEntity<?> getUsersByCompany(
            @PathVariable Long companyId,
            @PageableDefault(size = 20) Pageable pageable) {

        logger.info("Getting users by company: {}", companyId);

        try {
            Page<UserListResponse> users = userService.getUsersByCompany(companyId, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("users", users.getContent());
            response.put("totalElements", users.getTotalElements());
            response.put("totalPages", users.getTotalPages());
            response.put("currentPage", users.getNumber());
            response.put("size", users.getSize());
            response.put("hasNext", users.hasNext());
            response.put("hasPrevious", users.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting users by company: {}", companyId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("USERS_FETCH_ERROR", e.getMessage()));
        }
    }

    /**
     * Kullanıcı arama ve filtreleme
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> searchUsers(
            @RequestParam Long companyId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) Long departmentId,
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest request) {

        logger.info("Searching users in company: {} with query: {}", companyId, q);

        try {
            // Company access kontrolü
            if (!hasAccessToCompany(request, companyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "Access denied to company data"));
            }

            Page<UserListResponse> users = userService.searchUsers(companyId, q, role, status, departmentId, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("users", users.getContent());
            response.put("totalElements", users.getTotalElements());
            response.put("totalPages", users.getTotalPages());
            response.put("currentPage", users.getNumber());
            response.put("size", users.getSize());
            response.put("hasNext", users.hasNext());
            response.put("hasPrevious", users.hasPrevious());
            response.put("searchQuery", q);
            response.put("filters", Map.of(
                    "role", role,
                    "status", status,
                    "departmentId", departmentId
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching users", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("USER_SEARCH_ERROR", e.getMessage()));
        }
    }

    /**
     * Departmandaki agent'ları getir
     */
    @GetMapping("/agents/department/{departmentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT')")
    public ResponseEntity<?> getAgentsByDepartment(@PathVariable Long departmentId) {
        logger.info("Getting agents by department: {}", departmentId);

        try {
            List<UserListResponse> agents = userService.getAgentsByDepartment(departmentId);

            Map<String, Object> response = new HashMap<>();
            response.put("agents", agents);
            response.put("departmentId", departmentId);
            response.put("count", agents.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting agents by department: {}", departmentId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("AGENTS_FETCH_ERROR", e.getMessage()));
        }
    }

    /**
     * Müsait agent'ları getir (load balancing için)
     */
    @GetMapping("/agents/available")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getAvailableAgents(
            @RequestParam Long departmentId,
            @RequestParam(defaultValue = "5") int limit) {

        logger.info("Getting available agents for department: {}", departmentId);

        try {
            List<UserListResponse> agents = userService.getAvailableAgents(departmentId, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("availableAgents", agents);
            response.put("departmentId", departmentId);
            response.put("count", agents.size());
            response.put("limit", limit);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting available agents", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("AVAILABLE_AGENTS_ERROR", e.getMessage()));
        }
    }

    /**
     * Yeni kullanıcı oluştur
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request, HttpServletRequest httpRequest) {
        logger.info("Creating new user: {}", request.getEmail());

        try {
            // Company access kontrolü
            if (!hasAccessToCompany(httpRequest, request.getCompanyId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "Access denied to create user in this company"));
            }

            // Password confirmation kontrolü
            if (!request.getPassword().equals(request.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("PASSWORD_MISMATCH", "Password confirmation does not match"));
            }

            UserProfileResponse user = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);

        } catch (Exception e) {
            logger.error("Error creating user", e);

            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("already exists")) {
                status = HttpStatus.CONFLICT;
            } else if (e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            }

            return ResponseEntity.status(status)
                    .body(createErrorResponse("USER_CREATION_ERROR", e.getMessage()));
        }
    }

    /**
     * Kullanıcı güncelle
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or #id == authentication.principal.id")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest) {

        logger.info("Updating user: {}", id);

        try {
            // Kendi profilini güncelleyenler sadece belirli alanları değiştirebilir
            if (isUpdatingSelf(httpRequest, id) && !isSuperAdminOrManager(httpRequest)) {
                // Sadece kişisel bilgileri güncelleyebilir
                request.setRole(null);
                request.setStatus(null);
                request.setDepartmentId(null);
            }

            UserProfileResponse user = userService.updateUser(id, request);
            return ResponseEntity.ok(user);

        } catch (Exception e) {
            logger.error("Error updating user: {}", id, e);

            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            } else if (e.getMessage().contains("already exists")) {
                status = HttpStatus.CONFLICT;
            }

            return ResponseEntity.status(status)
                    .body(createErrorResponse("USER_UPDATE_ERROR", e.getMessage()));
        }
    }

    /**
     * Kullanıcı şifre değiştir
     */
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasRole('SUPER_ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {

        logger.info("Changing password for user: {}", id);

        try {
            // Password confirmation kontrolü
            if (!request.isPasswordConfirmed()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("PASSWORD_MISMATCH", "New password and confirmation do not match"));
            }

            userService.changePassword(id, request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            response.put("userId", id);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error changing password for user: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("PASSWORD_CHANGE_ERROR", e.getMessage()));
        }
    }

    /**
     * Kullanıcı şifre sıfırla (admin tarafından)
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> resetPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        logger.info("Resetting password for user: {}", id);

        try {
            String newPassword = request.get("newPassword");
            if (!StringUtils.hasText(newPassword)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("MISSING_PASSWORD", "New password is required"));
            }

            if (newPassword.length() < 8) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("WEAK_PASSWORD", "Password must be at least 8 characters long"));
            }

            userService.resetPassword(id, newPassword);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password reset successfully");
            response.put("userId", id);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error resetting password for user: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("PASSWORD_RESET_ERROR", e.getMessage()));
        }
    }

    /**
     * Kullanıcı aktif et
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        logger.info("Activating user: {}", id);

        try {
            userService.activateUser(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User activated successfully");
            response.put("userId", id);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error activating user: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("USER_ACTIVATION_ERROR", e.getMessage()));
        }
    }

    /**
     * Kullanıcı deaktif et
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        logger.info("Deactivating user: {}", id);

        try {
            userService.deactivateUser(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User deactivated successfully");
            response.put("userId", id);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deactivating user: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("USER_DEACTIVATION_ERROR", e.getMessage()));
        }
    }

    /**
     * Kullanıcı sil (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user: {}", id);

        try {
            userService.deleteUser(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            response.put("userId", id);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting user: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("USER_DELETION_ERROR", e.getMessage()));
        }
    }

    /**
     * Kullanıcı hesap kilidini aç
     */
    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> unlockUser(@PathVariable Long id) {
        logger.info("Unlocking user: {}", id);

        try {
            userService.unlockUser(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User unlocked successfully");
            response.put("userId", id);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error unlocking user: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("USER_UNLOCK_ERROR", e.getMessage()));
        }
    }

    /**
     * Şirketteki kullanıcı istatistikleri
     */
    @GetMapping("/stats/company/{companyId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('MANAGER') and @securityService.isFromSameCompany(#companyId))")
    public ResponseEntity<?> getUserStats(@PathVariable Long companyId) {
        logger.info("Getting user stats for company: {}", companyId);

        try {
            long totalUsers = userService.getUserCountByCompany(companyId);
            List<Object[]> roleStats = userService.getUserStatsByCompany(companyId);
            Map<String, Long> roleDistribution = new HashMap<>();

            for (Object[] stat : roleStats) {
                roleDistribution.put((String) stat[0], ((Number) stat[1]).longValue());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("companyId", companyId);
            response.put("totalUsers", totalUsers);
            response.put("roleDistribution", roleDistribution);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting user stats for company: {}", companyId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("USER_STATS_ERROR", e.getMessage()));
        }
    }

    /**
     * En aktif kullanıcıları getir
     */
    @GetMapping("/most-active")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getMostActiveUsers(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {

        logger.info("Getting most active users for company: {}", companyId);

        try {
            if (!hasAccessToCompany(request, companyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "Access denied to company data"));
            }

            List<UserListResponse> activeUsers = userService.getMostActiveUsers(companyId, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("mostActiveUsers", activeUsers);
            response.put("companyId", companyId);
            response.put("limit", limit);
            response.put("count", activeUsers.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting most active users", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("ACTIVE_USERS_ERROR", e.getMessage()));
        }
    }

    // ============ Helper Methods ============

    /**
     * JWT token'dan kullanıcının company access'i olup olmadığını kontrol eder
     */
    private boolean hasAccessToCompany(HttpServletRequest request, Long companyId) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) return false;

            String role = jwtUtil.getRoleFromToken(token);
            if ("SUPER_ADMIN".equals(role)) return true;

            Long userCompanyId = jwtUtil.getCompanyIdFromToken(token);
            return companyId.equals(userCompanyId);

        } catch (Exception e) {
            logger.error("Error checking company access", e);
            return false;
        }
    }

    /**
     * Kullanıcının kendi profilini güncelleyip güncellemediğini kontrol eder
     */
    private boolean isUpdatingSelf(HttpServletRequest request, Long userId) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) return false;

            Long tokenUserId = jwtUtil.getUserIdFromToken(token);
            return userId.equals(tokenUserId);

        } catch (Exception e) {
            logger.error("Error checking self update", e);
            return false;
        }
    }

    /**
     * Kullanıcının SUPER_ADMIN veya MANAGER olup olmadığını kontrol eder
     */
    private boolean isSuperAdminOrManager(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) return false;

            String role = jwtUtil.getRoleFromToken(token);
            return "SUPER_ADMIN".equals(role) || "MANAGER".equals(role);

        } catch (Exception e) {
            logger.error("Error checking admin/manager role", e);
            return false;
        }
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