package com.yourssu.application.service;

import com.yourssu.application.entity.User;
import com.yourssu.application.exception.AuthException;
import com.yourssu.application.repository.RefreshTokenRepository;
import com.yourssu.application.repository.UserRepository;
import com.yourssu.application.request.LoginRequestDTO;
import com.yourssu.application.request.SignupRequestDTO;
import com.yourssu.application.response.LoginResponseDTO;
import com.yourssu.application.response.SignupResponseDTO;
import com.yourssu.application.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    
    @InjectMocks
    private AuthService authService;
    
    private SignupRequestDTO signupRequest;
    private LoginRequestDTO loginRequest;
    private User mockUser;
    
    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequestDTO("test@example.com", "password123", "testuser");
        loginRequest = new LoginRequestDTO("test@example.com", "password123");
        mockUser = new User("test@example.com", "testuser", "encodedPassword");
        mockUser.setId(1L);
    }
    
    @Test
    void signup_성공() {
        // given
        when(userRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        
        // when
        SignupResponseDTO response = authService.signup(signupRequest);
        
        // then
        assertThat(response.getUserId()).isEqualTo(1L);
        verify(userRepository).findByEmail(signupRequest.getEmail());
        verify(passwordEncoder).encode(signupRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void signup_이메일_중복_실패() {
        // given
        when(userRepository.findByEmail(signupRequest.getEmail())).thenReturn(Optional.of(mockUser));
        
        // when & then
        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }
    
    @Test
    void login_성공() {
        // given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), mockUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateAccessToken(mockUser.getEmail())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(mockUser.getEmail())).thenReturn("refreshToken");
        when(jwtUtil.getAccessTokenValidityMs()).thenReturn(3600000L);
        
        // when
        LoginResponseDTO response = authService.login(loginRequest);
        
        // then
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(response.getExpiresIn()).isEqualTo(3600L);
    }
    
    @Test
    void login_사용자_없음_실패() {
        // given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthException.class)
                .hasMessage("존재하지 않는 사용자입니다.");
    }
    
    @Test
    void login_비밀번호_불일치_실패() {
        // given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), mockUser.getPassword())).thenReturn(false);
        
        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }
}

//AuthService 단위 테스트
//회원가입, 로그인 성공/실패 케이스 테스트 포함
//Mockito를 사용한 의존성 모킹