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
cd deployment/dev && docker-compose -f docker-compose.dev.yml up -d postgres redis
cd - > /dev/null

# 等待数据库启动
echo -e "${YELLOW}⏳ 等待数据库启动...${NC}"
until cd deployment/dev && docker-compose -f docker-compose.dev.yml exec postgres pg_isready -U postgres > /dev/null 2>&1; do
  echo "等待PostgreSQL启动..."
  sleep 2
done
cd - > /dev/null

echo -e "${GREEN}✅ 数据库启动成功${NC}"

# 等待Redis启动
echo -e "${YELLOW}⏳ 等待Redis启动...${NC}"
until cd deployment/dev && docker-compose -f docker-compose.dev.yml exec redis redis-cli ping > /dev/null 2>&1; do
  echo "等待Redis启动..."
  sleep 1
done
cd - > /dev/null

echo -e "${GREEN}✅ Redis启动成功${NC}"

# 显示服务状态
echo -e "${BLUE}📊 服务状态:${NC}"
cd deployment/dev && docker-compose -f docker-compose.dev.yml ps
cd - > /dev/null

echo ""
echo -e "${GREEN}🎉 开发环境启动完成!${NC}"
echo ""
echo "📍 服务访问地址:"
echo "   PostgreSQL: localhost:5433"
echo "   Redis: localhost:6379"
echo "   pgAdmin: http://localhost:5050 (admin@changsha.gov.cn / admin123)"
echo "   Redis Commander: http://localhost:8081"
echo ""
echo "🔧 启动后端服务..."
echo "   后端将在新终端窗口中启动"
sleep 2

# 在新终端窗口中启动后端 (使用端口检测脚本)
if command -v osascript > /dev/null 2>&1; then
    # macOS - 使用新的多实例启动脚本
    osascript -e "tell application \"Terminal\" to do script \"cd '$(pwd)' && echo '🚀 启动Spring Boot后端服务 (端口自动检测)...' && echo '💡 使用 Ctrl+C 停止服务' && echo '' && ./scripts/start-backend-dev.sh auto dev\""
else
    echo "请在新终端中运行: ./scripts/start-backend-dev.sh auto dev"
fi

# 等待后端启动
echo -e "${YELLOW}⏳ 等待后端服务启动...${NC}"
for i in {1..20}; do
    if curl -s --max-time 3 http://localhost:8080/actuator/health > /dev/null 2>&1 || \
       curl -s --max-time 3 http://localhost:8080/api/projects > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 后端服务启动成功${NC}"
        break
    fi
    echo "等待后端启动... ($i/20)"
    sleep 3
done

echo ""
echo "🎨 启动前端服务..."
echo "   前端将在新终端窗口中启动"
sleep 2

# 在新终端窗口中启动前端
if command -v osascript > /dev/null 2>&1; then
    # macOS
    osascript -e "tell application \"Terminal\" to do script \"cd '$(pwd)' && cd src/frontend && echo '🚀 启动Vue.js前端服务...' && echo '📍 访问地址: http://localhost:5173' && echo '💡 使用 Ctrl+C 停止服务' && echo '' && npm run dev\""
else
    echo "请在新终端中运行: cd src/frontend && npm run dev"
fi

# 等待前端启动
echo -e "${YELLOW}⏳ 等待前端服务启动...${NC}"
for i in {1..15}; do
    if curl -s --max-time 3 http://localhost:5173 > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 前端服务启动成功${NC}"
        break
    fi
    echo "等待前端启动... ($i/15)"
    sleep 3
done

echo ""
echo -e "${GREEN}🎉 完整开发环境启动完成!${NC}"
echo ""
echo -e "${BLUE}📍 服务访问地址:${NC}"
echo "   🌐 系统首页: http://localhost:5173"
echo "   🔧 API接口: http://localhost:8080/api"
echo "   📚 API文档: http://localhost:8080/swagger-ui.html"
echo "   💾 数据库管理: http://localhost:5050 (admin@changsha.gov.cn / admin123)"
echo "   🗄️  Redis管理: http://localhost:8081"
echo ""
echo "⏹️  停止服务:"
echo "   cd deployment/dev && docker-compose -f docker-compose.dev.yml down  # 停止数据库服务"
echo "   在各终端窗口中按 Ctrl+C 停止前后端服务"
echo ""