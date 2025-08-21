package com.blog.application.security;

import com.blog.application.common.jwt.JwtTokenProvider;
import com.blog.application.entity.User;
import com.blog.application.repository.jpa.UserRepository;
import com.blog.application.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = jwtTokenProvider.extractToken(authHeader);
            
            // 토큰 유효성 검사 및 블랙리스트 확인
            if (jwtTokenProvider.validateToken(token) && !tokenBlacklistService.isTokenBlacklisted(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                Optional<User> userOptional = userRepository.findByEmail(email);
                
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    UserDetails userDetails = createUserDetails(user);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.emptyList())
                .build();
    }
}

//JWT 토큰을 검증하고 인증 정보를 SecurityContext에 설정하는 필터
//Bearer 토큰 형태로 전달된 JWT를 파싱하여 사용자 인증 처리