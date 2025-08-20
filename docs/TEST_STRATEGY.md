# 테스트 전략

## 개요
SpringBoot 블로그 애플리케이션의 체계적인 테스트 전략을 정의합니다.

## 테스트 피라미드

### 1. 단위 테스트 (Unit Tests) - 70%
**목적**: 개별 컴포넌트의 비즈니스 로직 검증

**대상**:
- Service 클래스의 비즈니스 로직
- Utility 클래스
- Security 관련 컴포넌트 (JWT, 인증 필터)

**도구**:
- JUnit 5
- Mockito (모킹)
- AssertJ (Assertion)

**예시**:
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AuthService authService;
}
```

### 2. 통합 테스트 (Integration Tests) - 20%
**목적**: 컴포넌트 간 상호작용 및 외부 시스템 연동 검증

**대상**:
- Repository 계층 (JPA 쿼리)
- Redis 연동
- 전체 Authentication Flow

**도구**:
- @SpringBootTest
- Testcontainers (MySQL, Redis)
- @DataJpaTest

**예시**:
```java
@SpringBootTest
@Testcontainers
class AuthServiceIntegrationTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");
}
```

### 3. E2E 테스트 (End-to-End Tests) - 10%
**목적**: 전체 애플리케이션 워크플로우 검증

**대상**:
- 회원가입 → 로그인 → 게시글 작성 → 댓글 작성 플로우
- API 엔드포인트 전체 시나리오

**도구**:
- @SpringBootTest(webEnvironment = RANDOM_PORT)
- TestRestTemplate
- Testcontainers

## 테스트 범위 목표
- **코드 커버리지**: 80% 이상
- **라인 커버리지**: Service 계층 90% 이상
- **브랜치 커버리지**: 핵심 비즈니스 로직 85% 이상

## 테스트 명명 규칙
```
[테스트 대상]_[상황]_[예상결과]

예시:
- signup_WithDuplicateEmail_ThrowsException
- login_WithValidCredentials_ReturnsTokens
- createArticle_WithoutAuthentication_ThrowsAuthException
```

## CI/CD 테스트 전략
1. **PR 검증**: 모든 테스트 통과 필수
2. **커버리지 체크**: 신규 코드 80% 이상
3. **성능 테스트**: 주요 API 응답시간 모니터링

## 테스트 데이터 관리
- **단위 테스트**: Builder 패턴 활용한 테스트 데이터 생성
- **통합 테스트**: @Sql 또는 @Transactional + @Rollback
- **E2E 테스트**: Testcontainers로 격리된 환경