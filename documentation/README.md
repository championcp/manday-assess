# 软件规模评估系统 - 项目文档

## 文档组织结构

### 📋 项目概览
本文档库包含软件规模评估系统的所有技术文档、用户文档和Sprint开发记录。

### 📁 目录结构

```
documentation/
├── technical/                    # 技术文档
│   └── architecture/            # 系统架构文档
│       └── developer-engineer/  # Sprint 0架构设计成果
├── user/                        # 用户文档
│   ├── requirements/            # 用户需求和业务规范
│   │   └── 长沙市财政评审中心政府投资信息化项目评审指南.pdf
│   └── manuals/                 # 用户操作手册 (后续添加)
└── sprints/                     # Sprint开发记录
    └── sprint-0/                # Sprint 0规划和设计文档
        ├── user-stories/        # 用户故事和UI/UX设计
        ├── qa-test-engineer/    # 测试策略和质量保障
        ├── sprint-0-planning-preparation.md
        ├── product-owner-task-assignment.md
        └── git-workflow-specification.md
```

### 📖 文档说明

#### 技术文档 (technical/)
- **架构设计文档** - 系统整体架构、技术选型、实施计划
- **API文档** - (后续Sprint添加)
- **部署文档** - (后续Sprint添加)

#### 用户文档 (user/)
- **需求规范文档** - 用户提供的业务需求和评审指南
- **用户操作手册** - 系统使用指南 (后续Sprint添加)
- **管理员文档** - 系统管理和维护 (后续Sprint添加)

#### Sprint记录 (sprints/)
每个Sprint的完整开发记录，包括：
- 需求分析和用户故事
- 技术设计和实施方案  
- 测试计划和质量保障
- 团队协作和流程文档

### 🔄 文档维护规范

#### 文档更新原则
1. **同步更新** - 代码变更时同步更新相关文档
2. **版本控制** - 重要文档变更记录版本历史
3. **质量标准** - 文档内容准确、完整、易懂

#### Sprint文档归档流程
1. Sprint开发期间，文档保存在 `agile-workspace/` 工作区
2. Sprint完成后，正式文档归档到 `documentation/sprints/sprint-N/`
3. 技术文档同时更新到 `documentation/technical/`
4. 用户文档更新到 `documentation/user/`

#### 文档命名规范
- 使用中文文件名，描述清晰具体
- 包含创建日期或版本信息（如适用）
- 文件名避免特殊字符和空格

### 📚 重要文档索引

#### Sprint 0 核心文档
- [技术架构总结](technical/architecture/developer-engineer/architecture-summary.md)
- [开发实施指南](technical/architecture/developer-engineer/development-guidelines.md) 
- [UI/UX设计规范](sprints/sprint-0/user-stories/ui-design-specification.md)
- [测试策略文档](sprints/sprint-0/qa-test-engineer/testing-strategy.md)
- [Git工作流规范](sprints/sprint-0/git-workflow-specification.md)

#### 需求和规范
- [政府评审指南 (PDF)](user/requirements/长沙市财政评审中心政府投资信息化项目评审指南.pdf)

---

**维护责任：** 全体团队成员  
**更新频率：** 每Sprint结束后更新  
**最后更新：** Sprint 0 (2025-09-03)