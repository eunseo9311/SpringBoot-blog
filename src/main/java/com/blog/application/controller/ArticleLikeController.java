package com.blog.application.controller;

import com.blog.application.common.response.ApiResponse;
import com.blog.application.common.status.SuccessStatus;
import com.blog.application.service.ArticleLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/articles/{articleId}")
@RequiredArgsConstructor
@Tag(name = "Article Like", description = "게시글 좋아요 API")
public class ArticleLikeController {

    private final ArticleLikeService articleLikeService;

    @PostMapping("/like")
    @Operation(summary = "게시글 좋아요 추가", description = "게시글에 좋아요를 추가합니다. 이미 좋아요가 있으면 idempotent하게 성공을 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요 추가 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> addLike(
            @Parameter(description = "게시글 ID") @PathVariable Long articleId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        boolean wasAdded = articleLikeService.toggleLike(articleId, userDetails.getUsername());
        long likeCount = articleLikeService.getLikeCount(articleId);
        
        Map<String, Object> data = Map.of(
            "liked", true,
            "wasAdded", wasAdded,
            "likeCount", likeCount
        );
        
        return ResponseEntity.ok(ApiResponse.success(SuccessStatus.ARTICLE_LIKE_SUCCESS, data));
    }

    @DeleteMapping("/like")
    @Operation(summary = "게시글 좋아요 취소", description = "게시글의 좋아요를 취소합니다. 좋아요가 없으면 idempotent하게 성공을 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요 취소 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> removeLike(
            @Parameter(description = "게시글 ID") @PathVariable Long articleId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        boolean wasRemoved = !articleLikeService.toggleLike(articleId, userDetails.getUsername());
        long likeCount = articleLikeService.getLikeCount(articleId);
        
        Map<String, Object> data = Map.of(
            "liked", false,
            "wasRemoved", wasRemoved,
            "likeCount", likeCount
        );
        
        return ResponseEntity.ok(ApiResponse.success(SuccessStatus.ARTICLE_LIKE_SUCCESS, data));
    }

    @GetMapping("/like/status")
    @Operation(summary = "좋아요 상태 조회", description = "현재 사용자의 게시글 좋아요 상태를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLikeStatus(
            @Parameter(description = "게시글 ID") @PathVariable Long articleId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        boolean isLiked = articleLikeService.isLiked(articleId, userDetails.getUsername());
        long likeCount = articleLikeService.getLikeCount(articleId);
        
        Map<String, Object> data = Map.of(
            "liked", isLiked,
            "likeCount", likeCount
        );
        
        return ResponseEntity.ok(ApiResponse.success(SuccessStatus.ARTICLE_READ_SUCCESS, data));
    }
}