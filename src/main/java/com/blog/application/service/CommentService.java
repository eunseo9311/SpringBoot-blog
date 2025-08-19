package com.blog.application.service;

import com.blog.application.entity.Comment;
import com.blog.application.repository.jpa.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    // 댓글 저장 (생성 및 수정)
    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    // ID로 댓글 단건 조회
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    // 댓글 삭제
    public void deleteComment(Comment comment) {
        commentRepository.delete(comment);
    }

    // 특정 게시글에 속한 모든 댓글 조회
    public List<Comment> getCommentsByArticleId(Long articleId) {
        return commentRepository.findAllByArticleId(articleId);
    }
}

//CommentService는 댓글 관련 CRUD 로직을 수행한다
//댓글 생성, 수정, 삭제, 그리고 특정 게시글에 속한 댓글 목록 조회 기능을 제공한다
