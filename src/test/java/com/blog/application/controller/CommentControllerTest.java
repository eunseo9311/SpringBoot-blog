package com.blog.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.blog.application.entity.Article;
import com.blog.application.entity.User;
import com.blog.application.repository.ArticleRepository;
import com.blog.application.repository.CommentRepository;
import com.blog.application.repository.UserRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User testUser;
    private Article testArticle;

    @BeforeEach
    public void setup() {
        // 모든 테스트 시작 전 관련 데이터 초기화
        commentRepository.deleteAll();
        articleRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트용 사용자 생성 (비밀번호 암호화 적용)
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        testUser = new User();
        testUser.setEmail("commentuser@example.com");
        testUser.setPassword(encoder.encode("password")); // 평문 "password"를 암호화해서 저장
        testUser.setNickname("commentUser");
        userRepository.save(testUser);

        // 테스트용 게시글 생성
        testArticle = new Article();
        testArticle.setTitle("Comment Article");
        testArticle.setContent("Article content");
        testArticle.setUser(testUser);
        articleRepository.save(testArticle);
    }

    @Test
    public void testGetComments_Empty() throws Exception {
        // 댓글이 없을 경우, GET /articles/{articleId}/comments/ 는 빈 배열 반환
        mockMvc.perform(get("/articles/" + testArticle.getId() + "/comments/"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void testCreateComment_Success() throws Exception {
        // POST /articles/{articleId}/comments 로 댓글 생성 (정상 입력)
        String commentJson = "{ \"email\": \"" + testUser.getEmail() + "\", " +
                "\"password\": \"password\", " +
                "\"content\": \"This is a comment.\" }";

        mockMvc.perform(post("/articles/" + testArticle.getId() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("This is a comment."));
    }


    @Test
    public void testUpdateComment_Success() throws Exception {
        // 1. 댓글 생성 (평문 "password" 사용)
        String createJson = "{ \"email\": \"" + testUser.getEmail() + "\", " +
                "\"password\": \"password\", " +
                "\"content\": \"Initial Comment.\" }";
        String createResponse = mockMvc.perform(post("/articles/" + testArticle.getId() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map<?,?> commentMap = objectMapper.readValue(createResponse, Map.class);
        Number commentId = (Number) commentMap.get("commentId");

        // 2. 댓글 수정 요청 (평문 "password" 사용)
        String updateJson = "{ \"email\": \"" + testUser.getEmail() + "\", " +
                "\"password\": \"password\", " +
                "\"content\": \"Updated Comment.\" }";
        mockMvc.perform(put("/articles/" + testArticle.getId() + "/comments/" + commentId.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated Comment."));
    }


    @Test
    public void testDeleteComment_Success() throws Exception {
        // 1. 댓글 생성 (평문 "password" 사용)
        String createJson = "{ \"email\": \"" + testUser.getEmail() + "\", " +
                "\"password\": \"password\", " +
                "\"content\": \"To be deleted comment.\" }";
        String createResponse = mockMvc.perform(post("/articles/" + testArticle.getId() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map<?,?> commentMap = objectMapper.readValue(createResponse, Map.class);
        Number commentId = (Number) commentMap.get("commentId");

        // 2. 댓글 삭제 요청 (평문 "password" 사용)
        String deleteJson = "{ \"email\": \"" + testUser.getEmail() + "\", " +
                "\"password\": \"password\" }";
        mockMvc.perform(delete("/articles/" + testArticle.getId() + "/comments/" + commentId.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deleteJson))
                .andExpect(status().isOk());
    }

}

//검증 내용 및 의도:
//
//댓글 관련 기능 테스트: 댓글 생성, 조회(빈 배열 검증), 수정, 삭제 등의 API가 올바르게 동작하는지 검증한다
//연관성 확인: 댓글이 특정 게시글에 올바르게 연결되고, 인증 정보(이메일, 평문 비밀번호)를 통해 정상 작동하는지를 확인한다