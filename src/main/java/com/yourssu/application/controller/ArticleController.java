package com.yourssu.application.controller;

import com.yourssu.application.entity.Article;
import com.yourssu.application.response.ArticleDTO;
import com.yourssu.application.service.ArticleService;
import com.yourssu.application.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final UserService userService;

    public ArticleController(ArticleService articleService, UserService userService) {
        this.articleService = articleService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<ArticleDTO>> getAllArticles() {
        List<Article> articles = articleService.getAllArticles();
        return new ResponseEntity<>(articles.stream()
                .map(ArticleDTO::new)
                .toList(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleDTO> getArticleById(@PathVariable Long id) {
        Optional<Article> article = articleService.getArticleById(id);
        return article.map(value -> new ResponseEntity<>(new ArticleDTO(value), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticleById(
            @PathVariable Long id,
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
    public ResponseEntity<?> updateArticle(@PathVariable Long id, @RequestBody ArticleUpdateRequest request) {
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

