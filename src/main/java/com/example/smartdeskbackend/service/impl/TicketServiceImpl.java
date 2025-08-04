package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.controller.WebSocketMessageController;
import com.example.smartdeskbackend.dto.NotificationDTO;
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

    // WebSocket Controller enjekte edildi
    @Autowired
    private WebSocketMessageController webSocketMessageController;

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

        // Customer kontrolü (eğer belirtilmişse)
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
        }

        // Creator user kontrolü (eğer belirtilmişse)
        User creatorUser = null;
        if (request.getCreatorUserId() != null) {
            creatorUser = userRepository.findById(request.getCreatorUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getCreatorUserId()));
        }

        // Department kontrolü (eğer belirtilmişse)
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));
        }

        // Yeni ticket oluştur
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : TicketPriority.NORMAL);
        ticket.setCategory(request.getCategory());
        ticket.setSource(request.getSource() != null ? request.getSource() : TicketSource.WEB_FORM);
        ticket.setIsInternal(request.getIsInternal() != null ? request.getIsInternal() : false);
        ticket.setTags(request.getTags());
        ticket.setStatus(TicketStatus.NEW);
        ticket.setEscalationLevel(0);
        ticket.setCompany(company);
        ticket.setCustomer(customer);
        ticket.setCreatorUser(creatorUser);
        ticket.setDepartment(department);

        // Ticket numarası oluştur
        ticket.generateTicketNumber();

        // First save to get createdAt populated
        ticket = ticketRepository.save(ticket);
        
        // SLA deadline hesapla (default 24 saat) - save'den sonra çağır
        ticket.calculateSlaDeadline(24);
        
        // Update with SLA deadline
        ticket = ticketRepository.save(ticket);

        // History kaydı oluştur
        createHistoryRecord(ticket, "status", "", "NEW", creatorUser);

        // WebSocket bildirimi gönder - Yeni ticket oluşturuldu
        sendTicketCreationNotifications(ticket);

        // Email notification gönder
        try {
            emailService.sendTicketNotification(ticket, "CREATED");
        } catch (Exception e) {
            logger.warn("Failed to send ticket creation notification", e);
        }

        logger.info("Ticket created successfully with id: {}", ticket.getId());
        return mapToDetailResponse(ticket);
    }

    @Override
    public TicketDetailResponse updateTicket(Long id, UpdateTicketRequest request) {
        logger.info("Updating ticket: {}", id);

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        // Değişiklikleri kaydet
        if (request.getTitle() != null) {
            String oldValue = ticket.getTitle();
            ticket.setTitle(request.getTitle());
            createHistoryRecord(ticket, "title", oldValue, request.getTitle(), null);
        }

        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }

        if (request.getPriority() != null) {
            TicketPriority oldPriority = ticket.getPriority();
            ticket.setPriority(request.getPriority());
            createHistoryRecord(ticket, "priority", oldPriority.getCode(), request.getPriority().getCode(), null);
        }

        if (request.getCategory() != null) {
            TicketCategory oldCategory = ticket.getCategory();
            ticket.setCategory(request.getCategory());
            createHistoryRecord(ticket, "category",
                    oldCategory != null ? oldCategory.getCode() : null,
                    request.getCategory().getCode(), null);
        }

        if (request.getTags() != null) {
            ticket.setTags(request.getTags());
        }

        if (request.getEstimatedHours() != null) {
            ticket.setEstimatedHours(request.getEstimatedHours());
        }

        ticket.updateActivity();
        ticket = ticketRepository.save(ticket);

        // WebSocket bildirimi gönder - Ticket güncellendi
        sendTicketUpdateNotifications(ticket);

        logger.info("Ticket updated successfully: {}", id);
        return mapToDetailResponse(ticket);
    }

    // ============ TİCKET DURUM YÖNETİMİ ============

    @Override
    public TicketDetailResponse changeTicketStatus(Long id, TicketStatus newStatus, Long userId) {
        logger.info("Changing ticket status: {} to {}", id, newStatus);

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        }

        TicketStatus oldStatus = ticket.getStatus();
        ticket.updateStatus(newStatus, user);

        // History kaydı oluştur
        createHistoryRecord(ticket, "status", oldStatus.getCode(), newStatus.getCode(), user);

        ticket = ticketRepository.save(ticket);

        // WebSocket bildirimi gönder - Durum değişikliği
        sendTicketStatusChangeNotifications(ticket, oldStatus, newStatus);

        // Email notification gönder
        try {
            emailService.sendTicketNotification(ticket, "UPDATED");
        } catch (Exception e) {
            logger.warn("Failed to send ticket status change notification", e);
        }

        logger.info("Ticket status changed successfully: {} -> {}", oldStatus, newStatus);
        return mapToDetailResponse(ticket);
    }

    @Override
    public TicketDetailResponse assignTicket(Long id, AssignTicketRequest request) {
        logger.info("Assigning ticket: {} to agent: {}", id, request.getAgentId());

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        User agent = userRepository.findById(request.getAgentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: " + request.getAgentId()));

        // Agent rolü kontrolü
        if (!agent.hasRole(UserRole.AGENT) && !agent.hasRole(UserRole.MANAGER)) {
            throw new BusinessLogicException("User is not an agent or manager");
        }

        User oldAgent = ticket.getAssignedAgent();
        ticket.assignToAgent(agent);

        // History kaydı oluştur
        createHistoryRecord(ticket, "assignedAgent",
                oldAgent != null ? oldAgent.getFullName() : null,
                agent.getFullName(), null);

        ticket = ticketRepository.save(ticket);

        // WebSocket bildirimi gönder - Ticket atandı
        sendTicketAssignmentNotifications(ticket);

        // Email notification gönder
        try {
            emailService.sendTicketNotification(ticket, "ASSIGNED");
        } catch (Exception e) {
            logger.warn("Failed to send ticket assignment notification", e);
        }

        logger.info("Ticket assigned successfully to: {}", agent.getFullName());
        return mapToDetailResponse(ticket);
    }

    @Override
    public TicketDetailResponse autoAssignTicket(Long id) {
        logger.info("Auto-assigning ticket: {}", id);

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        // Department'a göre en az yüklü agent'ı bul
        Long departmentId = ticket.getDepartment() != null ? ticket.getDepartment().getId() : null;
        if (departmentId == null) {
            throw new BusinessLogicException("Cannot auto-assign ticket without department");
        }

        List<User> availableAgents = userRepository.findLeastBusyAgents(departmentId, 1);
        if (availableAgents.isEmpty()) {
            throw new BusinessLogicException("No available agents in department");
        }

        User selectedAgent = availableAgents.get(0);
        ticket.assignToAgent(selectedAgent);

        // History kaydı oluştur
        createHistoryRecord(ticket, "assignedAgent", null, selectedAgent.getFullName(), null);

        ticket = ticketRepository.save(ticket);

        // WebSocket bildirimi gönder - Otomatik atama
        sendTicketAssignmentNotifications(ticket);

        logger.info("Ticket auto-assigned successfully to: {}", selectedAgent.getFullName());
        return mapToDetailResponse(ticket);
    }

    @Override
    public TicketDetailResponse changeTicketPriority(Long id, TicketPriority newPriority, Long userId) {
        logger.info("Changing ticket priority: {} to {}", id, newPriority);

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        TicketPriority oldPriority = ticket.getPriority();
        ticket.setPriority(newPriority);
        ticket.updateActivity();

        // History kaydı oluştur
        createHistoryRecord(ticket, "priority", oldPriority.getCode(), newPriority.getCode(), user);

        ticket = ticketRepository.save(ticket);

        // WebSocket bildirimi gönder - Öncelik değişikliği
        sendTicketPriorityChangeNotifications(ticket, oldPriority, newPriority);

        logger.info("Ticket priority changed successfully: {} -> {}", oldPriority, newPriority);
        return mapToDetailResponse(ticket);
    }

    @Override
    public TicketDetailResponse escalateTicket(Long id, Long userId) {
        logger.info("Escalating ticket: {}", id);

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        int oldEscalationLevel = ticket.getEscalationLevel();
        ticket.escalate();

        // History kaydı oluştur
        createHistoryRecord(ticket, "escalationLevel",
                String.valueOf(oldEscalationLevel),
                String.valueOf(ticket.getEscalationLevel()), user);

        ticket = ticketRepository.save(ticket);

        // WebSocket bildirimi gönder - Ticket escalate edildi
        sendTicketEscalationNotifications(ticket, oldEscalationLevel);

        // Email notification gönder
        try {
            emailService.sendTicketNotification(ticket, "ESCALATED");
        } catch (Exception e) {
            logger.warn("Failed to send ticket escalation notification", e);
        }

        logger.info("Ticket escalated successfully to level: {}", ticket.getEscalationLevel());
        return mapToDetailResponse(ticket);
    }

    @Override
    public TicketDetailResponse closeTicket(Long id, String resolutionSummary, Long userId) {
        logger.info("Closing ticket: {}", id);

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        ticket.setResolutionSummary(resolutionSummary);
        TicketStatus oldStatus = ticket.getStatus();
        ticket.updateStatus(TicketStatus.CLOSED, user);

        // History kaydı oluştur
        createHistoryRecord(ticket, "status", oldStatus.getCode(), "CLOSED", user);

        ticket = ticketRepository.save(ticket);

        // WebSocket bildirimi gönder - Ticket kapatıldı
        sendTicketClosureNotifications(ticket);

        // Email notification gönder
        try {
            emailService.sendTicketNotification(ticket, "CLOSED");
        } catch (Exception e) {
            logger.warn("Failed to send ticket closure notification", e);
        }

        logger.info("Ticket closed successfully: {}", id);
        return mapToDetailResponse(ticket);
    }

    // ============ YORUM ve EK DOSYA YÖNETİMİ ============

    @Override
    public TicketCommentResponse addComment(Long ticketId, AddCommentRequest request) {
        logger.info("Adding comment to ticket: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getAuthorId()));

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setMessage(request.getMessage());
        comment.setIsInternal(request.getIsInternal() != null ? request.getIsInternal() : false);
        comment.setCommentType(request.getCommentType() != null ? request.getCommentType() : "COMMENT");

        comment = ticketCommentRepository.save(comment);

        // Ticket aktivitesini güncelle
        ticket.updateActivity();
        ticketRepository.save(ticket);

        // WebSocket bildirimi gönder - Yeni yorum eklendi
        sendTicketCommentNotifications(ticket, comment);

        logger.info("Comment added successfully to ticket: {}", ticketId);
        return mapToCommentResponse(comment);
    }

    @Override
    public List<TicketCommentResponse> getTicketComments(Long ticketId, boolean includeInternal) {
        logger.debug("Getting comments for ticket: {} (includeInternal: {})", ticketId, includeInternal);

        List<TicketComment> comments;
        if (includeInternal) {
            comments = ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        } else {
            comments = ticketCommentRepository.findByTicketIdAndIsInternalFalseOrderByCreatedAtAsc(ticketId);
        }

        return comments.stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public String addAttachment(Long ticketId, MultipartFile file, Long userId) {
        logger.info("Adding attachment to ticket: {}", ticketId);

        try {
            String filePath = fileService.uploadTicketAttachment(file, ticketId, userId);

            // WebSocket bildirimi gönder - Dosya eklendi
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket != null) {
                sendTicketAttachmentNotifications(ticket, file.getOriginalFilename());
            }

            return filePath;
        } catch (Exception e) {
            logger.error("Failed to add attachment to ticket: {}", ticketId, e);
            throw new BusinessLogicException("Failed to upload attachment: " + e.getMessage());
        }
    }

    @Override
    public void addSatisfactionRating(Long ticketId, int rating, String feedback) {
        logger.info("Adding satisfaction rating to ticket: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        if (rating < 1 || rating > 5) {
            throw new BusinessLogicException("Rating must be between 1 and 5");
        }

        ticket.addSatisfactionRating(rating, feedback);
        ticketRepository.save(ticket);

        // WebSocket bildirimi gönder - Müşteri değerlendirmesi eklendi
        sendCustomerSatisfactionNotifications(ticket, rating, feedback);

        logger.info("Satisfaction rating added successfully to ticket: {}", ticketId);
    }

    // ============ İSTATİSTİK ve ANALİTİK ============

    @Override
    public Map<String, Object> getTicketStats(Long companyId) {
        logger.debug("Getting ticket statistics for company: {}", companyId);

        Map<String, Object> stats = new HashMap<>();

        // Temel sayılar
        long totalTickets = ticketRepository.countByCompanyId(companyId);
        stats.put("totalTickets", totalTickets);

        // Status bazında istatistikler
        List<Object[]> statusStats = ticketRepository.getTicketStatsByStatus(companyId);
        Map<String, Long> statusDistribution = new HashMap<>();
        for (Object[] stat : statusStats) {
            statusDistribution.put((String) stat[0], ((Number) stat[1]).longValue());
        }
        stats.put("statusDistribution", statusDistribution);

        // Priority bazında istatistikler
        List<Object[]> priorityStats = ticketRepository.getTicketStatsByPriority(companyId);
        Map<String, Long> priorityDistribution = new HashMap<>();
        for (Object[] stat : priorityStats) {
            priorityDistribution.put((String) stat[0], ((Number) stat[1]).longValue());
        }
        stats.put("priorityDistribution", priorityDistribution);

        // Atanmamış ticketlar
        List<Ticket> unassignedTickets = ticketRepository.findUnassignedTickets(companyId);
        stats.put("unassignedTickets", unassignedTickets.size());

        // SLA riski olan ticketlar
        LocalDateTime riskTime = LocalDateTime.now().plusHours(2);
        List<Ticket> slaRiskTickets = ticketRepository.findTicketsAtRiskOfSlaViolation(companyId, riskTime);
        stats.put("slaRiskTickets", slaRiskTickets.size());

        // Müşteri memnuniyet ortalaması
        Double avgSatisfaction = ticketRepository.getAverageCustomerSatisfaction(companyId, LocalDateTime.now().minusMonths(1));
        stats.put("averageCustomerSatisfaction", avgSatisfaction != null ? avgSatisfaction : 0.0);

        return stats;
    }

    @Override
    public List<Object[]> getAgentPerformanceMetrics(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Getting agent performance metrics for company: {}", companyId);
        return ticketRepository.getAgentPerformanceMetrics(companyId, startDate, endDate);
    }

    @Override
    public void checkSlaViolations() {
        logger.info("Checking SLA violations");
        // Bu method scheduled task için kullanılacak
        // Tüm şirketleri kontrol et ve SLA ihlali olan ticketları escalate et
        // Bu implementasyon daha sonra scheduler sınıfında detaylandırılacak
    }

    @Override
    public List<Object[]> getTicketCreationTrend(Long companyId, int days) {
        logger.debug("Getting ticket creation trend for company: {} ({} days)", companyId, days);
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return ticketRepository.getDailyTicketCreationTrend(companyId, startDate);
    }

    // ============ WEBSOCKET BİLDİRİM METODLARı ============

    /**
     * Yeni ticket oluşturulduğunda bildirim gönder
     */
    private void sendTicketCreationNotifications(Ticket ticket) {
        try {
            // Departman yöneticilerine bildirim gönder
            if (ticket.getDepartment() != null) {
                List<User> managers = userRepository.findByDepartmentIdAndRole(
                        ticket.getDepartment().getId(), UserRole.MANAGER);

                for (User manager : managers) {
                    NotificationDTO notification = new NotificationDTO(
                            "Yeni ticket oluşturuldu: #" + ticket.getTicketNumber(),
                            "TICKET_CREATED",
                            "/tickets/" + ticket.getId()
                    );
                    webSocketMessageController.sendPrivateNotification(
                            manager.getEmail(), notification);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to send ticket creation notification: {}", e.getMessage());
        }
    }

    /**
     * Ticket güncellendiğinde bildirim gönder
     */
    private void sendTicketUpdateNotifications(Ticket ticket) {
        try {
            // Agent'a bildirim gönder
            if (ticket.getAssignedAgent() != null) {
                NotificationDTO agentNotification = new NotificationDTO(
                        "Ticket #" + ticket.getTicketNumber() + " güncellendi.",
                        "TICKET_UPDATE",
                        "/tickets/" + ticket.getId()
                );
                webSocketMessageController.sendPrivateNotification(
                        ticket.getAssignedAgent().getEmail(),
                        agentNotification
                );
            }

            // Müşteriye bildirim gönder
            if (ticket.getCustomer() != null) {
                NotificationDTO customerNotification = new NotificationDTO(
                        "Ticket #" + ticket.getTicketNumber() + " güncellendi.",
                        "TICKET_UPDATE",
                        "/customer/tickets/" + ticket.getId()
                );
                webSocketMessageController.sendPrivateNotification(
                        ticket.getCustomer().getEmail(),
                        customerNotification
                );
            }
        } catch (Exception e) {
            logger.warn("Failed to send ticket update notification: {}", e.getMessage());
        }
    }

    /**
     * Ticket atandığında bildirim gönder
     */
    private void sendTicketAssignmentNotifications(Ticket ticket) {
        try {
            // Yeni atanan agent'a bildirim
            if (ticket.getAssignedAgent() != null) {
                NotificationDTO notification = new NotificationDTO(
                        "Size yeni bir ticket atandı: #" + ticket.getTicketNumber(),
                        "TICKET_ASSIGNED",
                        "/tickets/" + ticket.getId()
                );
                webSocketMessageController.sendPrivateNotification(
                        ticket.getAssignedAgent().getEmail(),
                        notification
                );
            }

            // Müşteriye de bildirim
            if (ticket.getCustomer() != null) {
                NotificationDTO customerNotification = new NotificationDTO(
                        "Ticket #" + ticket.getTicketNumber() + " bir temsilciye atandı.",
                        "TICKET_ASSIGNED",
                        "/customer/tickets/" + ticket.getId()
                );
                webSocketMessageController.sendPrivateNotification(
                        ticket.getCustomer().getEmail(),
                        customerNotification
                );
            }
        } catch (Exception e) {
            logger.warn("Failed to send ticket assignment notification: {}", e.getMessage());
        }
    }

    /**
     * Ticket durumu değiştiğinde bildirim gönder
     */
    private void sendTicketStatusChangeNotifications(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus) {
        try {
            String statusMessage = getStatusChangeMessage(oldStatus, newStatus);

            // Agent'a bildirim
            if (ticket.getAssignedAgent() != null) {
                NotificationDTO notification = new NotificationDTO(
                        "Ticket #" + ticket.getTicketNumber() + " " + statusMessage,
                        "TICKET_STATUS_CHANGE",
                        "/tickets/" + ticket.getId()
                );
                webSocketMessageController.sendPrivateNotification(
                        ticket.getAssignedAgent().getEmail(),
                        notification
                );
            }

            // Müşteriye bildirim
            if (ticket.getCustomer() != null) {
                NotificationDTO customerNotification = new NotificationDTO(
                        "Ticket #" + ticket.getTicketNumber() + " " + statusMessage,
                        "TICKET_STATUS_CHANGE",
                        "/customer/tickets/" + ticket.getId()
                );
                webSocketMessageController.sendPrivateNotification(
                        ticket.getCustomer().getEmail(),
                        customerNotification
                );
            }
        } catch (Exception e) {
            logger.warn("Failed to send ticket status change notification: {}", e.getMessage());
        }
    }

    /**
     * Ticket önceliği değiştiğinde bildirim gönder
     */
    private void sendTicketPriorityChangeNotifications(Ticket ticket, TicketPriority oldPriority, TicketPriority newPriority) {
        try {
            String priorityMessage = getPriorityChangeMessage(oldPriority, newPriority);

            // Agent'a bildirim
            if (ticket.getAssignedAgent() != null) {
                NotificationDTO notification = new NotificationDTO(
                        "Ticket #" + ticket.getTicketNumber() + " önceliği " + priorityMessage,
                        "TICKET_PRIORITY_CHANGE",
                        "/tickets/" + ticket.getId()
                );
                webSocketMessageController.sendPrivateNotification(
                        ticket.getAssignedAgent().getEmail(),
                        notification
                );
            }

            // Yüksek öncelikli ticketlar için yöneticilere de bildirim
            if (newPriority == TicketPriority.HIGH || newPriority == TicketPriority.CRITICAL) {
                sendHighPriorityNotificationToManagers(ticket, newPriority);
            }
        } catch (Exception e) {
            logger.warn("Failed to send ticket priority change notification: {}", e.getMessage());
        }
    }

    /**
     * Ticket escalate edildiğinde bildirim gönder
     */
    private void sendTicketEscalationNotifications(Ticket ticket, int oldLevel) {
        try {
            // Agent'a bildirim
            if (ticket.getAssignedAgent() != null) {
                NotificationDTO notification = new NotificationDTO(
                        "Ticket #" + ticket.getTicketNumber() + " escalate edildi (Seviye: " + ticket.getEscalationLevel() + ")",
                        "TICKET_ESCALATED",
                        "/tickets/" + ticket.getId()
                );
                webSocketMessageController.sendPrivateNotification(
                        ticket.getAssignedAgent().getEmail(),
                        notification
                );
            }

            // Yöneticilere escalation bildirimi
            if (ticket.getDepartment() != null) {
                List<User> managers = userRepository.findByDepartmentIdAndRole(
                        ticket.getDepartment().getId(), UserRole.MANAGER);

                for (User manager : managers) {
                    NotificationDTO notification = new NotificationDTO(
                            "Ticket #" + ticket.getTicketNumber() + " escalate edildi! Acil müdahale gerekli.",
                            "TICKET_ESCALATED",
                            "/tickets/" + ticket.getId()
                    );
                    webSocketMessageController.sendPrivateNotification(
                            manager.getEmail(), notification);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to send ticket escalation notification: {}", e.getMessage());
        }
    }

    /**
     * Ticket kapatıldığında bildirim gönder
     */
    private void sendTicketClosureNotifications(Ticket ticket) {
        try {
            // Müşteriye bildirim
            if (ticket.getCustomer() != null) {
                NotificationDTO customerNotification = new NotificationDTO(
                        "Ticket #" + ticket.getTicketNumber() + " çözüldü ve kapatıldı. Memnuniyetinizi değerlendirmeyi unutmayın!",
                        "TICKET_CLOSED",
                        "/customer/tickets/" + ticket.getId()
                );
                webSocketMessageController.sendPrivateNotification(
                        ticket.getCustomer().getEmail(),
                        customerNotification
                );
            }

            // Agent'a bildirim
            if (ticket.getAssignedAgent() != null) {
                NotificationDTO agentNotification = new NotificationDTO(
                        "Ticket #" + ticket.getTicketNumber() + " başarıyla kapatıldı.",
                        "TICKET_CLOSED",
                        "/tickets/" + ticket.getId()
                );
                webSocketMessageController.sendPrivateNotification(
                        ticket.getAssignedAgent().getEmail(),
                        agentNotification
                );
            }
        } catch (Exception e) {
            logger.warn("Failed to send ticket closure notification: {}", e.getMessage());
        }
    }

    /**
     * Ticket'a yorum eklendiğinde bildirim gönder
     */
    private void sendTicketCommentNotifications(Ticket ticket, TicketComment comment) {
        try {
            // Internal comment değilse müşteriye bildirim gönder
            if (!comment.getIsInternal() && ticket.getCustomer() != null) {
                NotificationDTO customerNotification = new NotificationDTO(
                        "Ticket #" + ticket.getTicketNumber() + " için yeni yanıt geldi.",
                        "TICKET_COMMENT",
                        "/customer/tickets/" + ticket.getId()
                );
                webSocketMessageController.sendPrivateNotification(
                        ticket.getCustomer().getEmail(),
                        customerNotification
                );
            }

            // Agent'a bildirim (eğer yorum yazan agent değilse)
            if (ticket.getAssignedAgent() != null &&
                    !ticket.getAssignedAgent().getId().equals(comment.getAuthor().getId())) {
                NotificationDTO agentNotification = new NotificationDTO(
                        "Ticket #" + ticket.getTicketNumber() + " için yeni yorum eklendi.",
                        "TICKET_COMMENT",
                        "/tickets/" + ticket.getId()
                );
                webSocketMessageController.sendPrivateNotification(
                        ticket.getAssignedAgent().getEmail(),
                        agentNotification
                );
            }
        } catch (Exception e) {
            logger.warn("Failed to send ticket comment notification: {}", e.getMessage());
        }
    }

    /**
     * Ticket'a dosya eklendiğinde bildirim gönder
     */
    private void sendTicketAttachmentNotifications(Ticket ticket, String fileName) {
        try {
            // Agent'a bildirim
            if (ticket.getAssignedAgent() != null) {
                NotificationDTO notification = new NotificationDTO(
                        "Ticket #" + ticket.getTicketNumber() + " için yeni dosya eklendi: " + fileName,
                        "TICKET_ATTACHMENT",
                        "/tickets/" + ticket.getId()
                );
                webSocketMessageController.sendPrivateNotification(
                        ticket.getAssignedAgent().getEmail(),
                        notification
                );
            }

            // Müşteriye bildirim
            if (ticket.getCustomer() != null) {
                NotificationDTO customerNotification = new NotificationDTO(
                        "Ticket #" + ticket.getTicketNumber() + " için yeni dosya eklendi.",
                        "TICKET_ATTACHMENT",
                        "/customer/tickets/" + ticket.getId()
                );
                webSocketMessageController.sendPrivateNotification(
                        ticket.getCustomer().getEmail(),
                        customerNotification
                );
            }
        } catch (Exception e) {
            logger.warn("Failed to send ticket attachment notification: {}", e.getMessage());
        }
    }

    /**
     * Müşteri memnuniyet değerlendirmesi eklendiğinde bildirim gönder
     */
    private void sendCustomerSatisfactionNotifications(Ticket ticket, int rating, String feedback) {
        try {
            String ratingText = getRatingText(rating);

            // Agent'a bildirim
            if (ticket.getAssignedAgent() != null) {
                NotificationDTO notification = new NotificationDTO(
                        "Ticket #" + ticket.getTicketNumber() + " için müşteri değerlendirmesi: " + ratingText + " (" + rating + "/5)",
                        "CUSTOMER_FEEDBACK",
                        "/tickets/" + ticket.getId()
                );
                webSocketMessageController.sendPrivateNotification(
                        ticket.getAssignedAgent().getEmail(),
                        notification
                );
            }

            // Düşük puanlarda yöneticilere bildirim
            if (rating <= 2 && ticket.getDepartment() != null) {
                List<User> managers = userRepository.findByDepartmentIdAndRole(
                        ticket.getDepartment().getId(), UserRole.MANAGER);

                for (User manager : managers) {
                    NotificationDTO notification = new NotificationDTO(
                            "UYARI: Ticket #" + ticket.getTicketNumber() + " düşük müşteri puanı aldı: " + rating + "/5",
                            "LOW_SATISFACTION",
                            "/tickets/" + ticket.getId()
                    );
                    webSocketMessageController.sendPrivateNotification(
                            manager.getEmail(), notification);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to send customer satisfaction notification: {}", e.getMessage());
        }
    }

    /**
     * Yüksek öncelikli ticketlar için yöneticilere bildirim gönder
     */
    private void sendHighPriorityNotificationToManagers(Ticket ticket, TicketPriority priority) {
        try {
            if (ticket.getDepartment() != null) {
                List<User> managers = userRepository.findByDepartmentIdAndRole(
                        ticket.getDepartment().getId(), UserRole.MANAGER);

                for (User manager : managers) {
                    NotificationDTO notification = new NotificationDTO(
                            "YÜKSEK ÖNCELİK: Ticket #" + ticket.getTicketNumber() + " - " + priority.getDisplayName(),
                            "HIGH_PRIORITY_TICKET",
                            "/tickets/" + ticket.getId()
                    );
                    webSocketMessageController.sendPrivateNotification(
                            manager.getEmail(), notification);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to send high priority notification to managers: {}", e.getMessage());
        }
    }

    // ============ HELPER METHODS ============

    /**
     * Durum değişikliği mesajını oluştur
     */
    private String getStatusChangeMessage(TicketStatus oldStatus, TicketStatus newStatus) {
        switch (newStatus) {
            case OPEN:
                return "açıldı.";
            case IN_PROGRESS:
                return "işleme alındı.";
            case PENDING:
                return "beklemede.";
            case RESOLVED:
                return "çözüldü.";
            case CLOSED:
                return "kapatıldı.";
            default:
                return "durumu güncellendi.";
        }
    }

    /**
     * Öncelik değişikliği mesajını oluştur
     */
    private String getPriorityChangeMessage(TicketPriority oldPriority, TicketPriority newPriority) {
        return oldPriority.getDisplayName() + " -> " + newPriority.getDisplayName() + " olarak değiştirildi.";
    }

    /**
     * Rating puanını metne çevir
     */
    private String getRatingText(int rating) {
        switch (rating) {
            case 1: return "Çok Kötü";
            case 2: return "Kötü";
            case 3: return "Orta";
            case 4: return "İyi";
            case 5: return "Mükemmel";
            default: return "Bilinmeyen";
        }
    }

    /**
     * Ticket history kaydı oluşturur
     */
    private void createHistoryRecord(Ticket ticket, String fieldName, String oldValue, String newValue, User user) {
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setChangeType("FIELD_CHANGED");
        history.setUser(user);

        ticketHistoryRepository.save(history);
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTicketNumber(ticket.getTicketNumber());
        response.setTitle(ticket.getTitle());
        response.setStatus(ticket.getStatus().getCode());
        response.setStatusDisplayName(ticket.getStatus().getDisplayName());
        response.setPriority(ticket.getPriority().getCode());
        response.setPriorityDisplayName(ticket.getPriority().getDisplayName());
        response.setCategory(ticket.getCategory() != null ? ticket.getCategory().getCode() : null);
        response.setSource(ticket.getSource().getCode());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setLastActivityAt(ticket.getLastActivityAt());
        response.setSlaDeadline(ticket.getSlaDeadline());
        response.setEscalationLevel(ticket.getEscalationLevel());

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