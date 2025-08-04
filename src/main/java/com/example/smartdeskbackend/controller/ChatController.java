package com.example.smartdeskbackend.controller;

import com.example.smartdeskbackend.dto.request.chat.SendMessageRequest;
import com.example.smartdeskbackend.dto.response.chat.ChatMessageResponse;
import com.example.smartdeskbackend.entity.User;
import com.example.smartdeskbackend.service.ChatService;
import com.example.smartdeskbackend.service.UserService;
import com.example.smartdeskbackend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.DELETE, RequestMethod.OPTIONS })
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * Ticket'a ait mesajları getir
     */
    @GetMapping("/tickets/{ticketId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMessages(
            @PathVariable Long ticketId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.info("Getting messages for ticket: {}, page: {}, size: {}", ticketId, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ChatMessageResponse> messages = chatService.getMessagesByTicketId(ticketId, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages.getContent());
            response.put("totalElements", messages.getTotalElements());
            response.put("totalPages", messages.getTotalPages());
            response.put("currentPage", messages.getNumber());
            response.put("hasNext", messages.hasNext());
            response.put("hasPrevious", messages.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get messages for ticket: {}", ticketId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get messages", "message", e.getMessage()));
        }
    }

    /**
     * Ticket'a ait son mesajları getir
     */
    @GetMapping("/tickets/{ticketId}/messages/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRecentMessages(
            @PathVariable Long ticketId,
            @RequestParam(defaultValue = "20") int limit) {

        log.info("Getting recent {} messages for ticket: {}", limit, ticketId);

        try {
            List<ChatMessageResponse> messages = chatService.getRecentMessagesByTicketId(ticketId, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages);
            response.put("count", messages.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get recent messages for ticket: {}", ticketId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get recent messages", "message", e.getMessage()));
        }
    }

    /**
     * Belirli bir tarihten sonraki mesajları getir
     */
    @GetMapping("/tickets/{ticketId}/messages/since")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMessagesSince(
            @PathVariable Long ticketId,
            @RequestParam String since) {

        log.info("Getting messages since {} for ticket: {}", since, ticketId);

        try {
            LocalDateTime sinceDateTime = LocalDateTime.parse(since);
            List<ChatMessageResponse> messages = chatService.getMessagesByTicketIdSince(ticketId, sinceDateTime);

            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages);
            response.put("count", messages.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get messages since {} for ticket: {}", since, ticketId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get messages", "message", e.getMessage()));
        }
    }

    /**
     * Mesaj gönder (REST endpoint)
     */
    @PostMapping("/tickets/{ticketId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long ticketId,
            @Valid @RequestBody SendMessageRequest request,
            HttpServletRequest httpRequest) {

        log.info("Sending message to ticket: {}", ticketId);

        try {
            User currentUser = getCurrentUser(httpRequest);
            request.setTicketId(ticketId);

            ChatMessageResponse message = chatService.sendMessage(request, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", message);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Failed to send message to ticket: {}", ticketId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send message", "message", e.getMessage()));
        }
    }

    /**
     * Mesaj gönder (WebSocket endpoint)
     */
    @MessageMapping("/ticket/chat")
    public void sendMessageViaWebSocket(
            @Payload SendMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {

        log.info("Sending message via WebSocket to ticket: {}", request.getTicketId());

        try {
            User currentUser = getCurrentUserFromWebSocket(headerAccessor);
            
            // ticketId is already in the request payload
            chatService.sendMessage(request, currentUser);

        } catch (Exception e) {
            log.error("Failed to send message via WebSocket to ticket: {}", request.getTicketId(), e);
        }
    }

    /**
     * Yazıyor durumunu güncelle (WebSocket endpoint)
     */
    @MessageMapping("/ticket/typing")
    public void updateTypingStatusViaWebSocket(
            @Payload Map<String, Object> request,
            SimpMessageHeaderAccessor headerAccessor) {

        Long ticketId = Long.valueOf(request.get("ticketId").toString());
        Boolean isTyping = Boolean.valueOf(request.get("isTyping").toString());
        
        log.info("Updating typing status via WebSocket for ticket: {}, isTyping: {}", ticketId, isTyping);

        try {
            User currentUser = getCurrentUserFromWebSocket(headerAccessor);
            chatService.updateTypingStatus(ticketId, currentUser, isTyping);

        } catch (Exception e) {
            log.error("Failed to update typing status via WebSocket for ticket: {}", ticketId, e);
        }
    }

    /**
     * Mesajı okundu olarak işaretle
     */
    @PostMapping("/messages/{messageId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markMessageAsRead(
            @PathVariable Long messageId,
            HttpServletRequest httpRequest) {

        log.info("Marking message {} as read", messageId);

        try {
            User currentUser = getCurrentUser(httpRequest);
            chatService.markMessageAsRead(messageId, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Message marked as read");
            response.put("messageId", messageId);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to mark message {} as read", messageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to mark message as read", "message", e.getMessage()));
        }
    }

    /**
     * Ticket'a ait tüm mesajları okundu olarak işaretle
     */
    @PostMapping("/tickets/{ticketId}/messages/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markAllMessagesAsRead(
            @PathVariable Long ticketId,
            HttpServletRequest httpRequest) {

        log.info("Marking all messages as read for ticket: {}", ticketId);

        try {
            User currentUser = getCurrentUser(httpRequest);
            chatService.markAllMessagesAsRead(ticketId, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "All messages marked as read");
            response.put("ticketId", ticketId);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to mark all messages as read for ticket: {}", ticketId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to mark messages as read", "message", e.getMessage()));
        }
    }

    /**
     * Okunmamış mesaj sayısını getir
     */
    @GetMapping("/tickets/{ticketId}/messages/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUnreadMessageCount(
            @PathVariable Long ticketId,
            HttpServletRequest httpRequest) {

        log.info("Getting unread message count for ticket: {}", ticketId);

        try {
            User currentUser = getCurrentUser(httpRequest);
            long count = chatService.getUnreadMessageCount(ticketId, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", count);
            response.put("ticketId", ticketId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get unread message count for ticket: {}", ticketId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get unread count", "message", e.getMessage()));
        }
    }

    /**
     * Mesajı sil
     */
    @DeleteMapping("/messages/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long messageId,
            HttpServletRequest httpRequest) {

        log.info("Deleting message: {}", messageId);

        try {
            User currentUser = getCurrentUser(httpRequest);
            chatService.deleteMessage(messageId, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Message deleted successfully");
            response.put("messageId", messageId);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to delete message: {}", messageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete message", "message", e.getMessage()));
        }
    }

    /**
     * Ticket'a ait dosya ekli mesajları getir
     */
    @GetMapping("/tickets/{ticketId}/messages/attachments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMessagesWithAttachments(@PathVariable Long ticketId) {

        log.info("Getting messages with attachments for ticket: {}", ticketId);

        try {
            List<ChatMessageResponse> messages = chatService.getMessagesWithAttachments(ticketId);

            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages);
            response.put("count", messages.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get messages with attachments for ticket: {}", ticketId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get messages with attachments", "message", e.getMessage()));
        }
    }

    /**
     * Reply mesajlarını getir
     */
    @GetMapping("/messages/{messageId}/replies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getReplyMessages(@PathVariable Long messageId) {

        log.info("Getting reply messages for message: {}", messageId);

        try {
            List<ChatMessageResponse> messages = chatService.getReplyMessages(messageId);

            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages);
            response.put("count", messages.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get reply messages for message: {}", messageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get reply messages", "message", e.getMessage()));
        }
    }

    /**
     * Yazıyor durumunu güncelle
     */
    @PostMapping("/tickets/{ticketId}/typing")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateTypingStatus(
            @PathVariable Long ticketId,
            @RequestBody Map<String, Boolean> request,
            HttpServletRequest httpRequest) {

        boolean isTyping = request.getOrDefault("isTyping", false);
        log.info("Updating typing status for ticket: {}, isTyping: {}", ticketId, isTyping);

        try {
            User currentUser = getCurrentUser(httpRequest);
            chatService.updateTypingStatus(ticketId, currentUser, isTyping);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Typing status updated");
            response.put("isTyping", isTyping);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to update typing status for ticket: {}", ticketId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update typing status", "message", e.getMessage()));
        }
    }

    /**
     * Yazan kullanıcıları getir
     */
    @GetMapping("/tickets/{ticketId}/typing")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getTypingUsers(@PathVariable Long ticketId) {

        log.info("Getting typing users for ticket: {}", ticketId);

        try {
            List<String> typingUsers = chatService.getTypingUsers(ticketId);

            Map<String, Object> response = new HashMap<>();
            response.put("typingUsers", typingUsers);
            response.put("count", typingUsers.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get typing users for ticket: {}", ticketId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get typing users", "message", e.getMessage()));
        }
    }

    // Helper methods

    private User getCurrentUser(HttpServletRequest request) {
        // This should be implemented based on your authentication mechanism
        // For now, we'll return a mock user
        return userService.getCurrentUser();
    }

    private User getCurrentUserFromWebSocket(SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Get JWT token from WebSocket headers
            String token = headerAccessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                String email = jwtUtil.extractEmail(token);
                log.info("🔍 WebSocket getCurrentUser - JWT email: {}", email);
                return userService.findByEmail(email);
            }
            
            // Fallback to session-based authentication
            log.warn("🔍 WebSocket getCurrentUser - No Authorization header, using fallback");
            return userService.getCurrentUser();
            
        } catch (Exception e) {
            log.error("🔍 WebSocket getCurrentUser - Error extracting user from token: {}", e.getMessage());
            return userService.getCurrentUser();
        }
    }
}