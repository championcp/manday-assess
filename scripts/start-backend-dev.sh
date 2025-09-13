#!/bin/bash
# 多实例后端启动脚本 - 支持端口自动检测和分配
# 解决 Issue #6 - 后端服务端口冲突问题

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 默认端口范围
DEFAULT_PORTS=(8080 8081 8082 8083 8084)
BASE_PORT=8080
MAX_PORT=8090

echo -e "${BLUE}🚀 长沙市财政评审中心软件规模评估系统${NC}"
echo -e "${BLUE}   多实例后端启动脚本 (Issue #6 端口冲突修复版)${NC}"
echo "================================================"

# 检查端口是否被占用
check_port() {
    local port=$1
    if lsof -ti:$port >/dev/null 2>&1; then
        return 1  # 端口被占用
    else
        return 0  # 端口可用
    fi
}

# 查找可用端口
find_available_port() {
    local start_port=${1:-$BASE_PORT}
    local max_port=${2:-$MAX_PORT}
    
    for ((port=$start_port; port<=$max_port; port++)); do
        if check_port $port; then
            echo $port
            return 0
        fi
    done
    
    return 1  # 没有找到可用端口
}

# 显示当前占用的端口
show_occupied_ports() {
    echo -e "${YELLOW}🔍 检查当前端口占用状况...${NC}"
    for port in "${DEFAULT_PORTS[@]}"; do
        if check_port $port; then
            echo -e "   端口 $port: ${GREEN}✅ 可用${NC}"
        else
            echo -e "   端口 $port: ${RED}❌ 被占用${NC}"
            # 显示占用端口的进程信息
            local pid=$(lsof -ti:$port 2>/dev/null)
            if [ ! -z "$pid" ]; then
                local process_info=$(ps -p $pid -o pid,comm,args --no-headers 2>/dev/null | head -1)
                echo -e "      进程信息: ${CYAN}$process_info${NC}"
            fi
        fi
    done
    echo ""
}

# 获取启动参数
INSTANCE_NAME=${1:-"auto"}
PROFILE=${2:-"dev"}
PREFERRED_PORT=${3:-""}

echo -e "${CYAN}📋 启动参数:${NC}"
echo "   实例名称: $INSTANCE_NAME"
echo "   环境配置: $PROFILE"
echo "   首选端口: ${PREFERRED_PORT:-"自动分配"}"
echo ""

# 显示端口占用状况
show_occupied_ports

# 确定使用的端口
SELECTED_PORT=""

if [ ! -z "$PREFERRED_PORT" ]; then
    # 用户指定了端口
    if check_port $PREFERRED_PORT; then
        SELECTED_PORT=$PREFERRED_PORT
        echo -e "${GREEN}✅ 使用指定端口: $SELECTED_PORT${NC}"
    else
        echo -e "${RED}❌ 指定端口 $PREFERRED_PORT 被占用，自动寻找可用端口...${NC}"
        SELECTED_PORT=$(find_available_port)
    fi
else
    # 自动分配端口
    echo -e "${YELLOW}🔍 自动寻找可用端口...${NC}"
    SELECTED_PORT=$(find_available_port)
fi

# 检查是否找到可用端口
if [ -z "$SELECTED_PORT" ]; then
    echo -e "${RED}❌ 错误：在端口范围 $BASE_PORT-$MAX_PORT 内未找到可用端口${NC}"
    echo -e "${YELLOW}💡 建议操作:${NC}"
    echo "   1. 停止一些占用端口的服务"
    echo "   2. 扩大端口搜索范围"
    echo "   3. 检查防火墙设置"
    exit 1
fi

echo -e "${GREEN}🎯 选定端口: $SELECTED_PORT${NC}"
echo ""

# 确定使用的Spring Profile
SPRING_PROFILE=""
case $INSTANCE_NAME in
    "1"|"instance-1")
        SPRING_PROFILE="dev,dev-instance-1"
        ;;
    "2"|"instance-2")
        SPRING_PROFILE="dev,dev-instance-2"
        ;;
    "3"|"instance-3")
        SPRING_PROFILE="dev,dev-instance-3"
        ;;
    "4"|"instance-4")
        SPRING_PROFILE="dev,dev-instance-4"
        ;;
    "5"|"instance-5")
        SPRING_PROFILE="dev,dev-instance-5"
        ;;
    "auto")
        SPRING_PROFILE="dev"
        ;;
    *)
        SPRING_PROFILE="dev"
        ;;
esac

# 检查Java环境
if [ -z "$JAVA_HOME" ]; then
    echo -e "${YELLOW}⚠️  未设置 JAVA_HOME，尝试自动检测...${NC}"
    # 优先使用 JDK 17+ 
    if [ -d "/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home"
        export PATH="$JAVA_HOME/bin:$PATH"
        echo -e "${GREEN}✅ 自动设置 Java 24: $JAVA_HOME${NC}"
    elif [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
        export PATH="$JAVA_HOME/bin:$PATH"
        echo -e "${GREEN}✅ 自动设置 Java 17: $JAVA_HOME${NC}"
    elif command -v java >/dev/null 2>&1; then
        echo -e "${GREEN}✅ 找到系统 Java${NC}"
    else
        echo -e "${RED}❌ 未找到 Java 环境，请安装 JDK 17+${NC}"
        exit 1
    fi
else
    echo -e "${GREEN}✅ Java环境: $JAVA_HOME${NC}"
fi

# 进入后端目录
BACKEND_DIR="src/backend"
if [ ! -d "$BACKEND_DIR" ]; then
    echo -e "${RED}❌ 错误：后端目录 $BACKEND_DIR 不存在${NC}"
    echo "请在项目根目录下运行此脚本"
    exit 1
fi

cd $BACKEND_DIR

echo -e "${BLUE}🏗️  编译后端项目...${NC}"
./mvnw compile -q

echo ""
echo -e "${GREEN}🚀 启动后端服务实例...${NC}"
echo -e "${CYAN}📍 实例信息:${NC}"
echo "   服务端口: $SELECTED_PORT"
echo "   Spring Profile: $SPRING_PROFILE"
echo "   访问地址: http://localhost:$SELECTED_PORT"
echo "   API文档: http://localhost:$SELECTED_PORT/swagger-ui.html"
echo "   健康检查: http://localhost:$SELECTED_PORT/actuator/health"
echo ""
echo -e "${YELLOW}💡 提示: 按 Ctrl+C 可停止服务${NC}"
echo "================================================"

# 设置环境变量并启动服务
export SERVER_PORT=$SELECTED_PORT
export DEV_SERVER_PORT=$SELECTED_PORT

# 启动Spring Boot应用
exec ./mvnw spring-boot:run \
    -Dspring-boot.run.skip-tests=true \
    -Dmaven.test.skip=true \
    -Dspring.profiles.active=$SPRING_PROFILE \
    -Dserver.port=$SELECTED_PORT \
    -Dspring-boot.run.jvmArguments="-Dserver.port=$SELECTED_PORT"