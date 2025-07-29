// src/main/java/com/example/smartdeskbackend/entity/KbCategory.java
package com.example.smartdeskbackend.entity;
import com.example.smartdeskbackend.entity.base.AuditableEntity; // AuditableEntity sınıfı için gerekli import

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "kb_categories")
@Data
@EqualsAndHashCode(callSuper = true)
public class KbCategory extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company; // Multi-tenant yapıya uygun
}