package com.example.smartdeskbackend.entity;

import com.example.smartdeskbackend.entity.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customer_email", columnList = "email"),
        @Index(name = "idx_customer_company", columnList = "company_id"),
        @Index(name = "idx_customer_segment", columnList = "segment")
})
public class Customer extends AuditableEntity {

    @NotBlank(message = "Email is required")
    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "segment", length = 20)
    private com.example.smartdeskbackend.enums.CustomerSegment segment = com.example.smartdeskbackend.enums.CustomerSegment.STANDARD;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_contact")
    private java.time.LocalDateTime lastContact;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, foreignKey = @ForeignKey(name = "fk_customer_company"))
    private Company company;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    // Constructors
    public Customer() {
        super();
    }

    public Customer(String email, String firstName, String lastName, Company company) {
        this();
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.company = company;
    }

    // Business Methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return isActive != null && isActive;
    }

    public boolean isVipCustomer() {
        return segment == com.example.smartdeskbackend.enums.CustomerSegment.VIP;
    }

    public void updateLastContact() {
        this.lastContact = java.time.LocalDateTime.now();
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public com.example.smartdeskbackend.enums.CustomerSegment getSegment() { return segment; }
    public void setSegment(com.example.smartdeskbackend.enums.CustomerSegment segment) { this.segment = segment; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public java.time.LocalDateTime getLastContact() { return lastContact; }
    public void setLastContact(java.time.LocalDateTime lastContact) { this.lastContact = lastContact; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }

    @Override
    public String toString() {
        return String.format("Customer{id=%d, email='%s', fullName='%s', segment=%s}",
                getId(), email, getFullName(), segment);
    }
}