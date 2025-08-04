package com.example.smartdeskbackend.service;

import com.example.smartdeskbackend.dto.request.chat.SendMessageRequest;
import com.example.smartdeskbackend.dto.response.chat.ChatMessageResponse;
import com.example.smartdeskbackend.entity.ChatMessage;
import com.example.smartdeskbackend.entity.Ticket;
import com.example.smartdeskbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatService {

    /**
     * Yeni mesaj gönder
     */
    ChatMessageResponse sendMessage(SendMessageRequest request, User sender);

    /**
     * Ticket'a ait mesajları getir (sayfalama ile)
     */
    Page<ChatMessageResponse> getMessagesByTicketId(Long ticketId, Pageable pageable);

    /**
     * Ticket'a ait son N mesajı getir
     */
    List<ChatMessageResponse> getRecentMessagesByTicketId(Long ticketId, int limit);

    /**
     * Belirli bir tarihten sonraki mesajları getir
     */
    List<ChatMessageResponse> getMessagesByTicketIdSince(Long ticketId, LocalDateTime since);

    /**
     * Mesajı okundu olarak işaretle
     */
    void markMessageAsRead(Long messageId, User user);

    /**
     * Ticket'a ait tüm mesajları okundu olarak işaretle
     */
    void markAllMessagesAsRead(Long ticketId, User user);

    /**
     * Okunmamış mesaj sayısını getir
     */
    long getUnreadMessageCount(Long ticketId, User user);

    /**
     * Sistem mesajı gönder
     */
    ChatMessageResponse sendSystemMessage(Long ticketId, String content, User sender);

    /**
     * Mesajı sil
     */
    void deleteMessage(Long messageId, User user);

    /**
     * Ticket'a ait dosya ekli mesajları getir
     */
    List<ChatMessageResponse> getMessagesWithAttachments(Long ticketId);

    /**
     * Reply mesajlarını getir
     */
    List<ChatMessageResponse> getReplyMessages(Long messageId);

    /**
     * Mesaj arama
     */
    Page<ChatMessageResponse> searchMessages(Long ticketId, String query, Pageable pageable);

    /**
     * Kullanıcının yazıyor durumunu güncelle
     */
    void updateTypingStatus(Long ticketId, User user, boolean isTyping);

    /**
     * Ticket'a ait yazan kullanıcıları getir
     */
    List<String> getTypingUsers(Long ticketId);
}