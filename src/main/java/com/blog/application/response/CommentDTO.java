package com.blog.application.response;

public class CommentDTO {
    private Long commentId;
    private String email;
    private String content;
    // password 등 민감 정보는 포함하지 않음

    // 생성자: 댓글 ID, 작성자 이메일, 내용
    public CommentDTO(Long commentId, String email, String content) {
        this.commentId = commentId;
        this.email = email;
        this.content = content;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

//CommentDTO는 클라이언트에 반환할 댓글 정보를 정의한다
//댓글 ID, 작성자 이메일, 내용을 포함하여 민감정보는 제외한다