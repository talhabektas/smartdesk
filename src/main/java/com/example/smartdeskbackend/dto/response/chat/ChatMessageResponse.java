package com.example.smartdeskbackend.dto.response.chat;

import com.example.smartdeskbackend.enums.ChatMessageType;
import com.example.smartdeskbackend.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {

    private Long id;
    private String content;
    private ChatMessageType messageType;
    private Boolean isInternal;
    private Boolean isRead;
    private LocalDateTime readAt;
    private Long replyToMessageId;

    // File attachment fields
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String fileType;

    // Sender information
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private UserRole senderRole;
    private String senderAvatar;

    // Ticket information
    private Long ticketId;
    private String ticketNumber;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional fields
    private String preview;
    private Long ageInMinutes;
    private Boolean hasAttachment;
    private Boolean isSystemMessage;
    private Boolean isAgentMessage;
    private Boolean isCustomerMessage;
}