package com.example.smartdeskbackend.service;

import com.example.smartdeskbackend.dto.request.ticket.CreateTicketRequest;
import com.example.smartdeskbackend.dto.request.ticket.UpdateTicketRequest;
import com.example.smartdeskbackend.dto.request.ticket.AssignTicketRequest;
import com.example.smartdeskbackend.dto.request.ticket.AddCommentRequest;
import com.example.smartdeskbackend.dto.response.ticket.TicketResponse;
import com.example.smartdeskbackend.dto.response.ticket.TicketDetailResponse;
import com.example.smartdeskbackend.dto.response.ticket.TicketCommentResponse;
import com.example.smartdeskbackend.enums.TicketStatus;
import com.example.smartdeskbackend.enums.TicketPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Ticket service interface
 * Ticket yönetimi için tüm business logic operasyonlarını tanımlar
 */
public interface TicketService {

    // ============ TEMEL CRUD OPERASYONLARI ============

    /**
     * Ticket detaylarını ID ile getir
     * @param id Ticket ID
     * @return TicketDetailResponse
     */
    TicketDetailResponse getTicketById(Long id);

    /**
     * Ticket numarası ile ticket getir
     * @param ticketNumber Unique ticket number (TK-YYYYMMDD-XXXX format)
     * @return TicketDetailResponse
     */
    TicketDetailResponse getTicketByNumber(String ticketNumber);

    /**
     * Şirketteki tüm ticketları sayfalı olarak getir
     * @param companyId Şirket ID
     * @param pageable Sayfalama bilgisi
     * @return Page<TicketResponse>
     */
    Page<TicketResponse> getTicketsByCompany(Long companyId, Pageable pageable);

    /**
     * Tüm ticketları getir (SUPER_ADMIN için)
     * @param pageable Pagination bilgisi
     * @return Tüm ticketlar
     */
    Page<TicketResponse> getAllTickets(Pageable pageable);

    /**
     * Belirli müşterinin ticketlarını getir
     * @param customerId Müşteri ID
     * @param pageable Sayfalama bilgisi
     * @return Page<TicketResponse>
     */
    Page<TicketResponse> getTicketsByCustomer(Long customerId, Pageable pageable);

    /**
     * Belirli agent'a atanmış ticketları getir
     * @param agentId Agent ID
     * @param pageable Sayfalama bilgisi
     * @return Page<TicketResponse>
     */
    Page<TicketResponse> getTicketsByAgent(Long agentId, Pageable pageable);

    /**
     * CUSTOMER kullanıcının kendi ticketlarını getir (User ID ile)
     * @param userId User ID (CUSTOMER role)
     * @param pageable Sayfalama bilgisi
     * @return Page<TicketResponse>
     */
    Page<TicketResponse> getTicketsByUserId(Long userId, Pageable pageable);

    // ============ ARAMA ve FİLTRELEME ============

    /**
     * Ticket arama (title, description, ticket number)
     * @param companyId Şirket ID
     * @param searchTerm Arama terimi
     * @param pageable Sayfalama bilgisi
     * @return Page<TicketResponse>
     */
    Page<TicketResponse> searchTickets(Long companyId, String searchTerm, Pageable pageable);

    /**
     * Gelişmiş filtreleme ile ticket getirme
     * @param companyId Şirket ID
     * @param status Ticket durumu (opsiyonel)
     * @param priority Ticket önceliği (opsiyonel)
     * @param departmentId Departman ID (opsiyonel)
     * @param agentId Agent ID (opsiyonel)
     * @param customerId Müşteri ID (opsiyonel)
     * @param pageable Sayfalama bilgisi
     * @return Page<TicketResponse>
     */
    Page<TicketResponse> getTicketsWithFilters(Long companyId, TicketStatus status,
                                               TicketPriority priority, Long departmentId,
                                               Long agentId, Long customerId, Pageable pageable);

    /**
     * Aktif ticketları getir (NEW, OPEN, IN_PROGRESS, PENDING)
     * @param companyId Şirket ID
     * @param pageable Sayfalama bilgisi
     * @return Page<TicketResponse>
     */
    Page<TicketResponse> getActiveTickets(Long companyId, Pageable pageable);

    /**
     * Atanmamış ticketları getir
     * @param companyId Şirket ID
     * @return List<TicketResponse>
     */
    List<TicketResponse> getUnassignedTickets(Long companyId);

    /**
     * SLA riski olan ticketları getir
     * @param companyId Şirket ID
     * @return List<TicketResponse>
     */
    List<TicketResponse> getTicketsAtSlaRisk(Long companyId);

    // ============ TİCKET OLUŞTURMA ve GÜNCELLEME ============

    /**
     * Yeni ticket oluştur
     * @param request Ticket oluşturma isteği
     * @return TicketDetailResponse
     */
    TicketDetailResponse createTicket(CreateTicketRequest request);

    /**
     * Ticket bilgilerini güncelle
     * @param id Ticket ID
     * @param request Güncelleme isteği
     * @return TicketDetailResponse
     */
    TicketDetailResponse updateTicket(Long id, UpdateTicketRequest request);

