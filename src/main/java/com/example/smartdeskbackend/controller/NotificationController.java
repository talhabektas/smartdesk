package com.example.smartdeskbackend.controller;

import com.example.smartdeskbackend.entity.Notification;
import com.example.smartdeskbackend.dto.response.NotificationResponse;
import com.example.smartdeskbackend.service.NotificationService;
import com.example.smartdeskbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
 * Notification management REST Controller
 */
@RestController
@RequestMapping("/v1/notifications")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Kullanıcının bildirimlerini getir
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT') or hasRole('CUSTOMER')")
    public ResponseEntity<?> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "sentAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {

        logger.info("🔔 Getting notifications for user: {}", userId);

        try {
            // Kullanıcı authorization kontrolü
            if (!isAuthorizedForUser(request, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "You can only access your own notifications"));
            }

            // Sorting direction
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<Notification> notifications = notificationService.getUserNotifications(userId, pageable);

            // Convert entities to DTOs to avoid circular reference
            List<NotificationResponse> notificationDTOs = notifications.getContent().stream()
                    .map(this::convertToDTO)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notificationDTOs);
            response.put("totalElements", notifications.getTotalElements());
            response.put("totalPages", notifications.getTotalPages());
            response.put("currentPage", notifications.getNumber());
            response.put("size", notifications.getSize());
            response.put("hasNext", notifications.hasNext());
            response.put("hasPrevious", notifications.hasPrevious());
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting notifications for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("NOTIFICATIONS_FETCH_ERROR", e.getMessage()));
        }
    }

    /**
     * Okunmamış bildirimleri getir
     */
    @GetMapping("/user/{userId}/unread")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT') or hasRole('CUSTOMER')")
    public ResponseEntity<?> getUnreadNotifications(
            @PathVariable Long userId,
            HttpServletRequest request) {

        logger.info("🔔 Getting unread notifications for user: {}", userId);

        try {
            if (!isAuthorizedForUser(request, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "You can only access your own notifications"));
            }

            List<Notification> unreadNotifications = notificationService.getUnreadNotificationsForUser(userId);
            long unreadCount = notificationService.countUnreadNotificationsForUser(userId);

            // Convert entities to DTOs
            List<NotificationResponse> notificationDTOs = unreadNotifications.stream()
                    .map(this::convertToDTO)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notificationDTOs);
            response.put("count", unreadCount);
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting unread notifications for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("UNREAD_NOTIFICATIONS_ERROR", e.getMessage()));
        }
    }

    /**
     * Okunmamış bildirim sayısını getir
     */
    @GetMapping("/user/{userId}/unread/count")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT') or hasRole('CUSTOMER')")
    public ResponseEntity<?> getUnreadNotificationCount(
            @PathVariable Long userId,
            HttpServletRequest request) {

        try {
            if (!isAuthorizedForUser(request, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "You can only access your own notifications"));
            }

            long unreadCount = notificationService.countUnreadNotificationsForUser(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("count", unreadCount);
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting unread notification count for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("UNREAD_COUNT_ERROR", e.getMessage()));
        }
    }

    /**
     * Bildirimi okunmuş olarak işaretle
     */
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT') or hasRole('CUSTOMER')")
    public ResponseEntity<?> markNotificationAsRead(
            @PathVariable Long notificationId,
            HttpServletRequest request) {

        logger.info("🔔 Marking notification as read: {}", notificationId);

        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("UNAUTHORIZED", "Token not found"));
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            notificationService.markNotificationAsRead(notificationId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Notification marked as read successfully");
            response.put("notificationId", notificationId);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", notificationId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("MARK_READ_ERROR", e.getMessage()));
        }
    }

    /**
     * Kullanıcının tüm bildirimlerini okunmuş olarak işaretle
     */
    @PatchMapping("/user/{userId}/read-all")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT') or hasRole('CUSTOMER')")
    public ResponseEntity<?> markAllNotificationsAsRead(
            @PathVariable Long userId,
            HttpServletRequest request) {

        logger.info("🔔 Marking all notifications as read for user: {}", userId);

        try {
            // Kullanıcı authorization kontrolü
            if (!isAuthorizedForUser(request, userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "You can only access your own notifications"));
            }

            // Kullanıcının tüm okunmamış bildirimlerini bul ve okunmuş olarak işaretle
            List<Notification> unreadNotifications = notificationService.getUnreadNotificationsForUser(userId);
            
            for (Notification notification : unreadNotifications) {
                notificationService.markNotificationAsRead(notification.getId(), userId);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "All notifications marked as read successfully");
            response.put("markedCount", unreadNotifications.size());
            response.put("userId", userId);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error marking all notifications as read for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("MARK_ALL_READ_ERROR", e.getMessage()));
        }
    }

    /**
     * Bildirimi sil
     */
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT') or hasRole('CUSTOMER')")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long notificationId,
            HttpServletRequest request) {

        logger.info("🔔 Deleting notification: {}", notificationId);

        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("UNAUTHORIZED", "Token not found"));
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            notificationService.deleteNotification(notificationId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Notification deleted successfully");
            response.put("notificationId", notificationId);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting notification: {}", notificationId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("DELETE_NOTIFICATION_ERROR", e.getMessage()));
        }
    }

    // ============ HELPER METHODS ============

    /**
     * Kullanıcının kendi bildirimlerine erişip erişemeyeceğini kontrol eder
     */
    private boolean isAuthorizedForUser(HttpServletRequest request, Long userId) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) return false;

            String role = jwtUtil.getRoleFromToken(token);
            Long requestingUserId = jwtUtil.getUserIdFromToken(token);

            // SUPER_ADMIN her şeye erişebilir
            if ("SUPER_ADMIN".equals(role)) {
                return true;
            }

            // Diğerleri sadece kendi bildirimlerine erişebilir
            return userId.equals(requestingUserId);

        } catch (Exception e) {
            logger.error("Error checking user authorization", e);
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
     * Entity'yi DTO'ya dönüştürür (circular reference'dan kaçınmak için)
     */
    private NotificationResponse convertToDTO(Notification notification) {
        NotificationResponse dto = new NotificationResponse();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setTargetUrl(notification.getTargetUrl());
        dto.setRead(notification.isRead());
        dto.setSentAt(notification.getSentAt());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setUpdatedAt(notification.getUpdatedAt());
        
        // User basic info
        if (notification.getRecipientUser() != null) {
            dto.setRecipientUser(new NotificationResponse.UserBasicInfo(
                notification.getRecipientUser().getId(),
                notification.getRecipientUser().getEmail(),
                notification.getRecipientUser().getFirstName(),
                notification.getRecipientUser().getLastName()
            ));
        }
        
        // Company basic info
        if (notification.getCompany() != null) {
            dto.setCompany(new NotificationResponse.CompanyBasicInfo(
                notification.getCompany().getId(),
                notification.getCompany().getName()
            ));
        }
        
        return dto;
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