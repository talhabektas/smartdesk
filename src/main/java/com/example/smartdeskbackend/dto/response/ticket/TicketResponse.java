
package com.example.smartdeskbackend.dto.response.ticket;

import java.time.LocalDateTime;

public class TicketResponse {

    private Long id;
    private String ticketNumber;
    private String title;
    private String status;
    private String statusDisplayName;
    private String priority;
    private String priorityDisplayName;
    private String category;
    private String source;
    private String customerName;
    private String customerEmail;
    private String assignedAgentName;
    private String departmentName;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime slaDeadline;
    private Integer escalationLevel;

    // Constructors
    public TicketResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

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

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getAssignedAgentName() { return assignedAgentName; }
    public void setAssignedAgentName(String assignedAgentName) { this.assignedAgentName = assignedAgentName; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }

    public Integer getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(Integer escalationLevel) { this.escalationLevel = escalationLevel; }
}