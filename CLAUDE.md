# 软件规模评估系统 - Claude Code 项目指南

## 🎯 项目概述

**项目名称：** 软件规模评估系统 (manday-assess)  
**项目类型：** 政府投资信息化项目  
**核心需求：** 严格按照《长沙市财政评审中心政府投资信息化项目评审指南》实现NESMA功能点评估系统  
**质量标准：** 政府级项目，要求100%准确性，不容许任何偏差

## ⚠️ 重要原则

1. **严肃性** - 这是政府项目，所有决策都要谨慎，质量胜过速度
2. **准确性** - NESMA计算结果必须与PDF指南案例100%一致
3. **合规性** - 完全符合政府信息化项目评审的各项要求
4. **可追溯性** - 所有计算过程必须可追溯，支持审计要求

## 👥 敏捷团队协作模式

项目采用Scrum敏捷开发，由多agent协作完成：

### 团队角色分工
- **Scrum Master** - 流程管理和团队协调
- **Product Owner** - 需求分析、用户故事管理、验收标准制定
- **UI/UX Designer** - 界面设计、用户体验、设计规范制定
- **Developer Engineer** - 系统开发、架构设计、技术实现
- **QA Test Engineer** - 质量保障、测试策略、缺陷管理

### 协作原则
- 各角色专业分工，但需要密切协作
- 所有重要决策都要团队讨论确认
- 遇到不确定问题立即沟通，避免盲目开发
- 质量第一，宁可延期也不妥协质量

## 🔄 Git 工作流规范

### 分支管理策略
```bash
# 主分支
master                    # 稳定的生产代码

# Sprint 开发分支
sprint-0-planning         # Sprint 0: 项目规划和架构设计 
sprint-1-core-calculation # Sprint 1: 核心NESMA计算引擎
sprint-2-ui-implementation# Sprint 2: 用户界面实现
sprint-3-testing-integration # Sprint 3: 测试和集成
```

### 分支工作流程
1. **开始Sprint** - 从master创建Sprint分支
2. **开发期间** - 在Sprint分支上提交代码
3. **Sprint完成** - 创建PR申请合并到master
4. **用户确认** - 经用户确认后合并
5. **清理分支** - 删除已合并的Sprint分支

### 提交信息规范
```
[类型] Sprint {N}: 简洁描述

详细说明
- 具体变更1
- 具体变更2

🤖 Generated with [Claude Code](https://claude.ai/code)
Co-Authored-By: Claude <noreply@anthropic.com>
```

## 🏗️ 技术架构指导

### 技术栈（已确定）
- **前端:** Vue 3.x + TypeScript + Element Plus + Vite
- **后端:** Spring Boot 3.x + Java 17 + PostgreSQL + Redis  
- **部署:** Docker + Kubernetes + Nginx
- **测试:** JUnit 5 + Vue Test Utils + 自动化测试框架

### 架构原则
- **精准可靠:** BigDecimal数值计算，确保政府级计算精度
- **安全合规:** JWT认证 + RBAC权限 + 数据加密 + 审计日志
- **高性能:** 多级缓存架构，响应时间<2秒，支持100+并发
- **可扩展:** 微服务架构设计，支持业务功能灵活扩展

### 质量标准
- **代码质量:** 单元测试覆盖率≥80%，强制代码审查
- **性能要求:** 系统响应时间<2秒，可用性≥99.5%
- **安全要求:** 通过安全漏洞扫描，符合等保三级标准

## 📁 项目结构

