package com.example.smartdeskbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Ana API controller'ı
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "🚀 SmartDesk CRM API is running successfully!");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());

        // Available endpoints
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("health", "GET /api/health");
        endpoints.put("auth-health", "GET /api/v1/auth/health");
        endpoints.put("login", "POST /api/v1/auth/login");
        endpoints.put("register", "POST /api/v1/auth/register");
        endpoints.put("actuator", "GET /api/actuator/health");

        response.put("endpoints", endpoints);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "smartdesk-backend");
        response.put("timestamp", LocalDateTime.now());
        response.put("database", "Connected to MySQL");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "SmartDesk CRM Backend");
        response.put("description", "Kurumsal müşteri destek ve çağrı merkezi yönetim sistemi");
        response.put("version", "1.0.0");
        response.put("port", 8067);
        response.put("context-path", "/api");
        response.put("profile", "development");
        return ResponseEntity.ok(response);
    }
}