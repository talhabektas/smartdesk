package com.example.smartdeskbackend.controller;

import com.example.smartdeskbackend.dto.request.auth.LoginRequest;
import com.example.smartdeskbackend.dto.request.auth.RegisterRequest;
import com.example.smartdeskbackend.dto.response.auth.AuthResponse;
import com.example.smartdeskbackend.service.AuthService;
import com.example.smartdeskbackend.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication ve Authorization işlemleri için REST Controller
 * Login, Register, Token Refresh, Password Reset operations
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Kullanıcı kimlik doğrulama ve yetkilendirme işlemleri")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Kullanıcı giriş işlemi
     */
    @PostMapping("/login")
    @Operation(summary = "Kullanıcı Girişi",
            description = "Email ve şifre ile sistem girişi yapılır. JWT token döndürülür.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Giriş başarılı",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Geçersiz kimlik bilgileri"),
            @ApiResponse(responseCode = "423", description = "Hesap kilitli"),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek parametreleri")
    })
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        logger.info("Login request from IP: {} for email: {}",
                getClientIpAddress(request), loginRequest.getEmail());

        try {
            AuthResponse authResponse = authService.login(loginRequest);

            logger.info("Successful login for user: {}", loginRequest.getEmail());
            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            logger.error("Login failed for email: {}", loginRequest.getEmail(), e);

            Map<String, Object> errorResponse = createErrorResponse(
                    "AUTHENTICATION_FAILED",
                    e.getMessage(),
                    "/api/v1/auth/login"
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Yeni kullanıcı kaydı
     */
    @PostMapping("/register")
    @Operation(summary = "Kullanıcı Kaydı",
            description = "Yeni kullanıcı hesabı oluşturulur. Email doğrulaması gerekebilir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Kayıt başarılı",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "409", description = "Kullanıcı zaten mevcut"),
            @ApiResponse(responseCode = "400", description = "Geçersiz kayıt bilgileri")
    })
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request) {

        logger.info("Registration request from IP: {} for email: {}",
                getClientIpAddress(request), registerRequest.getEmail());

        try {
            AuthResponse authResponse = authService.register(registerRequest);

            logger.info("Successful registration for user: {}", registerRequest.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);

        } catch (Exception e) {
            logger.error("Registration failed for email: {}", registerRequest.getEmail(), e);

            Map<String, Object> errorResponse = createErrorResponse(
                    "REGISTRATION_FAILED",
                    e.getMessage(),
                    "/api/v1/auth/register"
            );

            HttpStatus statusCode = e.getMessage().contains("already exists") ?
                    HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(statusCode).body(errorResponse);
        }
    }

    /**
     * Token yenileme işlemi
     */
    @PostMapping("/refresh")
    @Operation(summary = "Token Yenileme",
            description = "Refresh token kullanarak yeni access token alınır.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token yenileme başarılı",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Geçersiz refresh token")
    })
    public ResponseEntity<?> refreshToken(
            @RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");

        if (!StringUtils.hasText(refreshToken)) {
            Map<String, Object> errorResponse = createErrorResponse(
                    "MISSING_REFRESH_TOKEN",
                    "Refresh token is required",
                    "/api/v1/auth/refresh"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            AuthResponse authResponse = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            logger.error("Token refresh failed", e);

            Map<String, Object> errorResponse = createErrorResponse(
                    "TOKEN_REFRESH_FAILED",
                    e.getMessage(),
                    "/api/v1/auth/refresh"
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Kullanıcı çıkış işlemi
     */
    @PostMapping("/logout")
    @Operation(summary = "Kullanıcı Çıkışı",
            description = "Aktif session sonlandırılır ve token geçersizleştirilir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Çıkış başarılı"),
            @ApiResponse(responseCode = "401", description = "Geçersiz token")
    })
    public ResponseEntity<?> logout(HttpServletRequest request) {

        String token = extractTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                String email = jwtUtil.getEmailFromToken(token);
                logger.info("Logout request for user: {}", email);

                authService.logout(token);

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Logout successful");
                response.put("timestamp", java.time.LocalDateTime.now());

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                logger.error("Logout error", e);
            }
        }

        // Logout işlemi her zaman başarılı sayılır
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Logout completed");
        response.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Şifre sıfırlama isteği
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Şifre Sıfırlama İsteği",
            description = "Email adresine şifre sıfırlama linki gönderilir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sıfırlama emaili gönderildi"),
            @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
    })
    public ResponseEntity<?> forgotPassword(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");

        if (!StringUtils.hasText(email)) {
            Map<String, Object> errorResponse = createErrorResponse(
                    "MISSING_EMAIL",
                    "Email address is required",
                    "/api/v1/auth/forgot-password"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            authService.requestPasswordReset(email);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password reset email sent successfully");
            response.put("email", email);
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Password reset request failed for email: {}", email, e);

            Map<String, Object> errorResponse = createErrorResponse(
                    "PASSWORD_RESET_FAILED",
                    e.getMessage(),
                    "/api/v1/auth/forgot-password"
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Şifre sıfırlama
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Şifre Sıfırlama",
            description = "Reset token ile yeni şifre belirlenir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Şifre başarıyla sıfırlandı"),
            @ApiResponse(responseCode = "401", description = "Geçersiz veya süresi dolmuş token")
    })
    public ResponseEntity<?> resetPassword(
            @RequestBody Map<String, String> request) {

        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (!StringUtils.hasText(token) || !StringUtils.hasText(newPassword)) {
            Map<String, Object> errorResponse = createErrorResponse(
                    "MISSING_PARAMETERS",
                    "Token and new password are required",
                    "/api/v1/auth/reset-password"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            authService.resetPassword(token, newPassword);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password reset successful");
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Password reset failed", e);

            Map<String, Object> errorResponse = createErrorResponse(
                    "PASSWORD_RESET_FAILED",
                    e.getMessage(),
                    "/api/v1/auth/reset-password"
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Email doğrulama
     */
    @PostMapping("/verify-email")
    @Operation(summary = "Email Doğrulama",
            description = "Email doğrulama token'ı ile hesap aktifleştirme.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email başarıyla doğrulandı"),
            @ApiResponse(responseCode = "401", description = "Geçersiz doğrulama token'ı")
    })
    public ResponseEntity<?> verifyEmail(
            @RequestParam("token") String token) {

        if (!StringUtils.hasText(token)) {
            Map<String, Object> errorResponse = createErrorResponse(
                    "MISSING_TOKEN",
                    "Verification token is required",
                    "/api/v1/auth/verify-email"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            authService.verifyEmail(token);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Email verified successfully");
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Email verification failed", e);

            Map<String, Object> errorResponse = createErrorResponse(
                    "EMAIL_VERIFICATION_FAILED",
                    e.getMessage(),
                    "/api/v1/auth/verify-email"
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Token doğrulama
     */
    @PostMapping("/validate-token")
    @Operation(summary = "Token Doğrulama",
            description = "JWT token'ın geçerliliğini kontrol eder.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token geçerli"),
            @ApiResponse(responseCode = "401", description = "Token geçersiz")
    })
    public ResponseEntity<?> validateToken(HttpServletRequest request) {

        String token = extractTokenFromRequest(request);

        if (!StringUtils.hasText(token)) {
            Map<String, Object> errorResponse = createErrorResponse(
                    "MISSING_TOKEN",
                    "Authorization token is required",
                    "/api/v1/auth/validate-token"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            boolean isValid = authService.validateToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("timestamp", java.time.LocalDateTime.now());

            if (isValid) {
                // Token geçerli ise kullanıcı bilgilerini de ekle
                Map<String, Object> userInfo = jwtUtil.getUserInfoFromToken(token);
                response.put("user", userInfo);
                response.put("expiresIn", jwtUtil.getTimeToExpiration(token) / 1000); // saniye
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Token validation failed", e);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Şifre değiştirme (authenticated user için)
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Şifre Değiştirme",
            description = "Mevcut kullanıcının şifresini değiştirir.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Şifre başarıyla değiştirildi"),
            @ApiResponse(responseCode = "401", description = "Mevcut şifre yanlış"),
            @ApiResponse(responseCode = "403", description = "Yetki yok")
    })
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (!StringUtils.hasText(currentPassword) || !StringUtils.hasText(newPassword)) {
            Map<String, Object> errorResponse = createErrorResponse(
                    "MISSING_PARAMETERS",
                    "Current password and new password are required",
                    "/api/v1/auth/change-password"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            String token = extractTokenFromRequest(httpRequest);
            Long userId = jwtUtil.getUserIdFromToken(token);

            authService.changePassword(userId, currentPassword, newPassword);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Password change failed", e);

            Map<String, Object> errorResponse = createErrorResponse(
                    "PASSWORD_CHANGE_FAILED",
                    e.getMessage(),
                    "/api/v1/auth/change-password"
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Kullanıcı profil bilgileri (token'dan)
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Kullanıcı Profil Bilgisi",
            description = "JWT token'dan kullanıcı bilgilerini döndürür.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil bilgileri başarıyla alındı"),
            @ApiResponse(responseCode = "401", description = "Geçersiz token")
    })
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {

        try {
            String token = extractTokenFromRequest(request);
            Map<String, Object> userInfo = jwtUtil.getUserInfoFromToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("user", userInfo);
            response.put("tokenInfo", Map.of(
                    "expiresIn", jwtUtil.getTimeToExpiration(token) / 1000,
                    "isExpiringSoon", jwtUtil.isTokenExpiringSoon(token)
            ));
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Get current user failed", e);

            Map<String, Object> errorResponse = createErrorResponse(
                    "USER_INFO_FAILED",
                    e.getMessage(),
                    "/api/v1/auth/me"
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Auth Service Health Check",
            description = "Authentication service'in durumunu kontrol eder.")
    public ResponseEntity<?> healthCheck() {

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "auth-service");
        response.put("timestamp", java.time.LocalDateTime.now());
        response.put("version", "1.0.0");

        return ResponseEntity.ok(response);
    }

    // ============ Helper Methods ============

    /**
     * HTTP request'ten JWT token'ı çıkarır
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * Client IP adresini alır
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Standart hata response'u oluşturur
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        errorResponse.put("timestamp", java.time.LocalDateTime.now());
        return errorResponse;
    }
}