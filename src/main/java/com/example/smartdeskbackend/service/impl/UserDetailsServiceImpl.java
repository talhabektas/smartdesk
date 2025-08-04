package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.entity.User;
import com.example.smartdeskbackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Spring Security UserDetailsService implementasyonu
 * Kullanıcı authentication işlemleri için
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        logger.debug("User found: {}", user.getEmail());
        return createUserPrincipal(user);
    }

    /**
     * User entity'sinden UserDetails oluşturur
     */
    private UserDetails createUserPrincipal(User user) {
        Collection<GrantedAuthority> authorities = getAuthorities(user);

        return new CustomUserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getRole().getCode(),
                user.getCompany() != null ? user.getCompany().getId() : null,
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.canLogin(), // enabled
                !user.isAccountLocked(), // account non locked
                true, // credentials non expired
                user.canLogin(), // account non expired (use canLogin instead of isActive)
                authorities
        );
    }

    /**
     * Kullanıcının yetkilerini (authorities) döndürür
     */
    private Collection<GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Role-based authority ekle
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getCode()));

        // Ek yetkiler (isteğe bağlı olarak genişletilebilir)
        switch (user.getRole()) {
            case SUPER_ADMIN:
                authorities.add(new SimpleGrantedAuthority("PERM_ADMIN_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERM_COMPANY_MANAGE"));
                authorities.add(new SimpleGrantedAuthority("PERM_USER_MANAGE"));
                authorities.add(new SimpleGrantedAuthority("PERM_REPORTS_FULL"));
                break;

            case MANAGER:
                authorities.add(new SimpleGrantedAuthority("PERM_TEAM_MANAGE"));
                authorities.add(new SimpleGrantedAuthority("PERM_TICKET_MANAGE"));
                authorities.add(new SimpleGrantedAuthority("PERM_REPORTS_DEPT"));
                authorities.add(new SimpleGrantedAuthority("PERM_ANALYTICS_VIEW"));
                break;

            case AGENT:
                authorities.add(new SimpleGrantedAuthority("PERM_TICKET_HANDLE"));
                authorities.add(new SimpleGrantedAuthority("PERM_CUSTOMER_VIEW"));
                authorities.add(new SimpleGrantedAuthority("PERM_KB_EDIT"));
                break;

            case CUSTOMER:
                authorities.add(new SimpleGrantedAuthority("PERM_TICKET_CREATE"));
                authorities.add(new SimpleGrantedAuthority("PERM_TICKET_VIEW_OWN"));
                authorities.add(new SimpleGrantedAuthority("PERM_KB_VIEW"));
                break;
        }

        return authorities;
    }

    /**
     * Custom UserDetails implementasyonu
     * Ek kullanıcı bilgilerini tutar
     */
    public static class CustomUserPrincipal implements UserDetails {

        private final Long id;
        private final String email;
        private final String password;
        private final String fullName;
        private final String role;
        private final Long companyId;
        private final Long departmentId;
        private final boolean enabled;
        private final boolean accountNonLocked;
        private final boolean credentialsNonExpired;
        private final boolean accountNonExpired;
        private final Collection<? extends GrantedAuthority> authorities;

        public CustomUserPrincipal(Long id, String email, String password, String fullName,
                                   String role, Long companyId, Long departmentId,
                                   boolean enabled, boolean accountNonLocked,
                                   boolean credentialsNonExpired, boolean accountNonExpired,
                                   Collection<? extends GrantedAuthority> authorities) {
            this.id = id;
            this.email = email;
            this.password = password;
            this.fullName = fullName;
            this.role = role;
            this.companyId = companyId;
            this.departmentId = departmentId;
            this.enabled = enabled;
            this.accountNonLocked = accountNonLocked;
            this.credentialsNonExpired = credentialsNonExpired;
            this.accountNonExpired = accountNonExpired;
            this.authorities = authorities;
        }

        // UserDetails interface methods
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getUsername() {
            return email;
        }

        @Override
        public boolean isAccountNonExpired() {
            return accountNonExpired;
        }

        @Override
        public boolean isAccountNonLocked() {
            return accountNonLocked;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return credentialsNonExpired;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        // Custom getters
        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getFullName() {
            return fullName;
        }

        public String getRole() {
            return role;
        }

        public Long getCompanyId() {
            return companyId;
        }

        public Long getDepartmentId() {
            return departmentId;
        }

        @Override
        public String toString() {
            return String.format("CustomUserPrincipal{id=%d, email='%s', role='%s', enabled=%s}",
                    id, email, role, enabled);
        }
    }
}