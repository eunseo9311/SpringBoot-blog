package com.blog.application.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private boolean success = false;
    private String message;
    private String code;
    private LocalDateTime timestamp;
    private String path;
    private List<FieldError> fieldErrors;
    
    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(String message, String code) {
        this.message = message;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(String message, String code, String path) {
        this.message = message;
        this.code = code;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(String message, List<FieldError> fieldErrors) {
        this.message = message;
        this.fieldErrors = fieldErrors;
        this.timestamp = LocalDateTime.now();
    }
    
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String message;
        
        public FieldError(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }
        
        public String getField() {
            return field;
        }
        
        public Object getRejectedValue() {
            return rejectedValue;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getCode() {
        return code;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getPath() {
        return path;
    }
    
    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
}