package com.blog.application.service;

import com.blog.application.entity.User;
import com.blog.application.exception.AuthException;
import com.blog.application.repository.jpa.UserRepository;
import com.blog.application.request.LoginRequestDTO;
import com.blog.application.request.SignupRequestDTO;
import com.blog.application.response.LoginResponseDTO;
import com.blog.application.response.SignupResponseDTO;
import com.blog.application.security.JwtUtil;
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
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RefreshTokenService refreshTokenService;
    
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
        given(userRepository.findByEmail(signupRequest.getEmail())).willReturn(Optional.empty());
        given(passwordEncoder.encode(signupRequest.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(mockUser);
        
        // when
        SignupResponseDTO response = authService.signup(signupRequest);
        
        // then
        assertThat(response.getUserId()).isEqualTo(1L);
        then(userRepository).should().findByEmail(signupRequest.getEmail());
        then(passwordEncoder).should().encode(signupRequest.getPassword());
        then(userRepository).should().save(any(User.class));
    }
    
    @Test
    void signup_이메일_중복_실패() {
        // given
        given(userRepository.findByEmail(signupRequest.getEmail())).willReturn(Optional.of(mockUser));
        
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