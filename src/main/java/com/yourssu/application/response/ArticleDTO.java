package com.yourssu.application.response;

import com.yourssu.application.entity.Article;

public class ArticleDTO {
    private Long articleId;
    private String email;
    private String title;
    private String content;

    public ArticleDTO() {
    }

    // Article 엔티티에서 필요한 필드만 추출하여 DTO에 매핑
    public ArticleDTO(Article article) {
        this.articleId = article.getId();
        // Article 엔티티의 연관된 User 엔티티에서 email을 가져옴
        this.email = article.getUser().getEmail();
        this.title = article.getTitle();
        this.content = article.getContent();
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

//ArticleDTO는 클라이언트에 노출할 게시글 데이터를 정의한다
//게시글 ID, 작성자 이메일, 제목, 내용을 포함하며 민감정보는 배제한다


