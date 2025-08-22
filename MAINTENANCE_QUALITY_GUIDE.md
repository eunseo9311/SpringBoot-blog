# 유지보수 및 품질개선 가이드

Spring Boot 애플리케이션의 운영 품질을 위한 핵심 기술과 구현 방법을 정리한 가이드입니다.

---

## 1. 전역 에러 스펙 + 컨트롤러 어드바이스

### 🎯 목표
- 일관된 API 응답 형태 제공
- 예외 처리 중앙화
- 클라이언트 친화적 에러 메시지

### 🏗 핵심 구조

#### 1.1 표준 API 응답 구조
```java
// ApiResponse.java - 모든 API 응답의 표준 래퍼
public class ApiResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;
    
    // 성공 응답
    public static <T> ApiResponse<T> success(SuccessStatus status, T data) {
        return new ApiResponse<>(true, status.getCode(), status.getMessage(), data);
    }
    
    // 에러 응답
    public static <T> ApiResponse<T> error(ErrorStatus status, T data) {
        return new ApiResponse<>(false, status.getCode(), status.getMessage(), data);
    }
}
```

#### 1.2 상태 코드 표준화
```java
// SuccessStatus.java - 성공 상태 코드 관리
public enum SuccessStatus {
    SIGNUP_SUCCESS(HttpStatus.CREATED, "A001", "회원가입이 성공적으로 완료되었습니다."),
    LOGIN_SUCCESS(HttpStatus.OK, "A002", "로그인이 성공적으로 완료되었습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "A003", "로그아웃이 성공적으로 완료되었습니다."),
    TOKEN_REFRESH_SUCCESS(HttpStatus.OK, "A004", "토큰 갱신이 성공적으로 완료되었습니다.");
    
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

// ErrorStatus.java - 에러 상태 코드 관리
public enum ErrorStatus {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E000", "서버 내부 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "E001", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E401", "인증이 필요합니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "E200", "존재하지 않는 사용자입니다.");
}
```

#### 1.3 전역 예외 처리
```java
// GlobalExceptionHandler.java - 중앙 집중식 예외 처리
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthException(
            AuthException e, HttpServletRequest request) {
        return ResponseEntity
            .status(ErrorStatus.UNAUTHORIZED.getHttpStatus())
            .body(ApiResponse.error(ErrorStatus.UNAUTHORIZED, e.getMessage()));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ErrorResponse.FieldError>>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ErrorResponse.FieldError(
                error.getField(),
                error.getRejectedValue(),
                error.getDefaultMessage()
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity
            .status(ErrorStatus.VALIDATION_ERROR.getHttpStatus())
            .body(ApiResponse.error(ErrorStatus.VALIDATION_ERROR, fieldErrors));
    }
}
```

### 💡 핵심 포인트
- **관심사 분리**: 컨트롤러에서 예외 처리 로직 제거
- **일관성**: 모든 API가 동일한 응답 구조 사용
- **확장성**: 새로운 에러 타입 추가 시 enum만 수정

---

## 2. 로그/추적 + Actuator

### 🎯 목표
- 요청별 추적 가능한 로그 체계
- 비즈니스 이벤트 기록
- 애플리케이션 헬스 모니터링

### 🏗 핵심 구조

#### 2.1 요청 ID 기반 로그 추적
```java
// RequestIdFilter.java - 모든 요청에 고유 ID 부여
@Component
public class RequestIdFilter implements Filter {
    
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 클라이언트에서 제공한 Request ID 또는 새로 생성
        String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }
        
        // MDC에 Request ID 설정 (모든 로그에 포함됨)
        MDC.put("requestId", requestId);
        httpResponse.setHeader(REQUEST_ID_HEADER, requestId);
        
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

#### 2.2 비즈니스 이벤트 로깅
```java
// EventLogService.java - 중요 비즈니스 이벤트 기록
@Service
@RequiredArgsConstructor
public class EventLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventLogService.class);
    
    public void logSignupEvent(String email, boolean success) {
        if (success) {
            logger.info("User signup successful - email: {}", email);
        } else {
            logger.warn("User signup failed - email: {}", email);
        }
    }
    
    public void logLoginEvent(String email, boolean success, String reason) {
        if (success) {
            logger.info("User login successful - email: {}", email);
        } else {
            logger.warn("User login failed - email: {}, reason: {}", email, reason);
        }
    }
    
    public void logTokenRefreshEvent(String email, boolean success) {
        if (success) {
            logger.info("Token refresh successful - email: {}", email);
        } else {
            logger.warn("Token refresh failed - email: {}", email);
        }
    }
}
```

#### 2.3 로그 패턴 설정
```yaml
# application.yml - 구조화된 로그 포맷
logging:
  level:
    com.blog.application: DEBUG
    org.springframework.security: WARN
    root: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{requestId}] %logger{36} - %msg%n"
