package com.example.smartdeskbackend.enums;

/**
 * Kullanıcı hesap durumlarını tanımlayan enum
 */
public enum UserStatus {

    ACTIVE("ACTIVE", "Aktif", "Kullanıcı aktif ve sistemi kullanabilir"),
    INACTIVE("INACTIVE", "Pasif", "Kullanıcı geçici olarak devre dışı"),
    PENDING("PENDING", "Beklemede", "Hesap aktivasyonu bekleniyor"),
    SUSPENDED("SUSPENDED", "Askıya Alınmış", "Kullanıcı askıya alınmış"),
    DELETED("DELETED", "Silinmiş", "Kullanıcı hesabı silinmiş");

    private final String code;
    private final String displayName;
    private final String description;

    UserStatus(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    // Getters
    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    /**
     * Kullanıcının aktif olup olmadığını kontrol eder
     */
    public boolean isActive() {
        return this == ACTIVE;
    }

    /**
     * Kullanıcının giriş yapıp yapamayacağını kontrol eder
     */
    public boolean canLogin() {
        return this == ACTIVE || this == PENDING;
    }

    /**
     * String değerden UserStatus enum'ına dönüşüm
     */
    public static UserStatus fromString(String status) {
        for (UserStatus userStatus : UserStatus.values()) {
            if (userStatus.code.equalsIgnoreCase(status)) {
                return userStatus;
            }
        }
        throw new IllegalArgumentException("Invalid status: " + status);
    }
}