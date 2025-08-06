package com.yourssu.application.controller;

import com.yourssu.application.entity.User;
import com.yourssu.application.response.UserDTO;
import com.yourssu.application.response.UserIdDTO;
import com.yourssu.application.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserIdDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users.stream()
                .map(user -> new UserIdDTO(user.getEmail(), user.getUsername(), user.getId()))
                .toList(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserRequest request) {
        User newUser = new User(request.getEmail(), request.getUsername(), request.getPassword());
        User savedUser = userService.saveUser(newUser);
        return ResponseEntity.ok(new UserDTO(savedUser.getEmail(), savedUser.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> withdrawUser(@RequestBody DeleteUserRequest request) {
        Optional<User> userOptional = userService.getUserByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        
        User user = userOptional.get();
        if (!userService.matchedPassword(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        userService.deleteUser(user);
        return ResponseEntity.ok().build();
    }
}

class UserRequest {
    private String email;
    private String password;
    private String username;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}

class DeleteUserRequest {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