```

#### 2.4 Actuator 헬스체크 설정
```yaml
# application.yml - 상세 헬스체크 구성
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      show-components: always
  health:
    redis:
      enabled: true
    db:
      enabled: true
  info:
    env:
      enabled: true
```

### 💡 핵심 포인트
- **추적성**: Request ID로 전체 요청 흐름 추적
- **가시성**: 비즈니스 로직의 중요 시점 기록
- **모니터링**: Actuator로 실시간 시스템 상태 확인

---

## 3. DB 마이그레이션(Flyway) + 인덱스 초안

### 🎯 목표
- 버전 관리되는 데이터베이스 스키마
- 안전한 스키마 변경 프로세스
- 성능 최적화를 위한 인덱스 관리

### 🏗 핵심 구조

#### 3.1 Flyway 설정
```yaml
# application.yml - Flyway 마이그레이션 설정
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # 운영 환경 안전 설정
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    sql-migration-suffixes: .sql
```

#### 3.2 초기 스키마 마이그레이션
```sql
-- V1__Create_initial_schema.sql
-- 사용자 테이블 (예약어 user를 위해 따옴표 사용)
CREATE TABLE "user" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- 게시글 테이블
CREATE TABLE "article" (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500),
    content TEXT,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_article_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

-- 댓글 테이블  
CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(1000),
    user_id BIGINT,
    article_id BIGINT,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES "user"(id),
    CONSTRAINT fk_comment_article FOREIGN KEY (article_id) REFERENCES "article"(id)
);
```

#### 3.3 성능 최적화 인덱스
```sql
-- V2__Add_performance_indexes.sql
-- 사용자 이메일 조회 최적화 (로그인 시 사용)
CREATE INDEX idx_user_email ON "user"(email);

-- 게시글 조회 최적화
CREATE INDEX idx_article_user_id ON "article"(user_id);
CREATE INDEX idx_article_title ON "article"(title);

-- 댓글 조회 최적화  
CREATE INDEX idx_comment_article_id ON comment(article_id);
CREATE INDEX idx_comment_user_id ON comment(user_id);
```

#### 3.4 마이그레이션 베스트 프랙티스
```sql
-- 예시: V3__Add_article_status.sql
-- 1. 새 컬럼 추가 (NULL 허용)
ALTER TABLE "article" ADD COLUMN status VARCHAR(20);

-- 2. 기본값 설정
UPDATE "article" SET status = 'PUBLISHED' WHERE status IS NULL;

-- 3. NOT NULL 제약 조건 추가
ALTER TABLE "article" ALTER COLUMN status SET NOT NULL;

