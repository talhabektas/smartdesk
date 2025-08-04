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
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + role) ||
                            grantedAuthority.getAuthority().equals(role));
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

    /**
     * Current user email'ini döndürür
     */
    public static String getCurrentUserEmail() {
        return getCurrentUserLogin().orElse(null);
    }

    /**
     * Current user role'unu döndürür
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .filter(authority -> authority.startsWith("ROLE_"))
                    .map(authority -> authority.substring(5)) // Remove "ROLE_" prefix
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }

    /**
     * Current user company ID'sini döndürür (JWT token'dan alınır)
     * Not: Bu method JWT utility ile implement edilmeli
     */
    public static Long getCurrentUserCompanyId() {
        // Bu method JWT token'dan company ID'yi extract etmek için
        // JwtUtil ile implement edilmeli. Şimdilik null dönüyoruz.
        // Gerçek implementasyon için JwtUtil dependency'si gerekli.
        return null;
    }
}