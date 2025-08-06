package com.yourssu.application.controller;

import com.yourssu.application.entity.Article;
import com.yourssu.application.entity.Comment;
import com.yourssu.application.entity.User;
import com.yourssu.application.response.CommentDTO;
import com.yourssu.application.service.ArticleService;
import com.yourssu.application.service.CommentService;
import com.yourssu.application.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/articles/{articleId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final ArticleService articleService;
    private final UserService userService;

    public CommentController(CommentService commentService, ArticleService articleService, UserService userService) {
        this.commentService = commentService;
        this.articleService = articleService;
        this.userService = userService;
    }

    @GetMapping("/")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long articleId) {
        List<Comment> comments = commentService.getCommentsByArticleId(articleId);
        return new ResponseEntity<>(comments.stream()
                .map(comment -> new CommentDTO(comment.getId(), comment.getContent(), comment.getUser().getEmail()))
                .toList(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createComment(@PathVariable Long articleId, @RequestBody CommentRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Content must not be empty.");
        }

        Optional<Article> articleOptional = articleService.getArticleById(articleId);
        if (articleOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Article not found.");
        }
        Article article = articleOptional.get();

        Optional<User> userOptional = userService.getUserByEmailAndPassword(request.getEmail(), request.getPassword());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOptional.get();

        Comment newComment = new Comment();
        newComment.setContent(request.getContent());
        newComment.setUser(user);
        newComment.setArticle(article);

        Comment savedComment = commentService.saveComment(newComment);
        CommentResponse response = new CommentResponse(savedComment.getId(), user.getEmail(), savedComment.getContent());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long articleId,
                                           @PathVariable Long commentId,
                                           @RequestBody DeleteCommentRequest request) {
        Optional<Comment> commentOptional = commentService.getCommentById(commentId);
        if (commentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found.");
        }
        Comment comment = commentOptional.get();

        if (!comment.getArticle().getId().equals(articleId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Comment does not belong to the specified article.");
        }

        if (!comment.getUser().getEmail().equals(request.getEmail()) ||
                !userService.matchedPassword(request.getPassword(), comment.getUser().getPassword())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        commentService.deleteComment(comment);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long articleId,
                                           @PathVariable Long commentId,
                                           @RequestBody CommentUpdateRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Content must not be empty.");
        }

        Optional<Comment> commentOptional = commentService.getCommentById(commentId);
        if (commentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found.");
        }
        Comment comment = commentOptional.get();

        if (!comment.getArticle().getId().equals(articleId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Comment does not belong to the specified article.");
        }

        if (!comment.getUser().getEmail().equals(request.getEmail()) ||
                !userService.matchedPassword(request.getPassword(), comment.getUser().getPassword())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentService.saveComment(comment);
        CommentResponse response = new CommentResponse(updatedComment.getId(), updatedComment.getUser().getEmail(), updatedComment.getContent());
        return ResponseEntity.ok(response);
    }

    static class CommentRequest {
        private String email;
        private String password;
        private String content;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    static class CommentUpdateRequest {
        private String email;
        private String password;
        private String content;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    static class CommentResponse {
        private Long commentId;
        private String email;
        private String content;

        public CommentResponse(Long commentId, String email, String content) {
            this.commentId = commentId;
            this.email = email;
            this.content = content;
        }

        public Long getCommentId() { return commentId; }
        public void setCommentId(Long commentId) { this.commentId = commentId; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    static class DeleteCommentRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
