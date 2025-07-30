package com.example.smartdeskbackend.controller;

import com.example.smartdeskbackend.dto.NotificationDTO;
import com.example.smartdeskbackend.dto.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.annotation.SendToUser;
import java.security.Principal;

@Controller
public class WebSocketMessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        System.out.println("Genel sohbet mesajı alındı: " + chatMessage.getSender() + " - " + chatMessage.getContent());
        messagingTemplate.convertAndSend("/topic/public-chat", chatMessage);
    }

    @MessageMapping("/chat.sendTicketMessage")
    public void sendTicketMessage(@Payload ChatMessage chatMessage, Principal principal) {
        if (chatMessage.getTicketId() != null) {
            String destination = "/topic/ticket-chat/" + chatMessage.getTicketId();
            System.out.println("Bilet sohbet mesajı alındı: " + chatMessage.getSender() + " (Ticket ID: " + chatMessage.getTicketId() + ") - " + chatMessage.getContent());
            messagingTemplate.convertAndSend(destination, chatMessage);
        } else {
            System.err.println("Ticket ID olmadan bilet sohbet mesajı alındı.");
        }
    }

    public void sendPrivateNotification(String username, NotificationDTO notification) {
        System.out.println("Özel bildirim gönderiliyor: " + username + " - " + notification.getMessage());
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", notification);
    }

}