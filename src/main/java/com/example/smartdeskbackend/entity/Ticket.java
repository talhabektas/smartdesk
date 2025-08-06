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
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_customer", columnList = "customer_id"),
        @Index(name = "idx_ticket_agent", columnList = "assigned_agent_id"),
        @Index(name = "idx_ticket_company", columnList = "company_id"),
        @Index(name = "idx_ticket_department", columnList = "department_id"),
        @Index(name = "idx_ticket_status", columnList = "status"),
        @Index(name = "idx_ticket_priority", columnList = "priority"),
        @Index(name = "idx_ticket_category", columnList = "category"),
        @Index(name = "idx_ticket_source", columnList = "source"),
        @Index(name = "idx_ticket_created_at", columnList = "created_at"),
        @Index(name = "idx_ticket_sla_deadline", columnList = "sla_deadline")
})
public class Ticket extends AuditableEntity {

    @NotBlank(message = "Ticket title is required")
    @Size(max = 500, message = "Title cannot exceed 500 characters")
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @NotBlank(message = "Ticket description is required")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private TicketPriority priority = TicketPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private TicketStatus status = TicketStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private TicketSource source = TicketSource.WEB_FORM;

    @Column(name = "ticket_number", unique = true, length = 20)
    private String ticketNumber;

    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @Column(name = "first_response_at")
    private LocalDateTime firstResponseAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "actual_hours")
    private Integer actualHours;

    @Column(name = "resolution_summary", columnDefinition = "TEXT")
    private String resolutionSummary;

    @Column(name = "customer_satisfaction_rating")
    private Integer customerSatisfactionRating;

    @Column(name = "customer_satisfaction_feedback", columnDefinition = "TEXT")
    private String customerSatisfactionFeedback;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "is_internal", nullable = false)
    private Boolean isInternal = false;

    @Column(name = "escalation_level")
    private Integer escalationLevel = 0;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "unread_message_count")
    private Integer unreadMessageCount = 0;

    @Column(name = "chat_enabled")
    private Boolean chatEnabled = true;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ticket_company"))
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", foreignKey = @ForeignKey(name = "fk_ticket_customer"))
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id", foreignKey = @ForeignKey(name = "fk_ticket_creator_user"))
    private User creatorUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id", foreignKey = @ForeignKey(name = "fk_ticket_assigned_agent"))
    private User assignedAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_ticket_department"))
    private Department department;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketHistory> history = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    // Constructors
    public Ticket() {
        super();
        this.lastActivityAt = LocalDateTime.now();
    }

    public Ticket(String title, String description, Company company) {
        this();
        this.title = title;
        this.description = description;
        this.company = company;
        this.generateTicketNumber();
    }

    // Business Methods

    /**
     * Bilet numarası oluşturur (TK-YYYYMMDD-XXXX formatında)
     */
    public void generateTicketNumber() {
        if (this.ticketNumber == null) {
            String dateStr = LocalDateTime.now().toString().substring(0, 10).replace("-", "");
            this.ticketNumber = "TK-" + dateStr + "-" + String.format("%04d",
                    (int) (Math.random() * 9999) + 1);
        }
    }

    /**
     * Bilet durumu güncelleme
     */
    public void updateStatus(TicketStatus newStatus, User updatedBy) {
        TicketStatus oldStatus = this.status;
        this.status = newStatus;
        this.lastActivityAt = LocalDateTime.now();

        // Durum değişikliğine göre özel işlemler
        switch (newStatus) {
            case OPEN:
                if (oldStatus == TicketStatus.NEW && this.firstResponseAt == null) {
                    this.firstResponseAt = LocalDateTime.now();
                }
                break;
            case RESOLVED:
                if (this.resolvedAt == null) {
                    this.resolvedAt = LocalDateTime.now();
                }
                break;
            case CLOSED:
                if (this.closedAt == null) {
                    this.closedAt = LocalDateTime.now();
                }
                if (this.resolvedAt == null) {
                    this.resolvedAt = LocalDateTime.now();
                }
                break;
        }
    }

    /**
     * Agent atama
     */
    public void assignToAgent(User agent) {
        this.assignedAgent = agent;
        this.department = agent.getDepartment();
        this.lastActivityAt = LocalDateTime.now();

        // Eğer bilet yeni durumda ise açık duruma geçir
        if (this.status == TicketStatus.NEW) {
            this.status = TicketStatus.OPEN;
        }
    }

    /**
     * SLA deadline hesaplama
     */
    public void calculateSlaDeadline(int responseTimeHours) {
        this.slaDeadline = this.getCreatedAt().plusHours(responseTimeHours);
    }

    /**
     * SLA ihlali kontrolü
     */
    public boolean isSlaBreached() {
        if (slaDeadline == null)
            return false;

        LocalDateTime checkTime = this.firstResponseAt != null ? this.firstResponseAt : LocalDateTime.now();

        return checkTime.isAfter(slaDeadline);
    }

    /**
     * Biletde aktivite güncelleme
     */
    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * Son mesaj zamanını güncelle
     */
    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * Okunmamış mesaj sayısını artır
     */
    public void incrementUnreadMessageCount() {
        this.unreadMessageCount = (this.unreadMessageCount == null ? 0 : this.unreadMessageCount) + 1;
    }

    /**
     * Okunmamış mesaj sayısını sıfırla
     */
    public void resetUnreadMessageCount() {
        this.unreadMessageCount = 0;
    }

    /**
     * Chat'i aktif/pasif yap
     */
    public void enableChat(Boolean enabled) {
        this.chatEnabled = enabled;
    }

    /**
     * Öncelik artırma (escalation)
     */
    public void escalate() {
        this.escalationLevel++;

        // Önceliği artır
        switch (this.priority) {
            case LOW:
                this.priority = TicketPriority.NORMAL;
                break;
            case NORMAL:
                this.priority = TicketPriority.HIGH;
                break;
            case HIGH:
                this.priority = TicketPriority.URGENT;
                break;
            case URGENT:
                this.priority = TicketPriority.CRITICAL;
                break;
        }

        this.status = TicketStatus.ESCALATED;
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * Müşteri memnuniyet puanı ekleme
     */
    public void addSatisfactionRating(int rating, String feedback) {
        this.customerSatisfactionRating = rating;
        this.customerSatisfactionFeedback = feedback;
    }

    /**
     * Bilet yaşı (gün cinsinden)
     */
    public long getAgeInDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(
                this.getCreatedAt().toLocalDate(),
                LocalDateTime.now().toLocalDate());
    }

    /**
     * Çözüm süresi (saat cinsinden)
     */
    public Long getResolutionTimeInHours() {
        if (resolvedAt == null)
            return null;
        return java.time.temporal.ChronoUnit.HOURS.between(getCreatedAt(), resolvedAt);
    }

    /**
     * İlk yanıt süresi (saat cinsinden)
     */
    public Long getFirstResponseTimeInHours() {
        if (firstResponseAt == null)
            return null;
        return java.time.temporal.ChronoUnit.HOURS.between(getCreatedAt(), firstResponseAt);
    }

    // Status check methods
    public boolean isNew() {
        return status == TicketStatus.NEW;
    }

    public boolean isOpen() {
        return status == TicketStatus.OPEN;
    }

    public boolean isInProgress() {
        return status == TicketStatus.IN_PROGRESS;
    }

    public boolean isPending() {
        return status == TicketStatus.PENDING;
    }

    public boolean isResolved() {
        return status == TicketStatus.RESOLVED;
    }

    public boolean isClosed() {
        return status == TicketStatus.CLOSED;
    }

    public boolean isEscalated() {
        return status == TicketStatus.ESCALATED;
    }

    public boolean isActive() {
        return status.isActive();
    }

    public boolean isCompleted() {
        return status.isClosed();
    }

    // Priority check methods
    public boolean isHighPriority() {
        return priority.isHighPriority();
    }

    public boolean isUrgent() {
        return priority == TicketPriority.URGENT || priority == TicketPriority.CRITICAL;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public TicketSource getSource() {
        return source;
    }

    public void setSource(TicketSource source) {
        this.source = source;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public LocalDateTime getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(LocalDateTime slaDeadline) {
        this.slaDeadline = slaDeadline;
    }

    public LocalDateTime getFirstResponseAt() {
        return firstResponseAt;
    }

    public void setFirstResponseAt(LocalDateTime firstResponseAt) {
        this.firstResponseAt = firstResponseAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public Integer getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Integer getActualHours() {
        return actualHours;
    }

    public void setActualHours(Integer actualHours) {
        this.actualHours = actualHours;
    }

    public String getResolutionSummary() {
        return resolutionSummary;
    }

    public void setResolutionSummary(String resolutionSummary) {
        this.resolutionSummary = resolutionSummary;
    }

    public Integer getCustomerSatisfactionRating() {
        return customerSatisfactionRating;
    }

    public void setCustomerSatisfactionRating(Integer customerSatisfactionRating) {
        this.customerSatisfactionRating = customerSatisfactionRating;
    }

    public String getCustomerSatisfactionFeedback() {
        return customerSatisfactionFeedback;
    }

    public void setCustomerSatisfactionFeedback(String customerSatisfactionFeedback) {
        this.customerSatisfactionFeedback = customerSatisfactionFeedback;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Boolean getIsInternal() {
        return isInternal;
    }

    public void setIsInternal(Boolean isInternal) {
        this.isInternal = isInternal;
    }

    public Integer getEscalationLevel() {
        return escalationLevel;
    }

    public void setEscalationLevel(Integer escalationLevel) {
        this.escalationLevel = escalationLevel;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public User getCreatorUser() {
        return creatorUser;
    }

    public void setCreatorUser(User creatorUser) {
        this.creatorUser = creatorUser;
    }

    public User getAssignedAgent() {
        return assignedAgent;
    }

    public void setAssignedAgent(User assignedAgent) {
        this.assignedAgent = assignedAgent;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<TicketComment> getComments() {
        return comments;
    }

    public void setComments(List<TicketComment> comments) {
        this.comments = comments;
    }

    public List<TicketAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<TicketAttachment> attachments) {
        this.attachments = attachments;
    }

    public List<TicketHistory> getHistory() {
        return history;
    }

    public void setHistory(List<TicketHistory> history) {
        this.history = history;
    }

    public List<ChatMessage> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public Integer getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(Integer unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public Boolean getChatEnabled() {
        return chatEnabled;
    }

    public void setChatEnabled(Boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }

    @Override
    public String toString() {
        return String.format("Ticket{id=%d, ticketNumber='%s', title='%s', status=%s, priority=%s}",
                getId(), ticketNumber, title, status, priority);
    }
}