package com.blog.application.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {
    
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수 항목입니다.")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수 항목입니다.")
    private String password;
    
    public LoginRequestDTO() {}
    
    public LoginRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}

//로그인 요청을 위한 DTO 클래스
//이메일과 비밀번호 필드로 구성