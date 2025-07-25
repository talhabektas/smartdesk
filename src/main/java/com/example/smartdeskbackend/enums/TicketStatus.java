package com.example.smartdeskbackend.enums;

/**
 * Bilet durumlarını tanımlayan enum
 */
public enum TicketStatus {
    NEW("NEW", "Yeni", "Yeni oluşturulmuş bilet"),
    OPEN("OPEN", "Açık", "İşleme alınmış bilet"),
    IN_PROGRESS("IN_PROGRESS", "İşlemde", "Üzerinde çalışılan bilet"),
    PENDING("PENDING", "Beklemede", "Müşteri veya başka departman bekleniyor"),
    RESOLVED("RESOLVED", "Çözülmüş", "Çözüm bulunmuş bilet"),
    CLOSED("CLOSED", "Kapatılmış", "Tamamlanmış ve kapatılmış bilet"),
    ESCALATED("ESCALATED", "Yükseltilmiş", "Üst seviyeye yönlendirilmiş bilet");

    private final String code;
    private final String displayName;
    private final String description;

    TicketStatus(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    // Getters
    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    public boolean isClosed() {
        return this == CLOSED || this == RESOLVED;
    }

    public boolean isActive() {
        return this == NEW || this == OPEN || this == IN_PROGRESS || this == PENDING;
    }
}