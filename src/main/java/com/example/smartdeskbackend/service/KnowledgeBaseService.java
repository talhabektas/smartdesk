package com.example.smartdeskbackend.service;
import com.example.smartdeskbackend.entity.KbArticle;
import com.example.smartdeskbackend.entity.KbCategory;
import java.util.List;
import java.util.Optional;

public interface KnowledgeBaseService {
    KbArticle createArticle(KbArticle article, Long companyId);
    KbArticle updateArticle(Long id, KbArticle article);
    void deleteArticle(Long id);
    Optional<KbArticle> getArticleById(Long id, Long companyId);
    List<KbArticle> searchArticles(String keyword, Long companyId);
    KbCategory createCategory(KbCategory category, Long companyId);
    KbCategory updateCategory(Long id, KbCategory category);
    void deleteCategory(Long id);
    List<KbCategory> getAllCategories(Long companyId);
}