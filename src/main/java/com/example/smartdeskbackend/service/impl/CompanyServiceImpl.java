package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.dto.request.company.CreateCompanyRequest;
import com.example.smartdeskbackend.dto.request.company.UpdateCompanyRequest;
import com.example.smartdeskbackend.dto.response.company.CompanyResponse;
import com.example.smartdeskbackend.entity.Company;
import com.example.smartdeskbackend.exception.BusinessLogicException;
import com.example.smartdeskbackend.exception.ResourceNotFoundException;
import com.example.smartdeskbackend.exception.UserAlreadyExistsException;
import com.example.smartdeskbackend.repository.CompanyRepository;
import com.example.smartdeskbackend.service.CompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Company service implementation
 */
@Service
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyServiceImpl.class);

    @Autowired
    private CompanyRepository companyRepository;

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long id) {
        logger.debug("Getting company by id: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));

        return mapToResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyByDomain(String domain) {
        logger.debug("Getting company by domain: {}", domain);

        Company company = companyRepository.findByDomain(domain)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with domain: " + domain));

        return mapToResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllActiveCompanies() {
        logger.debug("Getting all active companies");

        List<Company> companies = companyRepository.findByIsActiveTrue();
        return companies.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyResponse> searchCompanies(String searchTerm, Pageable pageable) {
        logger.debug("Searching companies with term: {}", searchTerm);

        Page<Company> companies = companyRepository.findByNameContainingIgnoreCase(searchTerm, pageable);
        return companies.map(this::mapToResponse);
    }

    @Override
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        logger.info("Creating new company: {}", request.getName());

        // Domain uniqueness kontrolü
        if (request.getDomain() != null && companyRepository.existsByDomain(request.getDomain())) {
            throw new UserAlreadyExistsException("Company already exists with domain: " + request.getDomain());
        }

        Company company = new Company();
        company.setName(request.getName());
        company.setDomain(request.getDomain());
        company.setPhone(request.getPhone());
        company.setAddress(request.getAddress());
        company.setWebsite(request.getWebsite());
        company.setTimezone(request.getTimezone() != null ? request.getTimezone() : "Europe/Istanbul");
        company.setPlanType(request.getPlanType() != null ? request.getPlanType() : "BASIC");
        company.setMaxUsers(request.getMaxUsers() != null ? request.getMaxUsers() : 10);
        company.setMaxTicketsPerMonth(request.getMaxTicketsPerMonth() != null ? request.getMaxTicketsPerMonth() : 100);
        company.setIsActive(true);

        company = companyRepository.save(company);

        logger.info("Company created successfully with id: {}", company.getId());
        return mapToResponse(company);
    }

    @Override
    public CompanyResponse updateCompany(Long id, UpdateCompanyRequest request) {
        logger.info("Updating company: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));

        // Domain değişikliği kontrolü
        if (request.getDomain() != null && !request.getDomain().equals(company.getDomain())) {
            if (companyRepository.existsByDomain(request.getDomain())) {
                throw new UserAlreadyExistsException("Domain already exists: " + request.getDomain());
            }
            company.setDomain(request.getDomain());
        }

        // Diğer alanları güncelle
        if (request.getName() != null) {
            company.setName(request.getName());
        }
        if (request.getPhone() != null) {
            company.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            company.setAddress(request.getAddress());
        }
        if (request.getWebsite() != null) {
            company.setWebsite(request.getWebsite());
        }
        if (request.getTimezone() != null) {
            company.setTimezone(request.getTimezone());
        }
        if (request.getPlanType() != null) {
            company.setPlanType(request.getPlanType());
        }
        if (request.getMaxUsers() != null) {
            company.setMaxUsers(request.getMaxUsers());
        }
        if (request.getMaxTicketsPerMonth() != null) {
            company.setMaxTicketsPerMonth(request.getMaxTicketsPerMonth());
        }
        if (request.getLogoUrl() != null) {
            company.setLogoUrl(request.getLogoUrl());
        }

        company = companyRepository.save(company);

        logger.info("Company updated successfully: {}", id);
        return mapToResponse(company);
    }

    @Override
    public void deactivateCompany(Long id) {
        logger.info("Deactivating company: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));

        if (!company.isActive()) {
            throw new BusinessLogicException("Company is already inactive");
        }

        company.deactivate();
        companyRepository.save(company);

        logger.info("Company deactivated successfully: {}", id);
    }

    @Override
    public void activateCompany(Long id) {
        logger.info("Activating company: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));

        if (company.isActive()) {
            throw new BusinessLogicException("Company is already active");
        }

        company.activate();
        companyRepository.save(company);

        logger.info("Company activated successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAddMoreUsers(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        return company.canAddMoreUsers();
    }

    @Override
    @Transactional(readOnly = true)
    public int getUserCount(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        return company.getUserCount();
    }

    /**
     * Entity'yi Response DTO'ya map eder
     */
    private CompanyResponse mapToResponse(Company company) {
        CompanyResponse response = new CompanyResponse();
        response.setId(company.getId());
        response.setName(company.getName());
        response.setDomain(company.getDomain());
        response.setPhone(company.getPhone());
        response.setAddress(company.getAddress());
        response.setWebsite(company.getWebsite());
        response.setLogoUrl(company.getLogoUrl());
        response.setTimezone(company.getTimezone());
        response.setPlanType(company.getPlanType());
        response.setMaxUsers(company.getMaxUsers());
        response.setMaxTicketsPerMonth(company.getMaxTicketsPerMonth());
        response.setIsActive(company.getIsActive());
        response.setUserCount(company.getUserCount());
        response.setCreatedAt(company.getCreatedAt());
        response.setUpdatedAt(company.getUpdatedAt());
        return response;
    }
}