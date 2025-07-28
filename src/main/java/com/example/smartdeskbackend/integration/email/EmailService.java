package com.example.smartdeskbackend.integration.email;

import com.example.smartdeskbackend.entity.User;
import com.example.smartdeskbackend.entity.Ticket;

/**
 * Email service interface
 */
public interface EmailService {

    /**
     * Welcome email gönder
     */
    void sendWelcomeEmail(User user);

    /**
     * Email doğrulama maili gönder
     */
    void sendVerificationEmail(User user);

    /**
     * Şifre sıfırlama maili gönder
     */
    void sendPasswordResetEmail(User user, String resetToken);

    /**
     * Ticket bildirimi gönder
     */
    void sendTicketNotification(Ticket ticket, String notificationType);

    /**
     * Genel email gönder
     */
    void sendEmail(String to, String subject, String content);

    /**
     * HTML email gönder
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);

    /**
     * Template ile email gönder
     */
    void sendTemplateEmail(String to, String subject, String templateName, Object model);
}
