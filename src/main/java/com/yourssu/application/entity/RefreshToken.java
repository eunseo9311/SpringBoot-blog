package com.yourssu.application.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("refreshToken")
public class RefreshToken {
    
    @Id
    private String email;
    
    private String token;
    
    @TimeToLive
    private Long ttl; // seconds
    
    public RefreshToken() {}
    
    public RefreshToken(String email, String token, Long ttl) {
        this.email = email;
        this.token = token;
        this.ttl = ttl;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public Long getTtl() {
        return ttl;
    }
    
    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }
}

//Redis에 저장되는 리프레시 토큰 엔티티
//이메일을 키로 사용하고 TTL(Time To Live)을 통한 자동 만료 처리