package com.example.smartdeskbackend.controller;

import com.example.smartdeskbackend.entity.User;
import com.example.smartdeskbackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test controller - debugging için
 */
@RestController
@CrossOrigin(origins = "*")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @GetMapping("/v1/test/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from SmartDesk Backend!");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "OK");
        return response;
    }

    @GetMapping("/v1/test/status")
    public Map<String, Object> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "smartdesk-backend");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping("/v1/test/users")
    public Map<String, Object> getAllUsers() {
        List<User> users = userRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", users.size());
        response.put("users", users.stream().map(u -> Map.of(
            "id", u.getId(),
            "email", u.getEmail(),
            "firstName", u.getFirstName(),
            "lastName", u.getLastName(),
            "role", u.getRole().name(),
            "status", u.getStatus().name()
        )).toList());
        return response;
    }

    @GetMapping("/v1/test/check-user/{email}")
    public Map<String, Object> checkUser(@PathVariable String email) {
        Map<String, Object> response = new HashMap<>();
        
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            response.put("found", true);
            response.put("user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "role", user.getRole().name(),
                "status", user.getStatus().name(),
                "emailVerified", user.getEmailVerified(),
                "canLogin", user.canLogin(),
                "isAccountLocked", user.isAccountLocked()
            ));
        } else {
            response.put("found", false);
            response.put("message", "User not found");
        }
        
        return response;
    }

    @PostMapping("/v1/test/verify-password")
    public Map<String, Object> verifyPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        
        Map<String, Object> response = new HashMap<>();
        
        if (email == null || password == null) {
            response.put("error", "Email and password required");
            return response;
        }
        
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            boolean passwordMatches = passwordEncoder.matches(password, user.getPasswordHash());
            response.put("email", email);
            response.put("passwordMatches", passwordMatches);
            response.put("storedHashLength", user.getPasswordHash().length());
            response.put("inputPasswordLength", password.length());
        } else {
            response.put("error", "User not found");
        }
        
        return response;
    }

    @PostMapping("/v1/test/generate-hash")
    public Map<String, Object> generatePasswordHash(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        
        Map<String, Object> response = new HashMap<>();
        
        if (password == null) {
            response.put("error", "Password required");
            return response;
        }
        
        String hash = passwordEncoder.encode(password);
        response.put("password", password);
        response.put("hash", hash);
        response.put("hashLength", hash.length());
        
        return response;
    }

    @PostMapping("/v1/test/update-password")
    @Transactional
    public Map<String, Object> updateUserPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("password");
        
        Map<String, Object> response = new HashMap<>();
        
        if (email == null || newPassword == null) {
            response.put("error", "Email and password required");
            return response;
        }
        
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            String newHash = passwordEncoder.encode(newPassword);
            user.setPasswordHash(newHash);
            userRepository.save(user);
            
            response.put("success", true);
            response.put("email", email);
            response.put("message", "Password updated successfully");
        } else {
            response.put("error", "User not found");
        }
        
        return response;
    }

    @PostMapping("/v1/test/verify-email")
    @Transactional
    public Map<String, Object> verifyUserEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        Map<String, Object> response = new HashMap<>();
        
        if (email == null) {
            response.put("error", "Email required");
            return response;
        }
        
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.verifyEmail(); // This sets emailVerified = true and clears verification token
            userRepository.save(user);
            
            response.put("success", true);
            response.put("email", email);
            response.put("emailVerified", user.getEmailVerified());
            response.put("canLogin", user.canLogin());
            response.put("message", "Email verified successfully");
        } else {
            response.put("error", "User not found");
        }
        
        return response;
    }

    @PostMapping("/v1/test/activate-user")
    @Transactional
    public Map<String, Object> activateUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        Map<String, Object> response = new HashMap<>();
        
        if (email == null) {
            response.put("error", "Email required");
            return response;
        }
        
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setStatus(com.example.smartdeskbackend.enums.UserStatus.ACTIVE);
            userRepository.save(user);
            
            response.put("success", true);
            response.put("email", email);
            response.put("status", user.getStatus().name());
            response.put("canLogin", user.canLogin());
            response.put("message", "User activated successfully");
        } else {
            response.put("error", "User not found");
        }
        
        return response;
    }

    @PostMapping("/v1/test/update-all-demo-passwords")
    @Transactional
    public Map<String, Object> updateAllDemoPasswords() {
        String newPassword = "demo123";
        String newHash = passwordEncoder.encode(newPassword);
        
        logger.info("Updating demo passwords with BCrypt hash: {}", newHash);
        
        Map<String, Object> response = new HashMap<>();
        
        // Demo user emails
        String[] demoEmails = {
            "admin@erdemir.com.tr",
            "mehmet.yilmaz@erdemir.com.tr", 
            "ayse.demir@erdemir.com.tr",
            "john.smith@techcorp.com",
            "ali.kaya@erdemir.com.tr",
            "fatma.ozturk@erdemir.com.tr",
            "can.arslan@erdemir.com.tr",
            "sarah.johnson@techcorp.com",
            "mike.davis@techcorp.com",
            "mtb@gmail.com"  // Added for testing
        };
        
        int updatedCount = 0;
        for (String email : demoEmails) {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                user.setPasswordHash(newHash);
                user.setEmailVerified(true); // Also verify emails
                userRepository.save(user);
                updatedCount++;
                logger.info("✅ Updated password for user: {} ({})", user.getEmail(), user.getRole());
            } else {
                logger.warn("⚠️ User not found: {}", email);
            }
        }
        
        response.put("success", true);
        response.put("newPassword", newPassword);
        response.put("updatedUsers", updatedCount);
        response.put("bcryptHash", newHash);
        response.put("message", "All demo passwords updated to: " + newPassword);
        
        return response;
    }
}