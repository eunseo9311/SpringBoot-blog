package com.blog.application.controller;

import com.blog.application.entity.Article;
import com.blog.application.response.ArticleDTO;
import com.blog.application.service.ArticleService;
import com.blog.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/articles")
@Tag(name = "Article", description = "게시글 관리 API")
public class ArticleController {

    private final ArticleService articleService;
    private final UserService userService;

    public ArticleController(ArticleService articleService, UserService userService) {
        this.articleService = articleService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "모든 게시글 조회", description = "모든 게시글을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 게시글 목록을 조회했습니다.")
    public ResponseEntity<List<ArticleDTO>> getAllArticles() {
        List<Article> articles = articleService.getAllArticles();
        return new ResponseEntity<>(articles.stream()
                .map(ArticleDTO::new)
                .toList(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "게시글 조회", description = "ID로 특정 게시글을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 조회 성공")
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다.")
    public ResponseEntity<ArticleDTO> getArticleById(@Parameter(description = "게시글 ID") @PathVariable Long id) {
        Optional<Article> article = articleService.getArticleById(id);
        return article.map(value -> new ResponseEntity<>(new ArticleDTO(value), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다.")
    public ResponseEntity<?> deleteArticleById(
            @Parameter(description = "게시글 ID") @PathVariable Long id,
            @RequestBody DeleteArticleRequest deleteRequest) {

        Optional<Article> articleOptional = articleService.getArticleById(id);
        if (articleOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Article article = articleOptional.get();
        if (!article.getUser().getEmail().equals(deleteRequest.getEmail()) ||
                !userService.matchedPassword(deleteRequest.getPassword(), article.getUser().getPassword())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        articleService.deleteArticle(article);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 작성 성공")
    @ApiResponse(responseCode = "400", description = "제목 또는 내용이 비어있습니다.")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    public ResponseEntity<?> createArticle(@RequestBody ArticleRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty() ||
                request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Title and content must not be empty.");
        }
        
        var user = userService.getUserByEmailAndPassword(request.getEmail(), request.getPassword());
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Article newArticle = new Article(request.getTitle(), request.getContent(), user.get());
        Article savedArticle = articleService.saveArticle(newArticle);
        return ResponseEntity.ok(new ArticleDTO(savedArticle));
    }

    @PutMapping("/{id}")
    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 수정 성공")
    @ApiResponse(responseCode = "400", description = "제목 또는 내용이 비어있습니다.")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다.")
    public ResponseEntity<?> updateArticle(@Parameter(description = "게시글 ID") @PathVariable Long id, @RequestBody ArticleUpdateRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty() ||
                request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Title and content must not be empty.");
        }

        Optional<Article> articleOptional = articleService.getArticleById(id);
        if (articleOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Article article = articleOptional.get();
        if (!article.getUser().getEmail().equals(request.getEmail()) ||
                !userService.matchedPassword(request.getPassword(), article.getUser().getPassword())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        Article updatedArticle = articleService.saveArticle(article);
        return ResponseEntity.ok(new ArticleDTO(updatedArticle));
    }

    static class ArticleRequest {
        private String email;
        private String password;
        private String title;
        private String content;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    static class DeleteArticleRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    static class ArticleUpdateRequest {
        private String email;
        private String password;
        private String title;
        private String content;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}

