
// DepartmentRepository.java
package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * Şirketteki departmanlar
     */
    List<Department> findByCompanyId(Long companyId);

    /**
     * Şirketteki aktif departmanlar
     */
    List<Department> findByCompanyIdAndIsActiveTrue(Long companyId);

    /**
     * İsimle departman bul
     */
    Optional<Department> findByCompanyIdAndName(Long companyId, String name);

    /**
     * Manager'ı olan departmanlar
     */
    List<Department> findByManagerId(Long managerId);

    /**
     * Departman performans istatistikleri
     */
    @Query("SELECT d.name, COUNT(t.id), AVG(CASE WHEN t.resolvedAt IS NOT NULL " +
            "THEN TIMESTAMPDIFF(HOUR, t.createdAt, t.resolvedAt) END) " +
            "FROM Department d LEFT JOIN Ticket t ON d.id = t.department.id " +
            "WHERE d.company.id = :companyId GROUP BY d.id, d.name")
    List<Object[]> getDepartmentStats(@Param("companyId") Long companyId);
}