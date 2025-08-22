package com.blog.application.service;

import com.blog.application.entity.Article;
import com.blog.application.entity.ArticleLike;
import com.blog.application.entity.User;
import com.blog.application.repository.jpa.ArticleLikeRepository;
import com.blog.application.repository.jpa.ArticleRepository;
import com.blog.application.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleLikeService {

    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 게시글 좋아요 토글 (idempotent)
     * @param articleId 게시글 ID
     * @param userEmail 사용자 이메일
     * @return true: 좋아요 추가, false: 좋아요 취소
     */
    @Transactional
    public boolean toggleLike(Long articleId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Article article = findArticleById(articleId);

        // 기존 좋아요 확인
        boolean alreadyLiked = articleLikeRepository.existsByUserAndArticle(user, article);
        
        if (alreadyLiked) {
            return removeLikeWithRetry(user, article);
        } else {
            return addLikeWithRetry(user, article);
        }
    }

    /**
     * 좋아요 추가 (낙관적 재시도)
     */
    private boolean addLikeWithRetry(User user, Article article) {
        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                return addLike(user, article);
            } catch (DataIntegrityViolationException e) {
                // UNIQUE 제약 조건 위반 - 이미 좋아요가 존재함
                log.debug("Like already exists for user {} and article {} (attempt {})", 
                         user.getEmail(), article.getId(), attempt);
                
                if (attempt == MAX_RETRY_COUNT) {
                    // 마지막 시도에서도 실패하면 idempotent하게 성공으로 처리
                    log.info("Like already exists after {} attempts - treating as success", attempt);
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
     * 좋아요 제거 (낙관적 재시도)
     */
    private boolean removeLikeWithRetry(User user, Article article) {
        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            try {
                return removeLike(user, article);
            } catch (Exception e) {
                log.debug("Error removing like for user {} and article {} (attempt {})", 
                         user.getEmail(), article.getId(), attempt, e);
                
                if (attempt == MAX_RETRY_COUNT) {
                    // 마지막 시도에서도 실패하면 idempotent하게 성공으로 처리
                    log.info("Failed to remove like after {} attempts - treating as success", attempt);
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
     * 좋아요 추가 로직
     */
    private boolean addLike(User user, Article article) {
        // 1. 좋아요 엔티티 저장
        ArticleLike like = new ArticleLike(user, article);
        articleLikeRepository.save(like);
        
        // 2. 게시글 좋아요 카운트 증가
        article.incrementLikeCount();
        articleRepository.save(article);
        
        log.info("Like added: user={}, article={}, new count={}", 
                user.getEmail(), article.getId(), article.getLikeCount());
        
        return true;
    }

    /**
     * 좋아요 제거 로직
     */
    private boolean removeLike(User user, Article article) {
        // 1. 좋아요 엔티티 삭제
        int deletedCount = articleLikeRepository.deleteByUserAndArticle(user, article);
        
        if (deletedCount > 0) {
            // 2. 게시글 좋아요 카운트 감소
            article.decrementLikeCount();
            articleRepository.save(article);
            
            log.info("Like removed: user={}, article={}, new count={}", 
                    user.getEmail(), article.getId(), article.getLikeCount());
        } else {
            // 삭제할 좋아요가 없음 (idempotent)
            log.debug("No like to remove for user={}, article={}", user.getEmail(), article.getId());
        }
        
        return false;
    }

    /**
     * 좋아요 상태 조회
     */
    @Transactional(readOnly = true)
    public boolean isLiked(Long articleId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Article article = findArticleById(articleId);
        return articleLikeRepository.existsByUserAndArticle(user, article);
    }

    /**
     * 게시글 좋아요 개수 조회
     */
    @Transactional(readOnly = true)
    public long getLikeCount(Long articleId) {
        Article article = findArticleById(articleId);
        return article.getLikeCount();
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