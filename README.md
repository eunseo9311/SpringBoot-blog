# Spring Boot Blog API

## 📝 프로젝트 개요
Spring Boot를 사용한 블로그 시스템입니다. 사용자 관리, 게시글 작성/수정/삭제, 댓글 기능을 제공합니다.

## 🛠 기술 스택
- **Framework**: Spring Boot 3.4.3
- **Language**: Java 21
- **Database**: H2 (In-memory)
- **ORM**: Spring Data JPA
- **Documentation**: Swagger (OpenAPI 3)
- **Build Tool**: Gradle

## 🚀 실행 방법

### 1. 프로젝트 클론
```bash
git clone https://github.com/eunseo9311/SpringBoot-blog.git
cd SpringBoot-blog
```

### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 3. 접속 확인
- **애플리케이션**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console

## 📚 API 문서
모든 API 엔드포인트는 Swagger UI에서 확인하고 테스트할 수 있습니다.

**Swagger UI**: http://localhost:8080/swagger-ui.html

## 🎯 주요 기능

### 👤 사용자 관리
- 회원가입 (비밀번호 암호화)
- 사용자 조회
- 회원탈퇴 (연관 데이터 자동 삭제)

### 📄 게시글 관리
- 게시글 작성/조회/수정/삭제
- 작성자 본인만 수정/삭제 가능
- 제목, 내용 유효성 검증

### 💬 댓글 관리
- 댓글 작성/조회/수정/삭제
- 댓글 작성자 본인만 수정/삭제 가능
- 게시글별 댓글 조회

## 🔐 인증 방식
이메일과 비밀번호 기반 인증을 사용합니다.

## 🗃 데이터베이스
- **타입**: H2 (In-memory)
- **접속 정보**: 
  - URL: `jdbc:h2:file:./data/testdb`
  - Username: `SA`
  - Password: (없음)