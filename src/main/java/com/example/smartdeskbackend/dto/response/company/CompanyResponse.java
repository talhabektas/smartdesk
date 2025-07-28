// CompanyResponse.java
package com.example.smartdeskbackend.dto.response.company;

import java.time.LocalDateTime;

public class CompanyResponse {

    private Long id;
    private String name;
    private String domain;
    private String phone;
    private String address;
    private String website;
    private String logoUrl;
    private String timezone;
    private String planType;
    private Integer maxUsers;
    private Integer maxTicketsPerMonth;
    private Boolean isActive;
    private Integer userCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CompanyResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

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

    public Integer getUserCount() { return userCount; }
    public void setUserCount(Integer userCount) { this.userCount = userCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}