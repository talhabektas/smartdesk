package com.example.smartdeskbackend.config;

import com.example.smartdeskbackend.security.JwtAuthenticationEntryPoint;
import com.example.smartdeskbackend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security yapılandırması
 * JWT tabanlı authentication ve authorization
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Password encoder bean - BCrypt kullanır
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength 12 for better security
    }

    /**
     * Authentication manager bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * DAO Authentication Provider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * CORS yapılandırması
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // İzin verilen origin'ler
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:8080",
                "http://localhost:8081",
                "https://*.smartdesk.com"
        ));

        // İzin verilen HTTP metodları
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // İzin verilen header'lar
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Credentials'a izin ver
        configuration.setAllowCredentials(true);

        // Preflight request'lerin cache süresi
        configuration.setMaxAge(3600L);

        // Response'da expose edilecek header'lar
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-Total-Count",
                "X-Page-Number",
                "X-Page-Size"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    /**
     * Security filter chain yapılandırması
     */
    /**
     * Security filter chain yapılandırması
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF'i devre dışı bırak (JWT kullandığımız için)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS yapılandırmasını uygula
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Session management - stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authentication entry point
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // Authorization rules - SIRALAMA ÇOK ÖNEMLİ!
                // Authorization rules - SIRALAMA ÇOK ÖNEMLİ!
                .authorizeHttpRequests(authz -> authz
                        // OPTIONS requests - CORS preflight için - EN BAŞTA OLMALI
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Ana sayfa ve temel endpoint'ler - EXACT MATCH
                        .requestMatchers("/", "/health", "/info").permitAll()

                        // Authentication ve Public endpoint'leri - WILDCARD PATTERNS
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/public/**").permitAll()
                        .requestMatchers("/api/v1/test/**").permitAll()

                        // Actuator endpoints
                        .requestMatchers("/api/actuator/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // Development endpoints
                        .requestMatchers("/api/swagger-ui/**").permitAll()
                        .requestMatchers("/api/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()

                        // ÇALIŞMAZSA DENEYİN - BROADER PATTERNS
                        .requestMatchers("/**/auth/**").permitAll()
                        .requestMatchers("/**/test/**").permitAll()
                        .requestMatchers("/**/public/**").permitAll()

                        // Admin endpoints - sadece SUPER_ADMIN
                        .requestMatchers("/api/v1/admin/**").hasRole("SUPER_ADMIN")

                        // Company management
                        .requestMatchers("/api/v1/companies/**").hasAnyRole("SUPER_ADMIN", "MANAGER")

                        // User management
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasAnyRole("SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/**").hasAnyRole("SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasAnyRole("SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("SUPER_ADMIN")

                        // Customer management
                        .requestMatchers("/api/v1/customers/**").authenticated()

                        // Ticket management
                        .requestMatchers(HttpMethod.GET, "/api/v1/tickets/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/tickets/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/tickets/**").hasAnyRole("SUPER_ADMIN", "MANAGER", "AGENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/tickets/**").hasAnyRole("SUPER_ADMIN", "MANAGER")

                        // Dashboard ve analytics
                        .requestMatchers("/api/v1/dashboard/**").authenticated()
                        .requestMatchers("/api/v1/analytics/**").hasAnyRole("SUPER_ADMIN", "MANAGER")

                        // Reports
                        .requestMatchers("/api/v1/reports/**").hasAnyRole("SUPER_ADMIN", "MANAGER")

                        // Knowledge base
                        .requestMatchers(HttpMethod.GET, "/api/v1/knowledge-base/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/knowledge-base/**").hasAnyRole("SUPER_ADMIN", "MANAGER", "AGENT")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/knowledge-base/**").hasAnyRole("SUPER_ADMIN", "MANAGER", "AGENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/knowledge-base/**").hasAnyRole("SUPER_ADMIN", "MANAGER")

                        // Diğer tüm API endpoints - EN SONDA OLMALI
                        .requestMatchers("/api/**").authenticated()

                        // Geri kalan her şey
                        .anyRequest().permitAll()
                )
                // Authentication provider'ı ekle
                .authenticationProvider(authenticationProvider())

                // JWT filter'ı ekle
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}