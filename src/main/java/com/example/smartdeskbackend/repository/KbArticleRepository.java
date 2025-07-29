// src/main/java/com/example/smartdeskbackend/repository/KbArticleRepository.java
package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.KbArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KbArticleRepository extends JpaRepository<KbArticle, Long> {
    List<KbArticle> findByCompanyId(Long companyId);
    List<KbArticle> findByCategoryId(Long categoryId);
    List<KbArticle> findByTitleContainingIgnoreCaseOrKeywordsContainingIgnoreCase(String title, String keywords);
    // KnowledgeBaseServiceImpl'deki searchArticles metodu için multi-tenant uyumlu sorgu:
    List<KbArticle> findByCompanyIdAndTitleContainingIgnoreCaseOrCompanyIdAndKeywordsContainingIgnoreCase(Long companyId1, String title, Long companyId2, String keywords);
}