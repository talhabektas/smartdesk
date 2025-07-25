package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Company entity için repository interface
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * Domain ile şirket bulma
     */
    Optional<Company> findByDomain(String domain);

    /**
     * Domain varlığı kontrolü
     */
    boolean existsByDomain(String domain);

    /**
     * Aktif şirketleri bulma
     */
    List<Company> findByIsActiveTrue();

    /**
     * İsim ile arama
     */
    @Query("SELECT c FROM Company c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND c.isActive = true")
    Page<Company> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * Plan tipine göre şirketler
     */
    List<Company> findByPlanType(String planType);
}
