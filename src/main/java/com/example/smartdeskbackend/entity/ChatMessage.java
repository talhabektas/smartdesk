package com.example.smartdeskbackend.entity;

import com.example.smartdeskbackend.entity.base.AuditableEntity;
import com.example.smartdeskbackend.enums.ChatMessageType;
import com.example.smartdeskbackend.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_ticket", columnList = "ticket_id"),
        @Index(name = "idx_chat_sender", columnList = "sender_id"),
        @Index(name = "idx_chat_created_at", columnList = "created_at"),
        @Index(name = "idx_chat_message_type", columnList = "message_type")
})
public class ChatMessage extends AuditableEntity {

    @NotBlank(message = "Message content is required")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private ChatMessageType messageType = ChatMessageType.TEXT;

    @Column(name = "is_internal", nullable = false)
    private Boolean isInternal = false;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "reply_to_message_id")
    private Long replyToMessageId;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type", length = 100)
    private String fileType;

    // Relationships
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_ticket"))
    private Ticket ticket;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_sender"))
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_chat_reply"))
    private ChatMessage replyToMessage;

    // Constructors
    public ChatMessage() {
        super();
    }

    public ChatMessage(String content, Ticket ticket, User sender) {
        this();
        this.content = content;
        this.ticket = ticket;
        this.sender = sender;
    }

    // Business Methods

    /**
     * Mesajı okundu olarak işaretle
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Mesajın yaşını hesapla (dakika cinsinden)
     */
    public long getAgeInMinutes() {
        return java.time.temporal.ChronoUnit.MINUTES.between(
                this.getCreatedAt(),
                LocalDateTime.now());
    }

    /**
     * Mesajın kısa önizlemesini al
     */
    public String getPreview() {
        if (content == null)
            return "";
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }

    /**
     * Dosya ekli mi kontrol et
     */
    public boolean hasAttachment() {
        return fileUrl != null && !fileUrl.isEmpty();
    }

    /**
     * Sistem mesajı mı kontrol et
     */
    public boolean isSystemMessage() {
        return messageType == ChatMessageType.SYSTEM;
    }

    /**
     * Agent mesajı mı kontrol et
     */
    public boolean isAgentMessage() {
        return sender != null && sender.getRole().isAgent();
    }

    /**
     * Müşteri mesajı mı kontrol et
     */
    public boolean isCustomerMessage() {
        return sender != null && sender.getRole() == UserRole.CUSTOMER;
    }

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ChatMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(ChatMessageType messageType) {
        this.messageType = messageType;
    }

    public Boolean getIsInternal() {
        return isInternal;
    }

    public void setIsInternal(Boolean isInternal) {
        this.isInternal = isInternal;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public Long getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(Long replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public ChatMessage getReplyToMessage() {
        return replyToMessage;
    }

    public void setReplyToMessage(ChatMessage replyToMessage) {
        this.replyToMessage = replyToMessage;
    }

    @Override
    public String toString() {
        return String.format("ChatMessage{id=%d, content='%s', sender='%s', ticketId=%d}",
                getId(), getPreview(), sender != null ? sender.getEmail() : "Unknown",
                ticket != null ? ticket.getId() : 0);
    }
}