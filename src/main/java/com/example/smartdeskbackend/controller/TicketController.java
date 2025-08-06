package com.example.smartdeskbackend.controller;

import com.example.smartdeskbackend.dto.request.ticket.*;
import com.example.smartdeskbackend.dto.response.ticket.*;
import com.example.smartdeskbackend.enums.TicketPriority;
import com.example.smartdeskbackend.enums.TicketStatus;
import com.example.smartdeskbackend.service.TicketService;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ticket management REST Controller
 */
@RestController
@RequestMapping("/v1/tickets")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketService ticketService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Tüm ticketları getir (sadece SUPER_ADMIN) - ÖNEMLİ: /{id} mapping'den ÖNCE olmalı
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAllTickets(@PageableDefault(size = 20) Pageable pageable) {
        logger.info("Getting ALL tickets for SUPER_ADMIN");

        try {
            Page<TicketResponse> tickets = ticketService.getAllTickets(pageable);

            Map<String, Object> response = createPageResponse(tickets);
            response.put("scope", "all");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting all tickets", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("ALL_TICKETS_FETCH_ERROR", e.getMessage()));
        }
    }

    /**
     * Ticket detaylarını getir - ÖNEMLİ: /all endpoint'inden SONRA olmalı
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT') or @ticketSecurityService.hasAccessToTicket(#id, authentication.principal.id)")
    public ResponseEntity<?> getTicketById(@PathVariable Long id) {
        logger.info("Getting ticket by id: {}", id);

        try {
            TicketDetailResponse ticket = ticketService.getTicketById(id);
            return ResponseEntity.ok(ticket);

        } catch (Exception e) {
            logger.error("Error getting ticket by id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("TICKET_NOT_FOUND", e.getMessage()));
        }
    }

    /**
     * Ticket numarası ile getir
     */
    @GetMapping("/number/{ticketNumber}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT')")
    public ResponseEntity<?> getTicketByNumber(@PathVariable String ticketNumber) {
        logger.info("Getting ticket by number: {}", ticketNumber);

        try {
            TicketDetailResponse ticket = ticketService.getTicketByNumber(ticketNumber);
            return ResponseEntity.ok(ticket);

        } catch (Exception e) {
            logger.error("Error getting ticket by number: {}", ticketNumber, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("TICKET_NOT_FOUND", e.getMessage()));
        }
    }

    /**
     * Şirketteki ticketları getir
     */
    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or ((hasRole('MANAGER') or hasRole('AGENT')) and @securityService.isFromSameCompany(#companyId))")
    public ResponseEntity<?> getTicketsByCompany(
            @PathVariable Long companyId,
            @PageableDefault(size = 20) Pageable pageable) {

        logger.info("Getting tickets by company: {}", companyId);

        try {
            Page<TicketResponse> tickets = ticketService.getTicketsByCompany(companyId, pageable);

            Map<String, Object> response = createPageResponse(tickets);
            response.put("companyId", companyId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting tickets by company: {}", companyId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("TICKETS_FETCH_ERROR", e.getMessage()));
        }
    }

    /**
     * Aktif ticketlar
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT')")
    public ResponseEntity<?> getActiveTickets(
            @RequestParam Long companyId,
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest request) {

        logger.info("Getting active tickets for company: {}", companyId);

        try {
            if (!hasAccessToCompany(request, companyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "Access denied to company data"));
            }

            Page<TicketResponse> tickets = ticketService.getActiveTickets(companyId, pageable);

            Map<String, Object> response = createPageResponse(tickets);
            response.put("companyId", companyId);
            response.put("filter", "active");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting active tickets", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("ACTIVE_TICKETS_ERROR", e.getMessage()));
        }
    }

    /**
     * Atanmamış ticketlar
     */
    @GetMapping("/unassigned")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getUnassignedTickets(
            @RequestParam Long companyId,
            HttpServletRequest request) {

        logger.info("Getting unassigned tickets for company: {}", companyId);

        try {
            if (!hasAccessToCompany(request, companyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "Access denied to company data"));
            }

            List<TicketResponse> tickets = ticketService.getUnassignedTickets(companyId);

            Map<String, Object> response = new HashMap<>();
            response.put("tickets", tickets);
            response.put("companyId", companyId);
            response.put("count", tickets.size());
            response.put("filter", "unassigned");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting unassigned tickets", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("UNASSIGNED_TICKETS_ERROR", e.getMessage()));
        }
    }

    /**
     * SLA riski olan ticketlar
     */
    @GetMapping("/sla-risk")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getTicketsAtSlaRisk(
            @RequestParam Long companyId,
            HttpServletRequest request) {

        logger.info("Getting tickets at SLA risk for company: {}", companyId);

        try {
            if (!hasAccessToCompany(request, companyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "Access denied to company data"));
            }

            List<TicketResponse> tickets = ticketService.getTicketsAtSlaRisk(companyId);

            Map<String, Object> response = new HashMap<>();
            response.put("tickets", tickets);
            response.put("companyId", companyId);
            response.put("count", tickets.size());
            response.put("filter", "sla-risk");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting SLA risk tickets", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("SLA_RISK_TICKETS_ERROR", e.getMessage()));
        }
    }

    /**
     * Ticket arama
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT')")
    public ResponseEntity<?> searchTickets(
            @RequestParam Long companyId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) Long customerId,
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest request) {

        logger.info("Searching tickets in company: {} with query: {}", companyId, q);

        try {
            if (!hasAccessToCompany(request, companyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "Access denied to company data"));
            }

            Page<TicketResponse> tickets;

            if (StringUtils.hasText(q)) {
                tickets = ticketService.searchTickets(companyId, q, pageable);
            } else {
                tickets = ticketService.getTicketsWithFilters(companyId, status, priority,
                        departmentId, agentId, customerId, pageable);
            }

            Map<String, Object> response = createPageResponse(tickets);
            response.put("companyId", companyId);
            response.put("searchQuery", q);
            response.put("filters", Map.of(
                    "status", status,
                    "priority", priority,
                    "departmentId", departmentId,
                    "agentId", agentId,
                    "customerId", customerId
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching tickets", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("TICKET_SEARCH_ERROR", e.getMessage()));
        }
    }

    /**
     * CUSTOMER kullanıcının kendi ticketları - ÖNEMLİ: /customer/{customerId} endpoint'inden ÖNCE olmalı
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getMyTickets(
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest request) {

        logger.info("🎫 CUSTOMER getting own tickets");

        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("UNAUTHORIZED", "Token not found"));
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            logger.info("🎫 CUSTOMER userId: {}", userId);

            Page<TicketResponse> tickets = ticketService.getTicketsByUserId(userId, pageable);

            Map<String, Object> response = createPageResponse(tickets);
            response.put("userId", userId);
            response.put("scope", "my");

            logger.info("🎫 Returning {} tickets for CUSTOMER userId: {}", tickets.getTotalElements(), userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting my tickets", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("MY_TICKETS_ERROR", e.getMessage()));
        }
    }

    /**
     * Müşterinin ticketları
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT') or @customerSecurityService.hasAccessToCustomer(#customerId, authentication.principal.id)")
    public ResponseEntity<?> getTicketsByCustomer(
            @PathVariable Long customerId,
            @PageableDefault(size = 20) Pageable pageable) {

        logger.info("Getting tickets by customer: {}", customerId);

        try {
            Page<TicketResponse> tickets = ticketService.getTicketsByCustomer(customerId, pageable);

            Map<String, Object> response = createPageResponse(tickets);
            response.put("customerId", customerId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting tickets by customer: {}", customerId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("CUSTOMER_TICKETS_ERROR", e.getMessage()));
        }
    }

    /**
     * Agent'ın ticketları
     */
    @GetMapping("/agent/{agentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or #agentId == authentication.principal.id")
    public ResponseEntity<?> getTicketsByAgent(
            @PathVariable Long agentId,
            @PageableDefault(size = 20) Pageable pageable) {

        logger.info("Getting tickets by agent: {}", agentId);

        try {
            Page<TicketResponse> tickets = ticketService.getTicketsByAgent(agentId, pageable);

            Map<String, Object> response = createPageResponse(tickets);
            response.put("agentId", agentId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting tickets by agent: {}", agentId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("AGENT_TICKETS_ERROR", e.getMessage()));
        }
    }

    /**
     * Yeni ticket oluştur
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT') or hasRole('CUSTOMER')")
    public ResponseEntity<?> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            HttpServletRequest httpRequest) {

        logger.info("🎫 Creating new ticket: {}", request.getTitle());
        logger.info("🎫 Ticket creation request details: title={}, companyId={}, customerId={}, departmentId={}, priority={}", 
            request.getTitle(), request.getCompanyId(), request.getCustomerId(), 
            request.getDepartmentId(), request.getPriority());

        try {
            // Company access kontrolü
            logger.info("🔍 Checking company access for companyId: {}", request.getCompanyId());
            if (!hasAccessToCompany(httpRequest, request.getCompanyId())) {
                logger.warn("❌ Access denied for companyId: {}", request.getCompanyId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "Access denied to create ticket in this company"));
            }
            logger.info("✅ Company access granted for companyId: {}", request.getCompanyId());

            // Creator user ID'sini token'dan al (eğer belirtilmemişse)
            if (request.getCreatorUserId() == null) {
                String token = extractTokenFromRequest(httpRequest);
                if (token != null) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    request.setCreatorUserId(userId);
                }
            }

            TicketDetailResponse ticket = ticketService.createTicket(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ticket);

        } catch (Exception e) {
            logger.error("Error creating ticket", e);

            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            }

            return ResponseEntity.status(status)
                    .body(createErrorResponse("TICKET_CREATION_ERROR", e.getMessage()));
        }
    }

    /**
     * Ticket güncelle
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT')")
    public ResponseEntity<?> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketRequest request) {

        logger.info("Updating ticket: {}", id);

        try {
            TicketDetailResponse ticket = ticketService.updateTicket(id, request);
            return ResponseEntity.ok(ticket);

        } catch (Exception e) {
            logger.error("Error updating ticket: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("TICKET_UPDATE_ERROR", e.getMessage()));
        }
    }

    /**
     * Ticket durumunu değiştir
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT')")
    public ResponseEntity<?> changeTicketStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        logger.info("Changing ticket status: {}", id);

        try {
            String statusString = request.get("status");
            if (!StringUtils.hasText(statusString)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("MISSING_STATUS", "Status is required"));
            }

            TicketStatus newStatus = TicketStatus.valueOf(statusString.toUpperCase());

            String token = extractTokenFromRequest(httpRequest);
            Long userId = token != null ? jwtUtil.getUserIdFromToken(token) : null;

            TicketDetailResponse ticket = ticketService.changeTicketStatus(id, newStatus, userId);
            return ResponseEntity.ok(ticket);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid ticket status", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("INVALID_STATUS", "Invalid ticket status"));

        } catch (Exception e) {
            logger.error("Error changing ticket status: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("STATUS_CHANGE_ERROR", e.getMessage()));
        }
    }

    /**
     * Ticket ata
     */
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody AssignTicketRequest request) {

        logger.info("Assigning ticket: {} to agent: {}", id, request.getAgentId());

        try {
            TicketDetailResponse ticket = ticketService.assignTicket(id, request);
            return ResponseEntity.ok(ticket);

        } catch (Exception e) {
            logger.error("Error assigning ticket: {}", id, e);

            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            }

            return ResponseEntity.status(status)
                    .body(createErrorResponse("TICKET_ASSIGNMENT_ERROR", e.getMessage()));
        }
    }

    /**
     * Ticket'ı otomatik ata
     */
    @PatchMapping("/{id}/auto-assign")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> autoAssignTicket(@PathVariable Long id) {
        logger.info("Auto-assigning ticket: {}", id);

        try {
            TicketDetailResponse ticket = ticketService.autoAssignTicket(id);
            return ResponseEntity.ok(ticket);

        } catch (Exception e) {
            logger.error("Error auto-assigning ticket: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("AUTO_ASSIGNMENT_ERROR", e.getMessage()));
        }
    }

    /**
     * Ticket önceliğini değiştir
     */
    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT')")
    public ResponseEntity<?> changeTicketPriority(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        logger.info("Changing ticket priority: {}", id);

        try {
            String priorityString = request.get("priority");
            if (!StringUtils.hasText(priorityString)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("MISSING_PRIORITY", "Priority is required"));
            }

            TicketPriority newPriority = TicketPriority.valueOf(priorityString.toUpperCase());

            String token = extractTokenFromRequest(httpRequest);
            Long userId = token != null ? jwtUtil.getUserIdFromToken(token) : null;

            TicketDetailResponse ticket = ticketService.changeTicketPriority(id, newPriority, userId);
            return ResponseEntity.ok(ticket);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid ticket priority", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("INVALID_PRIORITY", "Invalid ticket priority"));

        } catch (Exception e) {
            logger.error("Error changing ticket priority: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("PRIORITY_CHANGE_ERROR", e.getMessage()));
        }
    }

    /**
     * Ticket'ı escalate et
     */
    @PatchMapping("/{id}/escalate")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> escalateTicket(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        logger.info("Escalating ticket: {}", id);

        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = token != null ? jwtUtil.getUserIdFromToken(token) : null;

            TicketDetailResponse ticket = ticketService.escalateTicket(id, userId);
            return ResponseEntity.ok(ticket);

        } catch (Exception e) {
            logger.error("Error escalating ticket: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("TICKET_ESCALATION_ERROR", e.getMessage()));
        }
    }

    /**
     * Ticket kapat
     */
    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT')")
    public ResponseEntity<?> closeTicket(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        logger.info("Closing ticket: {}", id);

        try {
            String resolutionSummary = request.get("resolutionSummary");

            String token = extractTokenFromRequest(httpRequest);
            Long userId = token != null ? jwtUtil.getUserIdFromToken(token) : null;

            TicketDetailResponse ticket = ticketService.closeTicket(id, resolutionSummary, userId);
            return ResponseEntity.ok(ticket);

        } catch (Exception e) {
            logger.error("Error closing ticket: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("TICKET_CLOSE_ERROR", e.getMessage()));
        }
    }

    /**
     * Ticket yorumları
     */
    @GetMapping("/{id}/comments")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT') or @ticketSecurityService.hasAccessToTicket(#id, authentication.principal.id)")
    public ResponseEntity<?> getTicketComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean includeInternal,
            HttpServletRequest request) {

        logger.info("Getting comments for ticket: {} (includeInternal: {})", id, includeInternal);

        try {
            // Internal comments sadece SUPER_ADMIN, MANAGER ve AGENT görebilir
            if (includeInternal && !hasInternalAccess(request)) {
                includeInternal = false;
            }

            List<TicketCommentResponse> comments = ticketService.getTicketComments(id, includeInternal);

            Map<String, Object> response = new HashMap<>();
            response.put("comments", comments);
            response.put("ticketId", id);
            response.put("includeInternal", includeInternal);
            response.put("count", comments.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting ticket comments: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("COMMENTS_FETCH_ERROR", e.getMessage()));
        }
    }

    /**
     * Ticket'a yorum ekle
     */
    @PostMapping("/{id}/comments")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT') or @ticketSecurityService.hasAccessToTicket(#id, authentication.principal.id)")
    public ResponseEntity<?> addComment(
            @PathVariable Long id,
            @Valid @RequestBody AddCommentRequest request,
            HttpServletRequest httpRequest) {

        logger.info("Adding comment to ticket: {}", id);

        try {
            // Author ID'sini token'dan al (eğer belirtilmemişse)
            if (request.getAuthorId() == null) {
                String token = extractTokenFromRequest(httpRequest);
                if (token != null) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    request.setAuthorId(userId);
                }
            }

            // Internal comment sadece SUPER_ADMIN, MANAGER ve AGENT ekleyebilir
            if (request.getIsInternal() && !hasInternalAccess(httpRequest)) {
                request.setIsInternal(false);
            }

            TicketCommentResponse comment = ticketService.addComment(id, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);

        } catch (Exception e) {
            logger.error("Error adding comment to ticket: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("COMMENT_ADD_ERROR", e.getMessage()));
        }
    }

    /**
     * Ticket'a dosya ekle
     */
    @PostMapping("/{id}/attachments")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT') or @ticketSecurityService.hasAccessToTicket(#id, authentication.principal.id)")
    public ResponseEntity<?> addAttachment(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) {

        logger.info("Adding attachment to ticket: {}", id);

        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = token != null ? jwtUtil.getUserIdFromToken(token) : null;

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("UNAUTHORIZED", "User ID not found in token"));
            }

            String filePath = ticketService.addAttachment(id, file, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Attachment added successfully");
            response.put("ticketId", id);
            response.put("filePath", filePath);
            response.put("originalFileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error adding attachment to ticket: {}", id, e);

            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            } else if (e.getMessage().contains("size") || e.getMessage().contains("type")) {
                status = HttpStatus.PAYLOAD_TOO_LARGE;
            }

            return ResponseEntity.status(status)
                    .body(createErrorResponse("ATTACHMENT_ADD_ERROR", e.getMessage()));
        }
    }

    /**
     * Müşteri memnuniyet puanı ekle
     */
    @PostMapping("/{id}/satisfaction")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or @ticketSecurityService.isTicketCustomer(#id, authentication.principal.id)")
    public ResponseEntity<?> addSatisfactionRating(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        logger.info("Adding satisfaction rating to ticket: {}", id);

        try {
            Integer rating = (Integer) request.get("rating");
            String feedback = (String) request.get("feedback");

            if (rating == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("MISSING_RATING", "Rating is required"));
            }

            ticketService.addSatisfactionRating(id, rating, feedback);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Satisfaction rating added successfully");
            response.put("ticketId", id);
            response.put("rating", rating);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error adding satisfaction rating to ticket: {}", id, e);

            HttpStatus status = e.getMessage().contains("not found") ?
                    HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(createErrorResponse("SATISFACTION_ADD_ERROR", e.getMessage()));
        }
    }

    /**
     * Ticket istatistikleri
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getTicketStats(
            @RequestParam Long companyId,
            HttpServletRequest request) {

        logger.info("Getting ticket statistics for company: {}", companyId);

        try {
            if (!hasAccessToCompany(request, companyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "Access denied to company data"));
            }

            Map<String, Object> stats = ticketService.getTicketStats(companyId);
            stats.put("companyId", companyId);
            stats.put("generatedAt", LocalDateTime.now());

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("Error getting ticket statistics", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("TICKET_STATS_ERROR", e.getMessage()));
        }
    }

    /**
     * Ticket oluşturma trendi
     */
    @GetMapping("/trends/creation")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getTicketCreationTrend(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "30") int days,
            HttpServletRequest request) {

        logger.info("Getting ticket creation trend for company: {} ({} days)", companyId, days);

        try {
            if (!hasAccessToCompany(request, companyId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("ACCESS_DENIED", "Access denied to company data"));
            }

            List<Object[]> trendData = ticketService.getTicketCreationTrend(companyId, days);

            Map<String, Object> response = new HashMap<>();
            response.put("trendData", trendData);
            response.put("companyId", companyId);
            response.put("days", days);
            response.put("generatedAt", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting ticket creation trend", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("TREND_DATA_ERROR", e.getMessage()));
        }
    }

    // ============ Helper Methods ============

    /**
     * JWT token'dan kullanıcının company access'i olup olmadığını kontrol eder
     */
    private boolean hasAccessToCompany(HttpServletRequest request, Long companyId) {
        try {
            String token = extractTokenFromRequest(request);
            logger.info("🔍 Debug hasAccessToCompany - token exists: {}", token != null);
            
            if (token == null) {
                logger.warn("⚠️ No token found in request");
                return false;
            }

            String role = jwtUtil.getRoleFromToken(token);
            Long userCompanyId = jwtUtil.getCompanyIdFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            String email = jwtUtil.getEmailFromToken(token);
            
            logger.info("🔍 Debug hasAccessToCompany - Details: userId={}, email={}, role={}, userCompanyId={}, requestedCompanyId={}", 
                userId, email, role, userCompanyId, companyId);
            
            if ("SUPER_ADMIN".equals(role)) {
                logger.info("✅ SUPER_ADMIN access granted");
                return true;
            }

            boolean hasAccess = companyId.equals(userCompanyId);
            logger.info("🔍 Company access check result: userCompanyId={} == requestedCompanyId={} = {}", 
                userCompanyId, companyId, hasAccess);
            
            return hasAccess;

        } catch (Exception e) {
            logger.error("❌ Error checking company access", e);
            return false;
        }
    }

    /**
     * Internal access kontrolü (SUPER_ADMIN, MANAGER, AGENT)
     */
    private boolean hasInternalAccess(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) return false;

            String role = jwtUtil.getRoleFromToken(token);
            return "SUPER_ADMIN".equals(role) || "MANAGER".equals(role) || "AGENT".equals(role);

        } catch (Exception e) {
            logger.error("Error checking internal access", e);
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
    private Map<String, Object> createPageResponse(Page<TicketResponse> tickets) {
        Map<String, Object> response = new HashMap<>();
        response.put("tickets", tickets.getContent());
        response.put("totalElements", tickets.getTotalElements());
        response.put("totalPages", tickets.getTotalPages());
        response.put("currentPage", tickets.getNumber());
        response.put("size", tickets.getSize());
        response.put("hasNext", tickets.hasNext());
        response.put("hasPrevious", tickets.hasPrevious());
        return response;
    }

    // ============ APPROVAL WORKFLOW ENDPOINTS ============

    /**
     * AGENT/MANAGER ticket'ı RESOLVED yapıp MANAGER onayına gönder
     */
    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER') or hasRole('AGENT')")
    public ResponseEntity<?> resolveTicket(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody,
            HttpServletRequest request) {

        logger.info("🎯 Resolving ticket for manager approval: {}", id);

        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("UNAUTHORIZED", "Token not found"));
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            String resolutionSummary = requestBody != null ? requestBody.get("resolutionSummary") : "";

            // Ticket'ı RESOLVED duruma getir ve MANAGER onayına gönder
            TicketDetailResponse ticket = ticketService.resolveTicketForApproval(id, resolutionSummary, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ticket resolved and sent for manager approval");
            response.put("ticket", ticket);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error resolving ticket: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("RESOLVE_TICKET_ERROR", e.getMessage()));
        }
    }

    /**
     * MANAGER ticket'ı onaylar - SUPER_ADMIN onayına gönderir
     */
    @PatchMapping("/{id}/approve-manager")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> approveByManager(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody,
            HttpServletRequest request) {

        logger.info("🎯 Manager approving ticket: {}", id);

        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("UNAUTHORIZED", "Token not found"));
            }

            Long managerId = jwtUtil.getUserIdFromToken(token);
            String approvalComment = requestBody != null ? requestBody.get("approvalComment") : "";

            // MANAGER onayı - SUPER_ADMIN onayına gönder
            TicketDetailResponse ticket = ticketService.approveByManager(id, approvalComment, managerId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ticket approved by manager and sent for admin approval");
            response.put("ticket", ticket);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error in manager approval: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("MANAGER_APPROVAL_ERROR", e.getMessage()));
        }
    }

    /**
     * SUPER_ADMIN ticket'ı final onay verir - CLOSED yapar
     */
    @PatchMapping("/{id}/approve-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> approveByAdmin(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody,
            HttpServletRequest request) {

        logger.info("🎯 Admin giving final approval to ticket: {}", id);

        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("UNAUTHORIZED", "Token not found"));
            }

            Long adminId = jwtUtil.getUserIdFromToken(token);
            String finalComment = requestBody != null ? requestBody.get("finalComment") : "";

            // SUPER_ADMIN final onayı - CLOSED yapar
            TicketDetailResponse ticket = ticketService.approveByAdmin(id, finalComment, adminId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ticket given final approval and closed");
            response.put("ticket", ticket);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error in admin approval: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("ADMIN_APPROVAL_ERROR", e.getMessage()));
        }
    }

    /**
     * Onayı reddet - önceki duruma geri döndür
     */
    @PatchMapping("/{id}/reject-approval")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> rejectApproval(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {

        logger.info("🎯 Rejecting approval for ticket: {}", id);

        try {
            String token = extractTokenFromRequest(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("UNAUTHORIZED", "Token not found"));
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            String rejectionReason = requestBody.get("rejectionReason");

            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("VALIDATION_ERROR", "Rejection reason is required"));
            }

            // Onayı reddet
            TicketDetailResponse ticket = ticketService.rejectApproval(id, rejectionReason, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Approval rejected");
            response.put("ticket", ticket);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("❌ Error rejecting approval: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("REJECT_APPROVAL_ERROR", e.getMessage()));
        }
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