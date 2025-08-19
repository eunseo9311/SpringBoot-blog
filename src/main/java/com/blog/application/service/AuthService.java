package com.blog.application.service;

import com.blog.application.entity.User;
import com.blog.application.exception.AuthException;
import com.blog.application.repository.jpa.UserRepository;
import com.blog.application.request.LoginRequestDTO;
import com.blog.application.request.RefreshTokenRequestDTO;
import com.blog.application.request.SignupRequestDTO;
import com.blog.application.response.LoginResponseDTO;
import com.blog.application.response.SignupResponseDTO;
import com.blog.application.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    
    public AuthService(UserRepository userRepository,
                      RefreshTokenService refreshTokenService,
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil,
                      TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }
    
    public SignupResponseDTO signup(SignupRequestDTO signupRequest) {
        // 이메일 중복 검사
        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        
        // 비밀번호 해시화
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
        
        // 사용자 생성 및 저장
        User user = new User(signupRequest.getEmail(), signupRequest.getNickname(), encodedPassword);
        User savedUser = userRepository.save(user);
        
        return new SignupResponseDTO(savedUser.getId());
    }
    
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        // 사용자 조회
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AuthException("존재하지 않는 사용자입니다."));
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AuthException("비밀번호가 일치하지 않습니다.");
        }
        
        // 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        
        // RefreshToken을 메모리에 저장 (TTL은 JWT 자체 만료로 처리)
        refreshTokenService.saveRefreshToken(refreshToken, user.getEmail(), 1209600L);
        
        return new LoginResponseDTO(accessToken, refreshToken, jwtUtil.getAccessTokenValidityMs() / 1000);
    }
    
    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest) {
        // 리프레시 토큰 검증
        if (!jwtUtil.validateToken(refreshRequest.getRefreshToken())) {
            throw new AuthException("유효하지 않은 리프레시 토큰입니다.");
        }
        
        // 저장된 리프레시 토큰 조회
        String email = refreshTokenService.getRefreshTokenEmail(refreshRequest.getRefreshToken());
        if (email == null) {
            throw new AuthException("존재하지 않는 리프레시 토큰입니다.");
        }
        
        // 새로운 토큰 생성 (토큰 로테이션)
        String newAccessToken = jwtUtil.generateAccessToken(email);
        String newRefreshToken = jwtUtil.generateRefreshToken(email);
        
        // 기존 리프레시 토큰 삭제 후 새 토큰 저장
        refreshTokenService.deleteRefreshToken(refreshRequest.getRefreshToken());
        refreshTokenService.saveRefreshToken(newRefreshToken, email, 1209600L);
        
        return new LoginResponseDTO(newAccessToken, newRefreshToken, jwtUtil.getAccessTokenValidityMs() / 1000);
    }
    
    public void logout(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("액세스 토큰이 필요합니다.");
        }
        
        // Bearer 토큰에서 실제 토큰 추출
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        
        // 토큰 검증
        if (!jwtUtil.validateToken(accessToken)) {
            throw new AuthException("유효하지 않은 토큰입니다.");
        }
        
        String email = jwtUtil.getEmailFromToken(accessToken);
        
        // 리프레시 토큰 삭제는 복잡하므로 생략 (토큰 만료로 자연 삭제)
        // Redis에서 email 기반 검색/삭제는 복잡함 - 향후 개선 필요
        
        // 액세스 토큰 블랙리스트 등록
        // JWT의 만료 시간까지만 블랙리스트에 보관
        long accessTokenExpirationTime = System.currentTimeMillis() + jwtUtil.getAccessTokenValidityMs();
        tokenBlacklistService.blacklistToken(accessToken, accessTokenExpirationTime);
    }
}

//인증 서비스 클래스
//회원가입, 로그인, 토큰 갱신(로테이션), 로그아웃 기능 구현
//BCrypt 비밀번호 해시화, JWT 토큰 관리, Redis 기반 토큰 저장소 활용