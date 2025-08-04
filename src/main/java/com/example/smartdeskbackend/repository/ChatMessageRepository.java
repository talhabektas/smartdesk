package com.example.smartdeskbackend.repository;

import com.example.smartdeskbackend.entity.ChatMessage;
import com.example.smartdeskbackend.entity.Ticket;
import com.example.smartdeskbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Ticket'a ait tüm mesajları getir (sayfalama ile)
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.ticket.id = :ticketId ORDER BY cm.createdAt ASC")
    Page<ChatMessage> findByTicketIdOrderByCreatedAtAsc(@Param("ticketId") Long ticketId, Pageable pageable);

    /**
     * Ticket'a ait tüm mesajları getir (tarih aralığı ile)
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.ticket.id = :ticketId AND cm.createdAt BETWEEN :startDate AND :endDate ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByTicketIdAndCreatedAtBetweenOrderByCreatedAtAsc(
            @Param("ticketId") Long ticketId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Ticket'a ait okunmamış mesajları getir
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.ticket.id = :ticketId AND cm.isRead = false ORDER BY cm.createdAt ASC")
    List<ChatMessage> findUnreadMessagesByTicketId(@Param("ticketId") Long ticketId);

    /**
     * Kullanıcının gönderdiği mesajları getir
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.sender.id = :senderId ORDER BY cm.createdAt DESC")
    Page<ChatMessage> findBySenderIdOrderByCreatedAtDesc(@Param("senderId") Long senderId, Pageable pageable);

    /**
     * Ticket'a ait son mesajı getir
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.ticket.id = :ticketId ORDER BY cm.createdAt DESC")
    Optional<ChatMessage> findTopByTicketIdOrderByCreatedAtDesc(@Param("ticketId") Long ticketId);

    /**
     * Ticket'a ait mesaj sayısını getir
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.ticket.id = :ticketId")
    long countByTicketId(@Param("ticketId") Long ticketId);

    /**
     * Ticket'a ait okunmamış mesaj sayısını getir
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.ticket.id = :ticketId AND cm.isRead = false")
    long countUnreadMessagesByTicketId(@Param("ticketId") Long ticketId);

    /**
     * Belirli bir tarihten sonraki mesajları getir
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.ticket.id = :ticketId AND cm.createdAt > :since ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByTicketIdAndCreatedAtAfterOrderByCreatedAtAsc(
            @Param("ticketId") Long ticketId,
            @Param("since") LocalDateTime since);

    /**
     * Sistem mesajlarını getir
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.ticket.id = :ticketId AND cm.messageType = 'SYSTEM' ORDER BY cm.createdAt ASC")
    List<ChatMessage> findSystemMessagesByTicketId(@Param("ticketId") Long ticketId);

    /**
     * Dosya ekli mesajları getir
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.ticket.id = :ticketId AND cm.fileUrl IS NOT NULL ORDER BY cm.createdAt DESC")
    List<ChatMessage> findMessagesWithAttachmentsByTicketId(@Param("ticketId") Long ticketId);

    /**
     * Reply mesajlarını getir
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.replyToMessageId = :replyToMessageId ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByReplyToMessageIdOrderByCreatedAtAsc(@Param("replyToMessageId") Long replyToMessageId);

    /**
     * Ticket'a ait son N mesajı getir
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.ticket.id = :ticketId ORDER BY cm.createdAt DESC")
    List<ChatMessage> findTopNByTicketIdOrderByCreatedAtDesc(@Param("ticketId") Long ticketId, Pageable pageable);

    /**
     * Belirli bir kullanıcının ticket'a ait mesajlarını getir
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.ticket.id = :ticketId AND cm.sender.id = :senderId ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByTicketIdAndSenderIdOrderByCreatedAtAsc(
            @Param("ticketId") Long ticketId,
            @Param("senderId") Long senderId);

    /**
     * Ticket'a ait mesajları sil (ticket silindiğinde)
     */
    void deleteByTicketId(Long ticketId);

    /**
     * Kullanıcının mesajlarını sil (kullanıcı silindiğinde)
     */
    void deleteBySenderId(Long senderId);
}