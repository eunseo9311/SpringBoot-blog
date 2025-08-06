package com.yourssu.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.yourssu.application.entity.Article;
import com.yourssu.application.repository.ArticleRepository;
import com.yourssu.application.repository.CommentRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ArticleService articleService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllArticles() {
        // given
        Article article1 = new Article();
        Article article2 = new Article();
        List<Article> articles = Arrays.asList(article1, article2);
        when(articleRepository.findAll()).thenReturn(articles);

        // when
        List<Article> result = articleService.getAllArticles();

        // then
        assertEquals(2, result.size());
        verify(articleRepository).findAll();
    }

    @Test
    public void testGetArticleByIdFound() {
        // given
        Article article = new Article();
        article.setId(1L);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        // when
        Optional<Article> result = articleService.getArticleById(1L);

        // then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(articleRepository).findById(1L);
    }

    @Test
    public void testGetArticleByIdNotFound() {
        // given
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        Optional<Article> result = articleService.getArticleById(1L);

        // then
        assertFalse(result.isPresent());
        verify(articleRepository).findById(1L);
    }

    @Test
    public void testSaveArticle() {
        // given
        Article article = new Article();
        when(articleRepository.save(article)).thenReturn(article);

        // when
        Article saved = articleService.saveArticle(article);

        // then
        assertEquals(article, saved);
        verify(articleRepository).save(article);
    }

    @Test
    public void testDeleteArticle() {
        // given
        Article article = new Article();
        article.setId(1L);

        // when
        articleService.deleteArticle(article);

        // then
        verify(commentRepository).deleteAllByArticleId(1L);
        verify(articleRepository).delete(article);
    }

    @Test
    public void testUpdateArticle() {
        // given
        Article article = new Article();
        when(articleRepository.save(article)).thenReturn(article);

        // when
        Article updated = articleService.saveArticle(article);

        // then
        assertEquals(article, updated);
        verify(articleRepository).save(article);
    }
}
