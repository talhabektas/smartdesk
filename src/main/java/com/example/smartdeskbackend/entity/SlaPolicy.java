// src/main/java/com/example/smartdeskbackend/entity/SlaPolicy.java
package com.example.smartdeskbackend.entity;

import com.example.smartdeskbackend.entity.base.AuditableEntity; // AuditableEntity sınıfı için gerekli import
import com.example.smartdeskbackend.enums.TicketPriority; // TicketPriority enum'ı için import
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "sla_policies")
@Data
@EqualsAndHashCode(callSuper = true) // AuditableEntity'den miras aldığı için
public class SlaPolicy extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING) // Enum değerini string olarak kaydet
    private TicketPriority appliesToPriority; // Hangi öncelikteki biletlere uygulanacak
    private int firstResponseTimeHours; // İlk yanıt süresi (saat)
    private int resolutionTimeHours; // Çözüm süresi (saat)
    private boolean businessHoursOnly; // Sadece iş saatlerinde mi hesaplanacak
    private boolean isActive; // Politika aktif mi?

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id") // Hangi departmana ait
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id") // Multi-tenant yapıya uygun
    private Company company;
}