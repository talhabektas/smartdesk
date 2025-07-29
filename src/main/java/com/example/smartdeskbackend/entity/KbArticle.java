// src/main/java/com/example/smartdeskbackend/entity/KbArticle.java
package com.example.smartdeskbackend.entity;
import com.example.smartdeskbackend.entity.base.AuditableEntity; // AuditableEntity sınıfı için gerekli import
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "kb_articles")
@Data
@EqualsAndHashCode(callSuper = true) // AuditableEntity'den miras aldığı için
public class KbArticle extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    private String keywords;
    private boolean published;
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private KbCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company; // Multi-tenant yapıya uygun

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KbArticleView> views; // KbArticleView entity'si de olmalı
}