package com.example.smartdeskbackend.enums;

/**
 * Chat mesaj tipleri
 */
public enum ChatMessageType {
    TEXT, // Normal metin mesajı
    IMAGE, // Resim
    FILE, // Dosya
    SYSTEM, // Sistem mesajı (otomatik)
    NOTIFICATION, // Bildirim
    EMOJI, // Emoji
    QUICK_REPLY, // Hızlı yanıt
    TYPING, // Yazıyor... durumu
    READ_RECEIPT // Okundu bilgisi
}