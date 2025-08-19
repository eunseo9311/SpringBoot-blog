package com.blog.application.response;

public class UserIdDTO {
    private String email;
    private String nickname;
    private Long id;
    // password 등 민감 정보는 포함하지 않음


    public UserIdDTO(String email, String nickname, Long id) {
        this.email = email;
        this.nickname = nickname;
        this.id = id;
    }

    // 생성자: 이메일, 닉네임, 사용자 ID 포함
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

//UserIdDTO는 사용자 목록 조회 시, 사용자 ID와 함께 이메일, 닉네임을 클라이언트에 반환하기 위한 DTO이다
