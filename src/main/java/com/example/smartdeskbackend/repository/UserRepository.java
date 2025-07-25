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
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.status != 'DELETED'")
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
     * Şirkete ait aktif kullanıcıları bulma
     */
    @Query("SELECT u FROM User u WHERE u.company.id = :companyId AND u.status = :status")
    List<User> findByCompanyIdAndStatus(@Param("companyId") Long companyId,
                                        @Param("status") UserStatus status);

    /**
     * Şirkete ait kullanıcı sayısı
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.company.id = :companyId AND u.status != 'DELETED'")
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
    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId " +
            "AND u.role = :role AND u.status = 'ACTIVE'")
    List<User> findByDepartmentIdAndRole(@Param("departmentId") Long departmentId,
                                         @Param("role") UserRole role);

    // ============ Agent-specific Queries ============

    /**
     * Şirketteki aktif agent'ları bulma
     */
    @Query("SELECT u FROM User u WHERE u.company.id = :companyId " +
            "AND u.role IN ('AGENT', 'MANAGER') AND u.status = 'ACTIVE'")
    List<User> findActiveAgentsByCompanyId(@Param("companyId") Long companyId);

    /**
     * Departmandaki agent'ları bulma
     */
    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId " +
            "AND u.role = 'AGENT' AND u.status = 'ACTIVE'")
    List<User> findAgentsByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * En az ticket'a sahip agent'ı bulma (load balancing için)
     */
    @Query("SELECT u FROM User u LEFT JOIN u.assignedTickets t " +
            "WHERE u.department.id = :departmentId AND u.role = 'AGENT' " +
            "AND u.status = 'ACTIVE' " +
            "GROUP BY u.id " +
            "ORDER BY COUNT(t) ASC")
    List<User> findLeastBusyAgents(@Param("departmentId") Long departmentId, Pageable pageable);

    // ============ Search and Filter Queries ============

    /**
     * İsim veya email ile arama
     */
    @Query("SELECT u FROM User u WHERE u.company.id = :companyId " +
            "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND u.status != 'DELETED'")
    Page<User> searchUsers(@Param("companyId") Long companyId,
                           @Param("searchTerm") String searchTerm,
                           Pageable pageable);

    /**
     * Multiple criteria ile kullanıcı arama
     */
    @Query("SELECT u FROM User u WHERE u.company.id = :companyId " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:status IS NULL OR u.status = :status) " +
            "AND (:departmentId IS NULL OR u.department.id = :departmentId) " +
            "AND u.status != 'DELETED'")
    Page<User> findUsersWithFilters(@Param("companyId") Long companyId,
                                    @Param("role") UserRole role,
                                    @Param("status") UserStatus status,
                                    @Param("departmentId") Long departmentId,
                                    Pageable pageable);

    // ============ Statistics Queries ============

    /**
     * Şirketteki role göre kullanıcı sayıları
     */
    @Query("SELECT u.role, COUNT(u) FROM User u " +
            "WHERE u.company.id = :companyId AND u.status != 'DELETED' " +
            "GROUP BY u.role")
    List<Object[]> countUsersByRoleAndCompany(@Param("companyId") Long companyId);

    /**
     * Son login tarihine göre aktif kullanıcı sayısı
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.company.id = :companyId " +
            "AND u.lastLogin >= :since AND u.status = 'ACTIVE'")
    long countActiveUsersSince(@Param("companyId") Long companyId,
                               @Param("since") LocalDateTime since);

    /**
     * Departman başına kullanıcı sayıları
     */
    @Query("SELECT d.name, COUNT(u) FROM User u " +
            "RIGHT JOIN u.department d " +
            "WHERE u.company.id = :companyId AND u.status != 'DELETED' " +
            "GROUP BY d.id, d.name")
    List<Object[]> countUsersByDepartment(@Param("companyId") Long companyId);

    // ============ Maintenance Queries ============

    /**
     * Soft delete - kullanıcıyı silindi olarak işaretle
     */
    @Modifying
    @Query("UPDATE User u SET u.status = 'DELETED' WHERE u.id = :userId")
    void softDeleteUser(@Param("userId") Long userId);

    /**
     * Kullanıcı hesabını kilitle
     */
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = :lockUntil WHERE u.id = :userId")
    void lockUser(@Param("userId") Long userId, @Param("lockUntil") LocalDateTime lockUntil);

    /**
     * Kullanıcı hesabının kilidini aç
     */
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = NULL, u.loginAttempts = 0 WHERE u.id = :userId")
    void unlockUser(@Param("userId") Long userId);

    /**
     * Son giriş tarihini güncelle
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    /**
     * Süresi dolmuş reset token'ları temizle
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordResetToken = NULL, u.passwordResetExpires = NULL " +
            "WHERE u.passwordResetExpires < :now")
    void cleanupExpiredResetTokens(@Param("now") LocalDateTime now);

    /**
     * Email doğrulama token'ını temizle
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerificationToken = NULL WHERE u.id = :userId")
    void clearEmailVerificationToken(@Param("userId") Long userId);

    // ============ Performance Queries ============

    /**
     * Agent performans verileri için query
     */
    @Query("SELECT u.id, u.firstName, u.lastName, u.email, " +
            "COUNT(DISTINCT t.id) as ticketCount, " +
            "AVG(CASE WHEN t.status IN ('RESOLVED', 'CLOSED') AND t.resolvedAt IS NOT NULL " +
            "    THEN FUNCTION('TIMESTAMPDIFF', HOUR, t.createdAt, t.resolvedAt) END) as avgResolutionTime " +
            "FROM User u " +
            "LEFT JOIN u.assignedTickets t " +
            "WHERE u.company.id = :companyId " +
            "AND u.role IN ('AGENT', 'MANAGER') " +
            "AND u.status = 'ACTIVE' " +
            "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
            "AND (:endDate IS NULL OR t.createdAt <= :endDate) " +
            "GROUP BY u.id, u.firstName, u.lastName, u.email")
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
            "ORDER BY (COALESCE(ticket_count, 0) + COALESCE(comment_count, 0)) DESC",
            nativeQuery = true)
    List<User> findMostActiveUsers(@Param("companyId") Long companyId, Pageable pageable);
}