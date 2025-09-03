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
├── README.md                 # 项目概述
├── agile-workspace/          # 敏捷工作区（当前Sprint工作区）
├── documentation/            # 项目文档库
│   ├── technical/           # 技术文档
│   ├── user/               # 用户文档和需求规范
│   └── sprints/            # Sprint开发记录归档
├── src/                     # 源代码（后续Sprint创建）
├── tests/                   # 测试代码（后续Sprint创建）
└── deployment/             # 部署配置（后续Sprint创建）
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

# 数据库
docker-compose up -d postgresql redis

# 完整开发环境
npm run dev:all
```

### 测试命令
```bash
# 单元测试
npm run test:unit

# 集成测试
npm run test:integration  

# 算法验证测试（重要！）
npm run test:nesma-validation

# 完整测试套件
npm run test:all
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

### Sprint 1 (规划中)
- [ ] NESMA核心计算引擎
- [ ] 数据模型设计
- [ ] 基础REST API
- [ ] 算法验证测试

### Sprint 2 (规划中)  
- [ ] 用户界面实现
- [ ] 前后端集成
- [ ] 用户交互流程
- [ ] 界面功能测试

### Sprint 3 (规划中)
- [ ] 系统集成测试
- [ ] 性能优化
- [ ] 安全加固
- [ ] 用户验收测试

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
**最后更新:** Sprint 0 完成 (2025-09-03)

**重要提醒：这是政府项目，所有工作都要保持最高标准！**