-- 4. 인덱스 추가 (필요시)
CREATE INDEX idx_article_status ON "article"(status);
```

### 💡 핵심 포인트
- **버전 관리**: 모든 스키마 변경이 git과 함께 관리
- **안전성**: validate 모드로 의도치 않은 변경 방지
- **성능**: 쿼리 패턴에 맞는 인덱스 사전 설계

---

## 4. 테스트 전략 + Testcontainers

### 🎯 목표
- 실제 데이터베이스를 사용한 통합 테스트
- 격리된 테스트 환경
- CI/CD에서 일관된 테스트 실행

### 🏗 핵심 구조

#### 4.1 Testcontainers 의존성 설정
```gradle
// build.gradle - 테스트 컨테이너 의존성
dependencies {
    testImplementation 'org.springframework.boot:spring-boot-testcontainers'
    testImplementation 'org.testcontainers:junit-jupiter'
    testImplementation 'org.testcontainers:mysql'
    testImplementation 'org.testcontainers:redis'
}
```

#### 4.2 통합 테스트 기반 클래스
```java
// AbstractIntegrationTest.java - 통합 테스트 기반 설정
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
public abstract class AbstractIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("blogdb")
            .withUsername("test")
            .withPassword("test");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }
}
```

#### 4.3 레이어별 테스트 전략
```java
// Repository 레이어 테스트
@DataJpaTest
class UserRepositoryTest extends AbstractIntegrationTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void shouldFindUserByEmail() {
        // given
        User user = new User("test@example.com", "testuser", "password");
        entityManager.persistAndFlush(user);
        
        // when
        Optional<User> found = userRepository.findByEmail("test@example.com");
        
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }
}

// Service 레이어 테스트
@SpringBootTest
@Transactional
class AuthServiceTest extends AbstractIntegrationTest {
    
    @Autowired
    private AuthService authService;
    
    @Test
    void shouldSignupNewUser() {
        // given
        SignupRequestDTO request = new SignupRequestDTO(
            "newuser@example.com", "newuser", "password123"
        );
        
        // when
        SignupResponseDTO response = authService.signup(request);
        
        // then
        assertThat(response.getUserId()).isNotNull();
    }
}

// Controller 레이어 테스트
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest extends AbstractIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldSignupSuccessfully() {
        // given
        SignupRequestDTO request = new SignupRequestDTO(
            "test@example.com", "testuser", "password123"
        );
        
        // when
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            "/api/auth/signup", request, ApiResponse.class
        );
        
        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isSuccess()).isTrue();
    }
}
```

#### 4.4 테스트 프로파일 설정
```yaml
# application-test.yml - 테스트 전용 설정
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  flyway:
    enabled: false  # 테스트에서는 Hibernate가 스키마 생성
    
jwt:
  secret: test-secret-key-for-testing-purposes-must-be-long-enough
  access-token-validity-ms: 3600000
  
logging:
  level:
    com.blog.application: DEBUG
    org.testcontainers: INFO
```

### 💡 핵심 포인트
- **실제 환경**: 진짜 DB/Redis로 테스트하여 신뢰성 확보
- **격리**: 각 테스트가 독립적인 컨테이너 환경에서 실행
- **CI 호환**: GitHub Actions에서도 동일하게 실행

---

## 5. CI(Gradle test + 정적분석) + Docker Compose

### 🎯 목표
- 자동화된 빌드 및 테스트 파이프라인
- 코드 품질 자동 검증
- 개발 환경 일관성 확보

### 🏗 핵심 구조

#### 5.1 GitHub Actions CI 파이프라인
```yaml
# .github/workflows/ci.yml - 완전한 CI 파이프라인
name: CI Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'corretto'

    - name: Cache Gradle packages
      uses: actions/cache@v4
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}

    - name: Run tests
      run: ./gradlew test
      env:
        SPRING_PROFILES_ACTIVE: test

    - name: Run code quality checks
      run: ./gradlew check

    - name: Upload coverage reports
      uses: actions/upload-artifact@v4
      if: always()
      with:
        name: coverage-reports
        path: build/reports/jacoco/
```

#### 5.2 Gradle 빌드 최적화
```gradle
// build.gradle - 테스트 및 정적 분석 설정
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.4.3'
    id 'io.spring.dependency-management' version '1.1.7'
    id 'jacoco'  // 코드 커버리지
}