```
manday-assess/
├── CLAUDE.md                 # 本文件 - 项目指导文档
├── README.md                 # 项目概述和使用指南
├── LICENSE                   # MIT开源许可证
├── .gitignore                # Git忽略文件配置
├── DOCKER_USAGE.md           # Docker使用指南
├── agile-workspace/          # 敏捷工作区和Sprint协作记录
│   ├── acceptance-criteria/  # 验收标准定义
│   ├── daily-standups/      # 每日站会记录
│   ├── developer-engineer/  # 开发工程师工作区
│   ├── product-backlog/     # 产品待办事项
│   ├── qa-test-engineer/    # QA测试工程师工作区
│   ├── retrospectives/      # Sprint回顾总结
│   ├── sprint-planning/     # Sprint规划文档
│   ├── sprint-reviews/      # Sprint评审记录
│   ├── team-communications/ # 团队沟通记录
│   └── user-stories/        # 用户故事和需求分析
├── documentation/            # 项目文档库
│   ├── technical/           # 技术架构和开发文档
│   ├── user/               # 用户手册和需求规范
│   └── sprints/            # Sprint开发记录归档
├── src/                     # 源代码目录
│   ├── backend/            # Spring Boot后端代码
│   ├── frontend/           # Vue 3前端代码（含独立node_modules）
│   └── shared/             # 前后端共享代码
├── tests/                   # 测试工具套件（独立npm项目）
│   ├── integration/        # API集成测试
│   ├── performance/        # 性能压力测试
│   ├── e2e/               # 端到端测试
│   ├── ui/                # UI自动化测试
│   ├── security/          # 安全测试
│   ├── unit/              # 单元测试
│   ├── package.json       # 测试工具依赖管理
│   └── node_modules/      # 测试工具依赖包
├── deployment/             # 部署配置和环境管理
│   ├── dev/               # 开发环境配置（含docker-compose.dev.yml）
│   ├── staging/           # 测试环境配置
│   ├── prod/              # 生产环境配置
│   └── init-db/           # 数据库初始化脚本
├── scripts/                # 自动化脚本
│   ├── start-dev.sh       # 开发环境启动脚本
│   ├── start-test.sh      # 测试环境启动脚本
│   ├── start-prod.sh      # 生产环境启动脚本
│   └── security-scan.sh   # 安全扫描脚本
├── reports/                # 测试和质量报告
│   ├── sprint-2/          # Sprint 2完成报告
│   └── sprint-3/          # Sprint 3完成报告
├── logs/                   # 系统日志文件
├── tools/                  # 开发工具和实用程序
│   ├── generators/        # 代码生成器
│   └── scripts/           # 工具脚本
├── temp/                   # 临时文件存储
└── security-scan-results/  # 安全扫描结果
```

## 🧪 测试和质量保障

### 测试策略
- **单元测试:** 覆盖所有核心算法和业务逻辑
- **集成测试:** 验证各模块协作和数据流转
- **系统测试:** 端到端功能验证和性能测试  
- **验收测试:** 与PDF指南案例100%一致性验证

### 关键测试点
1. **NESMA计算精度** - 所有计算结果与PDF案例完全一致
2. **用户界面功能** - 政府工作人员使用习惯验证
3. **数据安全** - 敏感信息保护和访问权限控制
4. **系统稳定性** - 7×24小时稳定运行验证

## 🚀 常用命令和工具

### 开发环境
```bash
# 前端开发
cd src/frontend && npm run dev

# 后端开发  
cd src/backend && ./mvnw spring-boot:run

# 数据库服务
cd deployment/dev && docker-compose -f docker-compose.dev.yml up -d

# 完整开发环境（推荐）
./scripts/start-dev.sh
```

### 测试命令
```bash
# API集成测试
cd tests && npm run test:api

# Sprint 3 API集成测试套件  
cd tests && npm run test:api-suite

# API数据验证测试
cd tests && npm run test:api-validation

# 完整集成测试
cd tests && npm run test:integration

# 性能压力测试
cd tests && npm run test:performance

# 快速性能测试
cd tests && npm run test:quick

# 完整测试套件
cd tests && npm run test:all

# 后端单元测试
cd src/backend && ./mvnw test

# 前端单元测试
cd src/frontend && npm run test
```

### 代码质量检查
```bash
# 前端代码检查
npm run lint:frontend

# 后端代码检查  
./mvnw checkstyle:check

# 安全扫描
npm run security:scan

# 完整质量检查
npm run quality:check
```

### 构建和部署
```bash
# 开发构建
npm run build:dev

# 生产构建
npm run build:prod

# Docker构建
npm run docker:build

# 部署到测试环境
npm run deploy:test
```

## 📋 Sprint 里程碑

### Sprint 0 ✅ (已完成)
- [x] 项目规划和需求分析
- [x] 技术架构设计  
- [x] UI/UX设计规范
- [x] 测试策略制定
- [x] Git工作流建立

### Sprint 1 ✅ (已完成)
- [x] NESMA核心计算引擎开发
- [x] 数据模型设计和实现
- [x] 基础REST API开发
- [x] 算法验证测试（100%通过）

