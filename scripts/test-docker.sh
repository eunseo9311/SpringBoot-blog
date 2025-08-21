#!/bin/bash

# Docker 환경 테스트 스크립트

set -e

PROJECT_NAME="springboot-blog"
CONTAINER_NAME="springboot-blog-app"

echo "🧪 Docker 환경 테스트 시작..."

# 서비스 시작
echo "🚀 서비스 시작 중..."
docker-compose -p $PROJECT_NAME up -d

# 헬스체크 대기
echo "⏳ 애플리케이션 시작 대기 중..."
timeout=60
while [ $timeout -gt 0 ]; do
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ 애플리케이션이 정상적으로 시작되었습니다!"
        break
    fi
    sleep 2
    timeout=$((timeout-2))
done

if [ $timeout -le 0 ]; then
    echo "❌ 애플리케이션 시작 실패 - 타임아웃"
    docker-compose -p $PROJECT_NAME logs app
    exit 1
fi

# API 테스트
echo "🔍 API 엔드포인트 테스트..."

# Health check
echo "- Health check..."
if ! curl -f http://localhost:8080/actuator/health; then
    echo "❌ Health check 실패"
    exit 1
fi

# Info endpoint
echo "- Info endpoint..."
if ! curl -f http://localhost:8080/actuator/info; then
    echo "❌ Info endpoint 실패"
    exit 1
fi

# Metrics endpoint
echo "- Metrics endpoint..."
if ! curl -f http://localhost:8080/actuator/metrics; then
    echo "❌ Metrics endpoint 실패"
    exit 1
fi

# Swagger UI
echo "- Swagger UI..."
if ! curl -f http://localhost:8080/swagger-ui/index.html; then
    echo "❌ Swagger UI 접근 실패"
    exit 1
fi

echo "✅ 모든 API 테스트 통과!"

# 컨테이너 상태 확인
echo "📊 컨테이너 상태:"
docker-compose -p $PROJECT_NAME ps

# 로그 샘플 출력
echo "📋 최근 로그 (마지막 20줄):"
docker-compose -p $PROJECT_NAME logs --tail=20 app

# 정리
echo "🧹 테스트 환경 정리..."
docker-compose -p $PROJECT_NAME down

echo "🎉 Docker 환경 테스트 완료!"