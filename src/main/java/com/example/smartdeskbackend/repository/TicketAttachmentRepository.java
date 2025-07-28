
// TicketAttachmentRepository.java
package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {

    /**
     * Ticket'ın ekli dosyaları
     */
    List<TicketAttachment> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

    /**
     * Kullanıcının yüklediği dosyalar
     */
    List<TicketAttachment> findByUploadedByIdOrderByCreatedAtDesc(Long userId);

    /**
     * Image dosyaları
     */
    List<TicketAttachment> findByTicketIdAndIsImageTrueOrderByCreatedAtDesc(Long ticketId);

    /**
     * Dosya boyutu toplam hesaplama
     */
    @Query("SELECT SUM(ta.fileSize) FROM TicketAttachment ta WHERE ta.ticket.id = :ticketId")
    Long getTotalFileSizeByTicketId(@Param("ticketId") Long ticketId);

    /**
     * Şirketin toplam dosya boyutu
     */
    @Query("SELECT SUM(ta.fileSize) FROM TicketAttachment ta WHERE ta.ticket.company.id = :companyId")
    Long getTotalFileSizeByCompanyId(@Param("companyId") Long companyId);
}