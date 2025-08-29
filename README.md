# Spring Boot Blog API

JWT 인증 시스템과 현대적 DevOps 환경을 적용한 RESTful 블로그 API입니다.

## 🛠 기술 스택
- **Framework**: Spring Boot 3.4.3, Spring Security
- **Authentication**: JWT (Access + Refresh Token)
- **Database**: H2 Database, Redis (토큰 저장)
- **Migration**: Flyway Database Migration
- **Language**: Java 21
- **Documentation**: Swagger UI
- **Containerization**: Docker, Docker Compose
- **CI/CD**: GitHub Actions
- **Monitoring**: Spring Actuator, Prometheus Metrics
- **Testing**: JUnit 5, JaCoCo (70% 커버리지 목표)

## 🚀 빠른 시작

### 🐳 Docker로 실행 (권장)
```bash
git clone https://github.com/eunseo9311/SpringBoot-blog.git
cd SpringBoot-blog

# Docker Compose로 전체 환경 실행
docker-compose up -d

# 또는 편의 스크립트 사용
./scripts/docker-dev.sh up -d
```

### 🛠 로컬 개발 환경
#### 필수 요구사항
- Java 21+
- Redis Server
- Docker (선택사항)

#### 실행 방법
```bash
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
- **Flyway**: 데이터베이스 스키마 버전 관리

## 🔧 개발 도구

### Docker 환경 관리
```bash
# 개발 환경 시작
./scripts/docker-dev.sh up

# 환경 재빌드
./scripts/docker-dev.sh rebuild

# 로그 확인
./scripts/docker-dev.sh logs

# 환경 정리
./scripts/docker-dev.sh clean
```

### 테스트 및 빌드
```bash
# 단위 테스트 실행
./gradlew test

# 커버리지 리포트 생성
./gradlew jacocoTestReport

# Docker 환경 통합 테스트
./scripts/test-docker.sh

# 빌드
./gradlew build
```

## 📊 모니터링 및 관리

### Spring Actuator 엔드포인트
- **Health Check**: http://localhost:8080/actuator/health
- **Application Info**: http://localhost:8080/actuator/info  
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus

### H2 Database Console
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:file:./data/testdb`
- **Username**: `sa`
- **Password**: (비어있음)

## 🏗 아키텍처

### Phase 1: 운영 품질 기반 다지기 ✅
- [x] 코드 품질 개선 (Lombok, Exception Handling, Security)
- [x] 표준화된 API 응답 구조
- [x] Flyway 데이터베이스 마이그레이션
- [x] Docker 컨테이너화
- [x] CI/CD 파이프라인
- [x] 테스트 자동화 및 커버리지

### Phase 2: 핵심 기능 개발 (예정)
- [x] 북마크/좋아요 기능
- [ ] 재댓글 시스템
- [ ] 파일 업로드
- [ ] 검색 기능

## 📝 추가 문서
- **Docker 가이드**: [DOCKER_GUIDE.md](DOCKER_GUIDE.md)
- **Phase 1 개선 리포트**: [PHASE1_CODE_IMPROVEMENTS.md](PHASE1_CODE_IMPROVEMENTS.md)
