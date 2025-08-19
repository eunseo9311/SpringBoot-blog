package com.blog.application.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";
    
    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public void blacklistToken(String token, long expirationTimeMs) {
        String key = BLACKLIST_PREFIX + token;
        long ttlSeconds = (expirationTimeMs - System.currentTimeMillis()) / 1000;
        
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", ttlSeconds, TimeUnit.SECONDS);
        }
    }
    
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}

//토큰 블랙리스트 관리 서비스
//로그아웃된 토큰을 Redis에 블랙리스트로 등록하고 검증하는 기능 제공