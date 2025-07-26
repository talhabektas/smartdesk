package com.example.smartdeskbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SmartdeskBackendApplicationTests {

    @Test
    void contextLoads() {
        // Context loading test
    }

    @Test
    void applicationStartsSuccessfully() {
        // Application startup test
    }
}