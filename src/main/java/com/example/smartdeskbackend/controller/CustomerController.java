package com.example.smartdeskbackend.controller;

import com.example.smartdeskbackend.dto.request.customer.CreateCustomerRequest;
import com.example.smartdeskbackend.dto.request.customer.UpdateCustomerRequest;
import com.example.smartdeskbackend.dto.response.customer.CustomerResponse;
import com.example.smartdeskbackend.dto.response.customer.CustomerDetailResponse;
import com.example.smartdeskbackend.service.CustomerService;
import com.example.smartdeskbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Customer management REST Controller
 */
@RestController
@RequestMapping("/api/v1/customers")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Customer detaylarını getir
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT') or @customerSecurityService.hasAccessToCustomer(#id, authentication.principal.id)")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        logger.info("Getting customer by id: {}", id);

        try {
            CustomerDetailResponse customer = customerService.getCustomerById(id);
            return ResponseEntity.ok(customer);

        } catch (Exception e) {
            logger.error("Error getting customer by id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("CUSTOMER_NOT_FOUND", e.getMessage()));
        }
    }

    /**
     * Email ile customer getir
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT')")
    public ResponseEntity<?> getCustomerByEmail(@PathVariable String email) {
        logger.info("Getting customer by email: {}", email);

        try {
            CustomerDetailResponse customer = customerService.getCustomerByEmail(email);
            return ResponseEntity.ok(customer);

        } catch (Exception e) {
            logger.error("Error getting customer by email: {}", email, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("CUSTOMER_NOT_FOUND", e.getMessage()));
        }
    }

    /**
     * Şirketteki customerları getir
     */
    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or (hasRole('MANAGER') and @securityService.isFromSameCompany(#companyId))")
    public ResponseEntity<?> getCustomersByCompany(
            @PathVariable Long companyId,
            @PageableDefault(size = 20) Pageable pageable) {

        logger.info("Getting customers by company: {}", companyId);

        try {
            Page<CustomerResponse> customers = customerService.getCustomersByCompany(companyId, pageable);

            Map<String, Object> response = createPageResponse(customers);
            response.put("companyId", companyId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting customers by company: {}", companyId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("CUSTOMERS_FETCH_ERROR", e.getMessage()));
        }
    }

    /**
     * Customer arama
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT')")
    public ResponseEntity<?> searchCustomers(
            @RequestParam Long companyId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest request) {

        logger.info("Searching customers in company: {} with query: {}", companyId, q);

        try {
            if (!hasAccessToCompany(request, companyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "Access denied to company data"));
            }

            Page<CustomerResponse> customers = customerService.searchCustomers(companyId, q != null ? q : "", pageable);

            Map<String, Object> response = createPageResponse(customers);
            response.put("companyId", companyId);
            response.put("searchQuery", q);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching customers", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("CUSTOMER_SEARCH_ERROR", e.getMessage()));
        }
    }

    /**
     * VIP customerları getir
     */
    @GetMapping("/vip")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getVipCustomers(
            @RequestParam Long companyId,
            HttpServletRequest request) {

        logger.info("Getting VIP customers for company: {}", companyId);

        try {
            if (!hasAccessToCompany(request, companyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "Access denied to company data"));
            }

            List<CustomerResponse> vipCustomers = customerService.getVipCustomers(companyId);

            Map<String, Object> response = new HashMap<>();
            response.put("vipCustomers", vipCustomers);
            response.put("companyId", companyId);
            response.put("count", vipCustomers.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting VIP customers", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("VIP_CUSTOMERS_ERROR", e.getMessage()));
        }
    }

    /**
     * Yeni customer oluştur
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT')")
    public ResponseEntity<?> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request,
            HttpServletRequest httpRequest) {

        logger.info("Creating new customer: {}", request.getEmail());

        try {
            // Company access kontrolü
            if (!hasAccessToCompany(httpRequest, request.getCompanyId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "Access denied to create customer in this company"));
            }

            CustomerDetailResponse customer = customerService.createCustomer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(customer);

        } catch (Exception e) {
            logger.error("Error creating customer", e);

            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("already exists")) {
                status = HttpStatus.CONFLICT;
            } else if (e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            }

            return ResponseEntity.status(status)
                    .body(createErrorResponse("CUSTOMER_CREATION_ERROR", e.getMessage()));
        }
    }

    /**
     * Customer güncelle
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT')")
    public ResponseEntity<?> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {

        logger.info("Updating customer: {}", id);

        try {
            CustomerDetailResponse customer = customerService.updateCustomer(id, request);
            return ResponseEntity.ok(customer);

        } catch (Exception e) {
            logger.error("Error updating customer: {}", id, e);

            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            } else if (e.getMessage().contains("already exists")) {
                status = HttpStatus.CONFLICT;
            }

            return ResponseEntity.status(status)
                    .body(createErrorResponse("CUSTOMER_UPDATE_ERROR", e.getMessage()));
        }
    }

    /**
     * Customer sil (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        logger.info("Deleting customer: {}", id);

        try {
            customerService.deleteCustomer(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Customer deleted successfully");
            response.put("customerId", id);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting customer: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("CUSTOMER_DELETION_ERROR", e.getMessage()));
        }
    }

    /**
     * Customer istatistikleri
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getCustomerStats(
            @RequestParam Long companyId,
            HttpServletRequest request) {

        logger.info("Getting customer statistics for company: {}", companyId);

        try {
            if (!hasAccessToCompany(request, companyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "Access denied to company data"));
            }

            // Bu method'u CustomerService'e eklemek gerekecek
            Map<String, Object> stats = new HashMap<>();

            // VIP customer sayısı
            List<CustomerResponse> vipCustomers = customerService.getVipCustomers(companyId);
            stats.put("vipCount", vipCustomers.size());

            // Toplam customer sayısı (bu method'u da eklemek gerekecek)
            stats.put("totalCount", "TODO: Add total count method");

            stats.put("companyId", companyId);
            stats.put("generatedAt", LocalDateTime.now());

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("Error getting customer statistics", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("CUSTOMER_STATS_ERROR", e.getMessage()));
        }
    }

    // ============ Helper Methods ============

    /**
     * JWT token'dan kullanıcının company access'i olup olmadığını kontrol eder
     */
    private boolean hasAccessToCompany(HttpServletRequest request, Long companyId) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) return false;

            String role = jwtUtil.getRoleFromToken(token);
            if ("SUPER_ADMIN".equals(role)) return true;

            Long userCompanyId = jwtUtil.getCompanyIdFromToken(token);
            return companyId.equals(userCompanyId);

        } catch (Exception e) {
            logger.error("Error checking company access", e);
            return false;
        }
    }

    /**
     * HTTP request'ten JWT token'ı çıkarır
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Page response oluşturur
     */
    private Map<String, Object> createPageResponse(Page<CustomerResponse> customers) {
        Map<String, Object> response = new HashMap<>();
        response.put("customers", customers.getContent());
        response.put("totalElements", customers.getTotalElements());
        response.put("totalPages", customers.getTotalPages());
        response.put("currentPage", customers.getNumber());
        response.put("size", customers.getSize());
        response.put("hasNext", customers.hasNext());
        response.put("hasPrevious", customers.hasPrevious());
        return response;
    }

    /**
     * Standart hata response'u oluşturur
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());
        return errorResponse;
    }
}