# Docker 환경 가이드

Spring Boot 블로그 애플리케이션의 Docker 환경 구성 및 사용 가이드입니다.

## 🚀 빠른 시작

### 1. 기본 환경 실행
```bash
# 애플리케이션과 Redis 시작
docker-compose up -d

# 또는 편의 스크립트 사용
./scripts/docker-dev.sh up -d
```

### 2. PostgreSQL과 함께 실행
```bash
# PostgreSQL 포함하여 실행
docker-compose --profile postgres up -d

# 또는 편의 스크립트 사용
./scripts/docker-dev.sh postgres -d
```

## 📋 사용 가능한 명령어

### Docker Compose 기본 명령어
```bash
# 서비스 시작 (포그라운드)
docker-compose up

# 서비스 시작 (백그라운드)
docker-compose up -d

# 서비스 중지
docker-compose down

# 이미지 재빌드 후 시작
docker-compose up --build

# 볼륨까지 삭제하며 완전 정리
docker-compose down -v
```

### 편의 스크립트 명령어
```bash
# 도움말 확인
./scripts/docker-dev.sh help

# 서비스 시작
./scripts/docker-dev.sh up

# 서비스 중지
./scripts/docker-dev.sh down

# 애플리케이션 재빌드
./scripts/docker-dev.sh rebuild

# 로그 확인
./scripts/docker-dev.sh logs

# 서비스 상태 확인
./scripts/docker-dev.sh status

# 환경 완전 정리
./scripts/docker-dev.sh clean
```

## 🔧 환경 구성

### 서비스 구성
- **app**: Spring Boot 애플리케이션 (포트: 8080)
- **redis**: Redis 캐시 서버 (포트: 6379)
- **postgres**: PostgreSQL 데이터베이스 (포트: 5432, 선택사항)

### 환경 변수
| 변수명 | 기본값 | 설명 |
|--------|--------|------|
| `SPRING_PROFILES_ACTIVE` | docker | Spring 프로파일 |
| `SPRING_REDIS_HOST` | redis | Redis 호스트 |
| `SPRING_REDIS_PORT` | 6379 | Redis 포트 |
| `JWT_SECRET` | [기본값] | JWT 시크릿 키 |
| `SPRING_DATASOURCE_URL` | H2 파일 DB | 데이터베이스 URL |

## 🧪 테스트

### 자동 테스트 실행
```bash
# Docker 환경 통합 테스트
./scripts/test-docker.sh
```

### 수동 테스트
```bash
# 헬스체크
curl http://localhost:8080/actuator/health

# API 문서
curl http://localhost:8080/swagger-ui/index.html

# 메트릭스
curl http://localhost:8080/actuator/metrics
```

## 📊 모니터링

### 헬스체크 엔드포인트
- **애플리케이션**: http://localhost:8080/actuator/health
- **Redis**: `docker exec springboot-blog-redis redis-cli ping`
- **PostgreSQL**: `docker exec springboot-blog-postgres pg_isready`

### 로그 확인
```bash
# 모든 서비스 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f app
docker-compose logs -f redis

# 실시간 로그 (마지막 100줄)
docker-compose logs -f --tail=100 app
```

## 🗄️ 데이터 영속성

### 볼륨 관리
```bash
# 볼륨 목록 확인
docker volume ls

# 볼륨 상세 정보
docker volume inspect springboot-blog_redis-data

# 볼륨 백업 (예시)
docker run --rm -v springboot-blog_redis-data:/data -v $(pwd):/backup alpine tar czf /backup/redis-backup.tar.gz /data
```

### 데이터베이스 초기화
```bash
# H2 데이터베이스 초기화
rm -rf ./data/blogdb*

# PostgreSQL 데이터베이스 초기화
docker-compose down -v
docker volume rm springboot-blog_postgres-data
```

## 🔧 트러블슈팅

### 일반적인 문제들

#### 1. 포트 충돌
```bash
# 포트 사용 중인 프로세스 확인
lsof -i :8080
lsof -i :6379

# 프로세스 종료
kill -9 <PID>
```

#### 2. 메모리 부족
```bash
# Docker 메모리 사용량 확인
docker stats

# 미사용 리소스 정리
docker system prune -a
```

#### 3. 이미지 빌드 실패
```bash
# 캐시 없이 재빌드
docker-compose build --no-cache

# Docker buildx 캐시 정리
docker builder prune
```

#### 4. 네트워크 문제
```bash
# 네트워크 상태 확인
docker network ls
docker network inspect springboot-blog_blog-network

# 네트워크 재생성
docker-compose down
docker network prune
docker-compose up
```

## 🚀 배포 가이드

### 운영 환경 설정
```bash
# 환경 변수 파일 생성
cp .env.example .env

# 운영용 시크릿 생성
openssl rand -base64 32

# 운영 모드로 실행
SPRING_PROFILES_ACTIVE=prod docker-compose up -d
```

### CI/CD 통합
GitHub Actions가 자동으로 다음을 수행합니다:
1. 코드 변경 감지
2. 테스트 실행
3. Docker 이미지 빌드
4. 테스트 결과 및 빌드 아티팩트 업로드

## 📚 추가 자료

- [Docker Compose 공식 문서](https://docs.docker.com/compose/)
- [Spring Boot Docker 가이드](https://spring.io/guides/gs/spring-boot-docker/)
- [Redis Docker 가이드](https://hub.docker.com/_/redis)