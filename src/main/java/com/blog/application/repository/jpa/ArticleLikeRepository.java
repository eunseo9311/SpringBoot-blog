package com.blog.application.repository.jpa;

import com.blog.application.entity.Article;
import com.blog.application.entity.ArticleLike;
import com.blog.application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {

    /**
     * 특정 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
     */
    Optional<ArticleLike> findByUserAndArticle(User user, Article article);

    /**
     * 특정 사용자와 게시글의 좋아요 존재 여부 확인
     */
    boolean existsByUserAndArticle(User user, Article article);

    /**
     * 특정 게시글의 총 좋아요 개수 조회
     */
    long countByArticle(Article article);

    /**
     * 특정 사용자의 좋아요 목록 조회
     */
    @Query("SELECT al.article FROM ArticleLike al WHERE al.user = :user ORDER BY al.createdAt DESC")
    java.util.List<Article> findLikedArticlesByUser(@Param("user") User user);

    /**
     * 특정 사용자와 게시글의 좋아요 삭제
     */
    @Modifying
    @Query("DELETE FROM ArticleLike al WHERE al.user = :user AND al.article = :article")
    int deleteByUserAndArticle(@Param("user") User user, @Param("article") Article article);

    /**
     * 특정 게시글의 모든 좋아요 삭제 (게시글 삭제 시 사용)
     */
    void deleteAllByArticle(Article article);

    /**
     * 특정 사용자의 모든 좋아요 삭제 (사용자 삭제 시 사용)
     */
    void deleteAllByUser(User user);
}