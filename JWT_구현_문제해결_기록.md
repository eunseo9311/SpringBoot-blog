# JWT 인증 시스템 구현 시 발생한 문제들과 해결 과정

## 개요
Spring Boot 블로그 애플리케이션에 JWT 기반 인증 시스템을 구현하면서 발생한 주요 문제들과 해결 과정을 기록합니다.

---

## 1. Redis 의존성 문제

### 🔴 문제 상황
```
RedisSystemException: Error in execution; nested exception is 
io.lettuce.core.RedisConnectionException: Unable to connect to Redis
```

**발생 원인:**
- JWT 토큰 저장을 위해 Redis를 사용하려 했으나 Redis 서버가 실행되지 않음
- 로컬 개발 환경에서 Redis 설정 복잡성

**문제가 된 코드들:**

1. **RefreshTokenService.java** (초기 Redis 기반)
```java
@Service
public class RefreshTokenService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    public RefreshTokenService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public void saveRefreshToken(String token, String email, long ttlSeconds) {
        redisTemplate.opsForValue().set(token, email, ttlSeconds, TimeUnit.SECONDS);
    }
}
```

2. **TokenBlacklistService.java** (초기 Redis 기반)
```java
@Service
public class TokenBlacklistService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    public void blacklistToken(String token, long expirationTimeMs) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", ttlSeconds, TimeUnit.SECONDS);
    }
}
```

3. **RateLimitService.java** (초기 Redis 기반)
```java
@Service
public class RateLimitService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    public boolean isAllowed(String identifier, int maxAttempts, int windowSeconds) {
        String currentCount = (String) redisTemplate.opsForValue().get(key);
        // Redis 연산들...
    }
}
```

### 🔧 시도한 해결 방법들

1. **Redis 서버 설치 및 실행**
```bash
brew install redis
brew services start redis
redis-cli ping
```
- 여전히 연결 오류 발생

2. **RedisTemplate 설정 변경**
```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }
}
```
- 직렬화 설정 변경했으나 근본적인 연결 문제 해결되지 않음

3. **RefreshToken 엔티티 구조 변경**
```java
@RedisHash("refreshToken")
public class RefreshToken {
    @Id
    private String token;  // email에서 token으로 변경
    private String email;
    @TimeToLive
    private Long ttl;
}
```
- ID 필드를 email에서 token으로 변경했으나 여전히 Redis 연결 문제

### ✅ 최종 해결 방식

**메모리 기반 저장소로 완전 교체**

1. **RefreshTokenService.java** (최종 버전)
```java
@Service
public class RefreshTokenService {
    // 임시로 메모리 기반 저장소 사용 (프로덕션에서는 Redis 사용 권장)
    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();
    
    public void saveRefreshToken(String token, String email, long ttlSeconds) {
        refreshTokenStore.put(token, email);
    }
    
    public String getRefreshTokenEmail(String token) {
        return refreshTokenStore.get(token);
    }
    
    public void deleteRefreshToken(String token) {
        refreshTokenStore.remove(token);
    }
}
```

2. **TokenBlacklistService.java** (최종 버전)
```java
@Service
public class TokenBlacklistService {
    // 임시로 메모리 기반 저장소 사용
    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    
    public void blacklistToken(String token, long expirationTimeMs) {
        if (expirationTimeMs > System.currentTimeMillis()) {
            blacklistedTokens.put(token, expirationTimeMs);
        }
    }
    
    public boolean isTokenBlacklisted(String token) {
        Long expirationTime = blacklistedTokens.get(token);
        if (expirationTime == null) return false;
        
        // 만료된 토큰은 자동으로 제거
        if (expirationTime <= System.currentTimeMillis()) {
            blacklistedTokens.remove(token);
            return false;
        }
        return true;
    }
}
```

3. **RateLimitService.java** (최종 버전)
```java
@Service
public class RateLimitService {
    private final Map<String, RateLimitEntry> rateLimitMap = new ConcurrentHashMap<>();
    
    public boolean isAllowed(String identifier, int maxAttempts, int windowSeconds) {
        long currentTime = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;
        
        RateLimitEntry entry = rateLimitMap.get(identifier);
        
        if (entry == null || (currentTime - entry.getStartTime()) > windowMs) {
            rateLimitMap.put(identifier, new RateLimitEntry(currentTime, 1));
            return true;
        }
        
        if (entry.getCount() >= maxAttempts) return false;
        
        entry.incrementCount();
        return true;
    }
    
    private static class RateLimitEntry {
        private final long startTime;
        private int count;
        // getter, setter, incrementCount() 메서드들...
    }
}
```