tasks.named('test') {
    useJUnitPlatform()
    finalizedBy jacocoTestReport
    
    // 테스트 최적화
    systemProperty 'spring.profiles.active', 'test'
    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
    }
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
    finalizedBy jacocoTestCoverageVerification
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.70  // 70% 커버리지 목표
            }
        }
        rule {
            element = 'CLASS'
            excludes = [
                '*.Application',
                '*.config.*',
                '*.entity.*',
                '*.dto.*'
            ]
            limit {
                counter = 'LINE'
                value = 'COVEREDRATIO'
                minimum = 0.60
            }
        }
    }
}
```

#### 5.3 Docker Compose 개발 환경
```yaml
# docker-compose.yml - 완전한 개발 환경
version: '3.8'

services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: springboot-blog-app
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_REDIS_HOST=redis
      - MYSQL_HOST=mysql
      - MYSQL_DATABASE=blogdb
      - MYSQL_USER=blog_user
      - MYSQL_PASSWORD=blog_password
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - blog-network

  mysql:
    image: mysql:8.0
    container_name: springboot-blog-mysql
    environment:
      - MYSQL_ROOT_PASSWORD=rootpassword
      - MYSQL_DATABASE=blogdb
      - MYSQL_USER=blog_user
      - MYSQL_PASSWORD=blog_password
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - blog-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: springboot-blog-redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis-data:/data
    networks:
      - blog-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql-data:
  redis-data:

networks:
  blog-network:
    driver: bridge
```

#### 5.4 환경별 설정 관리
```bash
# .env.example - 환경 변수 템플릿
# Database
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=blogdb
MYSQL_USER=blog_user
MYSQL_PASSWORD=your_secure_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT
JWT_SECRET=your-super-secret-jwt-key-must-be-at-least-256-bits-long

# Application
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8080

# Monitoring
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
```

#### 5.5 개발 편의 스크립트
```bash
# scripts/docker-dev.sh - 개발 환경 관리 자동화
#!/bin/bash

case $1 in
    up)
        echo "🚀 개발 환경 시작..."
        docker-compose up -d
        echo "✅ 환경 시작 완료"
        echo "📱 애플리케이션: http://localhost:8080"
        echo "🗄️ MySQL: localhost:3306"
        echo "🔴 Redis: localhost:6379"
        ;;
    down)
        echo "🛑 개발 환경 중지..."
        docker-compose down
        ;;
    rebuild)
        echo "🔨 애플리케이션 재빌드..."
        docker-compose down
        docker-compose build --no-cache app
        docker-compose up -d
        ;;
    logs)
        docker-compose logs -f ${2:-app}
        ;;
    test)
        echo "🧪 통합 테스트 실행..."
        docker-compose up -d mysql redis
        sleep 10
        ./gradlew test
        ;;
    clean)
        echo "🧹 환경 완전 정리..."
        docker-compose down -v --rmi all
        docker system prune -f
        ;;
esac
```

### 💡 핵심 포인트
- **자동화**: 커밋할 때마다 자동으로 테스트/빌드 실행
- **일관성**: 로컬/CI/운영 환경에서 동일한 Docker 이미지 사용
- **편의성**: 스크립트로 복잡한 환경 설정을 단순화

---

## 🎯 종합 적용 가이드

### 개발 워크플로우
1. **로컬 개발**: `./scripts/docker-dev.sh up`로 환경 시작
2. **코드 작성**: 표준 응답/에러 구조 활용
3. **테스트 작성**: Testcontainers로 통합 테스트
4. **커밋**: GitHub Actions가 자동으로 검증
5. **배포**: Docker 이미지로 일관된 배포

### 품질 지표
- **테스트 커버리지**: 70% 이상 유지
- **응답 시간**: 모든 API 200ms 이하
- **로그 추적**: 모든 요청이 Request ID로 추적 가능
- **DB 마이그레이션**: 모든 스키마 변경이 버전 관리됨

### 모니터링 체크리스트
- [ ] `/actuator/health`로 서비스 상태 확인
- [ ] 로그에서 Request ID 추적 가능한지 확인
- [ ] Flyway 마이그레이션 히스토리 검토
- [ ] 테스트 커버리지 리포트 확인
- [ ] CI/CD 파이프라인 정상 동작 확인

이 가이드를 따라 구현하면 **운영에 안전하고 유지보수가 쉬운 고품질 애플리케이션**을 만들 수 있습니다.