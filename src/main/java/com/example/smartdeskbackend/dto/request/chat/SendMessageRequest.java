package com.example.smartdeskbackend.dto.request.chat;

import com.example.smartdeskbackend.enums.ChatMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    @NotBlank(message = "Message content is required")
    private String content;

    @NotNull(message = "Message type is required")
    private ChatMessageType messageType = ChatMessageType.TEXT;

    private Long replyToMessageId;

    private Boolean isInternal = false;

    // File attachment fields
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String fileType;
}