package com.blog.application.common.response;

import com.blog.application.common.status.ErrorStatus;
import com.blog.application.common.status.SuccessStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;
    
    // 성공 응답
    public static <T> ApiResponse<T> success(SuccessStatus status, T data) {
        return new ApiResponse<>(true, status.getCode(), status.getMessage(), data);
    }
    
    public static ApiResponse<Void> success(SuccessStatus status) {
        return new ApiResponse<>(true, status.getCode(), status.getMessage(), null);
    }
    
    // 실패 응답
    public static <T> ApiResponse<T> error(ErrorStatus status) {
        return new ApiResponse<>(false, status.getCode(), status.getMessage(), null);
    }
    
    public static <T> ApiResponse<T> error(ErrorStatus status, T data) {
        return new ApiResponse<>(false, status.getCode(), status.getMessage(), data);
    }
    
    // 커스텀 메시지 응답
    public static <T> ApiResponse<T> success(SuccessStatus status, String customMessage, T data) {
        return new ApiResponse<>(true, status.getCode(), customMessage, data);
    }
    
    public static <T> ApiResponse<T> error(ErrorStatus status, String customMessage) {
        return new ApiResponse<>(false, status.getCode(), customMessage, null);
    }
}