**추가 설정 변경:**
- `application.yml`에서 Redis 자동 구성 비활성화
- `RedisConfig.java` @Configuration 주석 처리
- `Application.java`에서 @EnableRedisRepositories 비활성화

---

## 2. Spring Data JPA/Redis Repository 충돌

### 🔴 문제 상황
```
Multiple Spring Data modules found, entering strict repository configuration mode
Could not safely identify store assignment for repository candidate interface
```

**발생 원인:**
- JPA Repository와 Redis Repository가 같은 패키지에 있어서 Spring Data가 구분하지 못함
- UserRepository, ArticleRepository 등이 Redis Repository로 잘못 인식됨

**문제가 된 패키지 구조:**
```
src/main/java/com/blog/application/repository/
├── UserRepository.java          (JPA)
├── ArticleRepository.java       (JPA) 
├── CommentRepository.java       (JPA)
└── RefreshTokenRepository.java  (Redis)
```

### 🔧 시도한 해결 방법들

1. **엔티티에 @Entity vs @RedisHash 명시적 지정**
- 각 엔티티에 명확한 어노테이션 추가했으나 패키지 충돌 지속

2. **Repository별 basePackages 지정**
```java
@EnableJpaRepositories(basePackages = "com.blog.application.repository")
@EnableRedisRepositories(basePackages = "com.blog.application.repository")
```
- 같은 패키지를 지정해서 여전히 충돌

### ✅ 최종 해결 방식

**패키지 분리 구조로 변경**

```
src/main/java/com/blog/application/repository/
├── jpa/
│   ├── UserRepository.java      
│   ├── ArticleRepository.java   
│   └── CommentRepository.java   
└── redis/
    └── RefreshTokenRepository.java
```

**Application.java 수정:**
```java
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.blog.application.repository.jpa")
@EnableRedisRepositories(basePackages = "com.blog.application.repository.redis")
```

---

## 3. Spring Security URL 패턴 불일치

### 🔴 문제 상황
```
DEBUG: Securing POST /api/auth/signup
DEBUG: Pre-authenticated entry point called. Rejecting access
HTTP 403 Forbidden
```

**발생 원인:**
- SecurityConfig에서 `/auth/**` 패턴 허용
- 실제 AuthController는 `/api/auth/**` 경로 사용
- URL 패턴 불일치로 인증 없이 접근 거부

**문제가 된 코드:**

**SecurityConfig.java:**
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**").permitAll()  // ❌ 잘못된 패턴
    .anyRequest().authenticated()
)
```

**AuthController.java:**
```java
@RestController
@RequestMapping("/auth")  // ❌ /api 누락
public class AuthController {
    @PostMapping("/signup")  // 실제 경로: /auth/signup
}
```

하지만 클라이언트는 `/api/auth/signup`으로 요청

### 🔧 시도한 해결 방법들

1. **SecurityConfig 패턴만 수정**
```java
.requestMatchers("/api/auth/**").permitAll()
```
- AuthController 경로는 여전히 `/auth`여서 매핑되지 않음

2. **AuthController 경로만 수정** 
```java
@RequestMapping("/api/auth")
```

### ✅ 최종 해결 방식

**두 설정 모두 일치하도록 수정**

**SecurityConfig.java:**
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()  // ✅ 올바른 패턴
    .requestMatchers("/h2-console/**").permitAll()
    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
    .anyRequest().authenticated()
)
```

**AuthController.java:**
```java
@RestController
@RequestMapping("/api/auth")  // ✅ 올바른 경로
public class AuthController {
    @PostMapping("/signup")  // 최종 경로: /api/auth/signup
}
```

---

## 4. JJWT 라이브러리 API 변경

### 🔴 문제 상황
```
java.lang.NoSuchMethodError: io.jsonwebtoken.JwtParserBuilder.build()
```

**발생 원인:**
- JJWT 0.12.x 버전에서 API가 변경됨
- 기존 `parserBuilder()` 메서드가 deprecated

**문제가 된 코드:**
```java
// ❌ 구버전 API
Jwts.parserBuilder()
    .setSigningKey(secretKey)
    .build()
    .parseClaimsJws(token);
```

