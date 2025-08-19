package com.blog.application.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {
    
    // 임시로 메모리 기반 저장소 사용 (프로덕션에서는 Redis 사용 권장)
    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    
    public void blacklistToken(String token, long expirationTimeMs) {
        if (expirationTimeMs > System.currentTimeMillis()) {
            blacklistedTokens.put(token, expirationTimeMs);
        }
    }
    
    public boolean isTokenBlacklisted(String token) {
        Long expirationTime = blacklistedTokens.get(token);
        if (expirationTime == null) {
            return false;
        }
        
        // 만료된 토큰은 자동으로 제거
        if (expirationTime <= System.currentTimeMillis()) {
            blacklistedTokens.remove(token);
            return false;
        }
        
        return true;
    }
}

//토큰 블랙리스트 관리 서비스
//로그아웃된 토큰을 Redis에 블랙리스트로 등록하고 검증하는 기능 제공