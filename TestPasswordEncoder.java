package com.example.smartdeskbackend.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPasswordEncoder {
    public static void main(String[] args) {
        // Test different encoder configurations
        System.out.println("Testing password encoders...");
        
        String plainPassword = "password123";
        
        // Test with strength 12 (current configuration)
        BCryptPasswordEncoder encoder12 = new BCryptPasswordEncoder(12);
        String encoded12 = encoder12.encode(plainPassword);
        System.out.println("Encoded with strength 12: " + encoded12);
        System.out.println("Matches with strength 12: " + encoder12.matches(plainPassword, encoded12));
        
        // Test with default strength (10)
        BCryptPasswordEncoder encoderDefault = new BCryptPasswordEncoder();
        String encodedDefault = encoderDefault.encode(plainPassword);
        System.out.println("Encoded with default strength: " + encodedDefault);
        System.out.println("Matches with default strength: " + encoderDefault.matches(plainPassword, encodedDefault));
        
        // Test the hash from data.sql
        String dataHash = "$2a$12$Tb5hvqw5eudWgVWBCyjv7u8rZ37KS55/zqi10GCk0qwy2rtdVN5e.";
        System.out.println("Data.sql hash: " + dataHash);
        System.out.println("Matches data.sql hash with strength 12: " + encoder12.matches(plainPassword, dataHash));
        System.out.println("Matches data.sql hash with default: " + encoderDefault.matches(plainPassword, dataHash));
        
        // Test cross-compatibility
        System.out.println("Cross-test - encoded with 12, validated with default: " + encoderDefault.matches(plainPassword, encoded12));
        System.out.println("Cross-test - encoded with default, validated with 12: " + encoder12.matches(plainPassword, encodedDefault));
    }
}