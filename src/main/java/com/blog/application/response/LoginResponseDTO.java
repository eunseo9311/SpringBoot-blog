package com.blog.application.response;

public class LoginResponseDTO {
    
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    
    public LoginResponseDTO() {}
    
    public LoginResponseDTO(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}

//로그인 성공 시 반환하는 응답 DTO 클래스
//액세스 토큰, 리프레시 토큰, 만료 시간 정보 포함