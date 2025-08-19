package com.blog.application.repository;

import com.blog.application.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveAndFindById() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setNickname("user");
        user.setPassword("password");
        user = userRepository.save(user);

        Optional<User> found = userRepository.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("user@example.com", found.get().getEmail());
    }

    @Test
    public void testFindByEmail() {
        User user = new User();
        user.setEmail("find@example.com");
        user.setNickname("finduser");
        user.setPassword("password");
        user = userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("find@example.com");
        assertTrue(found.isPresent());
        assertEquals("finduser", found.get().getNickname());
    }

    @Test
    public void testFindAll() {
        User user1 = new User();
        user1.setEmail("a@example.com");
        user1.setNickname("a");
        user1.setPassword("password");

        User user2 = new User();
        user2.setEmail("b@example.com");
        user2.setNickname("b");
        user2.setPassword("password");

        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userRepository.findAll();
        assertTrue(users.size() >= 2);
    }
}
