package com.blog.application.repository.jpa;

import com.blog.application.entity.Article;
import com.blog.application.entity.Comment;
import com.blog.application.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 특정 게시글에 속하는 모든 댓글 삭제
    void deleteAllByArticleId(Long articleId);

    // 특정 사용자의 모든 댓글 삭제
    void deleteAllByUserId(Long id);

    // 특정 게시글에 속하는 댓글 목록 조회
    List<Comment> findAllByArticleId(Long articleId);
}

//CommentRepository는 댓글 관련 CRUD 작업과 함께,
//게시글 또는 사용자 기준의 댓글 삭제/조회 커스텀 메서드를 제공한다.