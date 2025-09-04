#!/bin/bash
# 开发环境启动脚本

set -e

echo "🚀 启动长沙市财政评审中心软件规模评估系统开发环境"
echo "================================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker未运行，请启动Docker Desktop${NC}"
    exit 1
fi

echo -e "${BLUE}📦 启动数据库和缓存服务...${NC}"
docker-compose up -d postgres redis

# 等待数据库启动
echo -e "${YELLOW}⏳ 等待数据库启动...${NC}"
until docker-compose exec postgres pg_isready -U postgres > /dev/null 2>&1; do
  echo "等待PostgreSQL启动..."
  sleep 2
done

echo -e "${GREEN}✅ 数据库启动成功${NC}"

# 等待Redis启动
echo -e "${YELLOW}⏳ 等待Redis启动...${NC}"
until docker-compose exec redis redis-cli ping > /dev/null 2>&1; do
  echo "等待Redis启动..."
  sleep 1
done

echo -e "${GREEN}✅ Redis启动成功${NC}"

# 显示服务状态
echo -e "${BLUE}📊 服务状态:${NC}"
docker-compose ps

echo ""
echo -e "${GREEN}🎉 开发环境启动完成!${NC}"
echo ""
echo "📍 服务访问地址:"
echo "   PostgreSQL: localhost:5433"
echo "   Redis: localhost:6379"
echo "   pgAdmin: http://localhost:5050 (admin@changsha.gov.cn / admin123)"
echo "   Redis Commander: http://localhost:8081"
echo ""
echo "🔧 后端启动命令:"
echo "   cd src/backend && ./mvnw spring-boot:run"
echo ""
echo "🎨 前端启动命令:"
echo "   cd src/frontend && npm run dev"
echo ""
echo "⏹️  停止服务:"
echo "   docker-compose down"
echo ""