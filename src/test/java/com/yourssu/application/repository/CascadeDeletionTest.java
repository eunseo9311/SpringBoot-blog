package com.yourssu.application.repository;

import com.yourssu.application.entity.Article;
import com.yourssu.application.entity.Comment;
import com.yourssu.application.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class CascadeDeletionTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    public void testCascadeDeletionFromUser() {
        // 1. User 생성
        User user = new User("cascade@example.com", "cascadeUser", "password");

        // 2. Article 생성 및 User와 연관 설정
        Article article = new Article("Cascade Article", "Cascade Content", user);
        // User의 articles 컬렉션에 추가 (cascade persist를 위해)
        user.getArticles().add(article);

        // 3. Comment 생성 및 Article, User와 연관 설정
        Comment comment = new Comment();
        comment.setContent("Cascade Comment");
        comment.setUser(user);
        comment.setArticle(article);
        // User의 comments 컬렉션에 추가 (cascade persist를 위해)
        user.getComments().add(comment);

        // 4. User 저장 (cascade를 통해 Article, Comment도 함께 저장됨)
        user = userRepository.save(user);

        // 저장 후 Article과 Comment가 각각 존재하는지 확인
        assertFalse(articleRepository.findAll().isEmpty(), "Article이 저장되어야 합니다.");
        assertFalse(commentRepository.findAll().isEmpty(), "Comment가 저장되어야 합니다.");

        // 5. User 삭제 (cascade에 의해 연관된 Article, Comment도 삭제되어야 함)
        userRepository.delete(user);
        userRepository.flush(); // 삭제를 확정하기 위해 flush

        // 6. Article과 Comment가 삭제되었는지 검증
        assertTrue(articleRepository.findAll().isEmpty(), "User 삭제 후 Article이 모두 삭제되어야 합니다.");
        assertTrue(commentRepository.findAll().isEmpty(), "User 삭제 후 Comment가 모두 삭제되어야 합니다.");
    }
}
