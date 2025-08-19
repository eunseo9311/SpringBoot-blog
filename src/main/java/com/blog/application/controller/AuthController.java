package com.blog.application.controller;

import com.blog.application.request.LoginRequestDTO;
import com.blog.application.request.RefreshTokenRequestDTO;
import com.blog.application.request.SignupRequestDTO;
import com.blog.application.response.LoginResponseDTO;
import com.blog.application.response.SignupResponseDTO;
import com.blog.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이메일 중복, 유효성 검사 실패)")
    })
    public ResponseEntity<SignupResponseDTO> signup(@Valid @RequestBody SignupRequestDTO signupRequest) {
        try {
            SignupResponseDTO response = authService.signup(signupRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | com.blog.application.exception.AuthException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 로그인 정보")
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponseDTO response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | com.blog.application.exception.AuthException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 이용하여 새로운 액세스 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 리프레시 토큰")
    })
    public ResponseEntity<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshRequest) {
        try {
            LoginResponseDTO response = authService.refreshToken(refreshRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | com.blog.application.exception.AuthException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "액세스 토큰을 무효화하고 리프레시 토큰을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰")
    })
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            authService.logout(authHeader);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | com.blog.application.exception.AuthException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

//인증 관련 REST API 컨트롤러
//회원가입(POST /auth/signup), 로그인(POST /auth/login)
//토큰 갱신(POST /auth/refresh), 로그아웃(POST /auth/logout) 제공
//Swagger 문서화 및 유효성 검증 포함