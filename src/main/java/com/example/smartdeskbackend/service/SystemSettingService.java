package com.example.smartdeskbackend.service;

import com.example.smartdeskbackend.entity.SystemSetting;
import java.util.List;
import java.util.Optional;

public interface SystemSettingService {
    SystemSetting createSetting(SystemSetting setting, Long companyId);
    SystemSetting updateSetting(Long id, SystemSetting settingDetails);
    void deleteSetting(Long id);
    Optional<SystemSetting> getSettingByKey(Long companyId, String key);
    List<SystemSetting> getAllSettings(Long companyId);
    List<SystemSetting> getPublicSettings();
}