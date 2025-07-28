package com.example.smartdeskbackend.security;

import com.example.smartdeskbackend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * Her request'te JWT token'ı kontrol eder ve authentication context'i ayarlar
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                String email = jwtUtil.getEmailFromToken(jwt);

                // Kullanıcı bilgilerini yükle
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // Authentication token oluştur
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Security context'e authentication'ı set et
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Request'e kullanıcı bilgilerini ekle (opsiyonel)
                    request.setAttribute("userId", jwtUtil.getUserIdFromToken(jwt));
                    request.setAttribute("userEmail", email);
                    request.setAttribute("userRole", jwtUtil.getRoleFromToken(jwt));
                    request.setAttribute("companyId", jwtUtil.getCompanyIdFromToken(jwt));
                    request.setAttribute("departmentId", jwtUtil.getDepartmentIdFromToken(jwt));
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
            // Security context'i temizle
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Request'ten JWT token'ı çıkarır
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " kısmını çıkar
        }

        return null;
    }

    /**
     * Bu filter'ın çalışmayacağı endpoint'leri belirler
     */
    /**
     * Bu filter'ın çalışmayacağı endpoint'leri belirler
     */
    /**
     * Bu filter'ın çalışmayacağı endpoint'leri belirler
     */
    /**
     * Bu filter'ın çalışmayacağı endpoint'leri belirler
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        logger.debug("Checking JWT filter for path: {} method: {}", path, method);

        // OPTIONS requests için filter'ı atla (CORS preflight)
        if ("OPTIONS".equals(method)) {
            logger.debug("Skipping JWT filter for OPTIONS request");
            return true;
        }

        // Public endpoint'ler - EXACT MATCH
        boolean shouldSkip =
                // Ana endpoints
                path.equals("/api/") ||
                        path.equals("/api/health") ||
                        path.equals("/api/info") ||
                        path.equals("/") ||
                        path.equals("/health") ||
                        path.equals("/info") ||

                        // Auth endpoints
                        path.equals("/api/v1/auth/health") ||
                        path.equals("/api/v1/auth/login") ||
                        path.equals("/api/v1/auth/register") ||
                        path.equals("/api/v1/auth/refresh") ||
                        path.equals("/api/v1/auth/logout") ||
                        path.equals("/api/v1/auth/forgot-password") ||
                        path.equals("/api/v1/auth/reset-password") ||
                        path.equals("/api/v1/auth/verify-email") ||
                        path.equals("/api/v1/auth/validate-token") ||

                        // Test endpoints
                        path.equals("/api/v1/test/public") ||
                        path.equals("/api/v1/test/security") ||
                        path.equals("/api/v1/test/info") ||

                        // Prefix matches
                        path.startsWith("/api/v1/public/") ||
                        path.startsWith("/api/actuator/") ||
                        path.startsWith("/actuator/");

        if (shouldSkip) {
            logger.debug("Skipping JWT filter for public path: {}", path);
        } else {
            logger.debug("JWT filter will process path: {}", path);
        }

        return shouldSkip;
    }
}