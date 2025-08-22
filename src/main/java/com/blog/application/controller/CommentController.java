package com.blog.application.controller;

import com.blog.application.entity.Article;
import com.blog.application.entity.Comment;
import com.blog.application.entity.User;
import com.blog.application.response.CommentDTO;
import com.blog.application.service.ArticleService;
import com.blog.application.service.CommentService;
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
@RequestMapping("/articles/{articleId}/comments")
@Tag(name = "Comment", description = "댓글 관리 API")
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
    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 모든 댓글을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 댓글 목록을 조회했습니다.")
    public ResponseEntity<List<CommentDTO>> getComments(@Parameter(description = "게시글 ID") @PathVariable Long articleId) {
        List<Comment> comments = commentService.getCommentsByArticleId(articleId);
        return new ResponseEntity<>(comments.stream()
                .map(comment -> new CommentDTO(comment.getId(), comment.getUser().getEmail(), comment.getContent()))
                .toList(), HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "댓글 작성", description = "새로운 댓글을 작성합니다.")
    @ApiResponse(responseCode = "200", description = "댓글 작성 성공")
    @ApiResponse(responseCode = "400", description = "댓글 내용이 비어있습니다.")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다.")
    public ResponseEntity<?> createComment(@Parameter(description = "게시글 ID") @PathVariable Long articleId, @RequestBody CommentRequest request) {
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
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "댓글 삭제 성공")
    @ApiResponse(responseCode = "400", description = "비정상적인 요청")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다.")
    public ResponseEntity<?> deleteComment(@Parameter(description = "게시글 ID") @PathVariable Long articleId,
                                           @Parameter(description = "댓글 ID") @PathVariable Long commentId,
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
    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "댓글 수정 성공")
    @ApiResponse(responseCode = "400", description = "비정상적인 요청")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다.")
    public ResponseEntity<?> updateComment(@Parameter(description = "게시글 ID") @PathVariable Long articleId,
                                           @Parameter(description = "댓글 ID") @PathVariable Long commentId,
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
