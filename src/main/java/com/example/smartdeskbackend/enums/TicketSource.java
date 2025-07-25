package com.example.smartdeskbackend.enums;

public enum TicketSource {
    EMAIL("EMAIL", "E-posta", "customer-email@company.com"),
    WEB_FORM("WEB_FORM", "Web Formu", "Müşteri portal formu"),
    PHONE("PHONE", "Telefon", "Çağrı merkezi"),
    CHAT("CHAT", "Canlı Chat", "Web sitesi chat"),
    API("API", "API", "Sistem entegrasyonu"),
    MOBILE_APP("MOBILE_APP", "Mobil Uygulama", "iOS/Android app");

    private final String code;
    private final String displayName;
    private final String description;

    TicketSource(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    // Getters
    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
