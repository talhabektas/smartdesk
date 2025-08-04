package com.example.smartdeskbackend.enums;

/**
 * Kullanıcı rollerini tanımlayan enum
 * RBAC (Role-Based Access Control) sistemi için temel roller
 */
public enum UserRole {

    SUPER_ADMIN("SUPER_ADMIN", "Sistem Yöneticisi", "Tam sistem erişimi"),
    MANAGER("MANAGER", "Departman Müdürü", "Departman yönetimi ve raporlama"),
    AGENT("AGENT", "Destek Temsilcisi", "Bilet yönetimi ve müşteri iletişimi"),
    CUSTOMER("CUSTOMER", "Müşteri", "Bilet oluşturma ve takip");

    private final String code;
    private final String displayName;
    private final String description;

    UserRole(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    // Getters
    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * String değerden UserRole enum'ına dönüşüm
     */
    public static UserRole fromString(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.code.equalsIgnoreCase(role)) {
                return userRole;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + role);
    }

    /**
     * Yetki kontrolü için kullanılacak Spring Security authority string'i
     */
    public String getAuthority() {
        return "ROLE_" + this.code;
    }

    /**
     * Hiyerarşik rol kontrolü - üst rol alt rollerin yetkilerine sahip
     */
    public boolean hasPermission(UserRole requiredRole) {
        return this.ordinal() <= requiredRole.ordinal();
    }

    /**
     * Agent rolü mü kontrol et
     */
    public boolean isAgent() {
        return this == AGENT || this == MANAGER || this == SUPER_ADMIN;
    }

    /**
     * Yönetici rolü mü kontrol et
     */
    public boolean isManager() {
        return this == MANAGER || this == SUPER_ADMIN;
    }

    /**
     * Admin rolü mü kontrol et
     */
    public boolean isAdmin() {
        return this == SUPER_ADMIN;
    }
}