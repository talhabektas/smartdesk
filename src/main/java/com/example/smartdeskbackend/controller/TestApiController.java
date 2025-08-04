package com.example.smartdeskbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Test API Controller - API connectivity ve mapping test için
 */
@RestController
@RequestMapping("/v1/test")
@CrossOrigin(origins = "*")
public class TestApiController {

    @GetMapping("/hello")
    public ResponseEntity<?> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from SmartDesk Backend!");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("pong", true);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/echo")
    public ResponseEntity<?> echo(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        response.put("received", payload);
        response.put("timestamp", LocalDateTime.now());
        response.put("echo", "Your message has been received");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "SmartDesk Backend");
        response.put("version", "1.0.0");
        response.put("environment", "development");
        response.put("uptime", System.currentTimeMillis());
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}