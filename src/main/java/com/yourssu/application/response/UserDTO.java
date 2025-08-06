package com.yourssu.application.response;

public class UserDTO {
        private String email;
        private String username;
        // password 등 민감 정보는 포함하지 않음

    // 생성자: 이메일과 사용자명을 포함
        public UserDTO(String email, String username) {
            this.email = email;
            this.username = username;
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
}

//UserDTO는 회원가입이나 사용자 정보 조회 시 클라이언트에 노출할 사용자 데이터를 정의한다
//비밀번호 등 민감정보는 포함하지 않는다