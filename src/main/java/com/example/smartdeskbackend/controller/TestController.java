package com.example.smartdeskbackend.controller;

import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Test controller - debugging için
 */
@RestController
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/api/v1/test/public")
    public Map<String, Object> publicTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Public endpoint working!");
        response.put("timestamp", LocalDateTime.now());
        response.put("access", "public");
        response.put("status", "SUCCESS");
        response.put("endpoint", "/api/v1/test/public");
        return response;
    }

    @GetMapping("/api/v1/test/security")
    public Map<String, Object> securityTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Security test endpoint working!");
        response.put("timestamp", LocalDateTime.now());
        response.put("access", "public");
        response.put("status", "SUCCESS");
        response.put("endpoint", "/api/v1/test/security");
        return response;
    }

    @GetMapping("/api/v1/test/info")
    public Map<String, Object> info(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Info endpoint working!");
        response.put("requestURI", request.getRequestURI());
        response.put("contextPath", request.getContextPath());
        response.put("servletPath", request.getServletPath());
        response.put("method", request.getMethod());
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "SUCCESS");

        // Headers
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(name ->
                headers.put(name, request.getHeader(name))
        );
        response.put("headers", headers);

        return response;
    }
}