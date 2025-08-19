package com.blog.application.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.blog.application.repository.UserRepository;

import java.util.List;
import java.util.Map;

import com.blog.application.response.UserDTO;
import com.blog.application.response.UserIdDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    // 사용자 데이터 초기화
    public void setup() {
        userRepository.deleteAll();
    }

    @Test
    public void testCreateUser() throws Exception {
        // POST /users 요청으로 새로운 사용자 생성
        String userJson = "{ \"email\": \"newuser@example.com\", " +
                "\"password\": \"password\", " +
                "\"username\": \"newuser\" }";
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    public void testGetAllUsers() throws Exception {
        // 사용자를 하나 생성한 후, GET /users 호출 시 해당 사용자가 포함되어 있는지 검증
        String userJson = "{ \"email\": \"user1@example.com\", " +
                "\"password\": \"password\", " +
                "\"username\": \"user1\" }";
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("user1@example.com"));
    }

    @Test
    public void testGetUserById_NotFound() throws Exception {
        // 존재하지 않는 사용자 ID에 대해 GET /users/{id} 호출 시 404 반환
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testWithdrawUser_Success() throws Exception {
        // 1. 사용자를 생성
        String userJson = "{ \"email\": \"withdraw@example.com\", " +
                "\"password\": \"password\", " +
                "\"username\": \"withdrawUser\" }";
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // // 응답에서 UserDTO 추출
        var dto=objectMapper.readValue(response, UserDTO.class);
        String response1 = mockMvc.perform(get("/users")
                        )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // 응답에서 UserIdDTO 리스트 추출
        List<UserIdDTO> li = objectMapper.readValue(response1, new TypeReference<>() {
        });

        UserIdDTO u = null;
        System.out.println(u);
        for (UserIdDTO userIdDTO : li) {
            if(dto.getEmail().equals(userIdDTO.getEmail())){
                u = userIdDTO;
            }
        }
        if(u == null)
            throw new IllegalStateException("게시글 ID가 응답에 포함되지 않았습니다. DTO를 확인하세요.");


        // 2. DELETE /users/{id} 요청으로 회원 탈퇴 (인증 정보 포함)
        String deleteJson = "{ \"email\": \"withdraw@example.com\", " +
                "\"password\": \"password\" }";
        mockMvc.perform(delete("/users/" + u.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deleteJson))
                .andExpect(status().isOk());
    }
}

//검증 내용 및 의도:
//
//사용자 생성/조회/탈퇴 기능 테스트:
//회원가입 API가 올바르게 작동하는지,
//전체 사용자 목록 조회 및 특정 사용자 조회가 정상적으로 이루어지는지,
//탈퇴 요청 시 인증 후 사용자(및 연관 데이터)가 삭제되는지를 검증한다
//메인 코드와의 연관성:
//이 테스트들은 UserController 및 UserService의 주요 기능(회원가입, 조회, 탈퇴)을 검증하며, 실제 서비스 로직과 데이터베이스 연동을 확인한다.
