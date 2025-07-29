// src/main/java/com/example/smartdeskbackend/entity/AgentPerformance.java
package com.example.smartdeskbackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.example.smartdeskbackend.entity.base.AuditableEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_performance")
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentPerformance extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate reportDate; // Raporun ait olduğu tarih
    private int totalTicketsHandled; // Toplam ele alınan bilet sayısı
    private int resolvedTickets; // Çözülen bilet sayısı
    private int reopenedTickets; // Yeniden açılan bilet sayısı (isteğe bağlı)
    private double averageResolutionTimeHours; // Ortalama çözüm süresi (saat)
    private double averageFirstResponseTimeHours; // Ortalama ilk yanıt süresi (saat) (isteğe bağlı)
    private double customerSatisfactionScore; // Ortalama müşteri memnuniyet skoru (isteğe bağlı)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private User agent; // Performansı ölçülen ajan (User entity'si)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company; // Hangi şirkete ait olduğu
}