package com.example.smartdeskbackend.exception;

/**
 * Authentication ile ilgili hataları temsil eder
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
