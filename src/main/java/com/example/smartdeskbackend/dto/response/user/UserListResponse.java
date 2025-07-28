
// UserListResponse.java
package com.example.smartdeskbackend.dto.response.user;

import java.time.LocalDateTime;

public class UserListResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private String roleDisplayName;
    private String status;
    private String statusDisplayName;
    private String avatarUrl;
    private LocalDateTime lastLogin;
    private Boolean accountLocked;
    private String companyName;
    private String departmentName;
    private LocalDateTime createdAt;

    // Constructors
    public UserListResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getRoleDisplayName() { return roleDisplayName; }
    public void setRoleDisplayName(String roleDisplayName) { this.roleDisplayName = roleDisplayName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusDisplayName() { return statusDisplayName; }
    public void setStatusDisplayName(String statusDisplayName) { this.statusDisplayName = statusDisplayName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}