package com.example.smartdeskbackend.dto.response;

import java.time.LocalDateTime;

/**
 * Notification API response DTO
 */
public class NotificationResponse {
    private Long id;
    private String message;
    private String type;
    private String targetUrl;
    private boolean isRead;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // User bilgileri (circular reference'dan kaçınmak için sadece gerekli alanlar)
    private UserBasicInfo recipientUser;
    
    // Company bilgileri (circular reference'dan kaçınmak için sadece gerekli alanlar)
    private CompanyBasicInfo company;
    
    // Inner classes for basic info
    public static class UserBasicInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        
        public UserBasicInfo() {}
        
        public UserBasicInfo(Long id, String email, String firstName, String lastName) {
            this.id = id;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
        }
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }
    
    public static class CompanyBasicInfo {
        private Long id;
        private String name;
        
        public CompanyBasicInfo() {}
        
        public CompanyBasicInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    // Constructors
    public NotificationResponse() {}
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public UserBasicInfo getRecipientUser() { return recipientUser; }
    public void setRecipientUser(UserBasicInfo recipientUser) { this.recipientUser = recipientUser; }
    
    public CompanyBasicInfo getCompany() { return company; }
    public void setCompany(CompanyBasicInfo company) { this.company = company; }
}