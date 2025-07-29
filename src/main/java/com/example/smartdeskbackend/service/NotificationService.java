// src/main/java/com/example/smartdeskbackend/service/NotificationService.java
package com.example.smartdeskbackend.service;

import com.example.smartdeskbackend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface NotificationService {
    Notification createNotification(Notification notification, Long recipientUserId, Long companyId);
    void markNotificationAsRead(Long notificationId, Long userId);
    Page<Notification> getUserNotifications(Long userId, Pageable pageable);
    List<Notification> getUnreadNotificationsForUser(Long userId);
    long countUnreadNotificationsForUser(Long userId);
    void deleteNotification(Long notificationId, Long userId);
}