package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.User;
import com.example.smartdeskbackend.enums.UserRole;
import com.example.smartdeskbackend.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User entity için repository interface
 * JPA ve custom query metodları
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    // ============ Basic Finders ============

    /**
     * Email ile kullanıcı bulma
     */
    Optional<User> findByEmail(String email);

    /**
     * Aktif kullanıcıyı email ile bulma
     */
    @Query(value = "SELECT * FROM users u WHERE u.email = :email AND u.status != 'DELETED'",
            nativeQuery = true)
    Optional<User> findByEmailAndIsActiveTrue(@Param("email") String email);

    /**
     * Email varlığı kontrolü
     */
    boolean existsByEmail(String email);

    /**
     * Password reset token ile kullanıcı bulma
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Email verification token ile kullanıcı bulma
     */
    Optional<User> findByEmailVerificationToken(String token);

    // ============ Company-based Queries ============

    /**
     * Şirkete ait kullanıcıları bulma
     */
    List<User> findByCompanyId(Long companyId);

    /**
     * Şirkete ait belirli statüdeki kullanıcıları bulma
     */
    List<User> findByCompanyIdAndStatus(Long companyId, UserStatus status);

    /**
     * Şirkete ait kullanıcı sayısı
     */
    @Query(value = "SELECT COUNT(*) FROM users WHERE company_id = :companyId AND status != 'DELETED'",
            nativeQuery = true)
    long countByCompanyId(@Param("companyId") Long companyId);

    // ============ Role-based Queries ============

    /**
     * Role göre kullanıcıları bulma
     */
    List<User> findByRole(UserRole role);

    /**
     * Şirket ve role göre kullanıcıları bulma
     */
    List<User> findByCompanyIdAndRole(Long companyId, UserRole role);

    /**
     * Departman ve role göre kullanıcıları bulma
     */
    @Query(value = "SELECT * FROM users u WHERE u.department_id = :departmentId " +
            "AND u.role = :role AND u.status = 'ACTIVE'",
            nativeQuery = true)
    List<User> findByDepartmentIdAndRole(@Param("departmentId") Long departmentId,
                                         @Param("role") String role);

    // ============ Agent-specific Queries ============

    /**
     * Şirketteki aktif agent'ları bulma
     */
    @Query(value = "SELECT * FROM users u WHERE u.company_id = :companyId " +
            "AND u.role IN ('AGENT', 'MANAGER') AND u.status = 'ACTIVE'",
            nativeQuery = true)
    List<User> findActiveAgentsByCompanyId(@Param("companyId") Long companyId);

    /**
     * Departmandaki agent'ları bulma
     */
    @Query(value = "SELECT * FROM users u WHERE u.department_id = :departmentId " +
            "AND u.role = 'AGENT' AND u.status = 'ACTIVE'",
            nativeQuery = true)
    List<User> findAgentsByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * En az ticket'a sahip agent'ı bulma (load balancing için)
     */
    @Query(value = "SELECT u.* FROM users u " +
            "LEFT JOIN tickets t ON u.id = t.assigned_agent_id " +
            "WHERE u.department_id = :departmentId AND u.role = 'AGENT' " +
            "AND u.status = 'ACTIVE' " +
            "GROUP BY u.id " +
            "ORDER BY COUNT(t.id) ASC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<User> findLeastBusyAgents(@Param("departmentId") Long departmentId,
                                   @Param("limit") int limit);

    // ============ Search and Filter Queries ============

    /**
     * İsim veya email ile arama
     */
    @Query(value = "SELECT * FROM users u WHERE u.company_id = :companyId " +
            "AND (LOWER(u.first_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.last_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND u.status != 'DELETED'",
            countQuery = "SELECT COUNT(*) FROM users u WHERE u.company_id = :companyId " +
                    "AND (LOWER(u.first_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                    "OR LOWER(u.last_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                    "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                    "AND u.status != 'DELETED'",
            nativeQuery = true)
    Page<User> searchUsers(@Param("companyId") Long companyId,
                           @Param("searchTerm") String searchTerm,
                           Pageable pageable);

    /**
     * Multiple criteria ile kullanıcı arama
     */
    @Query(value = "SELECT * FROM users u WHERE u.company_id = :companyId " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:status IS NULL OR u.status = :status) " +
            "AND (:departmentId IS NULL OR u.department_id = :departmentId) " +
            "AND u.status != 'DELETED'",
            countQuery = "SELECT COUNT(*) FROM users u WHERE u.company_id = :companyId " +
                    "AND (:role IS NULL OR u.role = :role) " +
                    "AND (:status IS NULL OR u.status = :status) " +
                    "AND (:departmentId IS NULL OR u.department_id = :departmentId) " +
                    "AND u.status != 'DELETED'",
            nativeQuery = true)
    Page<User> findUsersWithFilters(@Param("companyId") Long companyId,
                                    @Param("role") String role,
                                    @Param("status") String status,
                                    @Param("departmentId") Long departmentId,
                                    Pageable pageable);

    // ============ Statistics Queries ============

    /**
     * Şirketteki role göre kullanıcı sayıları
     */
    @Query(value = "SELECT u.role, COUNT(*) FROM users u " +
            "WHERE u.company_id = :companyId AND u.status != 'DELETED' " +
            "GROUP BY u.role",
            nativeQuery = true)
    List<Object[]> countUsersByRoleAndCompany(@Param("companyId") Long companyId);

    /**
     * Son login tarihine göre aktif kullanıcı sayısı
     */
    @Query(value = "SELECT COUNT(*) FROM users u WHERE u.company_id = :companyId " +
            "AND u.last_login >= :since AND u.status = 'ACTIVE'",
            nativeQuery = true)
    long countActiveUsersSince(@Param("companyId") Long companyId,
                               @Param("since") LocalDateTime since);

    /**
     * Departman başına kullanıcı sayıları
     */
    @Query(value = "SELECT d.name, COUNT(u.id) FROM departments d " +
            "LEFT JOIN users u ON d.id = u.department_id " +
            "WHERE d.company_id = :companyId AND (u.status IS NULL OR u.status != 'DELETED') " +
            "GROUP BY d.id, d.name",
            nativeQuery = true)
    List<Object[]> countUsersByDepartment(@Param("companyId") Long companyId);

    // ============ Maintenance Queries ============

    /**
     * Soft delete - kullanıcıyı silindi olarak işaretle
     */
    @Modifying
    @Query(value = "UPDATE users SET status = 'DELETED' WHERE id = :userId",
            nativeQuery = true)
    void softDeleteUser(@Param("userId") Long userId);

    /**
     * Kullanıcı hesabını kilitle
     */
    @Modifying
    @Query(value = "UPDATE users SET locked_until = :lockUntil WHERE id = :userId",
            nativeQuery = true)
    void lockUser(@Param("userId") Long userId, @Param("lockUntil") LocalDateTime lockUntil);

    /**
     * Kullanıcı hesabının kilidini aç
     */
    @Modifying
    @Query(value = "UPDATE users SET locked_until = NULL, login_attempts = 0 WHERE id = :userId",
            nativeQuery = true)
    void unlockUser(@Param("userId") Long userId);

    /**
     * Son giriş tarihini güncelle
     */
    @Modifying
    @Query(value = "UPDATE users SET last_login = :loginTime WHERE id = :userId",
            nativeQuery = true)
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    /**
     * Süresi dolmuş reset token'ları temizle
     */
    @Modifying
    @Query(value = "UPDATE users SET password_reset_token = NULL, password_reset_expires = NULL " +
            "WHERE password_reset_expires < :now",
            nativeQuery = true)
    void cleanupExpiredResetTokens(@Param("now") LocalDateTime now);

    /**
     * Email doğrulama token'ını temizle
     */
    @Modifying
    @Query(value = "UPDATE users SET email_verification_token = NULL WHERE id = :userId",
            nativeQuery = true)
    void clearEmailVerificationToken(@Param("userId") Long userId);

    // ============ Performance Queries ============

    /**
     * Agent performans verileri için query
     */
    @Query(value = "SELECT u.id, u.first_name, u.last_name, u.email, " +
            "COUNT(DISTINCT t.id) as ticket_count, " +
            "AVG(CASE WHEN t.status IN ('RESOLVED', 'CLOSED') AND t.resolved_at IS NOT NULL " +
            "    THEN TIMESTAMPDIFF(HOUR, t.created_at, t.resolved_at) END) as avg_resolution_time " +
            "FROM users u " +
            "LEFT JOIN tickets t ON u.id = t.assigned_agent_id " +
            "WHERE u.company_id = :companyId " +
            "AND u.role IN ('AGENT', 'MANAGER') " +
            "AND u.status = 'ACTIVE' " +
            "AND (:startDate IS NULL OR t.created_at >= :startDate) " +
            "AND (:endDate IS NULL OR t.created_at <= :endDate) " +
            "GROUP BY u.id, u.first_name, u.last_name, u.email",
            nativeQuery = true)
    List<Object[]> getAgentPerformanceStats(@Param("companyId") Long companyId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    // ============ Custom Native Queries ============

    /**
     * Şirketteki en aktif kullanıcılar (son 30 günde)
     */
    @Query(value = "SELECT u.*, " +
            "COALESCE(ticket_count, 0) as ticket_activity, " +
            "COALESCE(comment_count, 0) as comment_activity " +
            "FROM users u " +
            "LEFT JOIN (SELECT creator_user_id, COUNT(*) as ticket_count " +
            "          FROM tickets WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "          GROUP BY creator_user_id) tc ON u.id = tc.creator_user_id " +
            "LEFT JOIN (SELECT author_id, COUNT(*) as comment_count " +
            "          FROM ticket_comments WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "          GROUP BY author_id) cc ON u.id = cc.author_id " +
            "WHERE u.company_id = :companyId AND u.status != 'DELETED' " +
            "ORDER BY (COALESCE(ticket_count, 0) + COALESCE(comment_count, 0)) DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<User> findMostActiveUsers(@Param("companyId") Long companyId, @Param("limit") int limit);

    // ============ Helper Methods for Services ============

    /**
     * Pageable için findLeastBusyAgents wrapper
     */
    default List<User> findLeastBusyAgents(Long departmentId, Pageable pageable) {
        return findLeastBusyAgents(departmentId, pageable.getPageSize());
    }

    /**
     * Pageable için findMostActiveUsers wrapper
     */
    default List<User> findMostActiveUsers(Long companyId, Pageable pageable) {
        return findMostActiveUsers(companyId, pageable.getPageSize());
    }

    // ============ Enum-safe Helper Methods ============

    /**
     * UserRole enum ile departman kullanıcıları
     */
    default List<User> findByDepartmentIdAndRole(Long departmentId, UserRole role) {
        return findByDepartmentIdAndRole(departmentId, role.getCode());
    }

    /**
     * Multiple filters için enum-safe versiyon
     */
    default Page<User> findUsersWithFilters(Long companyId, UserRole role,
                                            UserStatus status, Long departmentId,
                                            Pageable pageable) {
        return findUsersWithFilters(
                companyId,
                role != null ? role.getCode() : null,
                status != null ? status.getCode() : null,
                departmentId,
                pageable
        );
    }
}