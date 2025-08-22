package com.blog.application.service;

import com.blog.application.entity.Article;
import com.blog.application.entity.User;
import com.blog.application.repository.jpa.ArticleLikeRepository;
import com.blog.application.repository.jpa.ArticleRepository;
import com.blog.application.repository.jpa.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ArticleLikeConcurrencyTest {

    @Autowired
    private ArticleLikeService articleLikeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleLikeRepository articleLikeRepository;

    private User testUser;
    private Article testArticle;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setNickname("testuser");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        // 테스트 게시글 생성
        testArticle = new Article("Test Title", "Test Content", testUser);
        testArticle = articleRepository.save(testArticle);
    }

    @Test
    void 동시에_30번_좋아요_토글_테스트() throws InterruptedException {
        // Given
        final int threadCount = 30;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When - 30개 스레드가 동시에 좋아요 토글
        CompletableFuture<Void>[] futures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        // 각 스레드가 좋아요를 토글
                        articleLikeService.toggleLike(testArticle.getId(), testUser.getEmail());
                    } finally {
                        latch.countDown();
                    }
                }, executorService))
                .toArray(CompletableFuture[]::new);

        // 모든 스레드 완료 대기
        CompletableFuture.allOf(futures).join();
        latch.await();

        // Then - 검증
        // 1. 좋아요 개수는 0 또는 1이어야 함 (토글이므로)
        long likeCount = articleLikeService.getLikeCount(testArticle.getId());
        assertThat(likeCount).isBetween(0L, 1L);

        // 2. DB의 실제 좋아요 레코드 수와 일치해야 함
        long actualLikeRecords = articleLikeRepository.countByArticle(testArticle);
        assertThat(likeCount).isEqualTo(actualLikeRecords);

        // 3. 중복 레코드가 없어야 함 (UNIQUE 제약 조건)
        assertThat(actualLikeRecords).isLessThanOrEqualTo(1L);

        // 4. 게시글의 like_count와 실제 레코드 수가 일치해야 함
        Article updatedArticle = articleRepository.findById(testArticle.getId()).orElseThrow();
        assertThat(updatedArticle.getLikeCount()).isEqualTo(actualLikeRecords);

        executorService.shutdown();
    }

    @Test
    void 동시에_좋아요_추가_후_모두_제거_테스트() throws InterruptedException {
        // Given
        final int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // When - Phase 1: 모든 스레드가 좋아요 추가 시도
        CountDownLatch addLatch = new CountDownLatch(threadCount);
        CompletableFuture<Void>[] addFutures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        articleLikeService.toggleLike(testArticle.getId(), testUser.getEmail());
                    } finally {
                        addLatch.countDown();
                    }
                }, executorService))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(addFutures).join();
        addLatch.await();

        // Phase 2: 모든 스레드가 좋아요 제거 시도
        CountDownLatch removeLatch = new CountDownLatch(threadCount);
        CompletableFuture<Void>[] removeFutures = IntStream.range(0, threadCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        // 현재 상태가 liked이면 toggle로 제거
                        if (articleLikeService.isLiked(testArticle.getId(), testUser.getEmail())) {
                            articleLikeService.toggleLike(testArticle.getId(), testUser.getEmail());
                        }
                    } finally {
                        removeLatch.countDown();
                    }
                }, executorService))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(removeFutures).join();
        removeLatch.await();

        // Then - 최종적으로 좋아요가 모두 제거되어야 함
        long finalLikeCount = articleLikeService.getLikeCount(testArticle.getId());
        assertThat(finalLikeCount).isEqualTo(0L);

        long actualLikeRecords = articleLikeRepository.countByArticle(testArticle);
        assertThat(actualLikeRecords).isEqualTo(0L);

        executorService.shutdown();
    }

    @Test
    void 여러_사용자가_동시에_좋아요_테스트() throws InterruptedException {
        // Given - 10명의 사용자 생성
        final int userCount = 10;
        User[] users = new User[userCount];
        for (int i = 0; i < userCount; i++) {
            User user = new User();
            user.setEmail("user" + i + "@example.com");
            user.setNickname("user" + i);
            user.setPassword("password");
            users[i] = userRepository.save(user);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(userCount);
        CountDownLatch latch = new CountDownLatch(userCount);

        // When - 각 사용자가 동시에 좋아요
        CompletableFuture<Void>[] futures = IntStream.range(0, userCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        articleLikeService.toggleLike(testArticle.getId(), users[i].getEmail());
                    } finally {
                        latch.countDown();
                    }
                }, executorService))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        latch.await();

        // Then - 좋아요 개수가 사용자 수와 일치해야 함
        long likeCount = articleLikeService.getLikeCount(testArticle.getId());
        assertThat(likeCount).isEqualTo(userCount);

        long actualLikeRecords = articleLikeRepository.countByArticle(testArticle);
        assertThat(actualLikeRecords).isEqualTo(userCount);

        // 각 사용자의 좋아요 상태 확인
        for (User user : users) {
            assertThat(articleLikeService.isLiked(testArticle.getId(), user.getEmail())).isTrue();
        }

        executorService.shutdown();
    }
}