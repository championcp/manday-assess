#!/bin/bash
# 生产环境启动脚本

set -e

echo "🚀 启动长沙市财政评审中心软件规模评估系统生产环境"
echo "================================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 获取项目根目录
PROJECT_ROOT=$(cd "$(dirname "$0")/.." && pwd)

echo -e "${BLUE}📍 项目目录: $PROJECT_ROOT${NC}"

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker未运行，请启动Docker服务${NC}"
    exit 1
fi

echo -e "${BLUE}📦 启动生产环境服务...${NC}"
cd "$PROJECT_ROOT"

# 构建生产版本
echo -e "${YELLOW}🔨 构建生产版本...${NC}"

# 构建前端生产版本
if [ -d "src/frontend" ]; then
    echo -e "${BLUE}🎨 构建前端生产版本...${NC}"
    cd src/frontend
    npm run build
    cd "$PROJECT_ROOT"
    echo -e "${GREEN}✅ 前端构建完成${NC}"
fi

# 构建后端生产版本
if [ -d "src/backend" ]; then
    echo -e "${BLUE}🔧 构建后端生产版本...${NC}"
    cd src/backend
    ./mvnw clean package -DskipTests
    cd "$PROJECT_ROOT"
    echo -e "${GREEN}✅ 后端构建完成${NC}"
fi

# 启动生产环境Docker服务
echo -e "${BLUE}🐳 启动Docker生产环境...${NC}"
docker-compose -f docker-compose.prod.yml up -d

# 等待服务启动
echo -e "${YELLOW}⏳ 等待生产服务启动...${NC}"
sleep 10

# 检查服务状态
echo -e "${BLUE}📊 检查服务状态...${NC}"

# 检查数据库
if nc -z localhost 5432 > /dev/null 2>&1; then
    echo -e "PostgreSQL: ${GREEN}✅ 运行中${NC}"
else
    echo -e "PostgreSQL: ${RED}❌ 启动失败${NC}"
fi

# 检查Redis
if nc -z localhost 6379 > /dev/null 2>&1; then
    echo -e "Redis:      ${GREEN}✅ 运行中${NC}"
else
    echo -e "Redis:      ${RED}❌ 启动失败${NC}"
fi

# 检查应用
if curl -s --max-time 5 http://localhost:80 > /dev/null 2>&1; then
    echo -e "应用服务:    ${GREEN}✅ 运行中${NC}"
else
    echo -e "应用服务:    ${YELLOW}⚠️  正在启动中...${NC}"
fi

echo ""
echo -e "${GREEN}🎉 生产环境启动完成!${NC}"
echo ""
echo -e "${BLUE}📍 生产服务访问地址:${NC}"
echo "   🌐 系统主页: http://localhost"
echo "   🔧 API接口: http://localhost/api"
echo "   📊 监控面板: http://localhost/actuator"
echo ""
echo -e "${BLUE}📊 服务监控:${NC}"
echo "   查看容器状态: docker-compose -f docker-compose.prod.yml ps"
echo "   查看应用日志: docker-compose -f docker-compose.prod.yml logs app"
echo "   查看数据库日志: docker-compose -f docker-compose.prod.yml logs postgres"
echo ""
echo "⏹️  停止生产环境:"
echo "   docker-compose -f docker-compose.prod.yml down"
echo ""