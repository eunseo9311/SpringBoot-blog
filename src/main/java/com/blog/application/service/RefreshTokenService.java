package com.blog.application.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RefreshTokenService {
    
    // 임시로 메모리 기반 저장소 사용 (프로덕션에서는 Redis 사용 권장)
    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();
    
    public void saveRefreshToken(String token, String email, long ttlSeconds) {
        refreshTokenStore.put(token, email);
    }
    
    public String getRefreshTokenEmail(String token) {
        return refreshTokenStore.get(token);
    }
    
    public void deleteRefreshToken(String token) {
        refreshTokenStore.remove(token);
    }
}

//RedisTemplate을 직접 사용하는 RefreshToken 관리 서비스
//JSON 직렬화를 통해 토큰과 이메일 정보를 Redis에 저장/조회