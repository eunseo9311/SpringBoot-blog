package com.blog.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.blog.application.request.SignupRequestDTO;
import com.blog.application.service.AuthService;
import com.blog.application.response.SignupResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthService authService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void signup_성공() throws Exception {
        // given
        SignupRequestDTO request = new SignupRequestDTO("test@example.com", "password123", "testuser");
        SignupResponseDTO response = new SignupResponseDTO(1L);
        
        when(authService.signup(any(SignupRequestDTO.class))).thenReturn(response);
        
        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1L));
    }
    
    @Test
    void signup_유효성_검사_실패() throws Exception {
        // given
        SignupRequestDTO invalidRequest = new SignupRequestDTO("invalid-email", "123", "");
        
        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}

//AuthController 통합 테스트
//회원가입 API 성공/실패 케이스 테스트
//MockMvc를 사용한 HTTP 요청/응답 테스트