// src/main/java/com/example/smartdeskbackend/repository/SystemSettingRepository.java
package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {
    Optional<SystemSetting> findByCompanyIdAndSettingKey(Long companyId, String settingKey); // Şirket ve anahtara göre ayar bul
    List<SystemSetting> findByCompanyId(Long companyId); // Bir şirkete ait tüm ayarları getir
    List<SystemSetting> findByIsPublicTrue(); // Herkese açık ayarları getir (company_id'den bağımsız)
}