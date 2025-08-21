package com.blog.application.controller;

import com.blog.application.common.response.ApiResponse;
import com.blog.application.common.status.SuccessStatus;
import com.blog.application.request.LoginRequestDTO;
import com.blog.application.request.RefreshTokenRequestDTO;
import com.blog.application.request.SignupRequestDTO;
import com.blog.application.response.LoginResponseDTO;
import com.blog.application.response.SignupResponseDTO;
import com.blog.application.service.AuthService;
import com.blog.application.service.EventLogService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "인증 관련 API")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final EventLogService eventLogService;
    
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "201", description = "회원가입 성공"),
            @SwaggerApiResponse(responseCode = "400", description = "잘못된 요청 (이메일 중복, 유효성 검사 실패)")
    })
    public ResponseEntity<ApiResponse<SignupResponseDTO>> signup(@Valid @RequestBody SignupRequestDTO signupRequest) {
        SignupResponseDTO response = authService.signup(signupRequest);
        eventLogService.logSignupEvent(signupRequest.getEmail(), signupRequest.getNickname(), response.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessStatus.SIGNUP_SUCCESS, response));
    }
    
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다.")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "로그인 성공"),
            @SwaggerApiResponse(responseCode = "400", description = "잘못된 로그인 정보")
    })
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = authService.login(loginRequest);
        eventLogService.logLoginEvent(loginRequest.getEmail(), true, null);
        return ResponseEntity.ok(ApiResponse.success(SuccessStatus.LOGIN_SUCCESS, response));
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 이용하여 새로운 액세스 토큰을 발급합니다.")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @SwaggerApiResponse(responseCode = "400", description = "유효하지 않은 리프레시 토큰")
    })
    public ResponseEntity<ApiResponse<LoginResponseDTO>> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshRequest) {
        LoginResponseDTO response = authService.refreshToken(refreshRequest);
        return ResponseEntity.ok(ApiResponse.success(SuccessStatus.TOKEN_REFRESH_SUCCESS, response));
    }
    
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "액세스 토큰을 무효화하고 리프레시 토큰을 삭제합니다.")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @SwaggerApiResponse(responseCode = "400", description = "유효하지 않은 토큰")
    })
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserDetails userDetails,
                                                    HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String email = userDetails.getUsername(); // JWT 필터에서 email을 username으로 설정
        authService.logout(authHeader, email);
        eventLogService.logLogoutEvent(email);
        return ResponseEntity.ok(ApiResponse.success(SuccessStatus.LOGOUT_SUCCESS));
    }
}

//인증 관련 REST API 컨트롤러
//회원가입(POST /auth/signup), 로그인(POST /auth/login)
//토큰 갱신(POST /auth/refresh), 로그아웃(POST /auth/logout) 제공
//Swagger 문서화 및 유효성 검증 포함