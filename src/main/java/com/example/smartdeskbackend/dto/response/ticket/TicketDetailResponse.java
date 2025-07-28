
package com.example.smartdeskbackend.dto.response.ticket;

import java.time.LocalDateTime;
import java.util.List;

public class TicketDetailResponse {

    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private String status;
    private String statusDisplayName;
    private String priority;
    private String priorityDisplayName;
    private String category;
    private String source;
    private Boolean isInternal;
    private Integer escalationLevel;
    private String resolutionSummary;
    private Integer customerSatisfactionRating;
    private String customerSatisfactionFeedback;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime slaDeadline;
    private LocalDateTime firstResponseAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;

    // Related entities
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerSegment;

    private Long assignedAgentId;
    private String assignedAgentName;

    private Long departmentId;
    private String departmentName;

    private Long creatorUserId;
    private String creatorUserName;

    // Collections
    private List<TicketCommentResponse> comments;
    private List<Object> attachments;
    private List<Object> history;

    // Constructors
    public TicketDetailResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusDisplayName() { return statusDisplayName; }
    public void setStatusDisplayName(String statusDisplayName) { this.statusDisplayName = statusDisplayName; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getPriorityDisplayName() { return priorityDisplayName; }
    public void setPriorityDisplayName(String priorityDisplayName) { this.priorityDisplayName = priorityDisplayName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Boolean getIsInternal() { return isInternal; }
    public void setIsInternal(Boolean isInternal) { this.isInternal = isInternal; }

    public Integer getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(Integer escalationLevel) { this.escalationLevel = escalationLevel; }

    public String getResolutionSummary() { return resolutionSummary; }
    public void setResolutionSummary(String resolutionSummary) { this.resolutionSummary = resolutionSummary; }

    public Integer getCustomerSatisfactionRating() { return customerSatisfactionRating; }
    public void setCustomerSatisfactionRating(Integer customerSatisfactionRating) { this.customerSatisfactionRating = customerSatisfactionRating; }

    public String getCustomerSatisfactionFeedback() { return customerSatisfactionFeedback; }
    public void setCustomerSatisfactionFeedback(String customerSatisfactionFeedback) { this.customerSatisfactionFeedback = customerSatisfactionFeedback; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }

    public LocalDateTime getFirstResponseAt() { return firstResponseAt; }
    public void setFirstResponseAt(LocalDateTime firstResponseAt) { this.firstResponseAt = firstResponseAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerSegment() { return customerSegment; }
    public void setCustomerSegment(String customerSegment) { this.customerSegment = customerSegment; }

    public Long getAssignedAgentId() { return assignedAgentId; }
    public void setAssignedAgentId(Long assignedAgentId) { this.assignedAgentId = assignedAgentId; }

    public String getAssignedAgentName() { return assignedAgentName; }
    public void setAssignedAgentName(String assignedAgentName) { this.assignedAgentName = assignedAgentName; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public Long getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(Long creatorUserId) { this.creatorUserId = creatorUserId; }

    public String getCreatorUserName() { return creatorUserName; }
    public void setCreatorUserName(String creatorUserName) { this.creatorUserName = creatorUserName; }

    public List<TicketCommentResponse> getComments() { return comments; }
    public void setComments(List<TicketCommentResponse> comments) { this.comments = comments; }

    public List<Object> getAttachments() { return attachments; }
    public void setAttachments(List<Object> attachments) { this.attachments = attachments; }

    public List<Object> getHistory() { return history; }
    public void setHistory(List<Object> history) { this.history = history; }
}
