
package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.repository.CustomerRepository;
import com.example.smartdeskbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("customerSecurityService")
public class CustomerSecurityService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Kullanıcının customer'a erişim hakkı olup olmadığını kontrol eder
     */
    public boolean hasAccessToCustomer(Long customerId, Long userId) {
        try {
            // Super admin ve manager'lar her customer'a erişebilir
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getAuthorities().stream()
                    .anyMatch(grantedAuthority ->
                            grantedAuthority.getAuthority().equals("ROLE_SUPER_ADMIN") ||
                                    grantedAuthority.getAuthority().equals("ROLE_MANAGER"))) {
                return true;
            }

            // Aynı şirket kontrolü
            return customerRepository.findById(customerId)
                    .flatMap(customer -> userRepository.findById(userId)
                            .map(user -> customer.getCompany().getId().equals(user.getCompany().getId())))
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
}
