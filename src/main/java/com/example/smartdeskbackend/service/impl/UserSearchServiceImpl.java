package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.dto.request.user.UserSearchRequest;
import com.example.smartdeskbackend.dto.response.common.PageResponse;
import com.example.smartdeskbackend.dto.response.user.UserListResponse;
import com.example.smartdeskbackend.entity.User;
import com.example.smartdeskbackend.enums.UserRole;
import com.example.smartdeskbackend.enums.UserStatus;
import com.example.smartdeskbackend.repository.UserRepository;
import com.example.smartdeskbackend.security.SecurityUtils;
import com.example.smartdeskbackend.service.UserSearchService;
import com.example.smartdeskbackend.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of UserSearchService for advanced user search operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserSearchServiceImpl implements UserSearchService {

    private final UserRepository userRepository;

    @Override
    public PageResponse<UserListResponse> searchUsers(UserSearchRequest searchRequest) {
        log.debug("Searching users with criteria: {}", searchRequest);

        // Build specification
        Specification<User> spec = buildSpecification(searchRequest);

        // Build pageable
        Pageable pageable = buildPageable(searchRequest);

        // Execute search
        Page<User> userPage = userRepository.findAll(spec, pageable);

        // Convert to response
        List<UserListResponse> userResponses = userPage.getContent()
                .stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());

        return PageResponse.of(userPage, userResponses);
    }

    @Override
    public PageResponse<UserListResponse> searchUsersByCompany(Long companyId, UserSearchRequest searchRequest) {
        log.debug("Searching users for company {} with criteria: {}", companyId, searchRequest);

        // Override company filter
        searchRequest.setCompanyId(companyId);

        return searchUsers(searchRequest);
    }

    @Override
    public PageResponse<UserListResponse> searchUsersByDepartment(Long departmentId, UserSearchRequest searchRequest) {
        log.debug("Searching users for department {} with criteria: {}", departmentId, searchRequest);

        // Override department filter
        searchRequest.setDepartmentId(departmentId);

        return searchUsers(searchRequest);
    }

    @Override
    public Long countUsers(UserSearchRequest searchRequest) {
        log.debug("Counting users with criteria: {}", searchRequest);

        Specification<User> spec = buildSpecification(searchRequest);
        return userRepository.count(spec);
    }

    /**
     * Build JPA Specification from search request
     */
    private Specification<User> buildSpecification(UserSearchRequest searchRequest) {
        Specification<User> spec = Specification.where(null);

        // Search term (name and email)
        if (searchRequest.getSearchTerm() != null && !searchRequest.getSearchTerm().trim().isEmpty()) {
            spec = spec.and(UserSpecification.searchByTerm(searchRequest.getSearchTerm()));
        }

        // Company filter
        if (searchRequest.getCompanyId() != null) {
            spec = spec.and(UserSpecification.hasCompanyId(searchRequest.getCompanyId()));
        }

        // Department filter
        if (searchRequest.getDepartmentId() != null) {
            spec = spec.and(UserSpecification.hasDepartmentId(searchRequest.getDepartmentId()));
        }

        // Role filter
        if (searchRequest.getRoles() != null && !searchRequest.getRoles().isEmpty()) {
            spec = spec.and(UserSpecification.hasRoleIn(searchRequest.getRoles()));
        }

        // Status filter
        if (searchRequest.getStatuses() != null && !searchRequest.getStatuses().isEmpty()) {
            spec = spec.and(UserSpecification.hasStatusIn(searchRequest.getStatuses()));
        }

        // Active only filter
        if (Boolean.TRUE.equals(searchRequest.getActiveOnly())) {
            spec = spec.and(UserSpecification.hasStatus(UserStatus.ACTIVE));
        }

        // Email verified filter
        if (searchRequest.getEmailVerified() != null) {
            spec = spec.and(UserSpecification.isEmailVerified(searchRequest.getEmailVerified()));
        }

        // Date range filter
        if (searchRequest.getCreatedAfter() != null) {
            spec = spec.and(UserSpecification.createdAfter(searchRequest.getCreatedAfter().atStartOfDay()));
        }

        if (searchRequest.getCreatedBefore() != null) {
            spec = spec.and(UserSpecification.createdBefore(searchRequest.getCreatedBefore().atTime(23, 59, 59)));
        }

        // Include locked filter
        if (Boolean.FALSE.equals(searchRequest.getIncludeLocked())) {
            spec = spec.and(UserSpecification.isNotLocked());
        }

        return spec;
    }

    /**
     * Build Pageable from search request
     */
    private Pageable buildPageable(UserSearchRequest searchRequest) {
        String sortBy = searchRequest.getSortBy();
        String sortDirection = searchRequest.getSortDirection();

        // Validate sort field
        if (!isValidSortField(sortBy)) {
            sortBy = "createdAt";
        }

        // Build sort
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, sortBy);

        return PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                sort);
    }

    /**
     * Validate sort field
     */
    private boolean isValidSortField(String field) {
        return List.of("id", "firstName", "lastName", "email", "role", "status",
                "createdAt", "updatedAt", "lastLoginAt").contains(field);
    }

    /**
     * Convert User entity to UserListResponse
     */
    private UserListResponse convertToListResponse(User user) {
        return UserListResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .role(user.getRole().getCode())
                .roleDisplayName(user.getRole().getDisplayName())
                .status(user.getStatus().getCode())
                .statusDisplayName(user.getStatus().getDisplayName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .emailVerified(user.getEmailVerified())
                .accountLocked(user.isAccountLocked())
                .loginAttempts(user.getLoginAttempts())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLogin())
                .lastLogin(user.getLastLogin())
                .permissions(buildUserPermissions(user))
                .build();
    }

    /**
     * Build user permissions based on current user's role and target user
     */
    private UserListResponse.UserPermissions buildUserPermissions(User targetUser) {
        String currentUserEmail = SecurityUtils.getCurrentUserEmail();
        boolean isCurrentUser = currentUserEmail != null && currentUserEmail.equals(targetUser.getEmail());

        // Get current user role and company
        String currentUserRoleString = SecurityUtils.getCurrentUserRole();
        UserRole currentUserRole = currentUserRoleString != null ? UserRole.fromString(currentUserRoleString) : null;
        Long currentUserCompanyId = SecurityUtils.getCurrentUserCompanyId();

        boolean canEdit = canEditUser(currentUserRole, currentUserCompanyId, targetUser, isCurrentUser);
        boolean canDelete = canDeleteUser(currentUserRole, currentUserCompanyId, targetUser, isCurrentUser);
        boolean canChangeRole = canChangeUserRole(currentUserRole, currentUserCompanyId, targetUser);
        boolean canActivateDeactivate = canActivateDeactivateUser(currentUserRole, currentUserCompanyId, targetUser,
                isCurrentUser);
        boolean canResetPassword = canResetUserPassword(currentUserRole, currentUserCompanyId, targetUser,
                isCurrentUser);
        boolean canUnlock = canUnlockUser(currentUserRole, currentUserCompanyId, targetUser);

        return UserListResponse.UserPermissions.builder()
                .canEdit(canEdit)
                .canDelete(canDelete)
                .canChangeRole(canChangeRole)
                .canActivateDeactivate(canActivateDeactivate)
                .canResetPassword(canResetPassword)
                .canUnlock(canUnlock)
                .build();
    }

    /**
     * Permission check methods
     */
    private boolean canEditUser(UserRole currentRole, Long currentCompanyId, User targetUser, boolean isCurrentUser) {
        if (isCurrentUser)
            return true; // Users can always edit themselves
        if (currentRole == UserRole.SUPER_ADMIN)
            return true;
        if (currentRole == UserRole.MANAGER && isSameCompany(currentCompanyId, targetUser)) {
            return targetUser.getRole() != UserRole.SUPER_ADMIN;
        }
        return false;
    }

    private boolean canDeleteUser(UserRole currentRole, Long currentCompanyId, User targetUser, boolean isCurrentUser) {
        if (isCurrentUser)
            return false; // Users cannot delete themselves
        if (currentRole == UserRole.SUPER_ADMIN)
            return true;
        if (currentRole == UserRole.MANAGER && isSameCompany(currentCompanyId, targetUser)) {
            return targetUser.getRole() != UserRole.SUPER_ADMIN && targetUser.getRole() != UserRole.MANAGER;
        }
        return false;
    }

    private boolean canChangeUserRole(UserRole currentRole, Long currentCompanyId, User targetUser) {
        if (currentRole == UserRole.SUPER_ADMIN)
            return true;
        if (currentRole == UserRole.MANAGER && isSameCompany(currentCompanyId, targetUser)) {
            return targetUser.getRole() != UserRole.SUPER_ADMIN;
        }
        return false;
    }

    private boolean canActivateDeactivateUser(UserRole currentRole, Long currentCompanyId, User targetUser,
            boolean isCurrentUser) {
        if (isCurrentUser)
            return false; // Users cannot deactivate themselves
        if (currentRole == UserRole.SUPER_ADMIN)
            return true;
        if (currentRole == UserRole.MANAGER && isSameCompany(currentCompanyId, targetUser)) {
            return targetUser.getRole() != UserRole.SUPER_ADMIN;
        }
        return false;
    }

    private boolean canResetUserPassword(UserRole currentRole, Long currentCompanyId, User targetUser,
            boolean isCurrentUser) {
        if (currentRole == UserRole.SUPER_ADMIN)
            return true;
        if (currentRole == UserRole.MANAGER && isSameCompany(currentCompanyId, targetUser)) {
            return targetUser.getRole() != UserRole.SUPER_ADMIN;
        }
        return false;
    }

    private boolean canUnlockUser(UserRole currentRole, Long currentCompanyId, User targetUser) {
        if (currentRole == UserRole.SUPER_ADMIN)
            return true;
        if (currentRole == UserRole.MANAGER && isSameCompany(currentCompanyId, targetUser)) {
            return targetUser.getRole() != UserRole.SUPER_ADMIN;
        }
        return false;
    }

    /**
     * Check if target user is in the same company as current user
     */
    private boolean isSameCompany(Long currentCompanyId, User targetUser) {
        return currentCompanyId != null &&
                targetUser.getCompany() != null &&
                currentCompanyId.equals(targetUser.getCompany().getId());
    }
}