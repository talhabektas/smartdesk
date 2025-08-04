package com.example.smartdeskbackend.dto.response.user;

import com.example.smartdeskbackend.enums.UserRole;
import com.example.smartdeskbackend.enums.UserStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * User list response DTO optimized for list views
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserListResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private String roleDisplayName; // Display name for role enum
    private String status;
    private String statusDisplayName; // Display name for status enum
    private String phone;
    private String avatarUrl; // User profile picture URL

    // Company information
    private Long companyId;
    private String companyName;

    // Department information
    private Long departmentId;
    private String departmentName;

    // Account status
    private Boolean emailVerified;
    private Boolean accountLocked;
    private Integer loginAttempts;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime lastLogin; // Alternative field name for compatibility

    // Permissions for UI (what actions can be performed on this user)
    private UserPermissions permissions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserPermissions {
        private Boolean canEdit;
        private Boolean canDelete;
        private Boolean canChangeRole;
        private Boolean canActivateDeactivate;
        private Boolean canResetPassword;
        private Boolean canUnlock;
    }
}