package com.yourssu.application.exception;

public class AuthException extends RuntimeException {
    
    public AuthException(String message) {
        super(message);
    }
    
    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}

//인증 관련 커스텀 예외 클래스