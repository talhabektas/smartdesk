// User Service Interface
package com.example.smartdeskbackend.service;

import com.example.smartdeskbackend.dto.request.user.CreateUserRequest;
import com.example.smartdeskbackend.dto.request.user.UpdateUserRequest;
import com.example.smartdeskbackend.dto.request.user.ChangePasswordRequest;
import com.example.smartdeskbackend.dto.response.user.UserListResponse;
import com.example.smartdeskbackend.dto.response.user.UserProfileResponse;
import com.example.smartdeskbackend.enums.UserRole;
import com.example.smartdeskbackend.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User service interface
 */
public interface UserService {

    /**
     * ID ile kullanıcı getir
     */
    UserProfileResponse getUserById(Long id);

    /**
     * Email ile kullanıcı getir
     */
    UserProfileResponse getUserByEmail(String email);

    /**
     * Şirketteki kullanıcıları getir
     */
    Page<UserListResponse> getUsersByCompany(Long companyId, Pageable pageable);

    /**
     * Kullanıcı arama ve filtreleme
     */
    Page<UserListResponse> searchUsers(Long companyId, String searchTerm, UserRole role,
                                       UserStatus status, Long departmentId, Pageable pageable);

    /**
     * Departmandaki agent'ları getir
     */
    List<UserListResponse> getAgentsByDepartment(Long departmentId);

    /**
     * Müsait agent'ları getir (load balancing için)
     */
    List<UserListResponse> getAvailableAgents(Long departmentId, int limit);

    /**
     * Yeni kullanıcı oluştur
     */
    UserProfileResponse createUser(CreateUserRequest request);

    /**
     * Kullanıcı güncelle
     */
    UserProfileResponse updateUser(Long id, UpdateUserRequest request);

    /**
     * Kullanıcı şifre değiştir
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * Kullanıcı şifre sıfırla (admin tarafından)
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * Kullanıcı aktif et
     */
    void activateUser(Long userId);

    /**
     * Kullanıcı deaktif et
     */
    void deactivateUser(Long userId);

    /**
     * Kullanıcı sil (soft delete)
     */
    void deleteUser(Long userId);

    /**
     * Kullanıcı hesap kilidini aç
     */
    void unlockUser(Long userId);

    /**
     * Şirketteki kullanıcı sayısını getir
     */
    long getUserCountByCompany(Long companyId);

    /**
     * Şirketteki kullanıcı role istatistikleri
     */
    List<Object[]> getUserStatsByCompany(Long companyId);

    /**
     * En aktif kullanıcıları getir
     */
    List<UserListResponse> getMostActiveUsers(Long companyId, int limit);

    /**
     * Agent performans istatistikleri
     */
    List<Object[]> getAgentPerformanceStats(Long companyId, LocalDateTime startDate, LocalDateTime endDate);
}