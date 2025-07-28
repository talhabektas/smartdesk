package com.example.smartdeskbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

/**
 * JPA Configuration sınıfı
 * Auditing ve transaction management ayarları
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableTransactionManagement
public class JpaConfig {

    /**
     * JPA Auditing için auditor provider
     * Created/Updated by alanları için kullanılır
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }

    /**
     * Spring Security context'inden current user'ı alır
     */
    public static class SpringSecurityAuditorAware implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("system");
            }

            if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                org.springframework.security.core.userdetails.UserDetails userDetails =
                        (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
                return Optional.of(userDetails.getUsername());
            }

            return Optional.of(authentication.getName());
        }
    }
}