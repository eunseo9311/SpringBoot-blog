package com.blog.application.exception;

import com.blog.application.common.response.ApiResponse;
import com.blog.application.common.status.ErrorStatus;
import com.blog.application.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception e, HttpServletRequest request) {
        
        return ResponseEntity
            .status(ErrorStatus.INTERNAL_SERVER_ERROR.getHttpStatus())
            .body(ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException e, HttpServletRequest request) {
        
        return ResponseEntity
            .status(ErrorStatus.BAD_REQUEST.getHttpStatus())
            .body(ApiResponse.error(ErrorStatus.BAD_REQUEST, e.getMessage()));
    }
    
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthException(
            AuthException e, HttpServletRequest request) {
        
        return ResponseEntity
            .status(ErrorStatus.UNAUTHORIZED.getHttpStatus())
            .body(ApiResponse.error(ErrorStatus.UNAUTHORIZED, e.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ErrorResponse.FieldError>>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ErrorResponse.FieldError(
                error.getField(),
                error.getRejectedValue(),
                error.getDefaultMessage()
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity
            .status(ErrorStatus.VALIDATION_ERROR.getHttpStatus())
            .body(ApiResponse.error(ErrorStatus.VALIDATION_ERROR, fieldErrors));
    }
}