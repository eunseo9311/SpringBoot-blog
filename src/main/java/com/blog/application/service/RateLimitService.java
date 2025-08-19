package com.blog.application.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    
    // 임시로 메모리 기반 저장소 사용 (프로덕션에서는 Redis 사용 권장)
    private final Map<String, RateLimitEntry> rateLimitMap = new ConcurrentHashMap<>();
    
    public boolean isAllowed(String identifier, int maxAttempts, int windowSeconds) {
        long currentTime = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;
        
        RateLimitEntry entry = rateLimitMap.get(identifier);
        
        if (entry == null || (currentTime - entry.getStartTime()) > windowMs) {
            // 첫 번째 시도 또는 윈도우 만료
            rateLimitMap.put(identifier, new RateLimitEntry(currentTime, 1));
            return true;
        }
        
        if (entry.getCount() >= maxAttempts) {
            return false; // 제한 초과
        }
        
        // 카운터 증가
        entry.incrementCount();
        return true;
    }
    
    public void resetLimit(String identifier) {
        rateLimitMap.remove(identifier);
    }
    
    private static class RateLimitEntry {
        private final long startTime;
        private int count;
        
        public RateLimitEntry(long startTime, int count) {
            this.startTime = startTime;
            this.count = count;
        }
        
        public long getStartTime() {
            return startTime;
        }
        
        public int getCount() {
            return count;
        }
        
        public void incrementCount() {
            this.count++;
        }
    }
}

//Redis 기반 레이트 리미팅 서비스
//IP 또는 사용자별 요청 횟수 제한 기능 제공