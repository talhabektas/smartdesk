// src/main/java/com/example/smartdeskbackend/service/impl/DepartmentSecurityService.java
package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.repository.DepartmentRepository;
import com.example.smartdeskbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("departmentSecurityService")
public class DepartmentSecurityService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Kullanıcının department'a erişim hakkı olup olmadığını kontrol eder
     */
    public boolean hasAccessToDepartment(Long departmentId, Long userId) {
        try {
            // Super admin her department'a erişebilir
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getAuthorities().stream()
                    .anyMatch(grantedAuthority ->
                            grantedAuthority.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
                return true;
            }

            // Aynı şirket kontrolü
            return departmentRepository.findById(departmentId)
                    .flatMap(department -> userRepository.findById(userId)
                            .map(user -> department.getCompany().getId().equals(user.getCompany().getId())))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Kullanıcının department manager'ı olup olmadığını kontrol eder
     */
    public boolean isDepartmentManager(Long departmentId, Long userId) {
        try {
            return departmentRepository.findById(departmentId)
                    .map(department -> department.getManager() != null &&
                            department.getManager().getId().equals(userId))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
}