
package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.repository.UserRepository;
import com.example.smartdeskbackend.repository.CompanyRepository;
import com.example.smartdeskbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    /**
     * Kullanıcının aynı şirkette olup olmadığını kontrol eder
     */
    public boolean isFromSameCompany(Long companyId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }

            String email = auth.getName();
            return userRepository.findByEmail(email)
                    .map(user -> user.getCompany() != null && user.getCompany().getId().equals(companyId))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Super admin kontrolü
     */
    public boolean isSuperAdmin() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.getAuthorities().stream()
                    .anyMatch(grantedAuthority ->
                            grantedAuthority.getAuthority().equals("ROLE_SUPER_ADMIN"));
        } catch (Exception e) {
            return false;
        }
    }
}
