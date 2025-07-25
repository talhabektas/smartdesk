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
@Table(name = "ticket_history", indexes = {
        @Index(name = "idx_history_ticket", columnList = "ticket_id"),
        @Index(name = "idx_history_user", columnList = "user_id"),
        @Index(name = "idx_history_created_at", columnList = "created_at")
})
public class TicketHistory extends AuditableEntity {

    @Column(name = "field_name", nullable = false, length = 50)
    private String fieldName;

    @Column(name = "old_value", length = 1000)
    private String oldValue;

    @Column(name = "new_value", length = 1000)
    private String newValue;

    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType; // CREATED, UPDATED, STATUS_CHANGED, ASSIGNED, etc.

    @Column(name = "description", length = 500)
    private String description;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, foreignKey = @ForeignKey(name = "fk_history_ticket"))
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_history_user"))
    private User user;

    // Constructors
    public TicketHistory() {
        super();
    }

    public TicketHistory(String fieldName, String oldValue, String newValue, String changeType, Ticket ticket, User user) {
        this();
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeType = changeType;
        this.ticket = ticket;
        this.user = user;
    }

    // Business Methods
    public boolean isStatusChange() {
        return "STATUS_CHANGED".equals(changeType) || "status".equals(fieldName);
    }

    public boolean isAssignmentChange() {
        return "ASSIGNED".equals(changeType) || "assignedAgent".equals(fieldName);
    }

    public boolean isPriorityChange() {
        return "PRIORITY_CHANGED".equals(changeType) || "priority".equals(fieldName);
    }

    // Getters and Setters
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}