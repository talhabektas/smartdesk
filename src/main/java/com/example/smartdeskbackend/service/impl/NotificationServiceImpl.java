package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.entity.Company;
import com.example.smartdeskbackend.entity.Notification;
import com.example.smartdeskbackend.entity.User;
import com.example.smartdeskbackend.enums.NotificationType;
import com.example.smartdeskbackend.enums.UserRole;
import com.example.smartdeskbackend.exception.BusinessLogicException;
import com.example.smartdeskbackend.exception.ResourceNotFoundException;
import com.example.smartdeskbackend.repository.CompanyRepository;
import com.example.smartdeskbackend.repository.NotificationRepository;
import com.example.smartdeskbackend.repository.UserRepository;
import com.example.smartdeskbackend.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

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

    // ============ TICKET SPECIFIC NOTIFICATION METHODS ============

    @Override
    @Transactional
    public void notifyNewTicketCreated(Long ticketId, Long companyId, String ticketNumber, String customerName) {
        logger.info("🔔 Creating new ticket notifications for ticket: {}", ticketNumber);

        try {
            // Company'deki tüm MANAGER ve AGENT'ları bul
            List<User> managersAndAgents = userRepository.findByCompanyIdAndRoleIn(
                    companyId, List.of(UserRole.MANAGER, UserRole.AGENT));

            String message = String.format("Yeni ticket oluşturuldu: %s - %s", ticketNumber, customerName);
            String targetUrl = String.format("/tickets/%d", ticketId);

            for (User user : managersAndAgents) {
                Notification notification = new Notification();
                notification.setType(NotificationType.TICKET_CREATED.name());
                notification.setMessage(message);
                notification.setTargetUrl(targetUrl);
                createNotification(notification, user.getId(), companyId);
            }

            logger.info("✅ Created {} notifications for new ticket: {}", managersAndAgents.size(), ticketNumber);

        } catch (Exception e) {
            logger.error("❌ Error creating new ticket notifications: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void notifyTicketAssigned(Long ticketId, Long agentId, String ticketNumber) {
        logger.info("🔔 Creating ticket assignment notification for ticket: {}", ticketNumber);

        try {
            User agent = userRepository.findById(agentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + agentId));

            String message = String.format("Size yeni bir ticket atandı: %s", ticketNumber);
            String targetUrl = String.format("/tickets/%d", ticketId);

            Notification notification = new Notification();
            notification.setType(NotificationType.TICKET_ASSIGNED.name());
            notification.setMessage(message);
            notification.setTargetUrl(targetUrl);
            
            createNotification(notification, agentId, agent.getCompany().getId());
            logger.info("✅ Created assignment notification for agent: {} - ticket: {}", agent.getEmail(), ticketNumber);

        } catch (Exception e) {
            logger.error("❌ Error creating ticket assignment notification: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void notifyTicketStatusChanged(Long ticketId, String oldStatus, String newStatus, String ticketNumber) {
        logger.info("🔔 Creating status change notification for ticket: {} - {} -> {}", ticketNumber, oldStatus, newStatus);

        try {
            // Status değişikliği ile ilgilenen kullanıcıları belirle
            // Bu örnek implement'ta sadece log atacağız
            logger.info("📊 Ticket status changed: {} from {} to {}", ticketNumber, oldStatus, newStatus);

        } catch (Exception e) {
            logger.error("❌ Error creating status change notification: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void notifyTicketNeedsAttention(Long ticketId, Long companyId, String ticketNumber) {
        logger.info("🔔 Creating attention reminder notification for ticket: {}", ticketNumber);

        try {
            // Company'deki tüm MANAGER ve AGENT'ları bul
            List<User> managersAndAgents = userRepository.findByCompanyIdAndRoleIn(
                    companyId, List.of(UserRole.MANAGER, UserRole.AGENT));

            String message = String.format("Ticket dikkat gerektiriyor (2 saat geçti): %s", ticketNumber);
            String targetUrl = String.format("/tickets/%d", ticketId);

            for (User user : managersAndAgents) {
                Notification notification = new Notification();
                notification.setType(NotificationType.TICKET_REMINDER.name());
                notification.setMessage(message);
                notification.setTargetUrl(targetUrl);
                createNotification(notification, user.getId(), companyId);
            }

            logger.info("✅ Created {} reminder notifications for ticket: {}", managersAndAgents.size(), ticketNumber);

        } catch (Exception e) {
            logger.error("❌ Error creating ticket reminder notifications: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void notifyPendingApproval(Long ticketId, String approvalType, Long approverId, String ticketNumber) {
        logger.info("🔔 Creating pending approval notification: {} for ticket: {}", approvalType, ticketNumber);

        try {
            User approver = userRepository.findById(approverId)
                    .orElseThrow(() -> new ResourceNotFoundException("Approver not found with id: " + approverId));

            String message;
            NotificationType notificationType;
            if ("MANAGER".equals(approvalType)) {
                message = String.format("Ticket #%s awaits your manager approval", ticketNumber);
                notificationType = NotificationType.TICKET_PENDING_MANAGER_APPROVAL;
            } else {
                message = String.format("Ticket #%s awaits your approval", ticketNumber);
                notificationType = NotificationType.TICKET_PENDING_ADMIN_APPROVAL;
            }

            String targetUrl = String.format("/tickets/%d", ticketId);

            Notification notification = new Notification();
            notification.setType(notificationType.name());
            notification.setMessage(message);
            notification.setTargetUrl(targetUrl);
            
            createNotification(notification, approverId, approver.getCompany().getId());
            logger.info("✅ Created approval notification for: {} - ticket: {}", approver.getEmail(), ticketNumber);

        } catch (Exception e) {
            logger.error("❌ Error creating approval notification: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void notifyApprovalCompleted(Long ticketId, String approvalType, boolean approved, Long requesterId, String ticketNumber) {
        logger.info("🔔 Creating approval completed notification: {} {} for ticket: {}", approvalType, approved ? "APPROVED" : "REJECTED", ticketNumber);

        try {
            User requester = userRepository.findById(requesterId)
                    .orElseThrow(() -> new ResourceNotFoundException("Requester not found with id: " + requesterId));

            String message;
            NotificationType notificationType;
            if (approved) {
                if ("MANAGER".equals(approvalType)) {
                    message = String.format("Ticket manager tarafından onaylandı: %s", ticketNumber);
                    notificationType = NotificationType.TICKET_MANAGER_APPROVED;
                } else {
                    message = String.format("Ticket admin tarafından onaylandı: %s", ticketNumber);
                    notificationType = NotificationType.TICKET_ADMIN_APPROVED;
                }
            } else {
                message = String.format("Ticket onayı reddedildi: %s", ticketNumber);
                notificationType = NotificationType.TICKET_APPROVAL_REJECTED;
            }

            String targetUrl = String.format("/tickets/%d", ticketId);

            Notification notification = new Notification();
            notification.setType(notificationType.name());
            notification.setMessage(message);
            notification.setTargetUrl(targetUrl);
            
            createNotification(notification, requesterId, requester.getCompany().getId());
            logger.info("✅ Created approval completed notification for: {} - ticket: {}", requester.getEmail(), ticketNumber);

        } catch (Exception e) {
            logger.error("❌ Error creating approval completed notification: {}", e.getMessage(), e);
        }
    }
}