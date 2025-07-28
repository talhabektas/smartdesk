package com.example.smartdeskbackend.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * File service interface
 */
public interface FileService {

    /**
     * Dosya yükle
     */
    String uploadFile(MultipartFile file, String category) throws IOException;

    /**
     * Ticket için dosya yükle
     */
    String uploadTicketAttachment(MultipartFile file, Long ticketId, Long userId) throws IOException;

    /**
     * Avatar yükle
     */
    String uploadAvatar(MultipartFile file, Long userId) throws IOException;

    /**
     * Dosya indir
     */
    Resource downloadFile(String fileName) throws IOException;

    /**
     * Dosya sil
     */
    void deleteFile(String fileName) throws IOException;

    /**
     * Dosya var mı kontrol et
     */
    boolean fileExists(String fileName);

    /**
     * Dosya tipini kontrol et
     */
    boolean isValidFileType(String fileName, String[] allowedTypes);

    /**
     * Dosya boyutunu kontrol et
     */
    boolean isValidFileSize(MultipartFile file, long maxSize);

    /**
     * Güvenli dosya adı oluştur
     */
    String generateSafeFileName(String originalFileName);

    /**
     * Kategori için upload dizini oluştur
     */
    void createDirectoryIfNotExists(String category) throws IOException;
}
