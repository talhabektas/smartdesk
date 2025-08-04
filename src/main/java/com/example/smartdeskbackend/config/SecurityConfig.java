package com.example.smartdeskbackend.config;

import com.example.smartdeskbackend.security.JwtAuthenticationEntryPoint;
import com.example.smartdeskbackend.security.JwtAuthenticationFilter;
import com.example.smartdeskbackend.util.JwtUtil; // KESİNLİKLE BU IMPORT OLDUĞUNDAN EMİN OLUN
import com.example.smartdeskbackend.service.impl.UserDetailsServiceImpl; // KESİNLİKLE BU IMPORT OLDUĞUNDAN EMİN OLUN

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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // BU IMPORTU KESİNLİKLE EKLEYİN!
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

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
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl(); // Kendi UserDetailsService implementasyonunuzu döndürün
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Default strength (10)
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // Allow all origins for development
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-Total-Count",
                "X-Page-Number",
                "X-Page-Size",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    /**
     * Security filter chain yapılandırması
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc, JwtUtil jwtUtil)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(authz -> authz
                        // Allow all OPTIONS requests (preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/health", "/info").permitAll()

                        // Auth endpoints - PUBLIC ACCESS
                        .requestMatchers("/v1/auth/**").permitAll()
                        .requestMatchers("/v1/public/**").permitAll()
                        .requestMatchers("/v1/test/**").permitAll()
                        .requestMatchers("/v1/companies/domain/**").permitAll()
                        .requestMatchers("/v1/files/download/**").permitAll()

                        // WebSocket endpoint'ine izin ver (public olmalı)
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/ws").permitAll()

                        // Swagger UI - PUBLIC ACCESS
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // API endpoints
                        .requestMatchers("/v1/admin/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/v1/companies/**").hasAnyRole("SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/v1/users/**").authenticated()
                        .requestMatchers("/v1/customers/**").authenticated()
                        .requestMatchers("/v1/tickets/**").authenticated()
                        .requestMatchers("/v1/chat/**").authenticated()
                        .requestMatchers("/v1/dashboard/**").authenticated()
                        .requestMatchers("/v1/analytics/**").hasAnyRole("SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/v1/reports/**").hasAnyRole("SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/v1/knowledge-base/**").authenticated()
                        .requestMatchers("/v1/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(jwtAuthenticationFilter(jwtUtil, userDetailsService()),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}