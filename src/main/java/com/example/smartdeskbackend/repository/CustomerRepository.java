// CustomerRepository.java
package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.Customer;
import com.example.smartdeskbackend.enums.CustomerSegment;
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
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Email ile müşteri bul
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Email varlığı kontrolü
     */
    boolean existsByEmail(String email);

    /**
     * Şirketteki müşteriler
     */
    List<Customer> findByCompanyId(Long companyId);

    /**
     * Şirketteki aktif müşteriler
     */
    List<Customer> findByCompanyIdAndIsActiveTrue(Long companyId);

    /**
     * Segment'e göre müşteriler
     */
    List<Customer> findByCompanyIdAndSegment(Long companyId, CustomerSegment segment);

    /**
     * Müşteri arama
     */
    @Query("SELECT c FROM Customer c WHERE c.company.id = :companyId " +
            "AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND c.isActive = true")
    Page<Customer> searchCustomers(@Param("companyId") Long companyId,
                                   @Param("searchTerm") String searchTerm,
                                   Pageable pageable);

    /**
     * VIP müşteriler
     */
    @Query("SELECT c FROM Customer c WHERE c.company.id = :companyId " +
            "AND c.segment = 'VIP' AND c.isActive = true")
    List<Customer> findVipCustomers(@Param("companyId") Long companyId);
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.company.id = :companyId AND c.isActive = true")
    long countByCompanyId(@Param("companyId") Long companyId);
    /**
     * Son iletişim tarihine göre müşteriler
     */
    @Query("SELECT c FROM Customer c WHERE c.company.id = :companyId " +
            "AND c.lastContact < :date AND c.isActive = true " +
            "ORDER BY c.lastContact ASC")
    List<Customer> findCustomersNotContactedSince(@Param("companyId") Long companyId,
                                                  @Param("date") LocalDateTime date);

    /**
     * Segment bazında sayım
     */
    @Query("SELECT c.segment, COUNT(c) FROM Customer c " +
            "WHERE c.company.id = :companyId AND c.isActive = true " +
            "GROUP BY c.segment")
    List<Object[]> countCustomersBySegment(@Param("companyId") Long companyId);
}