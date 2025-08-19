package com.yourssu.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourssu.application.entity.Article;
import com.yourssu.application.entity.User;
import com.yourssu.application.repository.ArticleRepository;
import com.yourssu.application.repository.UserRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ArticleControllerTest {

    // MockMvc를 사용해 HTTP 요청을 시뮬레이션한다
    @Autowired
    private MockMvc mockMvc;

    // JSON 문자열과 객체 간 변환을 위해 사용한다
    @Autowired
    private ObjectMapper objectMapper;

    // 테스트를 위한 Repository 주입 (DB 초기화 및 엔티티 생성에 사용)
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    private User testUser;

    @BeforeEach
    public void setup() {
        // 테스트 시작 전 데이터베이스를 깨끗이 비운다
        articleRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트용 사용자 생성 (비밀번호는 암호화)
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        testUser = new User();
        testUser.setEmail("test@example.com");
        // 평문 "password"를 암호화해서 저장
        testUser.setPassword(encoder.encode("password"));
        testUser.setNickname("testuser");
        userRepository.save(testUser);
    }


    @Test
    public void testGetAllArticles_Empty() throws Exception {
        // 게시글이 없는 경우, GET /articles 호출 시 빈 배열을 반환해야 함을 검증
        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    // POST /articles 요청으로 게시글 생성 (정상적인 입력: 이메일, 평문 "password", 제목, 내용)
    public void testCreateArticle_Success() throws Exception {
        // 평문 "password"를 명시적으로 사용
        String articleJson = "{ \"email\": \"" + testUser.getEmail() + "\", " +
                "\"password\": \"password\", " +
                "\"title\": \"Test Article\", " +
                "\"content\": \"This is a test article.\" }";

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(articleJson))
                .andExpect(status().isOk())
                // 응답 JSON에 "title" 필드가 "Test Article"인지 확인합니다.
                .andExpect(jsonPath("$.title").value("Test Article"));
    }

    @Test
    public void testCreateArticle_BadRequest() throws Exception {
        // 제목과 내용이 빈 문자열이면 잘못된 요청(Bad Request)임을 검증합니다.
        String articleJson = "{ \"email\": \"" + testUser.getEmail() + "\", " +
                "\"password\": \"" + testUser.getPassword() + "\", " +
                "\"title\": \"\", " +
                "\"content\": \"\" }";

        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(articleJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetArticleById_NotFound() throws Exception {
        // 존재하지 않는 게시글 ID(예: 999)를 조회하면 404 NOT_FOUND를 반환해야 함을 검증
        mockMvc.perform(get("/articles/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateArticle_Success() throws Exception {
        // 1. 게시글 생성 요청 (평문 "password" 전송)
        String createJson = "{ \"email\": \"" + testUser.getEmail() + "\", " +
                "\"password\": \"password\", " +
                "\"title\": \"Original Title\", " +
                "\"content\": \"Original Content.\" }";
        String createResponse = mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 2. 생성 응답에서 게시글 식별자(articleId)를 추출합니다. (키 "id" 사용)
        Map<?,?> articleMap = objectMapper.readValue(createResponse, Map.class);
        Number articleId = (Number) articleMap.get("articleId");
        if(articleId == null) {
            throw new IllegalStateException("게시글 ID가 응답에 포함되지 않았습니다. DTO를 확인하세요.");
        }

        // 3. PUT 요청으로 게시글 수정 (제목과 내용을 변경, 평문 "password" 사용)
        String updateJson = "{ \"email\": \"" + testUser.getEmail() + "\", " +
                "\"password\": \"password\", " +
                "\"title\": \"Updated Title\", " +
                "\"content\": \"Updated Content.\" }";
        mockMvc.perform(put("/articles/"+articleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }


    @Test
    public void testDeleteArticle_Success() throws Exception {
        // 1. 게시글 생성 요청 (평문 "password" 전송)
        String createJson = "{ \"email\": \"" + testUser.getEmail() + "\", " +
                "\"password\": \"password\", " +
                "\"title\": \"Article to Delete\", " +
                "\"content\": \"Content.\" }";
        String createResponse = mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 2. 생성 응답에서 게시글 식별자(articleId)를 추출 (키 "id" 사용)
        Map<?,?> articleMap = objectMapper.readValue(createResponse, Map.class);
        Number articleId = (Number) articleMap.get("articleId");
        if(articleId == null) {
            throw new IllegalStateException("게시글 ID가 응답에 포함되지 않았습니다. DTO를 확인하세요.");
        }

        // 3. DELETE 요청으로 게시글 삭제 (인증 정보 포함)
        String deleteJson = "{ \"email\": \"" + testUser.getEmail() + "\", " +
                "\"password\": \"password\" }";
        mockMvc.perform(delete("/articles/" + articleId.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deleteJson))
                .andExpect(status().isOk());
    }

}

//검증 내용 및 의도:
//
//통합 테스트: ArticleController가 REST API 요청에 대해 올바른 응답(200, 201, 400, 404, 204 등)을 반환하는지 검증합니다.
//실제 동작 확인: 게시글 생성, 조회, 수정, 삭제의 전체 흐름이 메인 코드와 일치하는지 확인합니다.
