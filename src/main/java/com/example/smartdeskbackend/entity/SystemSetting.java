// src/main/java/com/example/smartdeskbackend/entity/SystemSetting.java
package com.example.smartdeskbackend.entity;

import com.example.smartdeskbackend.entity.base.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "system_settings")
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemSetting extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String settingKey;
    @Column(columnDefinition = "TEXT")
    private String settingValue;
    private String description;
    private boolean isPublic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
}