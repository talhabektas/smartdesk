package com.example.smartdeskbackend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Security utilities - güvenlik işlemleri için yardımcı sınıf
 */
@Component
public class SecurityUtils {

    /**
     * Şu anki authenticated kullanıcıyı döndürür
     */
    public static Optional<String> getCurrentUserLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                return Optional.of(userDetails.getUsername());
            } else if (authentication.getPrincipal() instanceof String) {
                return Optional.of((String) authentication.getPrincipal());
            }
        }

        return Optional.empty();
    }

    /**
     * Kullanıcının authenticated olup olmadığını kontrol eder
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Kullanıcının belirtilen role sahip olup olmadığını kontrol eder
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority ->
                            grantedAuthority.getAuthority().equals("ROLE_" + role) ||
                                    grantedAuthority.getAuthority().equals(role)
                    );
        }

        return false;
    }

    /**
     * Kullanıcının herhangi bir role sahip olup olmadığını kontrol eder
     */
    public static boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Current authentication objesini döndürür
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Optional.ofNullable(authentication);
    }

    /**
     * Current user details'i döndürür
     */
    public static Optional<UserDetails> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return Optional.of((UserDetails) authentication.getPrincipal());
        }

        return Optional.empty();
    }
}