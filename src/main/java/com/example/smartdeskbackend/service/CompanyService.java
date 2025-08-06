package com.example.smartdeskbackend.service;

import com.example.smartdeskbackend.dto.request.company.CreateCompanyRequest;
import com.example.smartdeskbackend.dto.request.company.UpdateCompanyRequest;
import com.example.smartdeskbackend.dto.response.company.CompanyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Company service interface
 */
public interface CompanyService {

    /**
     * ID ile şirket getir
     */
    CompanyResponse getCompanyById(Long id);

    /**
     * Domain ile şirket getir
     */
    CompanyResponse getCompanyByDomain(String domain);

    /**
     * Tüm aktif şirketleri getir
     */
    List<CompanyResponse> getAllActiveCompanies();

    /**
     * Şirket arama
     */
    Page<CompanyResponse> searchCompanies(String searchTerm, Pageable pageable);

    /**
     * Yeni şirket oluştur
     */
    CompanyResponse createCompany(CreateCompanyRequest request);

    /**
     * Şirket güncelle
     */
    CompanyResponse updateCompany(Long id, UpdateCompanyRequest request);

    /**
     * Şirket deaktif et
     */
    void deactivateCompany(Long id);

    /**
     * Şirket aktif et
     */
    void activateCompany(Long id);

    /**
     * Şirket sil
     */
    void deleteCompany(Long id);

    /**
     * Şirketin daha fazla kullanıcı ekleyip ekleyemeyeceğini kontrol et
     */
    boolean canAddMoreUsers(Long companyId);

    /**
     * Şirketteki kullanıcı sayısını getir
     */
    int getUserCount(Long companyId);
}