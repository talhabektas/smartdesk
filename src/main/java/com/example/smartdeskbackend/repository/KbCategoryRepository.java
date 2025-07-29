// src/main/java/com/example/smartdeskbackend/repository/KbCategoryRepository.java
package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.KbCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KbCategoryRepository extends JpaRepository<KbCategory, Long> {
    List<KbCategory> findByCompanyId(Long companyId);
    Optional<KbCategory> findByCompanyIdAndName(Long companyId, String name);
}