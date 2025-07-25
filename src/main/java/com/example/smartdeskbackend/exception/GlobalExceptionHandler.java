package com.example.smartdeskbackend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler
 * Tüm controller'lardaki hataları yakalar ve standart response döndürür
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Authentication hataları
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {

        logger.error("Authentication error: {}", ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "AUTHENTICATION_ERROR",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.UNAUTHORIZED
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Kullanıcı zaten mevcut hatası
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, WebRequest request) {

        logger.error("User already exists: {}", ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "USER_ALREADY_EXISTS",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.CONFLICT
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Kaynak bulunamadı hatası
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        logger.error("Resource not found: {}", ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.NOT_FOUND
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * İş mantığı hataları
     */
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<?> handleBusinessLogicException(
            BusinessLogicException ex, WebRequest request) {

        logger.error("Business logic error: {}", ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "BUSINESS_LOGIC_ERROR",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.BAD_REQUEST
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Validation hataları
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        logger.error("Validation errors: {}", fieldErrors);

        Map<String, Object> errorResponse = createErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed for one or more fields",
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.BAD_REQUEST
        );

        errorResponse.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Access denied hataları (Spring Security)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {

        logger.error("Access denied: {}", ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "ACCESS_DENIED",
                "You don't have permission to access this resource",
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.FORBIDDEN
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Genel exception handler
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(
            Exception ex, WebRequest request) {

        logger.error("Unexpected error: ", ex);

        Map<String, Object> errorResponse = createErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                request.getDescription(false).replace("uri=", ""),
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        // Development ortamında detaylı hata mesajı ekle
        if (isDevelopmentEnvironment()) {
            errorResponse.put("debugMessage", ex.getMessage());
            errorResponse.put("stackTrace", getStackTrace(ex));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Standart hata response'u oluşturur
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message,
                                                    String path, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("status", status.value());
        errorResponse.put("path", path);
        errorResponse.put("timestamp", LocalDateTime.now());
        return errorResponse;
    }

    /**
     * Development ortamı kontrolü
     */
    private boolean isDevelopmentEnvironment() {
        String profile = System.getProperty("spring.profiles.active");
        return profile != null && (profile.contains("dev") || profile.contains("local"));
    }

    /**
     * Stack trace'i string olarak döndürür
     */
    private String getStackTrace(Exception ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}