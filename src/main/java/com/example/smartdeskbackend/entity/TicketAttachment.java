package com.example.smartdeskbackend.entity;

import com.example.smartdeskbackend.entity.base.AuditableEntity;
import com.example.smartdeskbackend.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "ticket_attachments", indexes = {
        @Index(name = "idx_attachment_ticket", columnList = "ticket_id"),
        @Index(name = "idx_attachment_uploaded_by", columnList = "uploaded_by")
})
public class TicketAttachment extends AuditableEntity {

    @NotBlank(message = "Original filename is required")
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @NotBlank(message = "Stored filename is required")
    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;

    @Column(name = "is_image", nullable = false)
    private Boolean isImage = false;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attachment_ticket"))
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false, foreignKey = @ForeignKey(name = "fk_attachment_uploaded_by"))
    private User uploadedBy;

    // Constructors
    public TicketAttachment() {
        super();
    }

    public TicketAttachment(String originalName, String storedName, Long fileSize, String mimeType, Ticket ticket, User uploadedBy) {
        this();
        this.originalName = originalName;
        this.storedName = storedName;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.ticket = ticket;
        this.uploadedBy = uploadedBy;
        this.isImage = isImageFile(mimeType);
    }

    // Business Methods
    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    public String getFileExtension() {
        if (originalName == null) return "";
        int lastDot = originalName.lastIndexOf('.');
        return lastDot > 0 ? originalName.substring(lastDot + 1).toLowerCase() : "";
    }

    public String getFileSizeFormatted() {
        if (fileSize == null) return "0 B";

        long bytes = fileSize;
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;

        while (bytes >= 1024 && unitIndex < units.length - 1) {
            bytes /= 1024;
            unitIndex++;
        }

        return String.format("%d %s", bytes, units[unitIndex]);
    }

    private boolean isImageFile(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public boolean isImage() {
        return isImage != null && isImage;
    }

    // Getters and Setters
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getStoredName() { return storedName; }
    public void setStoredName(String storedName) { this.storedName = storedName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }

    public Boolean getIsImage() { return isImage; }
    public void setIsImage(Boolean isImage) { this.isImage = isImage; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }
}