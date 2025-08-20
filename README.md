# Spring Boot Blog API

JWT 인증 시스템을 적용한 RESTful 블로그 API입니다.

## 🛠 기술 스택
- **Framework**: Spring Boot 3.4.3, Spring Security
- **Authentication**: JWT (Access + Refresh Token)
- **Database**: H2 Database, Redis (토큰 저장)
- **Language**: Java 21
- **Documentation**: Swagger UI

## 🚀 빠른 시작

### 필수 요구사항
- Java 21+
- Redis Server

### 실행 방법
```bash
git clone https://github.com/eunseo9311/SpringBoot-blog.git
cd SpringBoot-blog

# Redis 시작
brew install redis && brew services start redis

# 애플리케이션 실행
./gradlew bootRun
```

## 📚 API 문서
**📖 [Swagger UI로 API 테스트하기](https://eunseo9311.github.io/SpringBoot-blog/)**

## 🔐 JWT 인증 시스템

### 주요 특징
- **Access Token**: 1시간 유효 (API 요청 인증)
- **Refresh Token**: 2주 유효 (토큰 갱신)
- **Redis 저장**: 토큰 영구 보관 및 블랙리스트 관리
- **Rate Limiting**: IP당 분당 5회 제한

### 인증 플로우
1. **회원가입**: `POST /api/auth/signup`
2. **로그인**: `POST /api/auth/login` → JWT 토큰 발급
3. **API 호출**: `Authorization: Bearer {accessToken}`
4. **토큰 갱신**: `POST /api/auth/refresh`
5. **로그아웃**: `POST /api/auth/logout` → 토큰 무효화

## 🎯 주요 기능

### 🔓 공개 API (인증 불필요)
- **회원가입**: `POST /api/auth/signup`
- **로그인**: `POST /api/auth/login` → JWT 토큰 발급
- **게시글 조회**: `GET /articles`
- **댓글 조회**: `GET /articles/{id}/comments`

### 🔒 인증 필요 API
- **게시글 작성/수정/삭제**: `POST/PUT/DELETE /articles`
- **댓글 작성/수정/삭제**: `POST/PUT/DELETE /articles/{id}/comments`
- **회원 탈퇴**: `DELETE /users/{id}`

## 🗃 데이터 저장소
- **H2 Database**: 사용자, 게시글, 댓글 데이터
- **Redis**: JWT 토큰, 세션 관리, Rate Limiting