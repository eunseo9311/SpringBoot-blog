package com.blog.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.blog.application.entity.Article;
import com.blog.application.entity.User;
import com.blog.application.repository.ArticleRepository;
import com.blog.application.repository.CommentRepository;
import com.blog.application.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ArticleService articleService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = Arrays.asList(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();
        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    public void testGetUserByIdFound() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(userRepository).findById(1L);
    }

    @Test
    public void testGetUserByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<User> result = userService.getUserById(1L);
        assertFalse(result.isPresent());
        verify(userRepository).findById(1L);
    }

    @Test
    public void testGetUserByEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmail("test@example.com");
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    public void testGetUserByEmailAndPassword_Success() {
        // given: user with encoded password
        User user = new User();
        user.setEmail("test@example.com");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode("password");
        user.setPassword(encoded);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // when
        Optional<User> result = userService.getUserByEmailAndPassword("test@example.com", "password");

        // then
        assertTrue(result.isPresent());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    public void testGetUserByEmailAndPassword_Failure() {
        // given: wrong password
        User user = new User();
        user.setEmail("test@example.com");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode("password");
        user.setPassword(encoded);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // when
        Optional<User> result = userService.getUserByEmailAndPassword("test@example.com", "wrongpassword");

        // then
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    public void testEncodePasswordAndMatchedPassword() {
        String original = "password";
        String encoded = userService.encodePassword(original);
        assertTrue(userService.matchedPassword(original, encoded));
    }

    @Test
    public void testSaveUser() {
        User user = new User();
        user.setPassword("password");
        user.setEmail("test@example.com");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.saveUser(user);
        // 저장 시 원래 평문은 암호화되어 저장되어야 하므로 평문과는 달라야 함.
        assertNotEquals("password", saved.getPassword());
        assertTrue(userService.matchedPassword("password", saved.getPassword()));
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testDeleteUser() {
        // given: user with articles
        User user = new User();
        user.setId(1L);

        Article article1 = new Article();
        article1.setId(101L);
        Article article2 = new Article();
        article2.setId(102L);
        List<Article> articles = Arrays.asList(article1, article2);
        when(articleRepository.findAllByUserId(1L)).thenReturn(articles);

        // when
        userService.deleteUser(user);

        // then: 각 article에 대해 articleService.deleteArticle이 호출되고,
        // commentRepository.deleteAllByUserId, userRepository.delete가 호출되어야 함.
        verify(articleService).deleteArticle(article1);
        verify(articleService).deleteArticle(article2);
        verify(commentRepository).deleteAllByUserId(1L);
        verify(userRepository).delete(user);
    }
}
