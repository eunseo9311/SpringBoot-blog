package com.blog.application.common.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    
    private final SecretKey secretKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;
    
    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.access-token-validity-ms}") long accessTokenValidityMs,
                           @Value("${jwt.refresh-token-validity-ms}") long refreshTokenValidityMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }
    
    /**
     * 액세스 토큰 생성
     */
    public String generateAccessToken(String email) {
        return generateToken(email, accessTokenValidityMs);
    }
    
    /**
     * 리프레시 토큰 생성
     */
    public String generateRefreshToken(String email) {
        return generateToken(email, refreshTokenValidityMs);
    }
    
    /**
     * 토큰 생성 (공통 로직)
     */
    private String generateToken(String email, long validityMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validityMs);
        
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * 토큰에서 이메일 추출
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }
    
    /**
     * Authorization 헤더에서 이메일 추출
     */
    public String getEmailFromAuthHeader(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        
        String token = authHeader;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            return getEmailFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Bearer 토큰에서 실제 토큰 추출
     */
    public String extractToken(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        return authHeader;
    }
    
    /**
     * 토큰 만료 시간 계산 (밀리초)
     */
    public long calculateExpirationTime() {
        return System.currentTimeMillis() + accessTokenValidityMs;
    }
    
    /**
     * 액세스 토큰 유효 기간 조회 (초 단위)
     */
    public long getAccessTokenValidityInSeconds() {
        return accessTokenValidityMs / 1000;
    }
    
    /**
     * 액세스 토큰 유효 기간 조회 (밀리초 단위)
     */
    public long getAccessTokenValidityMs() {
        return accessTokenValidityMs;
    }
}