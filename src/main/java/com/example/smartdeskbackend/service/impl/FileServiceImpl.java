
package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.config.ApplicationConfig;
import com.example.smartdeskbackend.entity.TicketAttachment;
import com.example.smartdeskbackend.entity.Ticket;
import com.example.smartdeskbackend.entity.User;
import com.example.smartdeskbackend.exception.BusinessLogicException;
import com.example.smartdeskbackend.exception.ResourceNotFoundException;
import com.example.smartdeskbackend.repository.TicketAttachmentRepository;
import com.example.smartdeskbackend.repository.TicketRepository;
import com.example.smartdeskbackend.repository.UserRepository;
import com.example.smartdeskbackend.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

/**
 * File service implementation
 */
@Service
@Transactional
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Autowired
    private ApplicationConfig.FileUploadProperties fileUploadProperties;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketAttachmentRepository ticketAttachmentRepository;

    private Path fileStorageLocation;

    public FileServiceImpl() {
        // Constructor'da fileStorageLocation initialize edilecek
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(fileUploadProperties.getDirectory())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("File storage location initialized: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            logger.error("Could not create the directory where the uploaded files will be stored.", ex);
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String category) throws IOException {
        logger.info("Uploading file: {} to category: {}", file.getOriginalFilename(), category);

        // Validasyonlar
        validateFile(file);

        // Güvenli dosya adı oluştur
        String fileName = generateSafeFileName(file.getOriginalFilename());

        // Kategori dizini oluştur
        createDirectoryIfNotExists(category);

        // Dosya yolu
        Path categoryPath = this.fileStorageLocation.resolve(category);
        Path targetLocation = categoryPath.resolve(fileName);

        // Dosyayı kaydet
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        logger.info("File uploaded successfully: {}", fileName);
        return category + "/" + fileName;
    }

    @Override
    public String uploadTicketAttachment(MultipartFile file, Long ticketId, Long userId) throws IOException {
        logger.info("Uploading ticket attachment for ticket: {} by user: {}", ticketId, userId);

        // Ticket kontrolü
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        // User kontrolü
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Dosya yükle
        String filePath = uploadFile(file, "tickets/" + ticketId);

        // TicketAttachment entity oluştur
        TicketAttachment attachment = new TicketAttachment(
                file.getOriginalFilename(),
                extractFileName(filePath),
                file.getSize(),
                file.getContentType(),
                ticket,
                user
        );

        attachment.setFilePath(filePath);
        ticketAttachmentRepository.save(attachment);

        logger.info("Ticket attachment created with id: {}", attachment.getId());
        return filePath;
    }

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        logger.info("Uploading avatar for user: {}", userId);

        // User kontrolü
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Sadece image dosyalarına izin ver
        if (!file.getContentType().startsWith("image/")) {
            throw new BusinessLogicException("Only image files are allowed for avatars");
        }

        // Dosya boyutu kontrolü (2MB limit)
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessLogicException("Avatar file size cannot exceed 2MB");
        }

        // Eski avatar'ı sil (varsa)
        if (StringUtils.hasText(user.getAvatarUrl())) {
            try {
                deleteFile(user.getAvatarUrl());
            } catch (Exception e) {
                logger.warn("Could not delete old avatar: {}", user.getAvatarUrl(), e);
            }
        }

        // Yeni avatar'ı yükle
        String filePath = uploadFile(file, "avatars");

        // User entity'sini güncelle
        user.setAvatarUrl(filePath);
        userRepository.save(user);

        logger.info("Avatar uploaded successfully for user: {}", userId);
        return filePath;
    }

    @Override
    public Resource downloadFile(String fileName) throws IOException {
        logger.debug("Downloading file: {}", fileName);

        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            logger.error("File not found: {}", fileName, ex);
            throw new ResourceNotFoundException("File not found: " + fileName);
        }
    }

    @Override
    public void deleteFile(String fileName) throws IOException {
        logger.info("Deleting file: {}", fileName);

        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                logger.info("File deleted successfully: {}", fileName);
            } else {
                logger.warn("File not found for deletion: {}", fileName);
            }
        } catch (IOException ex) {
            logger.error("Could not delete file: {}", fileName, ex);
            throw new IOException("Could not delete file: " + fileName, ex);
        }
    }

    @Override
    public boolean fileExists(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.exists(filePath);
        } catch (Exception e) {
            logger.error("Error checking file existence: {}", fileName, e);
            return false;
        }
    }

    @Override
    public boolean isValidFileType(String fileName, String[] allowedTypes) {
        if (fileName == null || allowedTypes == null) {
            return false;
        }

        String fileExtension = getFileExtension(fileName).toLowerCase();
        return Arrays.stream(allowedTypes)
                .anyMatch(type -> type.toLowerCase().equals(fileExtension));
    }

    @Override
    public boolean isValidFileSize(MultipartFile file, long maxSize) {
        return file.getSize() <= maxSize;
    }

    @Override
    public String generateSafeFileName(String originalFileName) {
        // Dosya uzantısını ayır
        String fileExtension = getFileExtension(originalFileName);
        String baseName = getBaseName(originalFileName);

        // Güvenli karakterlere dönüştür
        String safeName = baseName.replaceAll("[^a-zA-Z0-9.-]", "_");

        // Benzersiz prefix ekle
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        return timestamp + "_" + uniqueId + "_" + safeName + "." + fileExtension;
    }

    @Override
    public void createDirectoryIfNotExists(String category) throws IOException {
        Path categoryPath = this.fileStorageLocation.resolve(category);
        if (!Files.exists(categoryPath)) {
            Files.createDirectories(categoryPath);
            logger.debug("Created directory: {}", categoryPath);
        }
    }

    // ============ Helper Methods ============

    /**
     * Dosya validasyonları
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessLogicException("File is empty");
        }

        // Dosya boyutu kontrolü
        if (!isValidFileSize(file, fileUploadProperties.getMaxSize())) {
            throw new BusinessLogicException("File size exceeds maximum allowed size: " +
                    (fileUploadProperties.getMaxSize() / 1024 / 1024) + "MB");
        }

        // Dosya tipi kontrolü
        String fileName = file.getOriginalFilename();
        if (!isValidFileType(fileName, fileUploadProperties.getAllowedTypesArray())) {
            throw new BusinessLogicException("File type not allowed. Allowed types: " +
                    String.join(", ", fileUploadProperties.getAllowedTypesArray()));
        }

        // Dosya adı kontrolü
        if (!StringUtils.hasText(fileName)) {
            throw new BusinessLogicException("File name is invalid");
        }

        // Potansiyel tehlikeli dosya adları kontrolü
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new BusinessLogicException("File name contains invalid characters");
        }
    }

    /**
     * Dosya uzantısını çıkarır
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Dosya adının base kısmını çıkarır (uzantı olmadan)
     */
    private String getBaseName(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return fileName != null ? fileName : "";
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    /**
     * File path'ten dosya adını çıkarır
     */
    private String extractFileName(String filePath) {
        if (filePath == null) {
            return "";
        }
        return Paths.get(filePath).getFileName().toString();
    }

    /**
     * MIME type'dan file extension çıkarır
     */
    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) {
            return "bin";
        }

        switch (mimeType.toLowerCase()) {
            case "image/jpeg":
            case "image/jpg":
                return "jpg";
            case "image/png":
                return "png";
            case "image/gif":
                return "gif";
            case "application/pdf":
                return "pdf";
            case "application/msword":
                return "doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return "docx";
            case "text/plain":
                return "txt";
            case "application/zip":
                return "zip";
            case "application/vnd.ms-excel":
                return "xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return "xlsx";
            default:
                return "bin";
        }
    }

    /**
     * Dosya boyutunu human-readable format'a çevirir
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
