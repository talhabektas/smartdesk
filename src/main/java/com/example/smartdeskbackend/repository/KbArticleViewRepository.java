// src/main/java/com/example/smartdeskbackend/repository/KbArticleViewRepository.java
package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.KbArticleView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface KbArticleViewRepository extends JpaRepository<KbArticleView, Long> {
    List<KbArticleView> findByArticleId(Long articleId);
    long countByArticleId(Long articleId); // Bir makalenin toplam görüntülenme sayısı
    long countByArticleIdAndViewTimeBetween(Long articleId, LocalDateTime startDate, LocalDateTime endDate); // Belirli bir tarih aralığındaki görüntülenme sayısı
}