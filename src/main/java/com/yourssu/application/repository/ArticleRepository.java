package com.yourssu.application.repository;

import com.yourssu.application.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    // 특정 사용자의 모든 게시글 조회
    List<Article> findAllByUserId(Long userId);
}

//ArticleRepository는 Article 엔티티에 대한 CRUD 작업을 지원하며,
//사용자 ID로 게시글을 조회하는 커스텀 메서드를 제공한다


