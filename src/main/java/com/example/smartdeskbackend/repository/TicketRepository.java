// TicketRepository.java
package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.Ticket;
import com.example.smartdeskbackend.enums.TicketStatus;
import com.example.smartdeskbackend.enums.TicketPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Ticket numarası ile bul
     */
    Optional<Ticket> findByTicketNumber(String ticketNumber);

    /**
     * Şirketteki ticketlar
     */
    Page<Ticket> findByCompanyId(Long companyId, Pageable pageable);

    /**
     * Müşterinin ticketları
     */
    Page<Ticket> findByCustomerId(Long customerId, Pageable pageable);

    /**
     * Agent'a atanmış ticketlar
     */
    Page<Ticket> findByAssignedAgentId(Long agentId, Pageable pageable);

    /**
     * Departmandaki ticketlar
     */
    Page<Ticket> findByDepartmentId(Long departmentId, Pageable pageable);

    /**
     * Status'e göre ticketlar
     */
    Page<Ticket> findByCompanyIdAndStatus(Long companyId, TicketStatus status, Pageable pageable);

    /**
     * Önceliğe göre ticketlar
     */
    Page<Ticket> findByCompanyIdAndPriority(Long companyId, TicketPriority priority, Pageable pageable);

    /**
     * Aktif ticketlar (NEW, OPEN, IN_PROGRESS, PENDING)
     */
    @Query("SELECT t FROM Ticket t WHERE t.company.id = :companyId " +
            "AND t.status IN (com.example.smartdeskbackend.enums.TicketStatus.NEW, " +
            "com.example.smartdeskbackend.enums.TicketStatus.OPEN, " +
            "com.example.smartdeskbackend.enums.TicketStatus.IN_PROGRESS, " +
            "com.example.smartdeskbackend.enums.TicketStatus.PENDING) " +
            "ORDER BY t.priority DESC, t.createdAt ASC")
    Page<Ticket> findActiveTickets(@Param("companyId") Long companyId, Pageable pageable);

    /**
     * Agent'a atanmış ticket sayısı
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedAgent.id = :agentId")
    long countByAssignedAgentId(@Param("agentId") Long agentId);

    /**
     * Müşterinin ticket sayısı
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);

    /**
     * Şirketteki toplam ticket sayısı
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.company.id = :companyId")
    long countByCompanyId(@Param("companyId") Long companyId);

    /**
     * Atanmamış ticketlar
     */
    @Query("SELECT t FROM Ticket t WHERE t.company.id = :companyId " +
            "AND t.assignedAgent IS NULL AND t.status = com.example.smartdeskbackend.enums.TicketStatus.NEW " +
            "ORDER BY t.priority DESC, t.createdAt ASC")
    List<Ticket> findUnassignedTickets(@Param("companyId") Long companyId);

    /**
     * SLA ihlali riski olan ticketlar
     */
    @Query("SELECT t FROM Ticket t WHERE t.company.id = :companyId " +
            "AND t.slaDeadline IS NOT NULL AND t.slaDeadline < :riskTime " +
            "AND t.status NOT IN (com.example.smartdeskbackend.enums.TicketStatus.RESOLVED, " +
            "com.example.smartdeskbackend.enums.TicketStatus.CLOSED) " +
            "ORDER BY t.slaDeadline ASC")
    List<Ticket> findTicketsAtRiskOfSlaViolation(@Param("companyId") Long companyId,
                                                 @Param("riskTime") LocalDateTime riskTime);

    /**
     * SLA ihlali olan ticketlar
     */
    @Query("SELECT t FROM Ticket t WHERE t.company.id = :companyId " +
            "AND t.slaDeadline IS NOT NULL AND t.slaDeadline < :now " +
            "AND t.firstResponseAt IS NULL " +
            "ORDER BY t.slaDeadline ASC")
    List<Ticket> findSlaViolatedTickets(@Param("companyId") Long companyId,
                                        @Param("now") LocalDateTime now);

    /**
     * Ticket arama
     */
    @Query("SELECT t FROM Ticket t WHERE t.company.id = :companyId " +
            "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY t.createdAt DESC")
    Page<Ticket> searchTickets(@Param("companyId") Long companyId,
                               @Param("searchTerm") String searchTerm,
                               Pageable pageable);

    /**
     * Gelişmiş ticket filtreleme
     */
    @Query("SELECT t FROM Ticket t WHERE t.company.id = :companyId " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:departmentId IS NULL OR t.department.id = :departmentId) " +
            "AND (:agentId IS NULL OR t.assignedAgent.id = :agentId) " +
            "AND (:customerId IS NULL OR t.customer.id = :customerId) " +
            "ORDER BY t.createdAt DESC")
    Page<Ticket> findTicketsWithFilters(@Param("companyId") Long companyId,
                                        @Param("status") TicketStatus status,
                                        @Param("priority") TicketPriority priority,
                                        @Param("departmentId") Long departmentId,
                                        @Param("agentId") Long agentId,
                                        @Param("customerId") Long customerId,
                                        Pageable pageable);

    /**
     * Ticket istatistikleri
     */
    @Query("SELECT t.status, COUNT(t) FROM Ticket t " +
            "WHERE t.company.id = :companyId " +
            "GROUP BY t.status")
    List<Object[]> getTicketStatsByStatus(@Param("companyId") Long companyId);

    @Query("SELECT t.priority, COUNT(t) FROM Ticket t " +
            "WHERE t.company.id = :companyId " +
            "GROUP BY t.priority")
    List<Object[]> getTicketStatsByPriority(@Param("companyId") Long companyId);

    /**
     * Belirli tarih aralığındaki ticketlar
     */
    @Query("SELECT t FROM Ticket t WHERE t.company.id = :companyId " +
            "AND t.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY t.createdAt DESC")
    List<Ticket> findTicketsBetweenDates(@Param("companyId") Long companyId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Agent performans metrikleri (NATIVE SORGUNUZ İÇİN YENİ DÜZENLEME)
     */
    @Query(value = "SELECT u.id, u.first_name, u.last_name, " +
            "COUNT(t.id) as ticketCount, " +
            "AVG(CASE WHEN t.resolved_at IS NOT NULL " +
            "    THEN CAST(TIMESTAMPDIFF(HOUR, t.created_at, t.resolved_at) AS DECIMAL(10, 2)) ELSE 0 END) as avgResolutionTime, " + // CAST ve ELSE 0 eklendi, DECIMAL(10,2) daha uygun olabilir.
            "COUNT(CASE WHEN t.status = 'RESOLVED' THEN 1 END) as resolvedCount " +
            "FROM tickets t " +
            "JOIN users u ON t.assigned_agent_id = u.id " + // 'agents a' yerine 'users u' olarak düzeltildi
            "WHERE t.company_id = :companyId " +
            "AND u.role IN ('AGENT', 'MANAGER') " + // Sadece agent ve manager rolleri dikkate alındı
            "AND u.status = 'ACTIVE' " + // Sadece aktif kullanıcılar
            "AND (:startDate IS NULL OR t.created_at >= :startDate) " +
            "AND (:endDate IS NULL OR t.created_at <= :endDate) " +
            "GROUP BY u.id, u.first_name, u.last_name", nativeQuery = true) // GROUP BY da u.id kullanıldı
    List<Object[]> getAgentPerformanceMetrics(@Param("companyId") Long companyId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Günlük ticket oluşturma trendi
     */
    @Query("SELECT DATE(t.createdAt), COUNT(t) FROM Ticket t " +
            "WHERE t.company.id = :companyId " +
            "AND t.createdAt >= :startDate " +
            "GROUP BY DATE(t.createdAt) " +
            "ORDER BY DATE(t.createdAt)")
    List<Object[]> getDailyTicketCreationTrend(@Param("companyId") Long companyId,
                                               @Param("startDate") LocalDateTime startDate);

    /**
     * Müşteri memnuniyet ortalaması
     */
    @Query("SELECT AVG(t.customerSatisfactionRating) FROM Ticket t " +
            "WHERE t.company.id = :companyId " +
            "AND t.customerSatisfactionRating IS NOT NULL " +
            "AND t.createdAt >= :startDate")
    Double getAverageCustomerSatisfaction(@Param("companyId") Long companyId,
                                          @Param("startDate") LocalDateTime startDate);

    /**
     * En çok kullanılan kategoriler
     */
    @Query("SELECT t.category, COUNT(t) FROM Ticket t " +
            "WHERE t.company.id = :companyId " +
            "AND t.category IS NOT NULL " +
            "GROUP BY t.category " +
            "ORDER BY COUNT(t) DESC")
    List<Object[]> getMostUsedCategories(@Param("companyId") Long companyId);
}