package com.example.smartdeskbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching; // Bu importu ekleyin

@SpringBootApplication
@EnableCaching // Bu anotasyonu ekleyin
public class SmartdeskBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartdeskBackendApplication.class, args);
    }
}