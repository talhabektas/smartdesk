package com.example.smartdeskbackend.integration.email;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Email template helper sınıfı
 */
@Component
public class EmailTemplate {

    /**
     * Temel email template'i
     */
    public static String getBaseTemplate() {
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>{{title}}</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background-color: #007bff; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
                "        .content { background-color: #f8f9fa; padding: 30px; border: 1px solid #dee2e6; }" +
                "        .footer { background-color: #6c757d; color: white; padding: 15px; text-align: center; border-radius: 0 0 5px 5px; font-size: 12px; }" +
                "        .button { display: inline-block; background-color: #28a745; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; margin: 15px 0; }" +
                "        .button:hover { background-color: #218838; }" +
                "        .alert { padding: 15px; margin: 15px 0; border-radius: 5px; }" +
                "        .alert-warning { background-color: #fff3cd; border: 1px solid #ffeaa7; color: #856404; }" +
                "        .ticket-info { background-color: white; padding: 15px; border-radius: 5px; margin: 15px 0; }" +
                "        .ticket-info h4 { margin-top: 0; color: #007bff; }" +
                "        .ticket-status { display: inline-block; padding: 5px 10px; border-radius: 3px; font-size: 12px; font-weight: bold; }" +
                "        .status-new { background-color: #17a2b8; color: white; }" +
                "        .status-open { background-color: #28a745; color: white; }" +
                "        .status-resolved { background-color: #6c757d; color: white; }" +
                "        .priority-high { color: #dc3545; font-weight: bold; }" +
                "        .priority-urgent { color: #dc3545; font-weight: bold; text-transform: uppercase; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"header\">" +
                "        <h1>{{applicationName}}</h1>" +
                "    </div>" +
                "    <div class=\"content\">" +
                "        {{content}}" +
                "    </div>" +
                "    <div class=\"footer\">" +
                "        <p>&copy; 2024 {{applicationName}}. All rights reserved.</p>" +
                "        <p>This is an automated message, please do not reply to this email.</p>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Welcome email template'i
     */
    public static String getWelcomeTemplate() {
        return "<h2>Welcome to Our Platform!</h2>" +
                "<p>Dear {{userName}},</p>" +
                "<p>Welcome to our customer support system. Your account has been created successfully.</p>" +
                "<div class=\"ticket-info\">" +
                "<h4>Getting Started:</h4>" +
                "<ul>" +
                "<li>Log in to your dashboard</li>" +
                "<li>Create and manage support tickets</li>" +
                "<li>Track your request status</li>" +
                "<li>Access our knowledge base</li>" +
                "</ul>" +
                "</div>" +
                "<p>If you have any questions, please don't hesitate to contact our support team.</p>";
    }

    /**
     * Password reset template'i
     */
    public static String getPasswordResetTemplate() {
        return "<h2>Password Reset Request</h2>" +
                "<p>Dear {{userName}},</p>" +
                "<p>You have requested to reset your password. Click the button below to set a new password:</p>" +
                "<p><a href=\"{{resetLink}}\" class=\"button\">Reset Password</a></p>" +
                "<div class=\"alert alert-warning\">" +
                "<strong>Important:</strong> This link will expire in 1 hour for security reasons." +
                "</div>" +
                "<p>If you didn't request this password reset, please ignore this email and your password will remain unchanged.</p>";
    }

    /**
     * Ticket notification template'i
     */
    public static String getTicketNotificationTemplate() {
        return "<h2>Ticket {{action}}</h2>" +
                "<p>Ticket #{{ticketNumber}} has been {{action}}.</p>" +
                "<div class=\"ticket-info\">" +
                "<h4>Ticket Details:</h4>" +
                "<table style=\"width: 100%; border-collapse: collapse;\">" +
                "<tr><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\"><strong>Title:</strong></td><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\">{{ticketTitle}}</td></tr>" +
                "<tr><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\"><strong>Status:</strong></td><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\"><span class=\"ticket-status status-{{statusClass}}\">{{ticketStatus}}</span></td></tr>" +
                "<tr><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\"><strong>Priority:</strong></td><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\"><span class=\"priority-{{priorityClass}}\">{{ticketPriority}}</span></td></tr>" +
                "<tr><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\"><strong>Created:</strong></td><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\">{{createdAt}}</td></tr>" +
                "{{#assignedAgent}}" +
                "<tr><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\"><strong>Assigned to:</strong></td><td style=\"padding: 8px; border-bottom: 1px solid #ddd;\">{{assignedAgent}}</td></tr>" +
                "{{/assignedAgent}}" +
                "</table>" +
                "</div>" +
                "<p>You can view the full ticket details in your dashboard.</p>";
    }

    /**
     * Email verification template'i
     */
    public static String getEmailVerificationTemplate() {
        return "<h2>Email Verification Required</h2>" +
                "<p>Dear {{userName}},</p>" +
                "<p>Please click the button below to verify your email address:</p>" +
                "<p><a href=\"{{verificationLink}}\" class=\"button\">Verify Email</a></p>" +
                "<div class=\"alert alert-warning\">" +
                "<strong>Important:</strong> This verification link will expire in 24 hours." +
                "</div>" +
                "<p>If the button doesn't work, you can copy and paste this link into your browser:</p>" +
                "<p style=\"word-break: break-all; color: #007bff;\">{{verificationLink}}</p>";
    }

    /**
     * Template'i değişkenlerle replace eder
     */
    public static String processTemplate(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    /**
     * Ticket notification için değişkenleri hazırlar
     */
    public static Map<String, String> prepareTicketVariables(Object ticket, String action, String applicationName) {
        Map<String, String> variables = new HashMap<>();
        // Bu method ticket entity'sinden değişkenleri çıkarır
        // Gerçek implementasyonda ticket object'inin field'larını kullanır
        variables.put("applicationName", applicationName);
        variables.put("action", action);
        // Ticket fields buraya eklenecek
        return variables;
    }
}