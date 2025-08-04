package com.example.smartdeskbackend.dto.request.user;

import com.example.smartdeskbackend.enums.UserRole;
import com.example.smartdeskbackend.enums.UserStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;
import java.util.List;

/**
 * User search request DTO with advanced filtering capabilities
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchRequest {
    
    /**
     * Search term for name and email
     */
    private String searchTerm;
    
    /**
     * Filter by user roles
     */
    private List<UserRole> roles;
    
    /**
     * Filter by user statuses
     */
    private List<UserStatus> statuses;
    
    /**
     * Filter by company ID
     */
    private Long companyId;
    
    /**
     * Filter by department ID
     */
    private Long departmentId;
    
    /**
     * Filter users created after this date
     */
    private LocalDate createdAfter;
    
    /**
     * Filter users created before this date
     */
    private LocalDate createdBefore;
    
    /**
     * Sort field (firstName, lastName, email, createdAt, lastLogin)
     */
    @Builder.Default
    private String sortBy = "createdAt";
    
    /**
     * Sort direction (asc, desc)
     */
    @Builder.Default
    private String sortDirection = "desc";
    
    /**
     * Page number (0-based)
     */
    @Min(0)
    @Builder.Default
    private Integer page = 0;
    
    /**
     * Page size
     */
    @Min(1)
    @Max(100)
    @Builder.Default
    private Integer size = 20;
    
    /**
     * Include only active users
     */
    @Builder.Default
    private Boolean activeOnly = false;
    
    /**
     * Email verification status filter
     */
    private Boolean emailVerified;
    
    /**
     * Include locked accounts
     */
    @Builder.Default
    private Boolean includeLocked = true;
}