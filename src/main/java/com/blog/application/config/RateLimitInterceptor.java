package com.blog.application.config;

import com.blog.application.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final RateLimitService rateLimitService;
    
    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 인증 관련 엔드포인트에만 레이트 리미팅 적용
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/auth/")) {
            String clientIp = getClientIpAddress(request);
            
            // IP당 1분에 5회 제한
            if (!rateLimitService.isAllowed(clientIp, 5, 60)) {
                response.setStatus(429); // 429 Too Many Requests
                response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
                return false;
            }
        }
        
        return true;
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}

//레이트 리미팅 인터셉터
//인증 엔드포인트에 대한 IP별 요청 횟수 제한 (1분에 5회)