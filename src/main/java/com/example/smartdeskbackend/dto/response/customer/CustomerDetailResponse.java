package com.example.smartdeskbackend.dto.response.customer;

import java.time.LocalDateTime;

/**
 * Customer detail response DTO
 */
public class CustomerDetailResponse {

    private Long id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String fullName;
    private String segment;
    private String segmentDisplayName;
    private String companyName;
    private String address;
    private String notes;
    private Boolean isActive;
    private LocalDateTime lastContact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Company bilgileri
    private Long companyId;
    private String companyDisplayName;

    // İstatistikler
    private Integer totalTickets;
    private Integer activeTickets;

    // Constructors
    public CustomerDetailResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getSegment() { return segment; }
    public void setSegment(String segment) { this.segment = segment; }

    public String getSegmentDisplayName() { return segmentDisplayName; }
    public void setSegmentDisplayName(String segmentDisplayName) { this.segmentDisplayName = segmentDisplayName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getLastContact() { return lastContact; }
    public void setLastContact(LocalDateTime lastContact) { this.lastContact = lastContact; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getCompanyDisplayName() { return companyDisplayName; }
    public void setCompanyDisplayName(String companyDisplayName) { this.companyDisplayName = companyDisplayName; }

    public Integer getTotalTickets() { return totalTickets; }
    public void setTotalTickets(Integer totalTickets) { this.totalTickets = totalTickets; }

    public Integer getActiveTickets() { return activeTickets; }
    public void setActiveTickets(Integer activeTickets) { this.activeTickets = activeTickets; }
}
