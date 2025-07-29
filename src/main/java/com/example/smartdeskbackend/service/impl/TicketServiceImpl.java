package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.dto.request.ticket.*;
import com.example.smartdeskbackend.dto.response.ticket.*;
import com.example.smartdeskbackend.entity.*;
import com.example.smartdeskbackend.enums.*;
import com.example.smartdeskbackend.exception.*;
import com.example.smartdeskbackend.repository.*;
import com.example.smartdeskbackend.service.TicketService;
import com.example.smartdeskbackend.service.FileService;
import com.example.smartdeskbackend.integration.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Ticket service implementation
 * Ticket yönetimi için tüm business logic operations
 */
@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TicketCommentRepository ticketCommentRepository;

    @Autowired
    private TicketAttachmentRepository ticketAttachmentRepository;

    @Autowired
    private TicketHistoryRepository ticketHistoryRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private EmailService emailService;

    // ============ TEMEL CRUD OPERASYONLARI ============

    @Override
    @Transactional(readOnly = true)
    public TicketDetailResponse getTicketById(Long id) {
        logger.debug("Getting ticket by id: {}", id);

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        return mapToDetailResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDetailResponse getTicketByNumber(String ticketNumber) {
        logger.debug("Getting ticket by number: {}", ticketNumber);

        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));

        return mapToDetailResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTicketsByCompany(Long companyId, Pageable pageable) {
        logger.debug("Getting tickets by company: {}", companyId);

        Page<Ticket> tickets = ticketRepository.findByCompanyId(companyId, pageable);
        return tickets.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTicketsByCustomer(Long customerId, Pageable pageable) {
        logger.debug("Getting tickets by customer: {}", customerId);

        Page<Ticket> tickets = ticketRepository.findByCustomerId(customerId, pageable);
        return tickets.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTicketsByAgent(Long agentId, Pageable pageable) {
        logger.debug("Getting tickets by agent: {}", agentId);

        Page<Ticket> tickets = ticketRepository.findByAssignedAgentId(agentId, pageable);
        return tickets.map(this::mapToResponse);
    }

    // ============ ARAMA ve FİLTRELEME ============

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> searchTickets(Long companyId, String searchTerm, Pageable pageable) {
        logger.debug("Searching tickets in company: {} with term: {}", companyId, searchTerm);

        Page<Ticket> tickets = ticketRepository.searchTickets(companyId, searchTerm, pageable);
        return tickets.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTicketsWithFilters(Long companyId, TicketStatus status,
                                                      TicketPriority priority, Long departmentId,
                                                      Long agentId, Long customerId, Pageable pageable) {
        logger.debug("Getting tickets with filters for company: {}", companyId);

        Page<Ticket> tickets = ticketRepository.findTicketsWithFilters(
                companyId, status, priority, departmentId, agentId, customerId, pageable);
        return tickets.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getActiveTickets(Long companyId, Pageable pageable) {
        logger.debug("Getting active tickets for company: {}", companyId);

        Page<Ticket> tickets = ticketRepository.findActiveTickets(companyId, pageable);
        return tickets.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getUnassignedTickets(Long companyId) {
        logger.debug("Getting unassigned tickets for company: {}", companyId);

        List<Ticket> tickets = ticketRepository.findUnassignedTickets(companyId);
        return tickets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsAtSlaRisk(Long companyId) {
        logger.debug("Getting tickets at SLA risk for company: {}", companyId);

        // 2 saat içinde SLA'sı dolan ticketları risky kabul et
        LocalDateTime riskTime = LocalDateTime.now().plusHours(2);
        List<Ticket> tickets = ticketRepository.findTicketsAtRiskOfSlaViolation(companyId, riskTime);

        return tickets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ============ TİCKET OLUŞTURMA ve GÜNCELLEME ============

    @Override
    public TicketDetailResponse createTicket(CreateTicketRequest request) {
        logger.info("Creating new ticket: {}", request.getTitle());

        // Company kontrolü
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + request.getCompanyId()));

        // Customer bilgileri
        if (ticket.getCustomer() != null) {
            response.setCustomerName(ticket.getCustomer().getFullName());
            response.setCustomerEmail(ticket.getCustomer().getEmail());
        }

        // Agent bilgileri
        if (ticket.getAssignedAgent() != null) {
            response.setAssignedAgentName(ticket.getAssignedAgent().getFullName());
        }

        // Department bilgileri
        if (ticket.getDepartment() != null) {
            response.setDepartmentName(ticket.getDepartment().getName());
        }

        return response;
    }

    /**
     * Ticket entity'sini TicketDetailResponse'a map eder
     */
    private TicketDetailResponse mapToDetailResponse(Ticket ticket) {
        TicketDetailResponse response = new TicketDetailResponse();
        response.setId(ticket.getId());
        response.setTicketNumber(ticket.getTicketNumber());
        response.setTitle(ticket.getTitle());
        response.setDescription(ticket.getDescription());
        response.setStatus(ticket.getStatus().getCode());
        response.setStatusDisplayName(ticket.getStatus().getDisplayName());
        response.setPriority(ticket.getPriority().getCode());
        response.setPriorityDisplayName(ticket.getPriority().getDisplayName());
        response.setCategory(ticket.getCategory() != null ? ticket.getCategory().getCode() : null);
        response.setSource(ticket.getSource().getCode());
        response.setIsInternal(ticket.getIsInternal());
        response.setEscalationLevel(ticket.getEscalationLevel());
        response.setResolutionSummary(ticket.getResolutionSummary());
        response.setCustomerSatisfactionRating(ticket.getCustomerSatisfactionRating());
        response.setCustomerSatisfactionFeedback(ticket.getCustomerSatisfactionFeedback());

        // Timestamps
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        response.setLastActivityAt(ticket.getLastActivityAt());
        response.setSlaDeadline(ticket.getSlaDeadline());
        response.setFirstResponseAt(ticket.getFirstResponseAt());
        response.setResolvedAt(ticket.getResolvedAt());
        response.setClosedAt(ticket.getClosedAt());

        // Customer bilgileri
        if (ticket.getCustomer() != null) {
            response.setCustomerId(ticket.getCustomer().getId());
            response.setCustomerName(ticket.getCustomer().getFullName());
            response.setCustomerEmail(ticket.getCustomer().getEmail());
            response.setCustomerSegment(ticket.getCustomer().getSegment().getCode());
        }

        // Agent bilgileri
        if (ticket.getAssignedAgent() != null) {
            response.setAssignedAgentId(ticket.getAssignedAgent().getId());
            response.setAssignedAgentName(ticket.getAssignedAgent().getFullName());
        }

        // Department bilgileri
        if (ticket.getDepartment() != null) {
            response.setDepartmentId(ticket.getDepartment().getId());
            response.setDepartmentName(ticket.getDepartment().getName());
        }

        // Creator user bilgileri
        if (ticket.getCreatorUser() != null) {
            response.setCreatorUserId(ticket.getCreatorUser().getId());
            response.setCreatorUserName(ticket.getCreatorUser().getFullName());
        }

        // Comments (sadece public olanlar default olarak)
        List<TicketComment> publicComments = ticketCommentRepository.findByTicketIdAndIsInternalFalseOrderByCreatedAtAsc(ticket.getId());
        response.setComments(publicComments.stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList()));

        // Attachments
        List<TicketAttachment> attachments = ticketAttachmentRepository.findByTicketIdOrderByCreatedAtDesc(ticket.getId());
        response.setAttachments(attachments.stream()
                .map(this::mapToAttachmentResponse)
                .collect(Collectors.toList()));

        // History
        List<TicketHistory> history = ticketHistoryRepository.findByTicketIdOrderByCreatedAtDesc(ticket.getId());
        response.setHistory(history.stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList()));

        return response;
    }

    /**
     * TicketComment entity'sini TicketCommentResponse'a map eder
     */
    private TicketCommentResponse mapToCommentResponse(TicketComment comment) {
        TicketCommentResponse response = new TicketCommentResponse();
        response.setId(comment.getId());
        response.setMessage(comment.getMessage());
        response.setIsInternal(comment.getIsInternal());
        response.setIsAutoGenerated(comment.getIsAutoGenerated());
        response.setCommentType(comment.getCommentType());
        response.setCreatedAt(comment.getCreatedAt());

        // Author bilgileri
        if (comment.getAuthor() != null) {
            response.setAuthorId(comment.getAuthor().getId());
            response.setAuthorName(comment.getAuthor().getFullName());
            response.setAuthorRole(comment.getAuthor().getRole().getDisplayName());
        }

        return response;
    }

    /**
     * TicketAttachment entity'sini Object'e map eder
     */
    private Object mapToAttachmentResponse(TicketAttachment attachment) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", attachment.getId());
        response.put("originalName", attachment.getOriginalName());
        response.put("storedName", attachment.getStoredName());
        response.put("filePath", attachment.getFilePath());
        response.put("fileSize", attachment.getFileSize());
        response.put("fileSizeFormatted", attachment.getFileSizeFormatted());
        response.put("mimeType", attachment.getMimeType());
        response.put("isImage", attachment.isImage());
        response.put("downloadCount", attachment.getDownloadCount());
        response.put("createdAt", attachment.getCreatedAt());

        // Uploader bilgileri
        if (attachment.getUploadedBy() != null) {
            response.put("uploadedBy", attachment.getUploadedBy().getFullName());
            response.put("uploadedById", attachment.getUploadedBy().getId());
        }

        return response;
    }

    /**
     * TicketHistory entity'sini Object'e map eder
     */
    private Object mapToHistoryResponse(TicketHistory history) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", history.getId());
        response.put("fieldName", history.getFieldName());
        response.put("oldValue", history.getOldValue());
        response.put("newValue", history.getNewValue());
        response.put("changeType", history.getChangeType());
        response.put("description", history.getDescription());
        response.put("createdAt", history.getCreatedAt());

        // User bilgileri
        if (history.getUser() != null) {
            response.put("userId", history.getUser().getId());
            response.put("userName", history.getUser().getFullName());
            response.put("userRole", history.getUser().getRole().getDisplayName());
        }

        return response;
    }
}