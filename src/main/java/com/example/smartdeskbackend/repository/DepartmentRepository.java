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
     * Departman ismi varlığı kontrolü (şirket bazında)
     */
    boolean existsByNameAndCompanyId(String name, Long companyId);

    /**
     * Departman ismi varlığı kontrolü (güncellenecek departman hariç)
     */
    boolean existsByNameAndCompanyIdAndIdNot(String name, Long companyId, Long excludeId);

    /**
     * Manager'ı olan departmanlar
     */
    List<Department> findByManagerId(Long managerId);

    /**
     * Departman performans istatistikleri
     */
    @Query(value = "SELECT d.name, COUNT(t.id), " +
            "AVG(CASE WHEN t.resolved_at IS NOT NULL " +
            "    THEN TIMESTAMPDIFF(HOUR, t.created_at, t.resolved_at) ELSE 0 END) AS avgResolutionTime " +
            "FROM departments d " +
            "LEFT JOIN tickets t ON d.id = t.department_id " +
            "WHERE d.company_id = :companyId " +
            "GROUP BY d.id, d.name", nativeQuery = true)
    List<Object[]> getDepartmentStats(@Param("companyId") Long companyId);
}