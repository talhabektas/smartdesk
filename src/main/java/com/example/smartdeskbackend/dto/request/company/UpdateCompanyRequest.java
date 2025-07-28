// UpdateCompanyRequest.java
package com.example.smartdeskbackend.dto.request.company;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

public class UpdateCompanyRequest {

    @Size(max = 200, message = "Company name cannot exceed 200 characters")
    private String name;

    @Size(max = 100, message = "Domain cannot exceed 100 characters")
    private String domain;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    @Size(max = 100, message = "Website cannot exceed 100 characters")
    private String website;

    private String logoUrl;

    private String timezone;

    private String planType;

    @Min(value = 1, message = "Max users must be at least 1")
    private Integer maxUsers;

    @Min(value = 1, message = "Max tickets per month must be at least 1")
    private Integer maxTicketsPerMonth;

    // Constructors
    public UpdateCompanyRequest() {}

    // Getters and Setters
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
}
