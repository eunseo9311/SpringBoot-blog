package com.blog.application.service;

import com.blog.application.entity.Article;
import com.blog.application.entity.ArticleBookmark;
import com.blog.application.entity.User;
import com.blog.application.repository.jpa.ArticleBookmarkRepository;
import com.blog.application.repository.jpa.ArticleRepository;
import com.blog.application.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleBookmarkService {

    private final ArticleBookmarkRepository articleBookmarkRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 게시글 북마크 토글 (idempotent)
     * @param articleId 게시글 ID
     * @param userEmail 사용자 이메일
     * @return true: 북마크 추가, false: 북마크 취소
     */
    @Transactional
    public boolean toggleBookmark(Long articleId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Article article = findArticleById(articleId);

        // 기존 북마크 확인
        boolean alreadyBookmarked = articleBookmarkRepository.existsByUserAndArticle(user, article);
        
        if (alreadyBookmarked) {
            return removeBookmarkWithRetry(user, article);
        } else {
            return addBookmarkWithRetry(user, article);
        }
    }

    /**
     * 북마크 추가 (낙관적 재시도)
     */
    private boolean addBookmarkWithRetry(User user, Article article) {
        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                return addBookmark(user, article);
            } catch (DataIntegrityViolationException e) {
                // UNIQUE 제약 조건 위반 - 이미 북마크가 존재함
                log.debug("Bookmark already exists for user {} and article {} (attempt {})", 
                         user.getEmail(), article.getId(), attempt);
                
                if (attempt == MAX_RETRY_COUNT) {
                    // 마지막 시도에서도 실패하면 idempotent하게 성공으로 처리
                    log.info("Bookmark already exists after {} attempts - treating as success", attempt);
                    return true;
                }
                
                // 잠시 대기 후 재시도
                try {
                    Thread.sleep(10 * attempt); // 10ms, 20ms, 30ms
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
        return true; // 이론적으로 도달하지 않음
    }

    /**
     * 북마크 제거 (낙관적 재시도)
     */
    private boolean removeBookmarkWithRetry(User user, Article article) {
        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                return removeBookmark(user, article);
            } catch (Exception e) {
                log.debug("Error removing bookmark for user {} and article {} (attempt {})", 
                         user.getEmail(), article.getId(), attempt, e);
                
                if (attempt == MAX_RETRY_COUNT) {
                    // 마지막 시도에서도 실패하면 idempotent하게 성공으로 처리
                    log.info("Failed to remove bookmark after {} attempts - treating as success", attempt);
                    return false;
                }
                
                // 잠시 대기 후 재시도
                try {
                    Thread.sleep(10 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
        return false; // 이론적으로 도달하지 않음
    }

    /**
     * 북마크 추가 로직
     */
    private boolean addBookmark(User user, Article article) {
        ArticleBookmark bookmark = new ArticleBookmark(user, article);
        articleBookmarkRepository.save(bookmark);
        
        log.info("Bookmark added: user={}, article={}", user.getEmail(), article.getId());
        return true;
    }

    /**
     * 북마크 제거 로직
     */
    private boolean removeBookmark(User user, Article article) {
        int deletedCount = articleBookmarkRepository.deleteByUserAndArticle(user, article);
        
        if (deletedCount > 0) {
            log.info("Bookmark removed: user={}, article={}", user.getEmail(), article.getId());
        } else {
            // 삭제할 북마크가 없음 (idempotent)
            log.debug("No bookmark to remove for user={}, article={}", user.getEmail(), article.getId());
        }
        
        return false;
    }

    /**
     * 북마크 상태 조회
     */
    @Transactional(readOnly = true)
    public boolean isBookmarked(Long articleId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Article article = findArticleById(articleId);
        return articleBookmarkRepository.existsByUserAndArticle(user, article);
    }

    /**
     * 사용자의 북마크 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Article> getBookmarkedArticles(String userEmail) {
        User user = findUserByEmail(userEmail);
        return articleBookmarkRepository.findBookmarkedArticlesByUser(user);
    }

    /**
     * 게시글 북마크 개수 조회
     */
    @Transactional(readOnly = true)
    public long getBookmarkCount(Long articleId) {
        Article article = findArticleById(articleId);
        return articleBookmarkRepository.countByArticle(article);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
    }

    private Article findArticleById(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + articleId));
    }
}