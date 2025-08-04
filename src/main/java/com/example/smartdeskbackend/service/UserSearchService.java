package com.example.smartdeskbackend.service;

import com.example.smartdeskbackend.dto.request.user.UserSearchRequest;
import com.example.smartdeskbackend.dto.response.common.PageResponse;
import com.example.smartdeskbackend.dto.response.user.UserListResponse;
import org.springframework.data.domain.Page;

/**
 * Service interface for advanced user search operations
 */
public interface UserSearchService {
    
    /**
     * Search users with advanced filtering and pagination
     *
     * @param searchRequest Search criteria
     * @return Paginated user list response
     */
    PageResponse<UserListResponse> searchUsers(UserSearchRequest searchRequest);
    
    /**
     * Get users for a specific company with filtering
     *
     * @param companyId Company ID
     * @param searchRequest Search criteria
     * @return Paginated user list response
     */
    PageResponse<UserListResponse> searchUsersByCompany(Long companyId, UserSearchRequest searchRequest);
    
    /**
     * Get users for a specific department with filtering
     *
     * @param departmentId Department ID
     * @param searchRequest Search criteria
     * @return Paginated user list response
     */
    PageResponse<UserListResponse> searchUsersByDepartment(Long departmentId, UserSearchRequest searchRequest);
    
    /**
     * Count users by search criteria
     *
     * @param searchRequest Search criteria
     * @return Total count of matching users
     */
    Long countUsers(UserSearchRequest searchRequest);
}