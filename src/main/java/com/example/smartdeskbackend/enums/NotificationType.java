package com.example.smartdeskbackend.enums;

/**
 * Bildirim turleri
 */
public enum NotificationType {
    // Ticket bildirimleri
    TICKET_CREATED("Yeni ticket olusturuldu"),
    TICKET_ASSIGNED("Ticket size atandi"),
    TICKET_STATUS_CHANGED("Ticket durumu degisti"),
    TICKET_PRIORITY_CHANGED("Ticket onceligi degisti"),
    TICKET_ESCALATED("Ticket escalate edildi"),
    TICKET_RESOLVED("Ticket cozuldu"),
    TICKET_CLOSED("Ticket kapatildi"),
    TICKET_REMINDER("Ticket hatirlatmasi"),
    TICKET_SLA_WARNING("SLA uyarisi"),
    TICKET_SLA_VIOLATION("SLA ihlali"),
    
    // Approval bildirimleri
    TICKET_PENDING_MANAGER_APPROVAL("Manager onayi bekleniyor"),
    TICKET_PENDING_ADMIN_APPROVAL("Admin onayi bekleniyor"),
    TICKET_MANAGER_APPROVED("Manager tarafindan onaylandi"),
    TICKET_ADMIN_APPROVED("Admin tarafindan onaylandi"),
    TICKET_APPROVAL_REJECTED("Onay reddedildi"),
    
    // Yorum bildirimleri
    NEW_COMMENT("Yeni yorum eklendi"),
    COMMENT_REPLY("Yorumunuza yanit verildi"),
    
    // Sistem bildirimleri
    SYSTEM_MAINTENANCE("Sistem bakimi"),
    SYSTEM_UPDATE("Sistem guncellemesi"),
    WELCOME("Hos geldiniz"),
    
    // Genel bildirimler
    INFO("Bilgi"),
    WARNING("Uyari"),
    ERROR("Hata"),
    SUCCESS("Basarili");
    
    private final String description;
    
    NotificationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}