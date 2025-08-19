package com.blog.application.controller;

import com.blog.application.entity.User;
import com.blog.application.response.UserDTO;
import com.blog.application.response.UserIdDTO;
import com.blog.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "모든 사용자 조회", description = "모든 사용자 리스트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공적으로 사용자 목록을 조회했습니다.")
    public ResponseEntity<List<UserIdDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users.stream()
                .map(user -> new UserIdDTO(user.getEmail(), user.getNickname(), user.getId()))
                .toList(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "사용자 조회", description = "ID로 특정 사용자를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "사용자 조회 성공")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    public ResponseEntity<User> getUserById(@Parameter(description = "사용자 ID") @PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @Operation(summary = "사용자 등록", description = "새로운 사용자를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "사용자 등록 성공")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserRequest request) {
        User newUser = new User(request.getEmail(), request.getNickname(), request.getPassword());
        User savedUser = userService.saveUser(newUser);
        return ResponseEntity.ok(new UserDTO(savedUser.getEmail(), savedUser.getNickname()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "사용자 탈퇴", description = "사용자 계정을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "사용자 탈퇴 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
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
    private String nickname;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}

class DeleteUserRequest {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
