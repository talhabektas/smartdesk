// src/main/java/com/example/smartdeskbackend/entity/KbArticleView.java
package com.example.smartdeskbackend.entity;
import com.example.smartdeskbackend.entity.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "kb_article_views")
@Data
@EqualsAndHashCode(callSuper = true) // AuditableEntity'den miras aldığı için
public class KbArticleView extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime viewTime; // Görüntülenme zamanı
    private String ipAddress; // Görüntüleyen IP adresi (isteğe bağlı)
    private Long userId; // Eğer login olan bir kullanıcı görüntülediyse kullanıcının ID'si (isteğe bağlı)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private KbArticle article; // Hangi makalenin görüntülendiği
}