package com.blog.application.controller;

import com.blog.application.common.response.ApiResponse;
import com.blog.application.common.status.SuccessStatus;
import com.blog.application.service.ArticleBookmarkService;
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
@Tag(name = "Article Bookmark", description = "게시글 북마크 API")
public class ArticleBookmarkController {

    private final ArticleBookmarkService articleBookmarkService;

    @PostMapping("/bookmark")
    @Operation(summary = "게시글 북마크 추가", description = "게시글을 북마크에 추가합니다. 이미 북마크가 있으면 idempotent하게 성공을 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "북마크 추가 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> addBookmark(
            @Parameter(description = "게시글 ID") @PathVariable Long articleId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        boolean wasAdded = articleBookmarkService.toggleBookmark(articleId, userDetails.getUsername());
        long bookmarkCount = articleBookmarkService.getBookmarkCount(articleId);
        
        Map<String, Object> data = Map.of(
            "bookmarked", true,
            "wasAdded", wasAdded,
            "bookmarkCount", bookmarkCount
        );
        
        return ResponseEntity.ok(ApiResponse.success(SuccessStatus.ARTICLE_BOOKMARK_SUCCESS, data));
    }

    @DeleteMapping("/bookmark")
    @Operation(summary = "게시글 북마크 취소", description = "게시글을 북마크에서 제거합니다. 북마크가 없으면 idempotent하게 성공을 반환합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "북마크 취소 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> removeBookmark(
            @Parameter(description = "게시글 ID") @PathVariable Long articleId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        boolean wasRemoved = !articleBookmarkService.toggleBookmark(articleId, userDetails.getUsername());
        long bookmarkCount = articleBookmarkService.getBookmarkCount(articleId);
        
        Map<String, Object> data = Map.of(
            "bookmarked", false,
            "wasRemoved", wasRemoved,
            "bookmarkCount", bookmarkCount
        );
        
        return ResponseEntity.ok(ApiResponse.success(SuccessStatus.ARTICLE_BOOKMARK_SUCCESS, data));
    }

    @GetMapping("/bookmark/status")
    @Operation(summary = "북마크 상태 조회", description = "현재 사용자의 게시글 북마크 상태를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBookmarkStatus(
            @Parameter(description = "게시글 ID") @PathVariable Long articleId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        boolean isBookmarked = articleBookmarkService.isBookmarked(articleId, userDetails.getUsername());
        long bookmarkCount = articleBookmarkService.getBookmarkCount(articleId);
        
        Map<String, Object> data = Map.of(
            "bookmarked", isBookmarked,
            "bookmarkCount", bookmarkCount
        );
        
        return ResponseEntity.ok(ApiResponse.success(SuccessStatus.ARTICLE_READ_SUCCESS, data));
    }
}