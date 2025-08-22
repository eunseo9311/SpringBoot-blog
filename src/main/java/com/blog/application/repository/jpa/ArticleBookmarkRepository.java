package com.blog.application.repository.jpa;

import com.blog.application.entity.Article;
import com.blog.application.entity.ArticleBookmark;
import com.blog.application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleBookmarkRepository extends JpaRepository<ArticleBookmark, Long> {

    /**
     * 특정 사용자가 특정 게시글을 북마크했는지 확인
     */
    Optional<ArticleBookmark> findByUserAndArticle(User user, Article article);

    /**
     * 특정 사용자와 게시글의 북마크 존재 여부 확인
     */
    boolean existsByUserAndArticle(User user, Article article);

    /**
     * 특정 게시글의 총 북마크 개수 조회
     */
    long countByArticle(Article article);

    /**
     * 특정 사용자의 북마크 목록 조회
     */
    @Query("SELECT ab.article FROM ArticleBookmark ab WHERE ab.user = :user ORDER BY ab.createdAt DESC")
    java.util.List<Article> findBookmarkedArticlesByUser(@Param("user") User user);

    /**
     * 특정 사용자와 게시글의 북마크 삭제
     */
    @Modifying
    @Query("DELETE FROM ArticleBookmark ab WHERE ab.user = :user AND ab.article = :article")
    int deleteByUserAndArticle(@Param("user") User user, @Param("article") Article article);

    /**
     * 특정 게시글의 모든 북마크 삭제 (게시글 삭제 시 사용)
     */
    void deleteAllByArticle(Article article);

    /**
     * 특정 사용자의 모든 북마크 삭제 (사용자 삭제 시 사용)
     */
    void deleteAllByUser(User user);
}