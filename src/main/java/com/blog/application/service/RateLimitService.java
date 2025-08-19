package com.blog.application.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {
    
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RateLimitService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public boolean isAllowed(String identifier, int maxAttempts, int windowSeconds) {
        String key = RATE_LIMIT_PREFIX + identifier;
        String currentCount = (String) redisTemplate.opsForValue().get(key);
        
        if (currentCount == null) {
            // 첫 번째 시도
            redisTemplate.opsForValue().set(key, "1", windowSeconds, TimeUnit.SECONDS);
            return true;
        }
        
        int count = Integer.parseInt(currentCount);
        if (count >= maxAttempts) {
            return false; // 제한 초과
        }
        
        // 카운터 증가
        redisTemplate.opsForValue().increment(key);
        return true;
    }
    
    public void resetLimit(String identifier) {
        String key = RATE_LIMIT_PREFIX + identifier;
        redisTemplate.delete(key);
    }
}

//Redis 기반 레이트 리미팅 서비스
//IP 또는 사용자별 요청 횟수 제한 기능 제공