### Sprint 2 ✅ (已完成 2025-09-08)  
- [x] Vue3前端界面完整实现
- [x] 前后端完整集成和联调
- [x] 项目管理功能完整开发
- [x] NESMA计算界面和流程实现
- [x] Chrome UI自动化测试（AAA级质量）
- [x] 政府级质量标准验收通过

### Sprint 3 ✅ (已完成 2025-09-10)
**目标:** 打造政府级生产就绪系统，通过完整验收

#### Epic 1: 系统集成测试 (15 story points) ✅
- [x] 端到端业务流程自动化测试
- [x] API集成测试自动化（83.3%成功率）
- [x] 跨浏览器兼容性验证

#### Epic 2: 性能优化 (18 story points) ✅
- [x] 大数据量场景性能优化(1000+功能点<5秒)
- [x] 多级缓存策略实现(Redis集成)
- [x] 数据库性能调优(查询性能提升≥30%)

#### Epic 3: 安全加固 (20 story points) ✅
- [x] 政府级安全标准实现(等保三级)
- [x] 完整审计日志系统
- [x] 数据备份和恢复机制
- [x] CORS安全配置和JWT认证优化

#### Epic 4: 用户验收测试 (22 story points) ✅
- [x] 长沙市财政评审中心联合验收
- [x] 用户培训材料准备(操作手册+视频)
- [x] 生产环境部署准备
- [x] 运维文档和应急预案
- [x] 项目文件结构重组和规范化

**达成标准:**
- ✅ 响应时间<2秒, 可用性≥99.5%, 支持100+并发
- ✅ NESMA计算准确率100%, 用户满意度≥4.5/5
- ✅ 通过安全合规认证, 完成政府用户正式验收
- ✅ 系统集成质量分数: 85/100 (优秀级别)

## 🔍 关键文档索引

### 需求和规范
- [政府评审指南 (PDF)](documentation/user/requirements/长沙市财政评审中心政府投资信息化项目评审指南.pdf)
- [Git工作流规范](documentation/sprints/sprint-0/git-workflow-specification.md)

### 技术文档
- [系统架构总结](documentation/technical/architecture/developer-engineer/architecture-summary.md)
- [开发实施指南](documentation/technical/architecture/developer-engineer/development-guidelines.md)
- [技术架构设计](documentation/technical/architecture/developer-engineer/technical-architecture.md)

### 设计文档  
- [UI/UX设计规范](documentation/sprints/sprint-0/user-stories/ui-design-specification.md)
- [功能模块分析](documentation/sprints/sprint-0/user-stories/functional-modules-analysis.md)
- [用户工作流程图](documentation/sprints/sprint-0/user-stories/complete-user-workflow-diagram.md)

### 测试文档
- [测试策略文档](documentation/sprints/sprint-0/qa-test-engineer/testing-strategy.md)

## ⚡ 快速开始

### 新Sprint开始时
1. **同步master最新代码**
   ```bash
   git checkout master && git pull origin master
   ```

2. **创建新Sprint分支**
   ```bash  
   git checkout -b sprint-{N}-{feature}
   ```

3. **查看Sprint任务**
   - 检查 `documentation/sprints/sprint-{N}/` 中的任务分解
   - 确认各角色的具体职责

4. **开始Daily Standup**
   - 每日9:00-9:15站会跟踪进度

### Sprint完成时
1. **质量检查**
   ```bash
   npm run quality:check
   npm run test:all
   ```

2. **创建Pull Request**
   ```bash
   git push origin sprint-{N}-{feature}
   # 在GitHub创建PR，等待用户审核
   ```

3. **文档归档**
   - 将Sprint工作文档归档到 `documentation/sprints/sprint-{N}/`
   - 更新相关技术文档和用户文档

## 🆘 常见问题和解决方案

### NESMA计算不准确
1. 检查BigDecimal精度设置
2. 对比PDF指南中的具体案例
3. 验证复杂度判定逻辑

### 测试失败
1. 运行完整测试套件确认范围
2. 检查测试环境配置
3. 查看详细错误日志

### 部署问题
1. 确认Docker环境配置
2. 检查数据库连接配置
3. 验证环境变量设置

---

**维护责任:** Scrum Master  
**更新频率:** 每Sprint结束后更新  
**最后更新:** Sprint 3 完成 (2025-09-10)

**重要提醒：这是政府项目，所有工作都要保持最高标准！**