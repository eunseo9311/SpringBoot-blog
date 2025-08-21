#!/bin/bash

# Docker 개발 환경 관리 스크립트

set -e

PROJECT_NAME="springboot-blog"

show_help() {
    echo "Docker 개발 환경 관리 스크립트"
    echo ""
    echo "사용법: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  up        애플리케이션과 의존성 서비스 시작"
    echo "  down      모든 서비스 중지 및 제거"
    echo "  rebuild   애플리케이션 이미지 재빌드 후 시작"
    echo "  logs      서비스 로그 확인"
    echo "  status    서비스 상태 확인"
    echo "  clean     모든 컨테이너, 이미지, 볼륨 정리"
    echo "  postgres  PostgreSQL과 함께 시작"
    echo ""
    echo "Options:"
    echo "  -d, --detach    백그라운드로 실행"
    echo "  -h, --help      도움말 표시"
}

case $1 in
    up)
        echo "🚀 애플리케이션 시작 중..."
        if [[ "$2" == "-d" || "$2" == "--detach" ]]; then
            docker-compose -p $PROJECT_NAME up -d
        else
            docker-compose -p $PROJECT_NAME up
        fi
        ;;
    down)
        echo "🛑 애플리케이션 중지 중..."
        docker-compose -p $PROJECT_NAME down
        ;;
    rebuild)
        echo "🔨 애플리케이션 재빌드 중..."
        docker-compose -p $PROJECT_NAME down
        docker-compose -p $PROJECT_NAME build --no-cache app
        if [[ "$2" == "-d" || "$2" == "--detach" ]]; then
            docker-compose -p $PROJECT_NAME up -d
        else
            docker-compose -p $PROJECT_NAME up
        fi
        ;;
    logs)
        echo "📋 서비스 로그 확인..."
        docker-compose -p $PROJECT_NAME logs -f ${2:-app}
        ;;
    status)
        echo "📊 서비스 상태 확인..."
        docker-compose -p $PROJECT_NAME ps
        echo ""
        echo "🏥 헬스체크 상태:"
        docker ps --format "table {{.Names}}\t{{.Status}}" --filter "name=$PROJECT_NAME"
        ;;
    clean)
        echo "🧹 환경 정리 중..."
        docker-compose -p $PROJECT_NAME down -v --rmi all
        docker system prune -f
        ;;
    postgres)
        echo "🐘 PostgreSQL과 함께 시작..."
        if [[ "$2" == "-d" || "$2" == "--detach" ]]; then
            docker-compose -p $PROJECT_NAME --profile postgres up -d
        else
            docker-compose -p $PROJECT_NAME --profile postgres up
        fi
        ;;
    -h|--help|help)
        show_help
        ;;
    *)
        echo "❌ 알 수 없는 명령어: $1"
        echo ""
        show_help
        exit 1
        ;;
esac