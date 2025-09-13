# Issue #6 端口冲突修复 - 使用说明

## 📋 问题概述

修复了后端服务端口冲突导致无法启动多个开发实例的问题。现在支持：

- ✅ 开发环境多实例并行启动
- ✅ 自动端口检测和分配
- ✅ 灵活的端口配置机制
- ✅ 生产环境配置不受影响

## 🔧 解决方案

### 1. 创建了独立的开发环境配置

**文件**: `src/backend/src/main/resources/application-dev.yml`

支持以下配置模式：
- 环境变量端口配置: `SERVER_PORT` 或 `DEV_SERVER_PORT`
- 预设实例配置: `dev-instance-1` 到 `dev-instance-5`
- 默认端口范围: 8080-8084

### 2. 开发了智能启动脚本

**文件**: `scripts/start-backend-dev.sh`

功能特性：
- 🔍 自动检测端口占用状态
- 🎯 智能端口分配机制
- ☕ 自动Java环境检测和配置
- 📊 详细的启动过程反馈
- 🎨 彩色终端输出

### 3. 更新了主启动脚本

**文件**: `scripts/start-dev.sh`

集成了新的端口检测机制，确保开发环境启动的可靠性。

## 📖 使用方法

### 基本用法

```bash
# 自动端口分配启动
./scripts/start-backend-dev.sh

# 指定端口启动
./scripts/start-backend-dev.sh auto dev 8081

# 使用预设实例配置
./scripts/start-backend-dev.sh 2 dev  # 使用实例2配置(端口8081)
```

### 启动参数说明

```bash
./scripts/start-backend-dev.sh [实例名称] [环境配置] [首选端口]
```

- **实例名称**: `auto`(默认), `1-5`(预设实例), `instance-1`到`instance-5`
- **环境配置**: `dev`(默认)
- **首选端口**: 指定端口号，如果被占用会自动寻找可用端口

### 环境变量配置

```bash
# 使用环境变量设置端口
export SERVER_PORT=8085
./scripts/start-backend-dev.sh

# 或者
export DEV_SERVER_PORT=8086
./scripts/start-backend-dev.sh
```

## 📊 端口分配策略

### 默认端口范围
| 端口 | 用途 | 状态检测 |
|------|------|----------|
| 8080 | 默认开发端口 | 自动检测 |
| 8081 | 开发实例2 | 自动检测 |
| 8082 | 开发实例3 | 自动检测 |
| 8083 | 开发实例4 | 自动检测 |
| 8084 | 开发实例5 | 自动检测 |

### 端口冲突处理
1. **检测阶段**: 自动扫描8080-8084端口占用状态
2. **分配阶段**: 优先使用指定端口，被占用时自动分配下一个可用端口
3. **验证阶段**: 确认端口可用性后再启动服务

## 🌐 多实例开发场景

### 场景1: 团队协作开发
```bash
# 开发者A - 前端开发
./scripts/start-backend-dev.sh auto dev 8080

# 开发者B - API测试
./scripts/start-backend-dev.sh auto dev 8081

# 开发者C - 性能测试
./scripts/start-backend-dev.sh auto dev 8082
```

### 场景2: 版本对比测试
```bash
# 当前开发版本
git checkout feature/new-api
./scripts/start-backend-dev.sh auto dev 8080

# 基础版本对比
git checkout master
./scripts/start-backend-dev.sh auto dev 8081
```

### 场景3: 配置环境测试
```bash
# 实例1 - 标准开发配置
./scripts/start-backend-dev.sh 1 dev

# 实例2 - 特殊测试配置  
./scripts/start-backend-dev.sh 2 dev
```

## 🔧 故障排除

### 常见问题

**Q: 脚本提示"未找到可用端口"**
```bash
# 扩大端口搜索范围（修改脚本中的MAX_PORT）
# 或者停止占用端口的服务
lsof -ti:8080 | xargs kill -9
```

**Q: Java环境检测失败**
```bash
# 手动设置Java环境
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
./scripts/start-backend-dev.sh
```

**Q: 权限问题**
```bash
# 确保脚本有执行权限
chmod +x scripts/start-backend-dev.sh
```

### 端口占用检查

```bash
# 查看端口占用情况
lsof -i :8080-8084

# 终止占用端口的进程
lsof -ti:8081 | xargs kill -9
```

## 🏭 生产环境配置

生产环境配置**完全不受影响**，仍然使用：

```yaml
server:
  port: ${SERVER_PORT:8080}
```

支持通过环境变量动态配置：
```bash
# 生产环境端口配置
export SERVER_PORT=80
```

## ✅ 验证测试

### 功能验证清单

- [x] 端口检测机制正常工作
- [x] 自动端口分配功能正确
- [x] Java环境自动检测成功
- [x] 多实例并行启动无冲突
- [x] 生产环境配置未受影响
- [x] 启动脚本集成完成

### 测试命令

```bash
# 基础功能测试
./scripts/start-backend-dev.sh auto dev 8083

# 多实例测试（在不同终端运行）
./scripts/start-backend-dev.sh auto dev 8080
./scripts/start-backend-dev.sh auto dev 8081
./scripts/start-backend-dev.sh auto dev 8082

# 健康检查
curl -s http://localhost:8083/actuator/health
```

## 📈 改进效果

### 修复前
- ❌ 端口8080硬编码，无法启动多实例
- ❌ 端口冲突时启动失败，错误信息不明确
- ❌ 开发团队无法并行开发测试

### 修复后  
- ✅ 灵活的端口配置，支持环境变量
- ✅ 智能端口检测，自动冲突解决
- ✅ 完整的启动脚本，用户体验友好
- ✅ 支持最多5个并行开发实例
- ✅ 保持生产环境配置的稳定性

---

**维护人员**: Developer Engineer  
**解决时间**: 2025-09-13  
**相关文件**: 
- `/Users/chengpeng/traeWorkspace/manday-assess/src/backend/src/main/resources/application-dev.yml`
- `/Users/chengpeng/traeWorkspace/manday-assess/scripts/start-backend-dev.sh`
- `/Users/chengpeng/traeWorkspace/manday-assess/scripts/start-dev.sh`