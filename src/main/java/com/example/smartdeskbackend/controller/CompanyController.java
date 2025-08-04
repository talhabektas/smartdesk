package com.example.smartdeskbackend.controller;

import com.example.smartdeskbackend.dto.request.company.CreateCompanyRequest;
import com.example.smartdeskbackend.dto.request.company.UpdateCompanyRequest;
import com.example.smartdeskbackend.dto.response.company.CompanyResponse;
import com.example.smartdeskbackend.service.CompanyService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Company management REST Controller
 */
@RestController
@RequestMapping("/v1/companies")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class CompanyController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    @Autowired
    private CompanyService companyService;

    /**
     * Şirket listesini getir (sadece SUPER_ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAllActiveCompanies() {
        logger.info("Getting all active companies");

        try {
            List<CompanyResponse> companies = companyService.getAllActiveCompanies();

            Map<String, Object> response = new HashMap<>();
            response.put("companies", companies);
            response.put("total", companies.size());
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting companies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("COMPANIES_FETCH_ERROR", e.getMessage()));
        }
    }

    /**
     * ID ile şirket getir
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('MANAGER') and @securityService.isFromSameCompany(#id))")
    public ResponseEntity<?> getCompanyById(@PathVariable Long id) {
        logger.info("Getting company by id: {}", id);

        try {
            CompanyResponse company = companyService.getCompanyById(id);
            return ResponseEntity.ok(company);

        } catch (Exception e) {
            logger.error("Error getting company by id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("COMPANY_NOT_FOUND", e.getMessage()));
        }
    }

    /**
     * Domain ile şirket getir
     */
    @GetMapping("/domain/{domain}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getCompanyByDomain(@PathVariable String domain) {
        logger.info("Getting company by domain: {}", domain);

        try {
            CompanyResponse company = companyService.getCompanyByDomain(domain);
            return ResponseEntity.ok(company);

        } catch (Exception e) {
            logger.error("Error getting company by domain: {}", domain, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("COMPANY_NOT_FOUND", e.getMessage()));
        }
    }

    /**
     * Şirket arama
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> searchCompanies(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {

        logger.info("Searching companies with query: {}", q);

        try {
            Page<CompanyResponse> companies = companyService.searchCompanies(q != null ? q : "", pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("companies", companies.getContent());
            response.put("totalElements", companies.getTotalElements());
            response.put("totalPages", companies.getTotalPages());
            response.put("currentPage", companies.getNumber());
            response.put("size", companies.getSize());
            response.put("hasNext", companies.hasNext());
            response.put("hasPrevious", companies.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching companies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("COMPANY_SEARCH_ERROR", e.getMessage()));
        }
    }

    /**
     * Yeni şirket oluştur (sadece SUPER_ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        logger.info("Creating new company: {}", request.getName());

        try {
            CompanyResponse company = companyService.createCompany(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(company);

        } catch (Exception e) {
            logger.error("Error creating company", e);

            HttpStatus status = e.getMessage().contains("already exists") ?
                    HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("COMPANY_CREATION_ERROR", e.getMessage()));
        }
    }

    /**
     * Şirket güncelle
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('MANAGER') and @securityService.isFromSameCompany(#id))")
    public ResponseEntity<?> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCompanyRequest request) {

        logger.info("Updating company: {}", id);

        try {
            CompanyResponse company = companyService.updateCompany(id, request);
            return ResponseEntity.ok(company);

        } catch (Exception e) {
            logger.error("Error updating company: {}", id, e);

            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            } else if (e.getMessage().contains("already exists")) {
                status = HttpStatus.CONFLICT;
            }

            return ResponseEntity.status(status)
                    .body(createErrorResponse("COMPANY_UPDATE_ERROR", e.getMessage()));
        }
    }

    /**
     * Şirket deaktif et (sadece SUPER_ADMIN)
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deactivateCompany(@PathVariable Long id) {
        logger.info("Deactivating company: {}", id);

        try {
            companyService.deactivateCompany(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Company deactivated successfully");
            response.put("companyId", id);
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deactivating company: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("COMPANY_DEACTIVATION_ERROR", e.getMessage()));
        }
    }

    /**
     * Şirket aktif et (sadece SUPER_ADMIN)
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> activateCompany(@PathVariable Long id) {
        logger.info("Activating company: {}", id);

        try {
            companyService.activateCompany(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Company activated successfully");
            response.put("companyId", id);
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error activating company: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("COMPANY_ACTIVATION_ERROR", e.getMessage()));
        }
    }

    /**
     * Şirket kullanıcı kapasitesi kontrolü
     */
    @GetMapping("/{id}/capacity")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getCompanyCapacity(@PathVariable Long id) {
        logger.info("Getting company capacity: {}", id);

        try {
            boolean canAddMore = companyService.canAddMoreUsers(id);
            int currentUserCount = companyService.getUserCount(id);

            Map<String, Object> response = new HashMap<>();
            response.put("companyId", id);
            response.put("currentUserCount", currentUserCount);
            response.put("canAddMoreUsers", canAddMore);
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting company capacity: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("COMPANY_NOT_FOUND", e.getMessage()));
        }
    }

    /**
     * Standart hata response'u oluşturur
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", java.time.LocalDateTime.now());
        return errorResponse;
    }
}