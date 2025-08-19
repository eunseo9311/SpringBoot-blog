package com.blog.application.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RefreshTokenService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public void saveRefreshToken(String token, String email, long ttlSeconds) {
        redisTemplate.opsForValue().set(token, email, ttlSeconds, TimeUnit.SECONDS);
    }
    
    public String getRefreshTokenEmail(String token) {
        return (String) redisTemplate.opsForValue().get(token);
    }
    
    public void deleteRefreshToken(String token) {
        redisTemplate.delete(token);
    }
}

//RedisTemplate을 직접 사용하는 RefreshToken 관리 서비스
//JSON 직렬화를 통해 토큰과 이메일 정보를 Redis에 저장/조회