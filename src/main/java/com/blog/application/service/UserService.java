package com.blog.application.service;

import com.blog.application.entity.Article;
import com.blog.application.entity.Comment;
import com.blog.application.entity.User;
import com.blog.application.repository.jpa.ArticleRepository;
import com.blog.application.repository.jpa.CommentRepository;
import com.blog.application.repository.jpa.UserRepository;
import org.aspectj.bridge.ICommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final ArticleService articleService;
    private final CommentRepository commentRepository;
    // BCrypt를 이용해 비밀번호 암호화/검증
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository,
                       ArticleRepository articleRepository,
                       ArticleService articleService,
                       CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
        this.articleService = articleService;
        this.commentRepository = commentRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public Optional<User> getUserByEmailAndPassword(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public String encodePassword(String originalPassword) {
        return passwordEncoder.encode(originalPassword);
    }

    public boolean matchedPassword(String original, String encoded) {
        return passwordEncoder.matches(original, encoded);
    }

    public User saveUser(User user) {
        user.setPassword(encodePassword(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(User user) {
        var articles = articleRepository.findAllByUserId(user.getId());
        for (Article article : articles) {
            articleService.deleteArticle(article);
        }
        commentRepository.deleteAllByUserId(user.getId());
        userRepository.delete(user);
    }
}