    // ============ TİCKET DURUM YÖNETİMİ ============

    /**
     * Ticket durumunu değiştir
     * @param id Ticket ID
     * @param newStatus Yeni durum
     * @param userId İşlemi yapan kullanıcı ID
     * @return TicketDetailResponse
     */
    TicketDetailResponse changeTicketStatus(Long id, TicketStatus newStatus, Long userId);

    /**
     * Ticket'ı agent'a ata
     * @param id Ticket ID
     * @param request Atama isteği
     * @return TicketDetailResponse
     */
    TicketDetailResponse assignTicket(Long id, AssignTicketRequest request);

    /**
     * Ticket'ı otomatik olarak uygun agent'a ata
     * @param id Ticket ID
     * @return TicketDetailResponse
     */
    TicketDetailResponse autoAssignTicket(Long id);

    /**
     * Ticket önceliğini değiştir
     * @param id Ticket ID
     * @param newPriority Yeni öncelik
     * @param userId İşlemi yapan kullanıcı ID
     * @return TicketDetailResponse
     */
    TicketDetailResponse changeTicketPriority(Long id, TicketPriority newPriority, Long userId);

    /**
     * Ticket'ı escalate et (öncelik artırma)
     * @param id Ticket ID
     * @param userId İşlemi yapan kullanıcı ID
     * @return TicketDetailResponse
     */
    TicketDetailResponse escalateTicket(Long id, Long userId);

    /**
     * Ticket'ı kapat
     * @param id Ticket ID
     * @param resolutionSummary Çözüm özeti
     * @param userId İşlemi yapan kullanıcı ID
     * @return TicketDetailResponse
     */
    TicketDetailResponse closeTicket(Long id, String resolutionSummary, Long userId);

    // ============ YORUM ve EK DOSYA YÖNETİMİ ============

    /**
     * Ticket'a yorum ekle
     * @param ticketId Ticket ID
     * @param request Yorum ekleme isteği
     * @return TicketCommentResponse
     */
    TicketCommentResponse addComment(Long ticketId, AddCommentRequest request);

    /**
     * Ticket yorumlarını getir
     * @param ticketId Ticket ID
     * @param includeInternal Internal yorumları da dahil et
     * @return List<TicketCommentResponse>
     */
    List<TicketCommentResponse> getTicketComments(Long ticketId, boolean includeInternal);

    /**
     * Ticket'a dosya eki ekle
     * @param ticketId Ticket ID
     * @param file Yüklenecek dosya
     * @param userId Yükleyen kullanıcı ID
     * @return String dosya yolu
     */
    String addAttachment(Long ticketId, MultipartFile file, Long userId);

    /**
     * Müşteri memnuniyet puanı ekle
     * @param ticketId Ticket ID
     * @param rating Puan (1-5)
     * @param feedback Geri bildirim metni
     */
    void addSatisfactionRating(Long ticketId, int rating, String feedback);

    // ============ İSTATİSTİK ve ANALİTİK ============

    /**
     * Şirket bazında ticket istatistikleri
     * @param companyId Şirket ID
     * @return Map<String, Object> istatistik verileri
     */
    Map<String, Object> getTicketStats(Long companyId);

    /**
     * Agent performans metrikleri
     * @param companyId Şirket ID
     * @param startDate Başlangıç tarihi
     * @param endDate Bitiş tarihi
     * @return List<Object[]> performans verileri
     */
    List<Object[]> getAgentPerformanceMetrics(Long companyId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * SLA ihlali kontrolü (scheduled task için)
     */
    void checkSlaViolations();

    /**
     * Ticket oluşturma trendi
     * @param companyId Şirket ID
     * @param days Kaç günlük trend
     * @return List<Object[]> trend verileri
     */
    List<Object[]> getTicketCreationTrend(Long companyId, int days);

    // ============ APPROVAL WORKFLOW METHODS ============

    /**
     * Ticket'ı RESOLVED yapıp MANAGER onayına gönder
     * @param id Ticket ID
     * @param resolutionSummary Çözüm özeti
     * @param userId İşlemi yapan kullanıcı ID
     * @return TicketDetailResponse
     */
    TicketDetailResponse resolveTicketForApproval(Long id, String resolutionSummary, Long userId);

    /**
     * MANAGER onayı - SUPER_ADMIN onayına gönder
     * @param id Ticket ID
     * @param approvalComment Onay yorumu
     * @param managerId Manager ID
     * @return TicketDetailResponse
     */
    TicketDetailResponse approveByManager(Long id, String approvalComment, Long managerId);

    /**
     * SUPER_ADMIN final onayı - CLOSED yapar
     * @param id Ticket ID
     * @param finalComment Final yorum
     * @param adminId Admin ID
     * @return TicketDetailResponse
     */
    TicketDetailResponse approveByAdmin(Long id, String finalComment, Long adminId);

    /**
     * Onayı reddet - önceki duruma geri döndür
     * @param id Ticket ID
     * @param rejectionReason Red sebebi
     * @param userId İşlemi yapan kullanıcı ID
     * @return TicketDetailResponse
     */
    TicketDetailResponse rejectApproval(Long id, String rejectionReason, Long userId);
}