package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.entity.Company;
import com.example.smartdeskbackend.entity.Notification;
import com.example.smartdeskbackend.entity.User;
import com.example.smartdeskbackend.exception.BusinessLogicException;
import com.example.smartdeskbackend.exception.ResourceNotFoundException;
import com.example.smartdeskbackend.repository.CompanyRepository;
import com.example.smartdeskbackend.repository.NotificationRepository;
import com.example.smartdeskbackend.repository.UserRepository;
import com.example.smartdeskbackend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;

    @Override
    @Transactional
    public Notification createNotification(Notification notification, Long recipientUserId, Long companyId) {
        User recipient = userRepository.findById(recipientUserId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Recipient user not found with id: " + recipientUserId));
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        if (!recipient.getCompany().getId().equals(companyId)) {
            throw new BusinessLogicException("Recipient user does not belong to the specified company.");
        }

        notification.setRecipientUser(recipient);
        notification.setCompany(company);
        notification.setRead(false);
        notification.setSentAt(LocalDateTime.now());
        notification.setCreatedAt(LocalDateTime.now()); // AuditableEntity'den miras alınan metod
        notification.setUpdatedAt(LocalDateTime.now()); // AuditableEntity'den miras alınan metod
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markNotificationAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getRecipientUser().getId().equals(userId)) {
            throw new BusinessLogicException("You are not authorized to mark this notification as read.");
        }

        notification.setRead(true);
        notification.setUpdatedAt(LocalDateTime.now()); // AuditableEntity'den miras alınan metod
        notificationRepository.save(notification);
    }

    @Override
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientUserId(userId, pageable);
    }

    @Override
    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        return notificationRepository.findByRecipientUserIdAndIsReadFalse(userId);
    }

    @Override
    public long countUnreadNotificationsForUser(Long userId) {
        return notificationRepository.countByRecipientUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getRecipientUser().getId().equals(userId)) {
            throw new BusinessLogicException("You are not authorized to delete this notification.");
        }
        notificationRepository.delete(notification);
    }
}