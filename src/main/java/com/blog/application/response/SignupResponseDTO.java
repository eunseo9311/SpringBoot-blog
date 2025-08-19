package com.blog.application.response;

public class SignupResponseDTO {
    
    private Long userId;
    
    public SignupResponseDTO() {}
    
    public SignupResponseDTO(Long userId) {
        this.userId = userId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

//회원가입 성공 시 반환하는 응답 DTO 클래스
//생성된 사용자 ID 정보 포함