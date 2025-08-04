// src/main/java/com/example/smartdeskbackend/entity/Notification.java
package com.example.smartdeskbackend.entity;

import com.example.smartdeskbackend.entity.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@EqualsAndHashCode(callSuper = true)
public class Notification extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message;
    private String type;
    private String targetUrl;
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id") // Bildirimi alan kullanıcı
    private User recipientUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id") // Multi-tenant yapıya uygun
    private Company company;
}
