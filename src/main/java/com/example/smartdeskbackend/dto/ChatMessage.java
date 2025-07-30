package com.example.smartdeskbackend.dto;


public class ChatMessage {
    private String sender;
    private String content;
    private Long ticketId;

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public ChatMessage() {}
}


