package com.blog.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.blog.application.entity.Comment;
import com.blog.application.repository.CommentRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveComment() {
        // given
        Comment comment = new Comment();
        when(commentRepository.save(comment)).thenReturn(comment);

        // when
        Comment saved = commentService.saveComment(comment);

        // then
        assertEquals(comment, saved);
        verify(commentRepository).save(comment);
    }

    @Test
    public void testGetCommentByIdFound() {
        // given
        Comment comment = new Comment();
        comment.setId(1L);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // when
        Optional<Comment> result = commentService.getCommentById(1L);

        // then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(commentRepository).findById(1L);
    }

    @Test
    public void testGetCommentByIdNotFound() {
        // given
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        Optional<Comment> result = commentService.getCommentById(1L);

        // then
        assertFalse(result.isPresent());
        verify(commentRepository).findById(1L);
    }

    @Test
    public void testDeleteComment() {
        // given
        Comment comment = new Comment();

        // when
        commentService.deleteComment(comment);

        // then
        verify(commentRepository).delete(comment);
    }

    @Test
    public void testGetCommentsByArticleId() {
        // given
        Comment comment1 = new Comment();
        Comment comment2 = new Comment();
        List<Comment> comments = Arrays.asList(comment1, comment2);
        when(commentRepository.findAllByArticleId(1L)).thenReturn(comments);

        // when
        List<Comment> result = commentService.getCommentsByArticleId(1L);

        // then
        assertEquals(2, result.size());
        verify(commentRepository).findAllByArticleId(1L);
    }
}
