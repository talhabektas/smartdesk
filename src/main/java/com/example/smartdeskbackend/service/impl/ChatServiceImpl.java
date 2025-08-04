package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.dto.request.chat.SendMessageRequest;
import com.example.smartdeskbackend.dto.response.chat.ChatMessageResponse;
import com.example.smartdeskbackend.entity.ChatMessage;
import com.example.smartdeskbackend.entity.Ticket;
import com.example.smartdeskbackend.entity.User;
import com.example.smartdeskbackend.enums.ChatMessageType;
import com.example.smartdeskbackend.enums.UserRole;
import com.example.smartdeskbackend.exception.BusinessLogicException;
import com.example.smartdeskbackend.repository.ChatMessageRepository;
import com.example.smartdeskbackend.repository.TicketRepository;
import com.example.smartdeskbackend.repository.UserRepository;
import com.example.smartdeskbackend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Typing status tracking
    private final Map<Long, Map<Long, LocalDateTime>> typingUsers = new ConcurrentHashMap<>();

    @Override
    public ChatMessageResponse sendMessage(SendMessageRequest request, User sender) {
        log.info("Sending message for ticket: {}, sender: {}", request.getTicketId(), sender.getEmail());

        // Fresh fetch the sender to avoid detached entity issues
        User freshSender = userRepository.findById(sender.getId())
                .orElseThrow(() -> new BusinessLogicException("Sender user not found"));

        // Validate ticket exists and user has access
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new BusinessLogicException("Ticket not found"));

        // Check if user has permission to send message to this ticket
        if (!hasPermissionToSendMessage(ticket, freshSender)) {
            throw new BusinessLogicException("You don't have permission to send messages to this ticket");
        }

        // Create chat message
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(request.getContent());
        chatMessage.setMessageType(request.getMessageType());
        chatMessage.setTicket(ticket);
        chatMessage.setSender(freshSender);
        chatMessage.setIsInternal(request.getIsInternal() != null ? request.getIsInternal() : false);
        chatMessage.setReplyToMessageId(request.getReplyToMessageId());

        // Set file attachment fields if provided
        if (request.getFileUrl() != null) {
            chatMessage.setFileUrl(request.getFileUrl());
            chatMessage.setFileName(request.getFileName());
            chatMessage.setFileSize(request.getFileSize());
            chatMessage.setFileType(request.getFileType());
        }

        // Save message
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // Update ticket's last activity and message time
        ticket.updateLastMessageTime();
        ticket.incrementUnreadMessageCount();
        ticketRepository.save(ticket);

        // Convert to response
        ChatMessageResponse response = convertToResponse(savedMessage);

        // Send real-time notification
        sendRealTimeNotification(ticket.getId(), response);

        log.info("Message sent successfully: {}", savedMessage.getId());
        return response;
    }

    @Override
    public Page<ChatMessageResponse> getMessagesByTicketId(Long ticketId, Pageable pageable) {
        log.info("Getting messages for ticket: {}", ticketId);

        Page<ChatMessage> messages = chatMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId, pageable);
        return messages.map(this::convertToResponse);
    }

    @Override
    public List<ChatMessageResponse> getRecentMessagesByTicketId(Long ticketId, int limit) {
        log.info("Getting recent {} messages for ticket: {}", limit, ticketId);

        Pageable pageable = PageRequest.of(0, limit);
        List<ChatMessage> messages = chatMessageRepository.findTopNByTicketIdOrderByCreatedAtDesc(ticketId, pageable);
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageResponse> getMessagesByTicketIdSince(Long ticketId, LocalDateTime since) {
        log.info("Getting messages since {} for ticket: {}", since, ticketId);

        List<ChatMessage> messages = chatMessageRepository.findByTicketIdAndCreatedAtAfterOrderByCreatedAtAsc(ticketId,
                since);
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public void markMessageAsRead(Long messageId, User user) {
        log.info("Marking message {} as read by user: {}", messageId, user.getEmail());

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessLogicException("Message not found"));

        // Check if user has permission to read this message
        if (!hasPermissionToReadMessage(message.getTicket(), user)) {
            throw new BusinessLogicException("You don't have permission to read messages from this ticket");
        }

        message.markAsRead();
        chatMessageRepository.save(message);

        // Send read receipt notification
        sendReadReceiptNotification(message.getTicket().getId(), messageId, user);
    }

    @Override
    public void markAllMessagesAsRead(Long ticketId, User user) {
        log.info("Marking all messages as read for ticket: {} by user: {}", ticketId, user.getEmail());

        // Check if user has permission
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessLogicException("Ticket not found"));

        if (!hasPermissionToReadMessage(ticket, user)) {
            throw new BusinessLogicException("You don't have permission to read messages from this ticket");
        }

        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessagesByTicketId(ticketId);
        unreadMessages.forEach(ChatMessage::markAsRead);
        chatMessageRepository.saveAll(unreadMessages);

        // Reset unread message count
        ticket.resetUnreadMessageCount();
        ticketRepository.save(ticket);
    }

    @Override
    public long getUnreadMessageCount(Long ticketId, User user) {
        log.info("Getting unread message count for ticket: {} by user: {}", ticketId, user.getEmail());

        // Check if user has permission
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessLogicException("Ticket not found"));

        if (!hasPermissionToReadMessage(ticket, user)) {
            return 0;
        }

        return chatMessageRepository.countUnreadMessagesByTicketId(ticketId);
    }

    @Override
    public ChatMessageResponse sendSystemMessage(Long ticketId, String content, User sender) {
        log.info("Sending system message for ticket: {}", ticketId);

        SendMessageRequest request = new SendMessageRequest();
        request.setTicketId(ticketId);
        request.setContent(content);
        request.setMessageType(ChatMessageType.SYSTEM);
        request.setIsInternal(true);

        return sendMessage(request, sender);
    }

    @Override
    public void deleteMessage(Long messageId, User user) {
        log.info("Deleting message: {} by user: {}", messageId, user.getEmail());

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessLogicException("Message not found"));

        // Check if user has permission to delete this message
        if (!hasPermissionToDeleteMessage(message, user)) {
            throw new BusinessLogicException("You don't have permission to delete this message");
        }

        chatMessageRepository.delete(message);

        // Send deletion notification
        sendMessageDeletionNotification(message.getTicket().getId(), messageId);
    }

    @Override
    public List<ChatMessageResponse> getMessagesWithAttachments(Long ticketId) {
        log.info("Getting messages with attachments for ticket: {}", ticketId);

        List<ChatMessage> messages = chatMessageRepository.findMessagesWithAttachmentsByTicketId(ticketId);
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageResponse> getReplyMessages(Long messageId) {
        log.info("Getting reply messages for message: {}", messageId);

        List<ChatMessage> messages = chatMessageRepository.findByReplyToMessageIdOrderByCreatedAtAsc(messageId);
        return messages.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public Page<ChatMessageResponse> searchMessages(Long ticketId, String query, Pageable pageable) {
        log.info("Searching messages in ticket: {} with query: {}", ticketId, query);

        // This would require a custom repository method with full-text search
        // For now, we'll return empty results
        return Page.empty(pageable);
    }

    @Override
    public void updateTypingStatus(Long ticketId, User user, boolean isTyping) {
        log.info("Updating typing status for ticket: {}, user: {}, isTyping: {}", ticketId, user.getEmail(), isTyping);

        if (isTyping) {
            typingUsers.computeIfAbsent(ticketId, k -> new ConcurrentHashMap<>())
                    .put(user.getId(), LocalDateTime.now());
        } else {
            typingUsers.computeIfPresent(ticketId, (k, v) -> {
                v.remove(user.getId());
                return v.isEmpty() ? null : v;
            });
        }

        // Send typing status notification
        sendTypingStatusNotification(ticketId, user, isTyping);
    }

    @Override
    public List<String> getTypingUsers(Long ticketId) {
        Map<Long, LocalDateTime> users = typingUsers.get(ticketId);
        if (users == null)
            return List.of();

        // Remove users who stopped typing more than 5 seconds ago
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(5);
        users.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));

        return users.keySet().stream()
                .map(userId -> userRepository.findById(userId))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(User::getEmail)
                .collect(Collectors.toList());
    }

    // Helper methods

    private boolean hasPermissionToSendMessage(Ticket ticket, User user) {
        // Agents can send messages to any ticket
        if (user.getRole().isAgent()) {
            return true;
        }

        // Customers can only send messages to their own tickets
        if (user.getRole() == UserRole.CUSTOMER) {
            return ticket.getCustomer() != null && ticket.getCustomer().getId().equals(user.getId());
        }

        return false;
    }

    private boolean hasPermissionToReadMessage(Ticket ticket, User user) {
        // Agents can read messages from any ticket
        if (user.getRole().isAgent()) {
            return true;
        }

        // Customers can only read messages from their own tickets
        if (user.getRole() == UserRole.CUSTOMER) {
            return ticket.getCustomer() != null && ticket.getCustomer().getId().equals(user.getId());
        }

        return false;
    }

    private boolean hasPermissionToDeleteMessage(ChatMessage message, User user) {
        // Users can only delete their own messages
        return message.getSender().getId().equals(user.getId());
    }

    private ChatMessageResponse convertToResponse(ChatMessage message) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setId(message.getId());
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType());
        response.setIsInternal(message.getIsInternal());
        response.setIsRead(message.getIsRead());
        response.setReadAt(message.getReadAt());
        response.setReplyToMessageId(message.getReplyToMessageId());
        response.setFileUrl(message.getFileUrl());
        response.setFileName(message.getFileName());
        response.setFileSize(message.getFileSize());
        response.setFileType(message.getFileType());
        response.setCreatedAt(message.getCreatedAt());
        response.setUpdatedAt(message.getUpdatedAt());

        // Sender information
        if (message.getSender() != null) {
            response.setSenderId(message.getSender().getId());
            response.setSenderName(message.getSender().getFirstName() + " " + message.getSender().getLastName());
            response.setSenderEmail(message.getSender().getEmail());
            response.setSenderRole(message.getSender().getRole());
            response.setSenderAvatar(null); // Avatar field not implemented yet
        }

        // Ticket information
        if (message.getTicket() != null) {
            response.setTicketId(message.getTicket().getId());
            response.setTicketNumber(message.getTicket().getTicketNumber());
        }

        // Additional fields
        response.setPreview(message.getPreview());
        response.setAgeInMinutes(message.getAgeInMinutes());
        response.setHasAttachment(message.hasAttachment());
        response.setIsSystemMessage(message.isSystemMessage());
        response.setIsAgentMessage(message.isAgentMessage());
        response.setIsCustomerMessage(message.isCustomerMessage());

        return response;
    }

    private void sendRealTimeNotification(Long ticketId, ChatMessageResponse message) {
        String destination = "/topic/ticket/" + ticketId + "/chat";
        messagingTemplate.convertAndSend(destination, message);
    }

    private void sendReadReceiptNotification(Long ticketId, Long messageId, User user) {
        String destination = "/topic/ticket/" + ticketId + "/read-receipt";
        messagingTemplate.convertAndSend(destination, Map.of(
                "messageId", messageId,
                "readBy", user.getEmail(),
                "readAt", LocalDateTime.now()));
    }

    private void sendMessageDeletionNotification(Long ticketId, Long messageId) {
        String destination = "/topic/ticket/" + ticketId + "/message-deleted";
        messagingTemplate.convertAndSend(destination, Map.of(
                "messageId", messageId,
                "deletedAt", LocalDateTime.now()));
    }

    private void sendTypingStatusNotification(Long ticketId, User user, boolean isTyping) {
        String destination = "/topic/ticket/" + ticketId + "/typing";
        messagingTemplate.convertAndSend(destination, Map.of(
                "userId", user.getId(),
                "userEmail", user.getEmail(),
                "isTyping", isTyping,
                "timestamp", LocalDateTime.now()));
    }
}