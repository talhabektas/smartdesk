package com.example.smartdeskbackend.integration.email;

import com.example.smartdeskbackend.config.ApplicationConfig;
import com.example.smartdeskbackend.entity.Ticket;
import com.example.smartdeskbackend.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

/**
 * Email service implementation
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private TemplateEngine templateEngine;

    @Autowired
    private ApplicationConfig.NotificationProperties notificationProperties;

    @Value("${spring.application.name:SmartDesk CRM}")
    private String applicationName;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Override
    @Async
    public void sendWelcomeEmail(User user) {
        if (!notificationProperties.getEmail().isEnabled()) {
            logger.debug("Email notifications are disabled");
            return;
        }

        logger.info("Sending welcome email to: {}", user.getEmail());

        try {
            String subject = "Welcome to " + applicationName;
            String content = createWelcomeEmailContent(user);

            if (templateEngine != null) {
                sendTemplateEmail(user.getEmail(), subject, "email/welcome", user);
            } else {
                sendHtmlEmail(user.getEmail(), subject, content);
            }

            logger.info("Welcome email sent successfully to: {}", user.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", user.getEmail(), e);
        }
    }

    @Override
    @Async
    public void sendVerificationEmail(User user) {
        if (!notificationProperties.getEmail().isEnabled()) {
            logger.debug("Email notifications are disabled");
            return;
        }

        logger.info("Sending verification email to: {}", user.getEmail());

        try {
            String subject = "Verify Your Email - " + applicationName;
            String verificationLink = createVerificationLink(user.getEmailVerificationToken());
            String content = createVerificationEmailContent(user, verificationLink);

            sendHtmlEmail(user.getEmail(), subject, content);

            logger.info("Verification email sent successfully to: {}", user.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send verification email to: {}", user.getEmail(), e);
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        if (!notificationProperties.getEmail().isEnabled()) {
            logger.debug("Email notifications are disabled");
            return;
        }

        logger.info("Sending password reset email to: {}", user.getEmail());

        try {
            String subject = "Password Reset - " + applicationName;
            String resetLink = createPasswordResetLink(resetToken);
            String content = createPasswordResetEmailContent(user, resetLink);

            if (templateEngine != null) {
                Context context = new Context();
                context.setVariable("user", user);
                context.setVariable("resetLink", resetLink);
                context.setVariable("applicationName", applicationName);
                sendTemplateEmail(user.getEmail(), subject, "email/password-reset", context);
            } else {
                sendHtmlEmail(user.getEmail(), subject, content);
            }

            logger.info("Password reset email sent successfully to: {}", user.getEmail());

        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }

    @Override
    @Async
    public void sendTicketNotification(Ticket ticket, String notificationType) {
        if (!notificationProperties.getEmail().isEnabled()) {
            logger.debug("Email notifications are disabled");
            return;
        }

        logger.info("Sending ticket notification: {} for ticket: {}", notificationType, ticket.getTicketNumber());

        try {
            String recipientEmail = determineRecipientEmail(ticket, notificationType);
            if (recipientEmail == null) {
                logger.warn("No recipient email found for ticket notification: {}", ticket.getTicketNumber());
                return;
            }

            String subject = createTicketNotificationSubject(ticket, notificationType);
            String content = createTicketNotificationContent(ticket, notificationType);

            if (templateEngine != null) {
                Context context = new Context();
                context.setVariable("ticket", ticket);
                context.setVariable("notificationType", notificationType);
                context.setVariable("applicationName", applicationName);
                sendTemplateEmail(recipientEmail, subject, "email/ticket-notification", context);
            } else {
                sendHtmlEmail(recipientEmail, subject, content);
            }

            logger.info("Ticket notification sent successfully for: {}", ticket.getTicketNumber());

        } catch (Exception e) {
            logger.error("Failed to send ticket notification for: {}", ticket.getTicketNumber(), e);
        }
    }

    @Override
    @Async
    public void sendEmail(String to, String subject, String content) {
        if (!notificationProperties.getEmail().isEnabled()) {
            logger.debug("Email notifications are disabled");
            return;
        }

        logger.debug("Sending simple email to: {}", to);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(notificationProperties.getEmail().getFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);

            logger.debug("Simple email sent successfully to: {}", to);

        } catch (Exception e) {
            logger.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!notificationProperties.getEmail().isEnabled()) {
            logger.debug("Email notifications are disabled");
            return;
        }

        logger.debug("Sending HTML email to: {}", to);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(notificationProperties.getEmail().getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            logger.debug("HTML email sent successfully to: {}", to);

        } catch (MessagingException e) {
            logger.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    @Override
    @Async
    public void sendTemplateEmail(String to, String subject, String templateName, Object model) {
        if (!notificationProperties.getEmail().isEnabled()) {
            logger.debug("Email notifications are disabled");
            return;
        }

        if (templateEngine == null) {
            logger.warn("Template engine not available, falling back to simple email");
            sendEmail(to, subject, "Template email content not available");
            return;
        }

        logger.debug("Sending template email to: {} with template: {}", to, templateName);

        try {
            Context context = new Context(Locale.ENGLISH);

            if (model instanceof Context) {
                context = (Context) model;
            } else {
                context.setVariable("model", model);
            }

            context.setVariable("applicationName", applicationName);

            String htmlContent = templateEngine.process(templateName, context);
            sendHtmlEmail(to, subject, htmlContent);

            logger.debug("Template email sent successfully to: {}", to);

        } catch (Exception e) {
            logger.error("Failed to send template email to: {} with template: {}", to, templateName, e);
            throw new RuntimeException("Failed to send template email", e);
        }
    }

    // ============ Helper Methods ============

    /**
     * Welcome email content oluşturur
     */
    private String createWelcomeEmailContent(User user) {
        return String.format(
                "<html><body>" +
                        "<h2>Welcome to %s!</h2>" +
                        "<p>Dear %s,</p>" +
                        "<p>Welcome to our customer support system. Your account has been created successfully.</p>" +
                        "<p>You can now log in to access your dashboard and manage your support tickets.</p>" +
                        "<p>If you have any questions, please don't hesitate to contact our support team.</p>" +
                        "<br>" +
                        "<p>Best regards,<br>%s Team</p>" +
                        "</body></html>",
                applicationName, user.getFullName(), applicationName
        );
    }

    /**
     * Email verification content oluşturur
     */
    private String createVerificationEmailContent(User user, String verificationLink) {
        return String.format(
                "<html><body>" +
                        "<h2>Email Verification - %s</h2>" +
                        "<p>Dear %s,</p>" +
                        "<p>Please click the link below to verify your email address:</p>" +
                        "<p><a href=\"%s\" style=\"background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;\">Verify Email</a></p>" +
                        "<p>If the button doesn't work, you can copy and paste this link into your browser:</p>" +
                        "<p>%s</p>" +
                        "<p>This link will expire in 24 hours.</p>" +
                        "<br>" +
                        "<p>Best regards,<br>%s Team</p>" +
                        "</body></html>",
                applicationName, user.getFullName(), verificationLink, verificationLink, applicationName
        );
    }

    /**
     * Password reset email content oluşturur
     */
    private String createPasswordResetEmailContent(User user, String resetLink) {
        return String.format(
                "<html><body>" +
                        "<h2>Password Reset - %s</h2>" +
                        "<p>Dear %s,</p>" +
                        "<p>You have requested to reset your password. Click the link below to set a new password:</p>" +
                        "<p><a href=\"%s\" style=\"background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;\">Reset Password</a></p>" +
                        "<p>If the button doesn't work, you can copy and paste this link into your browser:</p>" +
                        "<p>%s</p>" +
                        "<p>This link will expire in 1 hour.</p>" +
                        "<p>If you didn't request this password reset, please ignore this email.</p>" +
                        "<br>" +
                        "<p>Best regards,<br>%s Team</p>" +
                        "</body></html>",
                applicationName, user.getFullName(), resetLink, resetLink, applicationName
        );
    }

    /**
     * Ticket notification content oluşturur
     */
    private String createTicketNotificationContent(Ticket ticket, String notificationType) {
        String action = getNotificationAction(notificationType);

        return String.format(
                "<html><body>" +
                        "<h2>Ticket %s - %s</h2>" +
                        "<p>Ticket #%s has been %s.</p>" +
                        "<h3>Ticket Details:</h3>" +
                        "<ul>" +
                        "<li><strong>Title:</strong> %s</li>" +
                        "<li><strong>Status:</strong> %s</li>" +
                        "<li><strong>Priority:</strong> %s</li>" +
                        "<li><strong>Created:</strong> %s</li>" +
                        "</ul>" +
                        "<p>You can view the full ticket details in your dashboard.</p>" +
                        "<br>" +
                        "<p>Best regards,<br>%s Team</p>" +
                        "</body></html>",
                action, applicationName,
                ticket.getTicketNumber(), action.toLowerCase(),
                ticket.getTitle(),
                ticket.getStatus().getDisplayName(),
                ticket.getPriority().getDisplayName(),
                ticket.getCreatedAt(),
                applicationName
        );
    }

    /**
     * Ticket notification için alıcı email'ini belirler
     */
    private String determineRecipientEmail(Ticket ticket, String notificationType) {
        switch (notificationType.toUpperCase()) {
            case "CREATED":
            case "UPDATED":
                // Assigned agent'a gönder
                if (ticket.getAssignedAgent() != null) {
                    return ticket.getAssignedAgent().getEmail();
                }
                break;
            case "ASSIGNED":
                // Assigned agent'a gönder
                if (ticket.getAssignedAgent() != null) {
                    return ticket.getAssignedAgent().getEmail();
                }
                break;
            case "RESOLVED":
            case "CLOSED":
                // Customer'a gönder
                if (ticket.getCustomer() != null) {
                    return ticket.getCustomer().getEmail();
                }
                // Creator user'a da gönder (eğer customer değilse)
                if (ticket.getCreatorUser() != null) {
                    return ticket.getCreatorUser().getEmail();
                }
                break;
        }
        return null;
    }

    /**
     * Ticket notification subject oluşturur
     */
    private String createTicketNotificationSubject(Ticket ticket, String notificationType) {
        String action = getNotificationAction(notificationType);
        return String.format("[%s] Ticket #%s %s - %s",
                applicationName, ticket.getTicketNumber(), action, ticket.getTitle());
    }

    /**
     * Notification type'dan action string'i çıkarır
     */
    private String getNotificationAction(String notificationType) {
        switch (notificationType.toUpperCase()) {
            case "CREATED": return "Created";
            case "UPDATED": return "Updated";
            case "ASSIGNED": return "Assigned";
            case "RESOLVED": return "Resolved";
            case "CLOSED": return "Closed";
            case "ESCALATED": return "Escalated";
            default: return "Updated";
        }
    }

    /**
     * Email verification link oluşturur
     */
    private String createVerificationLink(String token) {
        // Production'da gerçek frontend URL'i kullanılmalı
        return String.format("http://localhost:3000/verify-email?token=%s", token);
    }

    /**
     * Password reset link oluşturur
     */
    private String createPasswordResetLink(String token) {
        // Production'da gerçek frontend URL'i kullanılmalı
        return String.format("http://localhost:3000/reset-password?token=%s", token);
    }
}