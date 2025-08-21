# 멀티 스테이지 빌드를 사용한 최적화된 Docker 이미지

# Build stage
FROM amazoncorretto:21-alpine AS builder

WORKDIR /app

# Gradle wrapper와 설정 파일만 먼저 복사 (캐시 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 종속성 다운로드 (별도 레이어로 캐시)
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM amazoncorretto:21-alpine

# 애플리케이션 실행에 필요한 패키지 설치
RUN apk add --no-cache \
    curl \
    && addgroup -g 1001 -S spring \
    && adduser -S spring -G spring -u 1001

# 애플리케이션 디렉토리 생성
WORKDIR /app

# 빌드된 JAR 파일을 runtime 이미지로 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 파일 소유권 변경
RUN chown spring:spring /app/app.jar

# 비루트 사용자로 실행
USER spring

# 포트 노출
EXPOSE 8080

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM 최적화 옵션과 함께 애플리케이션 실행
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]