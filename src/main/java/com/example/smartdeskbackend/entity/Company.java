package com.example.smartdeskbackend.entity;

import com.example.smartdeskbackend.entity.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Şirket bilgilerini tutan entity sınıfı
 */
@Entity
@Table(name = "companies", indexes = {
        @Index(name = "idx_company_domain", columnList = "domain", unique = true),
        @Index(name = "idx_company_name", columnList = "name")
})
public class Company extends AuditableEntity {

    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name cannot exceed 200 characters")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 100, message = "Domain cannot exceed 100 characters")
    @Column(name = "domain", unique = true, length = 100)
    private String domain;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    @Column(name = "address", length = 500)
    private String address;

    @Size(max = 100, message = "Website cannot exceed 100 characters")
    @Column(name = "website", length = 100)
    private String website;

    @Column(name = "timezone", length = 50)
    private String timezone = "Europe/Istanbul";

    @Column(name = "plan_type", length = 20)
    private String planType = "BASIC";

    @Column(name = "max_users")
    private Integer maxUsers = 10;

    @Column(name = "max_tickets_per_month")
    private Integer maxTicketsPerMonth = 100;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Company settings as JSON
    @Column(name = "settings", columnDefinition = "TEXT")
    private String settings;

    // Relationships
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Department> departments = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Customer> customers = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    // Constructors
    public Company() {
        super();
    }

    public Company(String name, String domain) {
        this();
        this.name = name;
        this.domain = domain;
    }

    // Business Methods
    public boolean isActive() {
        return isActive != null && isActive;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public int getUserCount() {
        return users != null ? users.size() : 0;
    }

    public boolean canAddMoreUsers() {
        return maxUsers == null || getUserCount() < maxUsers;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public Integer getMaxUsers() { return maxUsers; }
    public void setMaxUsers(Integer maxUsers) { this.maxUsers = maxUsers; }

    public Integer getMaxTicketsPerMonth() { return maxTicketsPerMonth; }
    public void setMaxTicketsPerMonth(Integer maxTicketsPerMonth) { this.maxTicketsPerMonth = maxTicketsPerMonth; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getSettings() { return settings; }
    public void setSettings(String settings) { this.settings = settings; }

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }

    public List<Department> getDepartments() { return departments; }
    public void setDepartments(List<Department> departments) { this.departments = departments; }

    public List<Customer> getCustomers() { return customers; }
    public void setCustomers(List<Customer> customers) { this.customers = customers; }

    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }

    @Override
    public String toString() {
        return String.format("Company{id=%d, name='%s', domain='%s', isActive=%s}",
                getId(), name, domain, isActive);
    }
}