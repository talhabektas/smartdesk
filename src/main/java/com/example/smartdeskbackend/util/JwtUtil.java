package com.example.smartdeskbackend.util;

import com.example.smartdeskbackend.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Token işlemlerini yöneten utility sınıfı
 * Token oluşturma, doğrulama ve claim extraction işlemleri
 */
@Component // Bu anotasyonun olduğundan emin olun!
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private Long jwtRefreshExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Kullanıcı için access token oluşturur
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().getCode());
        claims.put("fullName", user.getFullName());
        claims.put("companyId", user.getCompany() != null ? user.getCompany().getId() : null);
        claims.put("departmentId", user.getDepartment() != null ? user.getDepartment().getId() : null);
        claims.put("tokenType", "ACCESS");

        return createToken(claims, user.getEmail(), jwtExpirationMs);
    }

    /**
     * Kullanıcı için refresh token oluşturur
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("tokenType", "REFRESH");

        return createToken(claims, user.getEmail(), jwtRefreshExpirationMs);
    }

    /**
     * Authentication objesi üzerinden token oluşturur
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateTokenFromEmail(userPrincipal.getUsername());
    }

    /**
     * Email üzerinden token oluşturur (basit versiyon)
     */
    public String generateTokenFromEmail(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "ACCESS");
        return createToken(claims, email, jwtExpirationMs);
    }

    /**
     * Claims ve subject ile token oluşturur
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Token'dan email/username'i çıkarır
     */
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Token'dan email'i çıkarır (alias for getEmailFromToken)
     */
    public String extractEmail(String token) {
        return getEmailFromToken(token);
    }

    /**
     * Token'dan kullanıcı ID'sini çıkarır
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object userId = claims.get("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    /**
     * Token'dan kullanıcı rolünü çıkarır
     */
    public String getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (String) claims.get("role");
    }

    /**
     * Token'dan şirket ID'sini çıkarır
     */
    public Long getCompanyIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object companyId = claims.get("companyId");
        return companyId != null ? Long.valueOf(companyId.toString()) : null;
    }

    /**
     * Token'dan departman ID'sini çıkarır
     */
    public Long getDepartmentIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object departmentId = claims.get("departmentId");
        return departmentId != null ? Long.valueOf(departmentId.toString()) : null;
    }

    /**
     * Token'dan token tipini çıkarır (ACCESS/REFRESH)
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (String) claims.get("tokenType");
    }

    /**
     * Token'dan expiration date'i çıkarır
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Token'dan belirli bir claim'i çıkarır
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Token'dan tüm claims'leri çıkarır
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                logger.error("JWT token is null or empty");
                throw new IllegalArgumentException("JWT token cannot be null or empty");
            }
            
            // Token format kontrolü (Bearer prefix varsa kaldır)
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.error("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            logger.error("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("JWT token compact of handler are invalid: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error parsing JWT token: {}", e.getMessage());
            throw new JwtException("Token parsing failed", e);
        }
    }

    /**
     * Token'ın expire olup olmadığını kontrol eder
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Token'ın geçerli olup olmadığını kontrol eder
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String email = getEmailFromToken(token);
            return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Token'ın geçerli olup olmadığını kontrol eder (UserDetails olmadan)
     */
    public Boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Refresh token'ın geçerli olup olmadığını kontrol eder
     */
    public Boolean validateRefreshToken(String token) {
        try {
            String tokenType = getTokenTypeFromToken(token);
            return "REFRESH".equals(tokenType) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Token'dan kalan süreyi milisaniye cinsinden döndürür
     */
    public Long getTimeToExpiration(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.getTime() - new Date().getTime();
        } catch (JwtException e) {
            return 0L;
        }
    }

    /**
     * Token'ın yakında expire olup olmayacağını kontrol eder (5 dakika kala)
     */
    public Boolean isTokenExpiringSoon(String token) {
        Long timeToExpiration = getTimeToExpiration(token);
        return timeToExpiration != null && timeToExpiration < 300000; // 5 minutes
    }

    /**
     * Token'dan user information map'i çıkarır
     */
    public Map<String, Object> getUserInfoFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Map<String, Object> userInfo = new HashMap<>();

        userInfo.put("userId", claims.get("userId"));
        userInfo.put("email", claims.getSubject());
        userInfo.put("role", claims.get("role"));
        userInfo.put("fullName", claims.get("fullName"));
        userInfo.put("companyId", claims.get("companyId"));
        userInfo.put("departmentId", claims.get("departmentId"));
        userInfo.put("issuedAt", claims.getIssuedAt());
        userInfo.put("expiration", claims.getExpiration());

        return userInfo;
    }

    /**
     * Token'ı parse eder ve hata durumunda detaylı bilgi verir
     */
    public String parseTokenWithErrorDetails(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return "Token is valid. Subject: " + claims.getSubject() +
                    ", Expiration: " + claims.getExpiration();
        } catch (ExpiredJwtException e) {
            return "Token is expired. Expiration was: " + e.getClaims().getExpiration();
        } catch (UnsupportedJwtException e) {
            return "Token is unsupported: " + e.getMessage();
        } catch (MalformedJwtException e) {
            return "Token is malformed: " + e.getMessage();
        } catch (io.jsonwebtoken.security.SignatureException e) {
            return "Token signature is invalid: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Token is illegal or empty: " + e.getMessage();
        } catch (Exception e) {
            return "Unknown token error: " + e.getMessage();
        }
    }

    // Getter methods for configuration values
    public Long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    public Long getJwtRefreshExpirationMs() {
        return jwtRefreshExpirationMs;
    }
}