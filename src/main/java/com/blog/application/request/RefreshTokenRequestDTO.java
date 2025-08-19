package com.blog.application.request;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequestDTO {
    
    @NotBlank(message = "리프레시 토큰은 필수 항목입니다.")
    private String refreshToken;
    
    public RefreshTokenRequestDTO() {}
    
    public RefreshTokenRequestDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

//토큰 갱신 요청을 위한 DTO 클래스
//리프레시 토큰 필드로 구성