package com.example.smartdeskbackend.service;

import com.example.smartdeskbackend.dto.request.auth.LoginRequest;
import com.example.smartdeskbackend.dto.request.auth.RegisterRequest;
import com.example.smartdeskbackend.dto.response.auth.AuthResponse;
import com.example.smartdeskbackend.entity.User;

/**
 * Authentication ve Authorization işlemleri için service interface
 */
public interface AuthService {

    /**
     * Kullanıcı girişi
     */
    AuthResponse login(LoginRequest loginRequest);

    /**
     * Kullanıcı kaydı
     */
    AuthResponse register(RegisterRequest registerRequest);

    /**
     * Refresh token ile yeni access token alma
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Kullanıcı çıkışı (token invalidate)
     */
    void logout(String token);

    /**
     * Şifre sıfırlama isteği
     */
    void requestPasswordReset(String email);

    /**
     * Şifre sıfırlama
     */
    void resetPassword(String token, String newPassword);

    /**
     * Email doğrulama
     */
    void verifyEmail(String token);

    /**
     * Token doğrulama
     */
    boolean validateToken(String token);

    /**
     * Kullanıcı şifre değiştirme
     */
    void changePassword(Long userId, String currentPassword, String newPassword);
}