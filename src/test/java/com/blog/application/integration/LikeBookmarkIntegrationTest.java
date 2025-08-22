package com.blog.application.integration;

import com.blog.application.entity.Article;
import com.blog.application.entity.User;
import com.blog.application.repository.jpa.ArticleRepository;
import com.blog.application.repository.jpa.UserRepository;
import com.blog.application.service.ArticleLikeService;
import com.blog.application.service.ArticleBookmarkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LikeBookmarkIntegrationTest {

    @Autowired
    private ArticleLikeService articleLikeService;

    @Autowired
    private ArticleBookmarkService articleBookmarkService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    private User testUser;
    private Article testArticle;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setNickname("testuser");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        testArticle = new Article("Test Title", "Test Content", testUser);
        testArticle = articleRepository.save(testArticle);
    }

    @Test
    void 좋아요_기능_통합_테스트() {
        // Given - 초기 상태 확인
        assertThat(articleLikeService.isLiked(testArticle.getId(), testUser.getEmail())).isFalse();
        assertThat(articleLikeService.getLikeCount(testArticle.getId())).isEqualTo(0L);

        // When - 좋아요 추가
        boolean result1 = articleLikeService.toggleLike(testArticle.getId(), testUser.getEmail());

        // Then
        assertThat(result1).isTrue(); // 좋아요 추가됨
        assertThat(articleLikeService.isLiked(testArticle.getId(), testUser.getEmail())).isTrue();
        assertThat(articleLikeService.getLikeCount(testArticle.getId())).isEqualTo(1L);

        // When - 좋아요 취소
        boolean result2 = articleLikeService.toggleLike(testArticle.getId(), testUser.getEmail());

        // Then
        assertThat(result2).isFalse(); // 좋아요 취소됨
        assertThat(articleLikeService.isLiked(testArticle.getId(), testUser.getEmail())).isFalse();
        assertThat(articleLikeService.getLikeCount(testArticle.getId())).isEqualTo(0L);
    }

    @Test
    void 북마크_기능_통합_테스트() {
        // Given - 초기 상태 확인
        assertThat(articleBookmarkService.isBookmarked(testArticle.getId(), testUser.getEmail())).isFalse();

        // When - 북마크 추가
        boolean result1 = articleBookmarkService.toggleBookmark(testArticle.getId(), testUser.getEmail());

        // Then
        assertThat(result1).isTrue(); // 북마크 추가됨
        assertThat(articleBookmarkService.isBookmarked(testArticle.getId(), testUser.getEmail())).isTrue();

        // When - 북마크 취소
        boolean result2 = articleBookmarkService.toggleBookmark(testArticle.getId(), testUser.getEmail());

        // Then
        assertThat(result2).isFalse(); // 북마크 취소됨
        assertThat(articleBookmarkService.isBookmarked(testArticle.getId(), testUser.getEmail())).isFalse();
    }
}