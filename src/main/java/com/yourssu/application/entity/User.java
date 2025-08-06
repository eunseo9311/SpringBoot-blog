package com.yourssu.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"user\"") // 테이블명이 user (예약어 회피를 위해 따옴표 사용)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email // 이메일 형식 검증
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    // 회원 탈퇴 시 연관된 게시글/댓글 자동 삭제 (cascade 및 orphanRemoval 적용)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Article> articles = new ArrayList<>();
    // Casade와 orphanRemoval 모두 부모 연관된 데이터의 생명주기를 부모 엔티티에 맞추어 관리, 수동으로 자식 엔티티를 관리하는 번거로움을 줄임

    // 회원 탈퇴 시 연관된 댓글 자동 삭제
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public User() {}

    public User(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public List<Article> getArticles() {
        return articles;
    }
    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }
    public List<Comment> getComments() {
        return comments;
    }
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}

//User 엔티티는 사용자 정보를 저장하며, 이메일, 사용자명, 암호를 필드로 가진다
//사용자가 작성한 게시글과 댓글과의 일대다 관계를 설정하여, 회원 탈퇴 시 관련 데이터도 함께 처리할 수 있도록 구성된다