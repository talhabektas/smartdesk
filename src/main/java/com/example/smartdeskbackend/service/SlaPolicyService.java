// src/main/java/com/example/smartdeskbackend/service/SlaPolicyService.java
package com.example.smartdeskbackend.service;

import com.example.smartdeskbackend.entity.SlaPolicy; // SlaPolicy entity'si kullanıldığı için import edildi
import com.example.smartdeskbackend.enums.TicketPriority; // TicketPriority enum'ı kullanıldığı için import edildi
import java.util.List;
import java.util.Optional;

public interface SlaPolicyService {
    SlaPolicy createSlaPolicy(SlaPolicy policy, Long companyId);
    SlaPolicy updateSlaPolicy(Long id, SlaPolicy policyDetails);
    void deleteSlaPolicy(Long id);
    Optional<SlaPolicy> getSlaPolicyById(Long id, Long companyId);
    List<SlaPolicy> getAllSlaPolicies(Long companyId);
    Optional<SlaPolicy> getApplicableSlaPolicy(Long companyId, Long departmentId, TicketPriority priority);
}