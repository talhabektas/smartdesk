package com.example.smartdeskbackend.entity.base;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Tüm entity sınıfları için temel abstract sınıf
 * Ortak ID alanı ve equals/hashCode implementasyonları
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Constructors
    protected BaseEntity() {}

    protected BaseEntity(Long id) {
        this.id = id;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Entity'nin yeni oluşturulmuş olup olmadığını kontrol eder
     */
    public boolean isNew() {
        return this.id == null;
    }

    // equals() ve hashCode() implementasyonları
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BaseEntity that = (BaseEntity) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d}", getClass().getSimpleName(), id);
    }
}
