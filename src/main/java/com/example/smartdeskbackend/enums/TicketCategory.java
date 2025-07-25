package com.example.smartdeskbackend.enums;

public enum TicketCategory {
    TECHNICAL_SUPPORT("TECHNICAL_SUPPORT", "Teknik Destek"),
    BILLING("BILLING", "Faturalama"),
    ACCOUNT_MANAGEMENT("ACCOUNT_MANAGEMENT", "Hesap Yönetimi"),
    FEATURE_REQUEST("FEATURE_REQUEST", "Özellik İsteği"),
    BUG_REPORT("BUG_REPORT", "Hata Bildirimi"),
    GENERAL_INQUIRY("GENERAL_INQUIRY", "Genel Sorgu"),
    COMPLAINT("COMPLAINT", "Şikayet"),
    REFUND_REQUEST("REFUND_REQUEST", "İade İsteği");

    private final String code;
    private final String displayName;

    TicketCategory(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    // Getters
    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
}
