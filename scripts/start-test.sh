#!/bin/bash
# 测试环境启动脚本

set -e

echo "🚀 启动长沙市财政评审中心软件规模评估系统测试环境"
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
    echo -e "${RED}❌ Docker未运行，请启动Docker Desktop${NC}"
    exit 1
fi

cd "$PROJECT_ROOT"

echo -e "${BLUE}📦 启动测试环境数据库服务...${NC}"
docker-compose up -d postgres redis

# 等待数据库启动
echo -e "${YELLOW}⏳ 等待数据库启动...${NC}"
until docker-compose exec postgres pg_isready -U postgres > /dev/null 2>&1; do
  echo "等待PostgreSQL启动..."
  sleep 2
done

echo -e "${GREEN}✅ 数据库启动成功${NC}"

# 运行数据库迁移和测试数据初始化
echo -e "${BLUE}🔄 执行数据库迁移和测试数据初始化...${NC}"
cd src/backend
./mvnw flyway:clean flyway:migrate -Dspring.profiles.active=test
cd "$PROJECT_ROOT"

echo -e "${GREEN}✅ 测试数据库准备完成${NC}"

# 在测试模式下启动后端
echo -e "${BLUE}🔧 启动测试后端服务...${NC}"
cd src/backend

# 在新终端窗口中启动测试后端
if command -v osascript > /dev/null 2>&1; then
    # macOS
    osascript -e "tell application \"Terminal\" to do script \"cd '$(pwd)' && echo '🚀 启动Spring Boot测试服务...' && echo '📍 访问地址: http://localhost:8080' && echo '💡 使用 Ctrl+C 停止服务' && echo '' && ./mvnw spring-boot:run -Dspring.profiles.active=test\""
else
    echo "请在新终端中运行: cd src/backend && ./mvnw spring-boot:run -Dspring.profiles.active=test"
fi

cd "$PROJECT_ROOT"

# 等待后端启动
echo -e "${YELLOW}⏳ 等待测试后端服务启动...${NC}"
for i in {1..20}; do
    if curl -s --max-time 3 http://localhost:8080/actuator/health > /dev/null 2>&1 || \
       curl -s --max-time 3 http://localhost:8080/api/projects > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 测试后端服务启动成功${NC}"
        break
    fi
    echo "等待测试后端启动... ($i/20)"
    sleep 3
done

# 启动前端（开发模式用于测试）
echo -e "${BLUE}🎨 启动前端服务（测试模式）...${NC}"
cd src/frontend

# 在新终端窗口中启动前端
if command -v osascript > /dev/null 2>&1; then
    # macOS
    osascript -e "tell application \"Terminal\" to do script \"cd '$(pwd)' && echo '🚀 启动Vue.js测试服务...' && echo '📍 访问地址: http://localhost:5173' && echo '💡 使用 Ctrl+C 停止服务' && echo '' && npm run dev\""
else
    echo "请在新终端中运行: cd src/frontend && npm run dev"
fi

cd "$PROJECT_ROOT"

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

# 运行自动化测试
echo -e "${BLUE}🧪 准备测试环境...${NC}"
echo ""
echo -e "${BLUE}📋 可执行的测试命令:${NC}"
echo "   单元测试: cd src/backend && ./mvnw test"
echo "   集成测试: cd src/backend && ./mvnw verify"
echo "   前端测试: cd src/frontend && npm test"
echo "   E2E测试: cd tests && npm run e2e"
echo ""

echo -e "${GREEN}🎉 测试环境启动完成!${NC}"
echo ""
echo -e "${BLUE}📍 测试服务访问地址:${NC}"
echo "   🌐 系统首页: http://localhost:5173"
echo "   🔧 API接口: http://localhost:8080/api"
echo "   📚 API文档: http://localhost:8080/swagger-ui.html"
echo "   💾 数据库管理: http://localhost:5050"
echo "   🗄️  Redis管理: http://localhost:8081"
echo ""
echo -e "${YELLOW}💡 测试环境特点:${NC}"
echo "   - 使用独立的测试数据库"
echo "   - 启用测试配置文件(test profile)"
echo "   - 包含完整的测试数据集"
echo "   - 支持自动化测试执行"
echo ""
echo "⏹️  停止测试环境:"
echo "   docker-compose down  # 停止数据库服务"
echo "   在各终端窗口中按 Ctrl+C 停止前后端服务"
echo ""