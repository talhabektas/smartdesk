package com.example.smartdeskbackend.service.impl;

import com.example.smartdeskbackend.dto.request.auth.LoginRequest;
import com.example.smartdeskbackend.dto.request.auth.RegisterRequest;
import com.example.smartdeskbackend.dto.response.auth.AuthResponse;
import com.example.smartdeskbackend.entity.Company;
import com.example.smartdeskbackend.entity.User;
import com.example.smartdeskbackend.enums.UserRole;
import com.example.smartdeskbackend.enums.UserStatus;
import com.example.smartdeskbackend.exception.AuthenticationException;
import com.example.smartdeskbackend.exception.BusinessLogicException;
import com.example.smartdeskbackend.exception.ResourceNotFoundException;
import com.example.smartdeskbackend.exception.UserAlreadyExistsException;
import com.example.smartdeskbackend.repository.CompanyRepository;
import com.example.smartdeskbackend.repository.UserRepository;
import com.example.smartdeskbackend.service.AuthService;
import com.example.smartdeskbackend.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Authentication service implementasyonu
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("Login attempt for email: {}", loginRequest.getEmail());

        try {
            // Kullanıcıyı kontrol et
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

            // Hesap durumu kontrolleri
            if (!user.canLogin()) {
                handleFailedLogin(user);
                throw new AuthenticationException("Account is not active or verified");
            }

            if (user.isAccountLocked()) {
                throw new AuthenticationException("Account is temporarily locked. Please try again later.");
            }

            // Authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Başarılı giriş işlemleri
            user.recordSuccessfulLogin();
            userRepository.save(user);

            // Token oluştur
            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            logger.info("Successful login for user: {}", user.getEmail());

            return createAuthResponse(accessToken, refreshToken, user);

        } catch (BadCredentialsException e) {
            logger.warn("Invalid credentials for email: {}", loginRequest.getEmail());
            handleFailedLoginAttempt(loginRequest.getEmail());
            throw new AuthenticationException("Invalid email or password");

        } catch (DisabledException e) {
            logger.warn("Disabled account login attempt: {}", loginRequest.getEmail());
            throw new AuthenticationException("Account is disabled");

        } catch (Exception e) {
            logger.error("Login error for email: {}", loginRequest.getEmail(), e);
            throw new AuthenticationException("Login failed: " + e.getMessage());
        }
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        logger.info("Registration attempt for email: {}", registerRequest.getEmail());

        // Email kontrolü
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("User already exists with email: " + registerRequest.getEmail());
        }

        // Company kontrolü (eğer belirtilmişse)
        Company company = null;
        if (registerRequest.getCompanyId() != null) {
            company = companyRepository.findById(registerRequest.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + registerRequest.getCompanyId()));

            // Company kullanıcı limitini kontrol et
            if (!company.canAddMoreUsers()) {
                throw new BusinessLogicException("Company user limit exceeded");
            }
        }

        // Yeni kullanıcı oluştur
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhone(registerRequest.getPhone());
        user.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : UserRole.CUSTOMER);
        user.setStatus(UserStatus.PENDING); // Email doğrulaması bekler
        user.setCompany(company);

        // Email doğrulama token'ı oluştur
        user.setEmailVerificationToken(UUID.randomUUID().toString());

        user = userRepository.save(user);

        // TODO: Email doğrulama maili gönder
        // emailService.sendVerificationEmail(user);

        logger.info("User registered successfully: {}", user.getEmail());

        // Token oluştur (email doğrulanmamış olsa da)
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return createAuthResponse(accessToken, refreshToken, user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        logger.debug("Refresh token request");

        try {
            // Refresh token'ı validate et
            if (!jwtUtil.validateRefreshToken(refreshToken)) {
                throw new AuthenticationException("Invalid refresh token");
            }

            // Token'dan email'i çıkar
            String email = jwtUtil.getEmailFromToken(refreshToken);

            // Kullanıcıyı bul
            User user = userRepository.findByEmailAndIsActiveTrue(email)
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            // Yeni access token oluştur
            String newAccessToken = jwtUtil.generateAccessToken(user);
            String newRefreshToken = jwtUtil.generateRefreshToken(user);

            logger.debug("Tokens refreshed for user: {}", user.getEmail());

            return createAuthResponse(newAccessToken, newRefreshToken, user);

        } catch (Exception e) {
            logger.error("Refresh token error", e);
            throw new AuthenticationException("Token refresh failed: " + e.getMessage());
        }
    }

    @Override
    public void logout(String token) {
        logger.debug("Logout request");

        try {
            // Token'ı validate et
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.getEmailFromToken(token);
                logger.info("User logged out: {}", email);

                // TODO: Token'ı blacklist'e ekle (Redis kullanılabilir)
                // tokenBlacklistService.addToBlacklist(token);
            }
        } catch (Exception e) {
            logger.error("Logout error", e);
            // Logout işlemi hata verirse bile başarılı sayılır
        }
    }

    @Override
    public void requestPasswordReset(String email) {
        logger.info("Password reset request for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Reset token oluştur
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(1)); // 1 saat geçerli

        userRepository.save(user);

        // TODO: Şifre sıfırlama emaili gönder
        // emailService.sendPasswordResetEmail(user, resetToken);

        logger.info("Password reset email sent to: {}", email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        logger.info("Password reset attempt with token");

        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new AuthenticationException("Invalid or expired reset token"));

        // Token geçerliliğini kontrol et
        if (!user.isPasswordResetTokenValid()) {
            throw new AuthenticationException("Reset token has expired");
        }

        // Yeni şifreyi kaydet
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);

        // Hesabı aktif et (eğer pending durumda ise)
        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
        }

        userRepository.save(user);

        logger.info("Password reset successful for user: {}", user.getEmail());
    }

    @Override
    public void verifyEmail(String token) {
        logger.info("Email verification attempt");

        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new AuthenticationException("Invalid verification token"));

        // Email'i doğrula
        user.verifyEmail();
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        logger.info("Email verified for user: {}", user.getEmail());
    }

    @Override
    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Password change request for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Mevcut şifreyi kontrol et
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        // Yeni şifreyi kaydet
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Password changed successfully for user: {}", userId);
    }

    // Helper Methods

    /**
     * AuthResponse oluşturur
     */
    private AuthResponse createAuthResponse(String accessToken, String refreshToken, User user) {
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole().getCode())
                .status(user.getStatus().getCode())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .avatarUrl(user.getAvatarUrl())
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getJwtExpirationMs() / 1000) // saniye cinsinden
                .user(userInfo)
                .build();
    }

    /**
     * Başarısız giriş denemesini işler
     */
    private void handleFailedLoginAttempt(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.recordFailedLogin();
            userRepository.save(user);
        });
    }

    /**
     * Başarısız giriş durumunda kullanıcı durumunu kontrol eder
     */
    private void handleFailedLogin(User user) {
        if (user.getStatus() == UserStatus.PENDING) {
            // TODO: Yeniden doğrulama emaili gönder
            // emailService.resendVerificationEmail(user);
        }
    }
}