// src/main/java/com/example/smartdeskbackend/repository/SlaPolicyRepository.java
package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.SlaPolicy;
import com.example.smartdeskbackend.enums.TicketPriority; // TicketPriority enum'ı için import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {
    List<SlaPolicy> findByCompanyId(Long companyId);
    Optional<SlaPolicy> findByCompanyIdAndAppliesToPriorityAndDepartmentId(Long companyId, TicketPriority priority, Long departmentId); // Şirket, öncelik ve departmana göre spesifik SLA politikası bul
    List<SlaPolicy> findByDepartmentId(Long departmentId);

    List<SlaPolicy> findByCompanyIdAndIsActive(Long companyId, boolean isActive);
}