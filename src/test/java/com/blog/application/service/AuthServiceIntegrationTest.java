package com.blog.application.service;

import com.blog.application.config.EmbeddedRedisConfig;
import com.blog.application.config.TestContainerConfig;
import com.blog.application.entity.User;
import com.blog.application.repository.jpa.UserRepository;
import com.blog.application.request.LoginRequestDTO;
import com.blog.application.request.SignupRequestDTO;
import com.blog.application.response.LoginResponseDTO;
import com.blog.application.response.SignupResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("testcontainers")
@Import({EmbeddedRedisConfig.class})
@DisplayName("AuthService 통합 테스트")
class AuthServiceIntegrationTest extends TestContainerConfig {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    @Transactional
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("전체 회원가입 플로우 - 실제 DB 저장 및 조회 확인")
    @Transactional
    void signup_FullFlow_SavesUserToDatabase() {
        // given
        SignupRequestDTO signupRequest = new SignupRequestDTO(
            "integration@test.com", 
            "password123", 
            "integrationUser"
        );

        // when
        SignupResponseDTO response = authService.signup(signupRequest);

        // then
        assertThat(response.getUserId()).isNotNull();
        
        Optional<User> savedUser = userRepository.findByEmail("integration@test.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getUsername()).isEqualTo("integrationUser");
        assertThat(savedUser.get().getEmail()).isEqualTo("integration@test.com");
    }

    @Test
    @DisplayName("회원가입 후 로그인 플로우 - JWT 토큰 발급 및 Redis 저장 확인")
    @Transactional
    void signupAndLogin_FullFlow_GeneratesTokensAndStoresInRedis() {
        // given
        SignupRequestDTO signupRequest = new SignupRequestDTO(
            "logintest@test.com", 
            "password123", 
            "loginUser"
        );
        authService.signup(signupRequest);

        LoginRequestDTO loginRequest = new LoginRequestDTO(
            "logintest@test.com", 
            "password123"
        );

        // when
        LoginResponseDTO loginResponse = authService.login(loginRequest);

        // then
        assertThat(loginResponse.getAccessToken()).isNotEmpty();
        assertThat(loginResponse.getRefreshToken()).isNotEmpty();
        assertThat(loginResponse.getExpiresIn()).isEqualTo(3600L);
        
        // Redis에 리프레시 토큰이 저장되었는지 확인
        String storedToken = refreshTokenService.getRefreshToken("logintest@test.com");
        assertThat(storedToken).isNotNull();
        assertThat(storedToken).isEqualTo(loginResponse.getRefreshToken());
    }

    @Test
    @DisplayName("토큰 갱신 플로우 - 기존 토큰 삭제 및 새 토큰 발급")
    @Transactional
    void refreshToken_FullFlow_RotatesTokensInRedis() {
        // given - 사용자 생성 및 로그인
        SignupRequestDTO signupRequest = new SignupRequestDTO(
            "refresh@test.com", 
            "password123", 
            "refreshUser"
        );
        authService.signup(signupRequest);

        LoginRequestDTO loginRequest = new LoginRequestDTO(
            "refresh@test.com", 
            "password123"
        );
        LoginResponseDTO loginResponse = authService.login(loginRequest);

        // when - 토큰 갱신
        com.blog.application.request.RefreshTokenRequestDTO refreshRequest = 
            new com.blog.application.request.RefreshTokenRequestDTO(loginResponse.getRefreshToken());
        LoginResponseDTO newTokenResponse = authService.refreshToken(refreshRequest);

        // then
        assertThat(newTokenResponse.getAccessToken()).isNotEmpty();
        assertThat(newTokenResponse.getRefreshToken()).isNotEmpty();
        assertThat(newTokenResponse.getAccessToken()).isNotEqualTo(loginResponse.getAccessToken());
        assertThat(newTokenResponse.getRefreshToken()).isNotEqualTo(loginResponse.getRefreshToken());

        // Redis에 새로운 리프레시 토큰이 저장되었는지 확인
        String currentToken = refreshTokenService.getRefreshToken("refresh@test.com");
        assertThat(currentToken).isEqualTo(newTokenResponse.getRefreshToken());
    }
}