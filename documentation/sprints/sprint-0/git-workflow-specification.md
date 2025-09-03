# Git 工作流规范

## 项目分支管理策略

### 分支命名规范

**主分支：**
- `master` - 主分支，包含稳定的生产代码

**开发分支：**
- `sprint-{N}-{feature}` - Sprint开发分支  
  - 例如：`sprint-0-planning`、`sprint-1-core-calculation`、`sprint-2-ui-implementation`

**功能分支（如需要）：**
- `feature/{功能名称}` - 大功能独立开发分支
- `bugfix/{问题描述}` - 紧急修复分支

### Sprint 分支工作流

#### 1. Sprint 开始
```bash
# 从master创建新的Sprint分支
git checkout master
git pull origin master
git checkout -b sprint-{N}-{feature}
```

#### 2. Sprint 开发期间
```bash
# 在Sprint分支上进行开发
git add .
git commit -m "详细的提交说明"

# 定期推送到远程
git push origin sprint-{N}-{feature}
```

#### 3. Sprint 完成后
```bash
# 推送最终版本
git push origin sprint-{N}-{feature}

# 创建PR申请合并到master
# 等待Product Owner确认和代码审查
```

#### 4. 合并后清理
```bash
# 合并后删除本地分支
git checkout master
git pull origin master
git branch -d sprint-{N}-{feature}
```

### 提交信息规范

**格式：**
```
[类型] Sprint {N}: 简洁描述

详细说明（如需要）
- 具体变更1
- 具体变更2

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

**类型标识：**
- `feat` - 新功能
- `fix` - 修复问题
- `docs` - 文档更新
- `style` - 代码格式调整
- `refactor` - 代码重构
- `test` - 测试相关
- `chore` - 构建/工具变更

### 代码审查流程

#### Pull Request 要求
1. **标题格式：** `Sprint {N}: {功能描述}`
2. **描述内容：**
   ```markdown
   ## 功能概述
   - 本Sprint实现的核心功能
   
   ## 技术变更
   - 主要技术实现
   - 架构调整说明
   
   ## 测试覆盖
   - 单元测试覆盖情况
   - 集成测试验证结果
   
   ## 验收标准确认
   - [ ] 功能需求完全实现
   - [ ] 代码质量符合标准
   - [ ] 测试覆盖率达标
   - [ ] 文档更新完整
   ```

#### 审查清单
**Product Owner审查：**
- [ ] 功能需求100%实现
- [ ] 用户体验符合设计要求
- [ ] 业务逻辑正确无误

**技术审查：**
- [ ] 代码质量符合规范
- [ ] 测试覆盖率≥80%
- [ ] 性能指标达标
- [ ] 安全要求满足

### 分支保护规则

**Master分支保护：**
- 禁止直接推送
- 必须通过PR合并
- 必须通过所有CI检查
- 必须有Product Owner审批

**Sprint分支管理：**
- 每个Sprint一个独立分支
- Sprint结束后及时合并和清理
- 避免长期存在的功能分支

### 冲突解决策略

#### 预防冲突
1. 每日同步master最新变更
2. 小步提交，频繁集成
3. 团队成员及时沟通修改范围

#### 冲突解决
1. **发现冲突：**
   ```bash
   git checkout sprint-{N}-{feature}
   git fetch origin
   git merge origin/master
   ```

2. **解决冲突：**
   - 手工解决代码冲突
   - 运行完整测试套件
   - 确认功能正常工作

3. **提交解决结果：**
   ```bash
   git add .
   git commit -m "resolve merge conflicts with master"
   git push origin sprint-{N}-{feature}
   ```

### CI/CD 集成

#### 自动化检查
每个PR必须通过：
- [ ] 代码静态分析
- [ ] 单元测试套件
- [ ] 集成测试验证
- [ ] 安全扫描检查

#### 部署策略
- **Sprint分支：** 自动部署到测试环境
- **Master分支：** 人工确认后部署到生产环境

### 历史记录管理

#### 重要节点标签
```bash
# Sprint完成后打标签
git tag -a v0.{N}.0 -m "Sprint {N} 完成"
git push origin v0.{N}.0
```

#### 文档归档
- 每个Sprint的详细变更记录
- 重要决策和技术选型记录
- 问题解决过程文档化

## 团队协作原则

1. **及时沟通** - 有疑问立即讨论，避免盲目开发
2. **小步迭代** - 每日小提交，避免大批量变更
3. **质量第一** - 宁可延期，不允许质量妥协
4. **文档同步** - 代码和文档同步更新
5. **知识共享** - 技术方案团队共享讨论

---
**制定日期：** Sprint 0  
**负责人：** Scrum Master  
**审批：** 全体团队成员确认