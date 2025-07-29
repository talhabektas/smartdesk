// src/main/java/com/example/smartdeskbackend/service/impl/SystemSettingServiceImpl.java
package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.entity.Company;
import com.example.smartdeskbackend.entity.SystemSetting;
import com.example.smartdeskbackend.exception.ResourceNotFoundException;
import com.example.smartdeskbackend.repository.CompanyRepository;
import com.example.smartdeskbackend.repository.SystemSettingRepository;
import com.example.smartdeskbackend.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SystemSettingServiceImpl implements SystemSettingService {

    @Autowired
    private SystemSettingRepository settingRepository;
    @Autowired
    private CompanyRepository companyRepository;

    @Override
    @Transactional
    public SystemSetting createSetting(SystemSetting setting, Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));
        setting.setCompany(company);

        // Aynı şirket ve anahtar için zaten bir ayar var mı kontrol et
        if (settingRepository.findByCompanyIdAndSettingKey(companyId, setting.getSettingKey()).isPresent()) {
            throw new IllegalArgumentException("A system setting with this key already exists for this company.");
        }

        setting.setCreatedAt(LocalDateTime.now());
        setting.setUpdatedAt(LocalDateTime.now());
        return settingRepository.save(setting);
    }

    @Override
    @Transactional
    public SystemSetting updateSetting(Long id, SystemSetting settingDetails) {
        SystemSetting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("System Setting not found with id: " + id));

        // Şirket kontrolü: Ayarı güncelleyen kullanıcının ayarın ait olduğu şirkete yetkisi olmalı.

        setting.setSettingValue(settingDetails.getSettingValue());
        setting.setDescription(settingDetails.getDescription());
        setting.setPublic(settingDetails.isPublic());
        setting.setUpdatedAt(LocalDateTime.now());
        return settingRepository.save(setting);
    }

    @Override
    @Transactional
    public void deleteSetting(Long id) {
        if (!settingRepository.existsById(id)) {
            throw new ResourceNotFoundException("System Setting not found with id: " + id);
        }
        settingRepository.deleteById(id);
    }

    @Override
    public Optional<SystemSetting> getSettingByKey(Long companyId, String key) {
        // Multi-tenant filtreleme
        return settingRepository.findByCompanyIdAndSettingKey(companyId, key);
    }

    @Override
    public List<SystemSetting> getAllSettings(Long companyId) {
        // Multi-tenant filtreleme
        return settingRepository.findByCompanyId(companyId);
    }

    @Override
    public List<SystemSetting> getPublicSettings() {
        return settingRepository.findByIsPublicTrue(); // Şirket bağımsız genel açık ayarlar
    }
}