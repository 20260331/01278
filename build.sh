#!/bin/bash

# ================================
# Docker 镜像构建脚本
# 支持多架构: AMD64 (x86_64) / ARM64
# ================================

set -e

# 镜像仓库前缀（可根据需要修改）
REGISTRY=${REGISTRY:-""}
VERSION=${VERSION:-"latest"}

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

echo_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

echo_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 Docker Buildx
check_buildx() {
    if ! docker buildx version > /dev/null 2>&1; then
        echo_error "Docker Buildx 未安装，请先安装 Docker Buildx"
        exit 1
    fi
    echo_info "Docker Buildx 版本: $(docker buildx version)"
}

# 创建/使用多架构构建器
setup_builder() {
    BUILDER_NAME="multiarch-builder"
    if ! docker buildx inspect $BUILDER_NAME > /dev/null 2>&1; then
        echo_info "创建多架构构建器: $BUILDER_NAME"
        docker buildx create --name $BUILDER_NAME --use --bootstrap
    else
        echo_info "使用已存在的构建器: $BUILDER_NAME"
        docker buildx use $BUILDER_NAME
    fi
}

# 构建单个服务镜像
build_image() {
    local service=$1
    local context=$2
    local platforms=${3:-"linux/amd64,linux/arm64"}
    
    local image_name="${REGISTRY}fitness-${service}:${VERSION}"
    
    echo_info "构建 ${service} 镜像: ${image_name}"
    echo_info "目标平台: ${platforms}"
    
    docker buildx build \
        --platform ${platforms} \
        -t ${image_name} \
        -f ${context}/Dockerfile \
        ${context} \
        --push=${PUSH:-false} \
        --load=${LOAD:-true}
}

# 主函数
main() {
    echo "========================================"
    echo "  健身俱乐部管理系统 - Docker 构建脚本"
    echo "========================================"
    echo ""
    
    # 解析参数
    case "${1:-all}" in
        "mysql"|"sql")
            check_buildx
            setup_builder
            build_image "mysql" "sql"
            ;;
        "backend")
            check_buildx
            setup_builder
            build_image "backend" "backend"
            ;;
        "frontend")
            check_buildx
            setup_builder
            build_image "frontend" "frontend"
            ;;
        "all")
            check_buildx
            setup_builder
            build_image "mysql" "sql"
            build_image "backend" "backend"
            build_image "frontend" "frontend"
            ;;
        "compose")
            echo_info "使用 docker-compose 构建并启动所有服务..."
            docker-compose up --build -d
            echo_info "服务启动完成！"
            echo ""
            echo "访问地址:"
            echo "  - 前端: http://localhost"
            echo "  - 后端 API: http://localhost:8080"
            echo "  - MySQL: localhost:3306"
            ;;
        "down")
            echo_info "停止并移除所有服务..."
            docker-compose down
            ;;
        "clean")
            echo_warn "清理所有镜像和数据卷..."
            docker-compose down -v --rmi all
            ;;
        *)
            echo "用法: $0 [命令]"
            echo ""
            echo "命令:"
            echo "  all       构建所有镜像 (默认)"
            echo "  mysql     仅构建 MySQL 镜像"
            echo "  backend   仅构建后端镜像"
            echo "  frontend  仅构建前端镜像"
            echo "  compose   使用 docker-compose 构建并启动"
            echo "  down      停止所有服务"
            echo "  clean     清理所有镜像和数据"
            echo ""
            echo "环境变量:"
            echo "  REGISTRY  镜像仓库前缀 (例: docker.io/username/)"
            echo "  VERSION   镜像版本标签 (默认: latest)"
            echo "  PUSH      是否推送到仓库 (true/false, 默认: false)"
            echo "  LOAD      是否加载到本地 (true/false, 默认: true)"
            ;;
    esac
}

main "$@"
