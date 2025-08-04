package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.dto.request.user.CreateUserRequest;
import com.example.smartdeskbackend.dto.request.user.UpdateUserRequest;
import com.example.smartdeskbackend.dto.request.user.ChangePasswordRequest;
import com.example.smartdeskbackend.dto.response.user.UserListResponse;
import com.example.smartdeskbackend.dto.response.user.UserProfileResponse;
import com.example.smartdeskbackend.entity.Company;
import com.example.smartdeskbackend.entity.Department;
import com.example.smartdeskbackend.entity.User;
import com.example.smartdeskbackend.enums.UserRole;
import com.example.smartdeskbackend.enums.UserStatus;
import com.example.smartdeskbackend.exception.BusinessLogicException;
import com.example.smartdeskbackend.exception.ResourceNotFoundException;
import com.example.smartdeskbackend.exception.UserAlreadyExistsException;
import com.example.smartdeskbackend.exception.AuthenticationException;
import com.example.smartdeskbackend.repository.CompanyRepository;
import com.example.smartdeskbackend.repository.DepartmentRepository;
import com.example.smartdeskbackend.repository.UserRepository;
import com.example.smartdeskbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User service implementation
 */
@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserById(Long id) {
        log.debug("Getting user by id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return mapToProfileResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return mapToProfileResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserListResponse> getUsersByCompany(Long companyId, Pageable pageable) {
        log.debug("Getting users by company: {}", companyId);

        // Company existence kontrolü
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Company not found with id: " + companyId);
        }

        // String parametreli metodu kullan
        Page<User> users = userRepository.findUsersWithStringFilters(
                companyId,
                null, // role as String
                null, // status as String
                null, // departmentId
                pageable);
        return users.map(this::mapToListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserListResponse> searchUsers(Long companyId, String searchTerm, UserRole role,
            UserStatus status, Long departmentId, Pageable pageable) {
        log.debug("Searching users in company: {} with term: {}", companyId, searchTerm);

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            return userRepository.searchUsers(companyId, searchTerm.trim(), pageable)
                    .map(this::mapToListResponse);
        } else {
            // String parametreli metodu kullan
            return userRepository.findUsersWithStringFilters(
                    companyId,
                    role != null ? role.getCode() : null, // String versiyonunu kullan
                    status != null ? status.getCode() : null, // String versiyonunu kullan
                    departmentId,
                    pageable).map(this::mapToListResponse);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserListResponse> getAgentsByDepartment(Long departmentId) {
        log.debug("Getting agents by department: {}", departmentId);

        List<User> agents = userRepository.findByDepartmentIdAndRole(departmentId, UserRole.AGENT);
        return agents.stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserListResponse> getAvailableAgents(Long departmentId, int limit) {
        log.debug("Getting available agents for department: {}", departmentId);

        List<User> agents = userRepository.findLeastBusyAgents(departmentId, limit);
        return agents.stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserProfileResponse createUser(CreateUserRequest request) {
        log.info("Creating new user: {}", request.getEmail());

        // Email uniqueness kontrolü
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        // Company kontrolü
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Company not found with id: " + request.getCompanyId()));

        // Company user limit kontrolü
        if (!company.canAddMoreUsers()) {
            throw new BusinessLogicException("Company user limit exceeded");
        }

        // Department kontrolü (eğer belirtilmişse)
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with id: " + request.getDepartmentId()));

            // Department'ın aynı company'ye ait olduğunu kontrol et
            if (!department.getCompany().getId().equals(company.getId())) {
                throw new BusinessLogicException("Department does not belong to the specified company");
            }
        }

        // Yeni kullanıcı oluştur
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE); // Yönetici tarafından oluşturulan kullanıcılar direkt aktif
        user.setCompany(company);
        user.setDepartment(department);
        user.setEmailVerified(true); // Yönetici tarafından oluşturulan kullanıcılar doğrulanmış sayılır

        user = userRepository.save(user);

        log.info("User created successfully with id: {}", user.getId());
        return mapToProfileResponse(user);
    }

    @Override
    public UserProfileResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Email değişikliği kontrolü
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // Diğer alanları güncelle
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        // Company güncelleme
        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Company not found with id: " + request.getCompanyId()));
            user.setCompany(company);
            
            // Şirket değiştiğinde department'ı temizle
            user.setDepartment(null);
        }

        // Department güncelleme
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with id: " + request.getDepartmentId()));

            // Department'ın user'ın company'sine ait olduğunu kontrol et
            if (!department.getCompany().getId().equals(user.getCompany().getId())) {
                throw new BusinessLogicException("Department does not belong to user's company");
            }

            user.setDepartment(department);
        }

        user = userRepository.save(user);

        log.info("User updated successfully: {}", id);
        return mapToProfileResponse(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Mevcut şifreyi kontrol et
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        // Yeni şifreyi kaydet
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        log.info("Resetting password for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);

        userRepository.save(user);

        log.info("Password reset successfully for user: {}", userId);
    }

    @Override
    public void activateUser(Long userId) {
        log.info("Activating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BusinessLogicException("User is already active");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("User activated successfully: {}", userId);
    }

    @Override
    public void deactivateUser(Long userId) {
        log.info("Deactivating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BusinessLogicException("User is already inactive");
        }

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);

        log.info("User deactivated successfully: {}", userId);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Soft delete
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);

        log.info("User deleted successfully: {}", userId);
    }

    @Override
    public void unlockUser(Long userId) {
        log.info("Unlocking user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!user.isAccountLocked()) {
            throw new BusinessLogicException("User account is not locked");
        }

        user.unlockAccount();
        userRepository.save(user);

        log.info("User unlocked successfully: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUserCountByCompany(Long companyId) {
        log.debug("Getting user count for company: {}", companyId);
        return userRepository.countByCompanyId(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getUserStatsByCompany(Long companyId) {
        return userRepository.countUsersByRoleAndCompany(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserListResponse> getMostActiveUsers(Long companyId, int limit) {
        List<User> activeUsers = userRepository.findMostActiveUsers(companyId, limit);
        return activeUsers.stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getAgentPerformanceStats(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.getAgentPerformanceStats(companyId, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        // TODO: Implement proper authentication context retrieval
        // For now, return a mock user for development
        log.warn("getCurrentUser() called - returning mock user for development");
        return userRepository.findById(1L).orElse(null);
    }

    /**
     * User entity'sini UserProfileResponse'a map eder
     */
    private UserProfileResponse mapToProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole().getCode());
        response.setRoleDisplayName(user.getRole().getDisplayName());
        response.setStatus(user.getStatus().getCode());
        response.setStatusDisplayName(user.getStatus().getDisplayName());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setEmailVerified(user.getEmailVerified());
        response.setLastLogin(user.getLastLogin());
        response.setLoginAttempts(user.getLoginAttempts());
        response.setAccountLocked(user.isAccountLocked());
        response.setLockedUntil(user.getLockedUntil());

        // Company bilgileri
        if (user.getCompany() != null) {
            response.setCompanyId(user.getCompany().getId());
            response.setCompanyName(user.getCompany().getName());
        }

        // Department bilgileri
        if (user.getDepartment() != null) {
            response.setDepartmentId(user.getDepartment().getId());
            response.setDepartmentName(user.getDepartment().getName());
        }

        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }

    /**
     * User entity'sini UserListResponse'a map eder
     */
    private UserListResponse mapToListResponse(User user) {
        UserListResponse response = new UserListResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole().getCode());
        response.setRoleDisplayName(user.getRole().getDisplayName());
        response.setStatus(user.getStatus().getCode());
        response.setStatusDisplayName(user.getStatus().getDisplayName());
        response.setPhone(user.getPhone());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setLastLogin(user.getLastLogin());
        response.setAccountLocked(user.isAccountLocked());
        response.setEmailVerified(user.getEmailVerified());
        response.setLoginAttempts(user.getLoginAttempts());

        // Company bilgileri
        if (user.getCompany() != null) {
            response.setCompanyId(user.getCompany().getId());
            response.setCompanyName(user.getCompany().getName());
        }

        // Department bilgileri
        if (user.getDepartment() != null) {
            response.setDepartmentId(user.getDepartment().getId());
            response.setDepartmentName(user.getDepartment().getName());
        }

        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }

    @Override
    public User findByEmail(String email) {
        log.info("Finding user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException("User not found with email: " + email));
    }
}