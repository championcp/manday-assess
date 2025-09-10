# Docker 使用指南

## 🚀 快速启动开发环境

### 方式一：使用启动脚本（推荐）
```bash
./scripts/start-dev.sh
```

### 方式二：手动启动Docker服务
```bash
# 启动数据库和缓存服务
cd deployment/dev && docker-compose -f docker-compose.dev.yml up -d

# 查看服务状态
cd deployment/dev && docker-compose -f docker-compose.dev.yml ps

# 停止服务
cd deployment/dev && docker-compose -f docker-compose.dev.yml down
```

## 📂 Docker配置文件位置

- **开发环境:** `deployment/dev/docker-compose.dev.yml`
- **测试环境:** `deployment/staging/` (预留)
- **生产环境:** `deployment/prod/` (预留)

## 📍 服务访问地址

启动后可以访问以下服务：

- **PostgreSQL:** localhost:5433 (用户名: postgres, 密码: postgres)
- **Redis:** localhost:6379
- **pgAdmin:** http://localhost:5050 (admin@changsha.gov.cn / admin123)
- **Redis Commander:** http://localhost:8081

## ⚠️ 重要说明

- 不要直接在项目根目录执行 `docker-compose` 命令
- Docker配置文件已按环境分类管理，请使用正确的路径
- 推荐使用 `./scripts/start-dev.sh` 脚本一键启动完整开发环境