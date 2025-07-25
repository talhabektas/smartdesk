package com.example.smartdeskbackend.exception;

/**
 * İş mantığı hatalarını temsil eder
 */
public class BusinessLogicException extends RuntimeException {

    public BusinessLogicException(String message) {
        super(message);
    }

    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
    }
}