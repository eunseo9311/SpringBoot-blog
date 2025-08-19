package com.yourssu.application.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    
    private final SecretKey secretKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;
    
    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.access-token-validity-ms}") long accessTokenValidityMs,
                   @Value("${jwt.refresh-token-validity-ms}") long refreshTokenValidityMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }
    
    public String generateAccessToken(String email) {
        return generateToken(email, accessTokenValidityMs);
    }
    
    public String generateRefreshToken(String email) {
        return generateToken(email, refreshTokenValidityMs);
    }
    
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
    
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }
    
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
    
    public long getAccessTokenValidityMs() {
        return accessTokenValidityMs;
    }
}

//JWT 토큰 생성, 검증, 파싱을 담당하는 유틸리티 클래스
//AccessToken(1시간), RefreshToken(2주) 유효기간 설정
//이메일 기반 토큰 생성 및 검증 기능 제공