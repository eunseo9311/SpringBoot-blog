package com.yourssu.application.repository;

import com.yourssu.application.entity.Article;
import com.yourssu.application.entity.Comment;
import com.yourssu.application.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveAndFindById() {
        // 테스트용 사용자 및 게시글 생성
        User user = new User();
        user.setEmail("user@example.com");
        user.setUsername("user");
        user.setPassword("password");
        user = userRepository.save(user);

        Article article = new Article();
        article.setTitle("Article");
        article.setContent("Content");
        article.setUser(user);
        article = articleRepository.save(article);

        // 댓글 저장
        Comment comment = new Comment();
        comment.setContent("Nice article");
        comment.setUser(user);
        comment.setArticle(article);
        comment = commentRepository.save(comment);

        // 댓글 조회
        Comment found = commentRepository.findById(comment.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Nice article", found.getContent());
    }

    @Test
    public void testFindAllByArticleId() {
        // 사용자, 게시글, 댓글 생성
        User user = new User();
        user.setEmail("user2@example.com");
        user.setUsername("user2");
        user.setPassword("password");
        user = userRepository.save(user);

        Article article = new Article();
        article.setTitle("Article");
        article.setContent("Content");
        article.setUser(user);
        article = articleRepository.save(article);

        Comment comment1 = new Comment();
        comment1.setContent("Comment 1");
        comment1.setUser(user);
        comment1.setArticle(article);
        commentRepository.save(comment1);

        Comment comment2 = new Comment();
        comment2.setContent("Comment 2");
        comment2.setUser(user);
        comment2.setArticle(article);
        commentRepository.save(comment2);

        List<Comment> comments = commentRepository.findAllByArticleId(article.getId());
        assertEquals(2, comments.size());
    }

    @Test
    public void testDeleteAllByArticleId() {
        // 사용자, 게시글, 댓글 생성
        User user = new User();
        user.setEmail("user3@example.com");
        user.setUsername("user3");
        user.setPassword("password");
        user = userRepository.save(user);

        Article article = new Article();
        article.setTitle("Article");
        article.setContent("Content");
        article.setUser(user);
        article = articleRepository.save(article);

        Comment comment1 = new Comment();
        comment1.setContent("Comment 1");
        comment1.setUser(user);
        comment1.setArticle(article);
        commentRepository.save(comment1);

        Comment comment2 = new Comment();
        comment2.setContent("Comment 2");
        comment2.setUser(user);
        comment2.setArticle(article);
        commentRepository.save(comment2);

        // 게시글 ID로 댓글 전체 삭제
        commentRepository.deleteAllByArticleId(article.getId());
        List<Comment> comments = commentRepository.findAllByArticleId(article.getId());
        assertEquals(0, comments.size());
    }

    @Test
    public void testDeleteAllByUserId() {
        // 사용자, 게시글, 댓글 생성
        User user = new User();
        user.setEmail("user4@example.com");
        user.setUsername("user4");
        user.setPassword("password");
        user = userRepository.save(user);

        Article article = new Article();
        article.setTitle("Article");
        article.setContent("Content");
        article.setUser(user);
        article = articleRepository.save(article);

        Comment comment1 = new Comment();
        comment1.setContent("Comment 1");
        comment1.setUser(user);
        comment1.setArticle(article);
        commentRepository.save(comment1);

        Comment comment2 = new Comment();
        comment2.setContent("Comment 2");
        comment2.setUser(user);
        comment2.setArticle(article);
        commentRepository.save(comment2);

        // 사용자 ID로 댓글 전체 삭제
        commentRepository.deleteAllByUserId(user.getId());
        List<Comment> comments = commentRepository.findAllByArticleId(article.getId());
        // 게시글에 연결된 댓글도 삭제되었어야 함.
        assertEquals(0, comments.size());
    }
}
