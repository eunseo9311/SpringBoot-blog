package com.yourssu.application.entity;

import jakarta.persistence.*;

@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    // 여러 댓글은 하나의 User에 속함 (다대일 관계)
    @ManyToOne
    private User user;

    // 여러 댓글은 하나의 Article에 속함 (다대일 관계)
    @ManyToOne
    private Article article;

    // getter
    public Long getId() {
        return id;
    }

    // setter
    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
}

//Comment 엔티티는 댓글 정보를 나타내며, 댓글의 내용과 댓글 작성자(User), 그리고 어느 게시글(Article)에 속하는지의 정보를 포함한다.
//다대일(Many-to-One) 관계를 통해 여러 댓글이 하나의 게시글 또는 사용자에 연결된다.
