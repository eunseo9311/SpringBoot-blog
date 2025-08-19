package com.blog.application.repository;

import com.blog.application.entity.Article;
import com.blog.application.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveAndFindById() {
        // 테스트용 사용자 생성
        User user = new User();
        user.setEmail("test@example.com");
        user.setNickname("testuser");
        user.setPassword("password"); // Repository 테스트에서는 암호화 여부와 무관하게 저장
        user = userRepository.save(user);

        // 게시글 저장
        Article article = new Article();
        article.setTitle("Test Article");
        article.setContent("Test Content");
        article.setUser(user);
        article = articleRepository.save(article);

        // 게시글 조회
        Article found = articleRepository.findById(article.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Test Article", found.getTitle());
    }

    @Test
    public void testFindAllByUserId() {
        // 두 사용자를 생성합니다.
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setNickname("user1");
        user1.setPassword("password");
        user1 = userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setNickname("user2");
        user2.setPassword("password");
        user2 = userRepository.save(user2);

        // 각 사용자마다 단 하나의 Article만 생성합니다.
        Article article1 = new Article();
        article1.setTitle("Article 1");
        article1.setContent("Content 1");
        article1.setUser(user1);
        articleRepository.save(article1);

        Article article2 = new Article();
        article2.setTitle("Article 2");
        article2.setContent("Content 2");
        article2.setUser(user2);
        articleRepository.save(article2);

        // 각 사용자의 게시글 조회 (각각 1개씩 반환)
        List<Article> user1Articles = articleRepository.findAllByUserId(user1.getId());
        assertEquals(1, user1Articles.size());

        List<Article> user2Articles = articleRepository.findAllByUserId(user2.getId());
        assertEquals(1, user2Articles.size());
    }
}
