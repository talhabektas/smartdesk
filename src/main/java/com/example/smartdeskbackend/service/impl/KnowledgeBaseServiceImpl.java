// src/main/java/com/example/smartdeskbackend/service/impl/KnowledgeBaseServiceImpl.java
package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.entity.Company;
import com.example.smartdeskbackend.entity.KbArticle;
import com.example.smartdeskbackend.entity.KbCategory;
import com.example.smartdeskbackend.exception.ResourceNotFoundException;
import com.example.smartdeskbackend.repository.CompanyRepository;
import com.example.smartdeskbackend.repository.KbArticleRepository;
import com.example.smartdeskbackend.repository.KbCategoryRepository;
import com.example.smartdeskbackend.service.KnowledgeBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    @Autowired
    private KbArticleRepository articleRepository;
    @Autowired
    private KbCategoryRepository categoryRepository;
    @Autowired
    private CompanyRepository companyRepository;

    @Override
    @Transactional
    public KbArticle createArticle(KbArticle article, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        article.setCompany(company);

        if (article.getCategory() != null && article.getCategory().getId() != null) {
            KbCategory category = categoryRepository.findById(article.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + article.getCategory().getId()));
            if (!category.getCompany().getId().equals(companyId)) {
                throw new IllegalArgumentException("Category does not belong to the specified company.");
            }
            article.setCategory(category);
        }

        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        article.setPublishedAt(article.isPublished() ? LocalDateTime.now() : null);
        return articleRepository.save(article);
    }

    @Override
    @Transactional
    public KbArticle updateArticle(Long id, KbArticle articleDetails) {
        KbArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge Base Article not found with id: " + id));

        article.setTitle(articleDetails.getTitle());
        article.setContent(articleDetails.getContent());
        article.setKeywords(articleDetails.getKeywords());
        article.setPublished(articleDetails.isPublished());
        article.setUpdatedAt(LocalDateTime.now());

        if (articleDetails.getCategory() != null && articleDetails.getCategory().getId() != null) {
            KbCategory category = categoryRepository.findById(articleDetails.getCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + articleDetails.getCategory().getId()));
            if (!category.getCompany().getId().equals(article.getCompany().getId())) {
                throw new IllegalArgumentException("Category does not belong to the article's company.");
            }
            article.setCategory(category);
        } else {
            article.setCategory(null);
        }

        if (articleDetails.isPublished() && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        } else if (!articleDetails.isPublished()) {
            article.setPublishedAt(null);
        }

        return articleRepository.save(article);
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Knowledge Base Article not found with id: " + id);
        }
        articleRepository.deleteById(id);
    }

    @Override
    public Optional<KbArticle> getArticleById(Long id, Long companyId) {
        return articleRepository.findById(id)
                .filter(article -> article.getCompany().getId().equals(companyId));
    }

    @Override
    public List<KbArticle> searchArticles(String keyword, Long companyId) {
        return articleRepository.findByCompanyIdAndTitleContainingIgnoreCaseOrCompanyIdAndKeywordsContainingIgnoreCase(
                companyId, keyword, companyId, keyword);
    }

    @Override
    @Transactional
    public KbCategory createCategory(KbCategory category, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        category.setCompany(company);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public KbCategory updateCategory(Long id, KbCategory categoryDetails) {
        KbCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge Base Category not found with id: " + id));

        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setActive(categoryDetails.isActive());
        category.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Knowledge Base Category not found with id: " + id);
        }
        // Kategoriye bağlı makalelerin kategorisini null'a çekme
        articleRepository.findByCategoryId(id).forEach(article -> article.setCategory(null));
        categoryRepository.deleteById(id);
    }

    @Override
    public List<KbCategory> getAllCategories(Long companyId) {
        return categoryRepository.findByCompanyId(companyId);
    }
}