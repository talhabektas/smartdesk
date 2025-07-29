// src/main/java/com/example/smartdeskbackend/service/impl/SlaPolicyServiceImpl.java
package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.entity.Company;
import com.example.smartdeskbackend.entity.Department;
import com.example.smartdeskbackend.entity.SlaPolicy;
import com.example.smartdeskbackend.enums.TicketPriority;
import com.example.smartdeskbackend.exception.ResourceNotFoundException;
import com.example.smartdeskbackend.repository.CompanyRepository;
import com.example.smartdeskbackend.repository.DepartmentRepository;
import com.example.smartdeskbackend.repository.SlaPolicyRepository;
import com.example.smartdeskbackend.service.SlaPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SlaPolicyServiceImpl implements SlaPolicyService {

    @Autowired
    private SlaPolicyRepository slaPolicyRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public SlaPolicy createSlaPolicy(SlaPolicy policy, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        policy.setCompany(company);

        // Eğer departman belirtilmişse, varlığını ve şirkete aitliğini doğrula
        if (policy.getDepartment() != null && policy.getDepartment().getId() != null) {
            Department department = departmentRepository.findById(policy.getDepartment().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + policy.getDepartment().getId()));
            if (!department.getCompany().getId().equals(companyId)) {
                throw new IllegalArgumentException("Department does not belong to the specified company.");
            }
            policy.setDepartment(department);
        } else {
            policy.setDepartment(null); // Departman belirtilmemişse veya kaldırılmışsa null ayarla
        }

        // Aynı şirket, departman ve öncelik için zaten bir SLA politikası var mı kontrol et
        Optional<SlaPolicy> existingPolicy = slaPolicyRepository.findByCompanyIdAndAppliesToPriorityAndDepartmentId(
                companyId, policy.getAppliesToPriority(), policy.getDepartment() != null ? policy.getDepartment().getId() : null
        );
        if (existingPolicy.isPresent()) {
            throw new IllegalArgumentException("An SLA policy for this company, department and priority already exists.");
        }


        policy.setCreatedAt(LocalDateTime.now());
        policy.setUpdatedAt(LocalDateTime.now());
        return slaPolicyRepository.save(policy);
    }

    @Override
    @Transactional
    public SlaPolicy updateSlaPolicy(Long id, SlaPolicy policyDetails) {
        SlaPolicy policy = slaPolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA Policy not found with id: " + id));

        // Şirket kontrolü: Politika güncelleyen kullanıcının politikanın ait olduğu şirkete yetkisi olmalı.
        // Bu güvenlik katmanında veya daha üst seviye bir servis metodunda yapılmalı.

        policy.setName(policyDetails.getName());
        policy.setDescription(policyDetails.getDescription());
        policy.setAppliesToPriority(policyDetails.getAppliesToPriority());
        policy.setFirstResponseTimeHours(policyDetails.getFirstResponseTimeHours());
        policy.setResolutionTimeHours(policyDetails.getResolutionTimeHours());
        policy.setBusinessHoursOnly(policyDetails.isBusinessHoursOnly());
        policy.setActive(policyDetails.isActive());

        // Departman güncelleniyorsa, varlığını ve aynı şirkete aitliğini doğrula
        if (policyDetails.getDepartment() != null && policyDetails.getDepartment().getId() != null) {
            Department department = departmentRepository.findById(policyDetails.getDepartment().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + policyDetails.getDepartment().getId()));
            if (!department.getCompany().getId().equals(policy.getCompany().getId())) {
                throw new IllegalArgumentException("Department does not belong to the policy's company.");
            }
            policy.setDepartment(department);
        } else {
            policy.setDepartment(null);
        }

        policy.setUpdatedAt(LocalDateTime.now());
        return slaPolicyRepository.save(policy);
    }

    @Override
    @Transactional
    public void deleteSlaPolicy(Long id) {
        if (!slaPolicyRepository.existsById(id)) {
            throw new ResourceNotFoundException("SLA Policy not found with id: " + id);
        }
        // Bu SLA politikasına bağlı SLA takipleri (SlaTracking) varsa ne yapılacağı burada düşünülmeli
        // Örneğin, ilgili SlaTracking kayıtlarını güncelle/sil veya silmeye izin verme
        slaPolicyRepository.deleteById(id);
    }

    @Override
    public Optional<SlaPolicy> getSlaPolicyById(Long id, Long companyId) {
        return slaPolicyRepository.findById(id)
                .filter(p -> p.getCompany().getId().equals(companyId)); // Multi-tenant filtreleme
    }

    @Override
    public List<SlaPolicy> getAllSlaPolicies(Long companyId) {
        return slaPolicyRepository.findByCompanyId(companyId); // Multi-tenant filtreleme
    }

    @Override
    public Optional<SlaPolicy> getApplicableSlaPolicy(Long companyId, Long departmentId, TicketPriority priority) {
        // En spesifik SLA politikasını bulma sırası:
        // 1. Şirket, belirli departman ve belirli öncelik için
        if (departmentId != null) {
            Optional<SlaPolicy> policy = slaPolicyRepository.findByCompanyIdAndAppliesToPriorityAndDepartmentId(companyId, priority, departmentId);
            if (policy.isPresent()) {
                return policy;
            }
        }
        // 2. Şirket, herhangi bir departman (null) ve belirli öncelik için (genel SLA)
        return slaPolicyRepository.findByCompanyIdAndAppliesToPriorityAndDepartmentId(companyId, priority, null);
    }
}