### ✅ 해결 방식

**신버전 API로 변경:**
```java
// ✅ 신버전 API
Jwts.parser()
    .verifyWith(secretKey)
    .build()
    .parseSignedClaims(token);
```

---

## 5. Spring Security 설정 Deprecation

### 🔴 문제 상황
```
The method frameOptions() is deprecated
```

**문제가 된 코드:**
```java
// ❌ Deprecated API
.headers().frameOptions().disable()
```

### ✅ 해결 방식

**Lambda 기반 설정으로 변경:**
```java
// ✅ 최신 API
.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
```

---

## 6. Bean 의존성 주입 실패

### 🔴 문제 상황
```
Parameter 0 of constructor in RateLimitService required a bean of type 
'org.springframework.data.redis.core.RedisTemplate' that could not be found.
```

**발생 원인:**
- Redis 자동 구성을 비활성화했지만 RedisTemplate을 요구하는 서비스들이 여전히 존재
- RateLimitService가 RedisTemplate 주입을 요구

**문제가 된 설정:**
```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
      - org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
```

### ✅ 해결 방식

1. **RedisConfig 비활성화:**
```java
//@Configuration  // Redis 사용하지 않으므로 비활성화
public class RedisConfig {
    // @Bean RedisTemplate 메서드들...
}
```

2. **모든 Redis 의존 서비스를 메모리 기반으로 변경**

---

## 7. 테스트 Import 오류

### 🔴 문제 상황
```
cannot find symbol: class UserRepository
symbol: class UserRepository  
location: class UserRepositoryTest
```

**발생 원인:**
- 패키지 구조 변경으로 Repository 경로가 바뀜
- 테스트 파일들의 import 경로가 업데이트되지 않음

**문제가 된 Import:**
```java
// ❌ 구 경로
import com.blog.application.repository.UserRepository;

// ✅ 신 경로  
import com.blog.application.repository.jpa.UserRepository;
```

### 🔧 임시 해결

테스트 제외하고 실행:
```bash
./gradlew bootRun -x test
```

---

## 최종 아키텍처

### 패키지 구조
```
src/main/java/com/blog/application/
├── controller/
│   └── AuthController.java          (@RequestMapping("/api/auth"))
├── service/
│   ├── AuthService.java
│   ├── RefreshTokenService.java     (ConcurrentHashMap 기반)
│   ├── TokenBlacklistService.java   (ConcurrentHashMap 기반)
│   └── RateLimitService.java        (ConcurrentHashMap 기반)
├── repository/
│   ├── jpa/                         (JPA Repositories)
│   │   ├── UserRepository.java
│   │   ├── ArticleRepository.java
│   │   └── CommentRepository.java
│   └── redis/                       (Redis Repositories - 미사용)
│       └── RefreshTokenRepository.java
├── config/
│   ├── SecurityConfig.java          (URL 패턴: /api/auth/**)
│   └── RedisConfig.java             (비활성화)
└── security/
    ├── JwtUtil.java                 (JJWT 0.12.x API)
    └── JwtAuthenticationFilter.java
```

### 주요 설정
```yaml
# application.yml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
      - org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
```

```java
// Application.java
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.blog.application.repository.jpa")
//@EnableRedisRepositories(basePackages = "com.blog.application.repository.redis")
```

---

## 핵심 교훈

1. **로컬 개발 환경의 복잡성**: Redis 같은 외부 의존성은 로컬 개발에서 복잡성을 증가시킴
2. **패키지 분리의 중요성**: Spring Data에서 여러 저장소 타입 사용 시 명확한 패키지 분리 필요
3. **URL 패턴 일치**: Controller 경로와 Security 설정의 URL 패턴이 정확히 일치해야 함
4. **라이브러리 버전 호환성**: 최신 버전 라이브러리 사용 시 API 변경사항 확인 필요
5. **의존성 주입 체인**: 하나의 서비스가 Redis에 의존하면 연쇄적으로 다른 서비스들도 영향받음

---

## 최종 API 테스트 결과

✅ **회원가입**: `POST /api/auth/signup` → `{"userId":19}`
✅ **로그인**: `POST /api/auth/login` → JWT Access Token + Refresh Token 발급
✅ **토큰 갱신**: `POST /api/auth/refresh` → 새로운 토큰 쌍 발급

모든 기본 인증 기능이 정상 동작합니다.