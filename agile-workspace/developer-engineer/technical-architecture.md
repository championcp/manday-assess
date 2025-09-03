# 软件规模评估系统 - 技术架构设计方案

## 项目概述

**项目名称：** 软件规模评估系统 (Software Scale Assessment System)  
**项目类型：** 政府信息化项目  
**核心功能：** NESMA功能点计算、费用评估、报告生成、多级审批  
**技术要求：** 高稳定性、高准确性、安全可靠、符合政府标准

## 一、技术栈选型方案

### 1. 前端技术栈

#### 主技术选型
```
核心框架：Vue 3.x + TypeScript
- 选择理由：
  ✓ 政府部门广泛使用，技术成熟稳定
  ✓ 中文文档完善，团队学习成本低
  ✓ 生态系统丰富，组件库选择多
  ✓ 性能优异，适合复杂表单和数据处理

UI组件库：Element Plus
- 选择理由：
  ✓ 专为Vue 3设计，完全支持TypeScript
  ✓ 组件丰富，覆盖政府系统所需的所有UI组件
  ✓ 设计风格严谨，符合政府系统气质
  ✓ 表格、表单组件功能强大，适合数据密集型应用

状态管理：Pinia
- 选择理由：
  ✓ Vue 3官方推荐的状态管理库
  ✓ 类型安全，与TypeScript完美集成
  ✓ API简洁，易于理解和维护
  ✓ 支持模块化状态管理
```

#### 辅助技术选型
```
构建工具：Vite 4.x
- 快速的开发服务器和构建工具
- 原生ES模块支持，开发体验优异
- 丰富的插件生态系统

路由管理：Vue Router 4.x
- Vue 3官方路由解决方案
- 支持路由守卫，便于权限控制
- 支持懒加载，优化性能

HTTP客户端：Axios
- 功能强大的HTTP库
- 支持请求/响应拦截器
- 完善的错误处理机制

图表库：ECharts 5.x
- 功能强大的数据可视化库
- 支持多种图表类型
- 性能优异，适合大数据量展示

工具库：
- Lodash：实用工具函数库
- Day.js：轻量级日期处理库
- UUID：唯一标识符生成
- File-saver：文件下载功能
```

### 2. 后端技术栈

#### 主技术选型
```
核心框架：Spring Boot 3.x + Java 17
- 选择理由：
  ✓ 政府项目首选技术栈，技术成熟可靠
  ✓ 企业级开发标准，安全性和稳定性有保障
  ✓ 丰富的生态系统，满足各种业务需求
  ✓ 强类型语言，减少运行时错误

数据访问：Spring Data JPA + MyBatis Plus
- Spring Data JPA：标准化数据访问，简化CRUD操作
- MyBatis Plus：复杂查询和SQL优化，性能更好

安全框架：Spring Security + JWT
- Spring Security：成熟的安全框架
- JWT：无状态认证，适合分布式系统

API文档：SpringDoc OpenAPI 3.0
- 自动生成API文档
- 支持Swagger UI界面
- 与Spring Boot无缝集成
```

#### 数据库选型
```
主数据库：PostgreSQL 14.x
- 选择理由：
  ✓ 开源免费，符合政府采购要求
  ✓ 数据完整性和一致性保障强
  ✓ 支持复杂查询和事务处理
  ✓ JSON字段支持，适合存储计算过程数据
  ✓ 备份恢复机制完善

缓存数据库：Redis 7.x
- 用途：会话存储、缓存热点数据、任务队列
- 提高系统响应速度和并发能力

文档存储：MinIO
- 用途：存储生成的PDF报告和附件文件
- 兼容S3 API，部署简单，安全可靠
```

### 3. 开发和部署技术

#### 开发环境
```
容器化：Docker + Docker Compose
- 统一开发环境，避免环境差异
- 便于本地开发和测试

版本控制：Git + GitLab/GitHub
- 代码版本管理
- 分支管理策略：GitFlow

代码质量：
- 前端：ESLint + Prettier + Husky
- 后端：Checkstyle + SpotBugs + PMD
- 单元测试：Jest (前端) + JUnit 5 (后端)
```

#### 部署方案
```
容器编排：Kubernetes (推荐) 或 Docker Swarm
- 容器化部署，便于扩展和管理
- 支持滚动更新，保证系统可用性
- 配置管理和密钥管理

负载均衡：Nginx
- 反向代理和负载均衡
- SSL终端处理
- 静态文件服务

监控运维：
- 应用监控：Prometheus + Grafana
- 日志管理：ELK Stack (Elasticsearch + Logstash + Kibana)
- 健康检查：Spring Boot Actuator
```

## 二、系统整体架构设计

### 1. 架构分层设计

```
┌─────────────────────────────────────────────────────────────┐
│                     表现层 (Presentation Layer)              │
├─────────────────────────────────────────────────────────────┤
│ Vue 3 前端应用                                               │
│ ├── 用户界面组件 (UI Components)                             │
│ ├── 状态管理 (Pinia Store)                                  │
│ ├── 路由管理 (Vue Router)                                   │
│ └── API通信 (Axios)                                         │
└─────────────────────────────────────────────────────────────┘
                              │ HTTP/HTTPS
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     网关层 (Gateway Layer)                   │
├─────────────────────────────────────────────────────────────┤
│ Nginx 反向代理                                               │
│ ├── 负载均衡                                                │
│ ├── SSL证书管理                                             │
│ ├── 静态资源服务                                            │
│ └── API路由分发                                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     应用层 (Application Layer)               │
├─────────────────────────────────────────────────────────────┤
│ Spring Boot 后端应用                                         │
│ ├── 控制器层 (Controller)                                   │
│ ├── 业务服务层 (Service)                                    │
│ ├── 安全认证 (Security)                                     │
│ └── API文档 (OpenAPI)                                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     业务层 (Business Layer)                  │
├─────────────────────────────────────────────────────────────┤
│ 核心业务逻辑                                                │
│ ├── NESMA计算引擎                                           │
│ ├── 成本计算引擎                                            │
│ ├── 审批流程引擎                                            │
│ ├── 报告生成引擎                                            │
│ └── 质量控制引擎                                            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     数据层 (Data Layer)                      │
├─────────────────────────────────────────────────────────────┤
│ 数据访问和存储                                              │
│ ├── Spring Data JPA (数据访问)                             │
│ ├── PostgreSQL (主数据库)                                  │
│ ├── Redis (缓存数据库)                                      │
│ └── MinIO (文档存储)                                        │
└─────────────────────────────────────────────────────────────┘
```

### 2. 微服务架构设计 (推荐方案)

```
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│   前端应用        │  │   API网关        │  │   认证服务        │
│   Vue 3 App      │  │   Nginx/Kong     │  │   Auth Service   │
│                  │  │                  │  │                  │
│ - 用户界面       │◄─┤ - 路由分发       │◄─┤ - 用户认证       │
│ - 状态管理       │  │ - 负载均衡       │  │ - 权限管理       │
│ - 路由控制       │  │ - 限流控制       │  │ - JWT令牌        │
└──────────────────┘  └──────────────────┘  └──────────────────┘
                              │
                   ┌──────────┼──────────┐
                   │          │          │
          ┌────────▼────┐ ┌──▼─────┐ ┌──▼─────────┐
          │ 核心业务服务 │ │ 工作流  │ │ 报告服务    │
          │Core Service │ │Workflow│ │Report Svc  │
          │             │ │Service │ │            │
          │- 功能点计算  │ │        │ │- 报告生成   │
          │- 成本计算   │ │- 审批流 │ │- 模板管理   │
          │- 项目管理   │ │- 流程控 │ │- 导出功能   │
          │- 数据验证   │ │- 状态管 │ │- 打印服务   │
          └─────────────┘ └────────┘ └────────────┘
                   │          │          │
                   └──────────┼──────────┘
                              │
                    ┌─────────▼─────────┐
                    │   数据服务层       │
                    │   Data Layer      │
                    │                   │
                    │ ┌─────────────────┤
                    │ │ PostgreSQL      │
                    │ │ (主数据库)      │
                    │ ├─────────────────┤
                    │ │ Redis           │
                    │ │ (缓存/会话)     │
                    │ ├─────────────────┤
                    │ │ MinIO           │
                    │ │ (文档存储)      │
                    │ └─────────────────┤
                    └───────────────────┘
```

## 三、核心功能模块设计

### 1. NESMA功能点计算引擎

#### 架构设计
```java
// 计算引擎核心接口
public interface FunctionPointCalculator {
    
    /**
     * 计算内部逻辑文件功能点
     * @param ilfData 内部逻辑文件数据
     * @return 计算结果
     */
    CalculationResult calculateILF(ILFData ilfData);
    
    /**
     * 计算外部接口文件功能点
     * @param eifData 外部接口文件数据
     * @return 计算结果
     */
    CalculationResult calculateEIF(EIFData eifData);
    
    /**
     * 计算外部输入功能点
     * @param eiData 外部输入数据
     * @return 计算结果
     */
    CalculationResult calculateEI(EIData eiData);
    
    /**
     * 计算外部输出功能点
     * @param eoData 外部输出数据
     * @return 计算结果
     */
    CalculationResult calculateEO(EOData eoData);
    
    /**
     * 计算外部查询功能点
     * @param eqData 外部查询数据
     * @return 计算结果
     */
    CalculationResult calculateEQ(EQData eqData);
    
    /**
     * 计算总功能点数
     * @param projectData 项目完整数据
     * @return 总计算结果
     */
    TotalCalculationResult calculateTotal(ProjectData projectData);
}
```

#### 计算引擎实现架构
```
计算引擎模块结构：
├── calculation/
│   ├── engine/
│   │   ├── FunctionPointCalculatorImpl.java     # 计算引擎实现
│   │   ├── ComplexityAnalyzer.java              # 复杂度分析器
│   │   └── ValidationEngine.java               # 数据验证引擎
│   ├── model/
│   │   ├── ILFData.java                        # 内部逻辑文件数据模型
│   │   ├── EIFData.java                        # 外部接口文件数据模型
│   │   ├── EIData.java                         # 外部输入数据模型
│   │   ├── EOData.java                         # 外部输出数据模型
│   │   ├── EQData.java                         # 外部查询数据模型
│   │   └── CalculationResult.java              # 计算结果模型
│   ├── rule/
│   │   ├── ComplexityRule.java                 # 复杂度判定规则
│   │   ├── FunctionPointRule.java              # 功能点计算规则
│   │   └── ValidationRule.java                # 数据验证规则
│   └── util/
│       ├── CalculationUtil.java                # 计算工具类
│       └── FormulaUtil.java                    # 公式处理工具
```

### 2. 成本计算引擎

#### 成本计算公式实现
```java
/**
 * 成本计算引擎
 * 实现指南中的成本计算公式
 */
@Service
public class CostCalculationEngine {
    
    /**
     * 软件开发费计算
     * 公式：软件开发费 = 功能点数 × 软件类别调整因子 × 软件质量特性调整系数 
     *               × 信创调整系数 × 软件开发基准生产率/人月折算系数 
     *               × 开发人月费用单价 + 直接非人力成本
     */
    public CostCalculationResult calculateDevelopmentCost(CostCalculationRequest request) {
        // 获取功能点数
        BigDecimal functionPoints = request.getFunctionPoints();
        
        // 获取各项调整因子
        BigDecimal categoryFactor = request.getCategoryFactor();      // 0.6-1.4
        BigDecimal qualityFactor = request.getQualityFactor();        // 0.65-1.35
        BigDecimal innovationFactor = request.getInnovationFactor();  // 信创调整系数
        BigDecimal productivityRatio = request.getProductivityRatio(); // 生产率/人月折算系数
        BigDecimal monthlyRate = request.getMonthlyRate();            // 开发人月费用单价
        BigDecimal directCost = request.getDirectCost();              // 直接非人力成本
        
        // 执行计算
        BigDecimal developmentCost = functionPoints
            .multiply(categoryFactor)
            .multiply(qualityFactor)
            .multiply(innovationFactor)
            .multiply(productivityRatio)
            .multiply(monthlyRate)
            .add(directCost);
            
        // 构建结果
        return CostCalculationResult.builder()
            .developmentCost(developmentCost)
            .calculationDetail(buildCalculationDetail(request))
            .calculationTime(LocalDateTime.now())
            .build();
    }
}
```

### 3. 审批工作流引擎

#### 工作流设计
```java
/**
 * 审批工作流引擎
 * 支持多级审批和流程控制
 */
@Service
public class ApprovalWorkflowEngine {
    
    /**
     * 工作流节点定义
     */
    public enum ApprovalNode {
        EVALUATOR_REVIEW("评估员初评"),
        DEPARTMENT_REVIEW("部门负责人审核"),
        EXPERT_REVIEW("专家评审"),
        FINAL_APPROVAL("最终审批");
    }
    
    /**
     * 启动审批流程
     */
    public WorkflowInstance startApprovalProcess(Long projectId, String initiator) {
        // 创建工作流实例
        // 设置初始状态
        // 分配给评估员
    }
    
    /**
     * 处理审批节点
     */
    public void processApprovalNode(Long workflowId, ApprovalAction action) {
        // 验证操作权限
        // 更新流程状态
        // 流转到下一节点或结束流程
        // 发送通知消息
    }
}
```

## 四、数据库架构设计

### 1. 核心数据模型

#### 项目管理相关表
```sql
-- 项目基本信息表
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    project_name VARCHAR(200) NOT NULL COMMENT '项目名称',
    project_code VARCHAR(50) UNIQUE NOT NULL COMMENT '项目编码',
    project_type VARCHAR(50) NOT NULL COMMENT '项目类型',
    software_category VARCHAR(50) NOT NULL COMMENT '软件类别',
    assessment_stage VARCHAR(20) NOT NULL COMMENT '评估阶段',
    description TEXT COMMENT '项目描述',
    created_by BIGINT NOT NULL COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 功能点计算数据表
CREATE TABLE function_point_calculations (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    calculation_type VARCHAR(10) NOT NULL COMMENT '计算类型(ILF/EIF/EI/EO/EQ)',
    module_name VARCHAR(100) NOT NULL COMMENT '模块名称',
    complexity_level VARCHAR(10) NOT NULL COMMENT '复杂度等级',
    data_elements INTEGER COMMENT '数据元素项',
    record_types INTEGER COMMENT '记录元素类型',
    file_types INTEGER COMMENT '文件类型',
    function_points INTEGER NOT NULL COMMENT '功能点数',
    calculation_detail JSONB COMMENT '计算详情',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 成本计算结果表
CREATE TABLE cost_calculations (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    total_function_points INTEGER NOT NULL COMMENT '总功能点数',
    category_factor DECIMAL(4,2) NOT NULL COMMENT '类别调整因子',
    quality_factor DECIMAL(4,2) NOT NULL COMMENT '质量调整系数',
    innovation_factor DECIMAL(4,2) NOT NULL COMMENT '信创调整系数',
    productivity_ratio DECIMAL(6,2) NOT NULL COMMENT '生产率人月折算系数',
    monthly_rate DECIMAL(10,2) NOT NULL COMMENT '开发人月费用单价',
    direct_cost DECIMAL(12,2) DEFAULT 0 COMMENT '直接非人力成本',
    total_cost DECIMAL(15,2) NOT NULL COMMENT '总成本',
    calculation_formula TEXT COMMENT '计算公式记录',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 审批流程相关表
```sql
-- 工作流实例表
CREATE TABLE workflow_instances (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    workflow_type VARCHAR(50) NOT NULL COMMENT '工作流类型',
    current_node VARCHAR(50) NOT NULL COMMENT '当前节点',
    status VARCHAR(20) NOT NULL COMMENT '流程状态',
    initiator BIGINT NOT NULL COMMENT '发起人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 审批记录表
CREATE TABLE approval_records (
    id BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT NOT NULL REFERENCES workflow_instances(id),
    node_name VARCHAR(50) NOT NULL COMMENT '审批节点',
    approver BIGINT NOT NULL COMMENT '审批人',
    action VARCHAR(20) NOT NULL COMMENT '审批动作',
    comments TEXT COMMENT '审批意见',
    approved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2. 数据库性能优化

#### 索引策略
```sql
-- 项目查询优化索引
CREATE INDEX idx_projects_code ON projects(project_code);
CREATE INDEX idx_projects_created_by ON projects(created_by);
CREATE INDEX idx_projects_updated_at ON projects(updated_at);

-- 功能点计算查询优化索引
CREATE INDEX idx_fp_calc_project_id ON function_point_calculations(project_id);
CREATE INDEX idx_fp_calc_type ON function_point_calculations(calculation_type);

-- 工作流查询优化索引
CREATE INDEX idx_workflow_project_id ON workflow_instances(project_id);
CREATE INDEX idx_workflow_status ON workflow_instances(status);
CREATE INDEX idx_approval_workflow_id ON approval_records(workflow_id);
```

## 五、安全架构设计

### 1. 认证和授权

#### JWT认证方案
```java
/**
 * JWT安全配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/projects/**").hasRole("EVALUATOR")
                .requestMatchers("/api/calculations/**").hasRole("EVALUATOR")
                .requestMatchers("/api/approvals/**").hasRole("APPROVER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
            .build();
    }
}
```

#### 角色权限设计
```java
/**
 * 角色权限枚举
 */
public enum Role {
    EVALUATOR("评估员", Set.of(
        Permission.PROJECT_CREATE,
        Permission.PROJECT_READ,
        Permission.PROJECT_UPDATE,
        Permission.CALCULATION_CREATE,
        Permission.CALCULATION_READ
    )),
    
    APPROVER("审核员", Set.of(
        Permission.PROJECT_READ,
        Permission.CALCULATION_READ,
        Permission.APPROVAL_PROCESS,
        Permission.REPORT_GENERATE
    )),
    
    ADMIN("管理员", Set.of(
        Permission.SYSTEM_CONFIG,
        Permission.USER_MANAGE,
        Permission.DATA_EXPORT,
        Permission.AUDIT_LOG
    )),
    
    VIEWER("查阅员", Set.of(
        Permission.PROJECT_READ,
        Permission.CALCULATION_READ,
        Permission.REPORT_READ
    ));
}
```

### 2. 数据安全

#### 敏感数据加密
```java
/**
 * 数据加密服务
 */
@Service
public class DataEncryptionService {
    
    private final AESUtil aesUtil;
    
    /**
     * 加密敏感字段
     */
    @EventListener
    public void encryptSensitiveData(EntityPrePersistEvent event) {
        Object entity = event.getEntity();
        if (entity instanceof SensitiveDataEntity) {
            encryptFields((SensitiveDataEntity) entity);
        }
    }
    
    /**
     * 解密敏感字段
     */
    @EventListener
    public void decryptSensitiveData(EntityPostLoadEvent event) {
        Object entity = event.getEntity();
        if (entity instanceof SensitiveDataEntity) {
            decryptFields((SensitiveDataEntity) entity);
        }
    }
}
```

#### 审计日志
```java
/**
 * 审计日志记录
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;            // 操作用户
    
    @Column(name = "action")
    private String action;          // 操作类型
    
    @Column(name = "resource_type")
    private String resourceType;    // 资源类型
    
    @Column(name = "resource_id")
    private String resourceId;      // 资源ID
    
    @Column(name = "old_value", columnDefinition = "JSONB")
    private String oldValue;        // 修改前数据
    
    @Column(name = "new_value", columnDefinition = "JSONB")
    private String newValue;        // 修改后数据
    
    @Column(name = "ip_address")
    private String ipAddress;       // 操作IP
    
    @Column(name = "user_agent")
    private String userAgent;       // 用户代理
    
    @Column(name = "created_at")
    private LocalDateTime createdAt; // 操作时间
}
```

## 六、项目目录结构设计

### 1. 前端项目结构
```
frontend/
├── public/                          # 静态资源
│   ├── favicon.ico
│   └── index.html
├── src/
│   ├── api/                        # API接口定义
│   │   ├── auth.ts                 # 认证接口
│   │   ├── project.ts              # 项目管理接口
│   │   ├── calculation.ts          # 计算接口
│   │   ├── approval.ts             # 审批接口
│   │   └── report.ts               # 报告接口
│   ├── components/                 # 公共组件
│   │   ├── common/                 # 通用组件
│   │   │   ├── AppHeader.vue       # 应用头部
│   │   │   ├── AppSidebar.vue      # 侧边栏
│   │   │   ├── AppFooter.vue       # 页脚
│   │   │   └── LoadingSpinner.vue  # 加载动画
│   │   ├── business/               # 业务组件
│   │   │   ├── FunctionPointTable.vue    # 功能点计算表格
│   │   │   ├── CostCalculator.vue         # 成本计算器
│   │   │   ├── ApprovalFlow.vue           # 审批流程组件
│   │   │   └── ReportViewer.vue           # 报告查看器
│   │   └── forms/                  # 表单组件
│   │       ├── ProjectForm.vue     # 项目表单
│   │       ├── CalculationForm.vue # 计算表单
│   │       └── ApprovalForm.vue    # 审批表单
│   ├── composables/                # 组合式函数
│   │   ├── useAuth.ts              # 认证逻辑
│   │   ├── useCalculation.ts       # 计算逻辑
│   │   └── useWorkflow.ts          # 工作流逻辑
│   ├── stores/                     # 状态管理
│   │   ├── auth.ts                 # 认证状态
│   │   ├── project.ts              # 项目状态
│   │   ├── calculation.ts          # 计算状态
│   │   └── approval.ts             # 审批状态
│   ├── types/                      # TypeScript类型定义
│   │   ├── api.ts                  # API类型
│   │   ├── auth.ts                 # 认证类型
│   │   ├── project.ts              # 项目类型
│   │   └── calculation.ts          # 计算类型
│   ├── utils/                      # 工具函数
│   │   ├── http.ts                 # HTTP客户端
│   │   ├── validation.ts           # 数据验证
│   │   ├── format.ts               # 数据格式化
│   │   └── export.ts               # 导出功能
│   ├── views/                      # 页面组件
│   │   ├── auth/                   # 认证页面
│   │   ├── dashboard/              # 仪表板
│   │   ├── projects/               # 项目管理
│   │   ├── calculations/           # 功能点计算
│   │   ├── approvals/              # 审批管理
│   │   ├── reports/                # 报告中心
│   │   └── admin/                  # 系统管理
│   ├── styles/                     # 样式文件
│   │   ├── variables.scss          # SCSS变量
│   │   ├── mixins.scss             # SCSS混入
│   │   ├── components.scss         # 组件样式
│   │   └── layouts.scss            # 布局样式
│   ├── App.vue                     # 根组件
│   └── main.ts                     # 应用入口
├── tests/                          # 测试文件
├── package.json                    # 依赖配置
├── vite.config.ts                  # Vite配置
├── tsconfig.json                   # TypeScript配置
└── .env.example                    # 环境变量示例
```

### 2. 后端项目结构
```
backend/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/government/assessment/
│       │       ├── AssessmentApplication.java           # 应用入口
│       │       ├── config/                             # 配置类
│       │       │   ├── SecurityConfig.java             # 安全配置
│       │       │   ├── DatabaseConfig.java             # 数据库配置
│       │       │   ├── RedisConfig.java                # Redis配置
│       │       │   └── SwaggerConfig.java              # API文档配置
│       │       ├── controller/                         # 控制器层
│       │       │   ├── AuthController.java             # 认证控制器
│       │       │   ├── ProjectController.java          # 项目管理控制器
│       │       │   ├── CalculationController.java      # 计算控制器
│       │       │   ├── ApprovalController.java         # 审批控制器
│       │       │   └── ReportController.java           # 报告控制器
│       │       ├── service/                            # 业务服务层
│       │       │   ├── auth/                           # 认证服务
│       │       │   ├── project/                        # 项目服务
│       │       │   ├── calculation/                    # 计算服务
│       │       │   ├── approval/                       # 审批服务
│       │       │   └── report/                         # 报告服务
│       │       ├── repository/                         # 数据访问层
│       │       │   ├── ProjectRepository.java          # 项目数据访问
│       │       │   ├── CalculationRepository.java      # 计算数据访问
│       │       │   └── ApprovalRepository.java         # 审批数据访问
│       │       ├── entity/                             # 实体类
│       │       │   ├── Project.java                    # 项目实体
│       │       │   ├── FunctionPointCalculation.java   # 功能点计算实体
│       │       │   ├── CostCalculation.java            # 成本计算实体
│       │       │   └── WorkflowInstance.java           # 工作流实例实体
│       │       ├── dto/                                # 数据传输对象
│       │       │   ├── request/                        # 请求DTO
│       │       │   └── response/                       # 响应DTO
│       │       ├── common/                             # 公共组件
│       │       │   ├── exception/                      # 异常处理
│       │       │   ├── util/                           # 工具类
│       │       │   └── constant/                       # 常量定义
│       │       └── aspect/                             # 切面编程
│       │           ├── AuditAspect.java                # 审计切面
│       │           └── LoggingAspect.java              # 日志切面
│       └── resources/
│           ├── application.yml                         # 应用配置
│           ├── application-dev.yml                     # 开发环境配置
│           ├── application-prod.yml                    # 生产环境配置
│           ├── db/
│           │   ├── migration/                          # 数据库迁移脚本
│           │   └── seed/                               # 初始数据
│           └── templates/                              # 报告模板
├── src/test/                                           # 测试代码
├── pom.xml                                             # Maven配置
└── Dockerfile                                          # Docker镜像构建
```

## 七、部署架构设计

### 1. 容器化部署方案

#### Docker Compose开发环境
```yaml
version: '3.8'

services:
  # 前端应用
  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
    environment:
      - NODE_ENV=development
      - VITE_API_BASE_URL=http://localhost:8080
    volumes:
      - ./frontend:/app
      - /app/node_modules
    depends_on:
      - backend

  # 后端应用
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=development
      - DATABASE_URL=jdbc:postgresql://postgres:5432/assessment
      - REDIS_URL=redis://redis:6379
    depends_on:
      - postgres
      - redis
    volumes:
      - ./backend:/app

  # PostgreSQL数据库
  postgres:
    image: postgres:14-alpine
    environment:
      - POSTGRES_DB=assessment
      - POSTGRES_USER=assessment_user
      - POSTGRES_PASSWORD=assessment_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/init:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"

  # Redis缓存
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  # MinIO对象存储
  minio:
    image: minio/minio:latest
    environment:
      - MINIO_ROOT_USER=minioadmin
      - MINIO_ROOT_PASSWORD=minioadmin123
    volumes:
      - minio_data:/data
    ports:
      - "9000:9000"
      - "9001:9001"
    command: server /data --console-address ":9001"

volumes:
  postgres_data:
  redis_data:
  minio_data:
```

### 2. 生产环境Kubernetes部署

#### Kubernetes配置示例
```yaml
# 后端应用部署
apiVersion: apps/v1
kind: Deployment
metadata:
  name: assessment-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: assessment-backend
  template:
    metadata:
      labels:
        app: assessment-backend
    spec:
      containers:
      - name: backend
        image: assessment/backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10

---
# 前端应用部署
apiVersion: apps/v1
kind: Deployment
metadata:
  name: assessment-frontend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: assessment-frontend
  template:
    metadata:
      labels:
        app: assessment-frontend
    spec:
      containers:
      - name: frontend
        image: assessment/frontend:latest
        ports:
        - containerPort: 80
        resources:
          requests:
            memory: "256Mi"
            cpu: "100m"
          limits:
            memory: "512Mi"
            cpu: "200m"
```

## 八、性能优化方案

### 1. 前端性能优化

#### 代码分割和懒加载
```typescript
// 路由懒加载配置
const routes = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/Dashboard.vue')
  },
  {
    path: '/projects',
    name: 'Projects',
    component: () => import('@/views/projects/ProjectList.vue')
  },
  {
    path: '/calculations',
    name: 'Calculations',
    component: () => import('@/views/calculations/CalculationList.vue')
  }
];

// 组件懒加载
const AsyncComponent = defineAsyncComponent({
  loader: () => import('@/components/business/FunctionPointTable.vue'),
  loadingComponent: LoadingSpinner,
  errorComponent: ErrorComponent,
  delay: 200,
  timeout: 3000
});
```

#### 数据缓存和状态管理
```typescript
// Pinia Store with caching
export const useCalculationStore = defineStore('calculation', () => {
  const calculations = ref<Calculation[]>([]);
  const cache = new Map<string, Calculation>();
  
  const getCalculation = async (id: string) => {
    // 检查缓存
    if (cache.has(id)) {
      return cache.get(id);
    }
    
    // 从API获取数据
    const result = await calculationApi.getById(id);
    cache.set(id, result);
    return result;
  };
  
  return {
    calculations,
    getCalculation
  };
});
```

### 2. 后端性能优化

#### 数据库查询优化
```java
/**
 * 查询优化示例
 */
@Repository
public class ProjectRepository {
    
    /**
     * 使用分页查询，避免一次性加载大量数据
     */
    @Query("SELECT p FROM Project p WHERE p.status = :status")
    Page<Project> findByStatus(@Param("status") String status, Pageable pageable);
    
    /**
     * 使用@EntityGraph避免N+1查询问题
     */
    @EntityGraph(attributePaths = {"calculations", "approvals"})
    @Query("SELECT p FROM Project p WHERE p.id = :id")
    Optional<Project> findByIdWithDetails(@Param("id") Long id);
    
    /**
     * 使用投影查询，只查询需要的字段
     */
    @Query("SELECT new com.government.assessment.dto.ProjectSummary(p.id, p.name, p.status) " +
           "FROM Project p WHERE p.createdBy = :userId")
    List<ProjectSummary> findProjectSummaries(@Param("userId") Long userId);
}
```

#### 缓存策略
```java
/**
 * Redis缓存配置
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))  // 30分钟过期
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
            
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}

/**
 * 缓存使用示例
 */
@Service
public class CalculationService {
    
    @Cacheable(value = "calculations", key = "#projectId")
    public CalculationResult getCalculationResult(Long projectId) {
        // 复杂计算逻辑
        return performCalculation(projectId);
    }
    
    @CacheEvict(value = "calculations", key = "#projectId")
    public void updateCalculation(Long projectId, CalculationData data) {
        // 更新计算数据，清除缓存
    }
}
```

## 九、质量控制和监控方案

### 1. 代码质量控制

#### 前端代码规范
```json
// .eslintrc.js
{
  "extends": [
    "@vue/typescript/recommended",
    "@vue/prettier",
    "@vue/prettier/@typescript-eslint"
  ],
  "rules": {
    "@typescript-eslint/no-unused-vars": "error",
    "@typescript-eslint/explicit-function-return-type": "warn",
    "vue/component-name-in-template-casing": ["error", "PascalCase"],
    "vue/no-unused-components": "error"
  }
}

// prettier.config.js
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "es5",
  "printWidth": 100
}
```

#### 后端代码规范
```xml
<!-- Checkstyle配置 -->
<module name="Checker">
    <module name="TreeWalker">
        <module name="JavadocMethod"/>
        <module name="JavadocType"/>
        <module name="JavadocVariable"/>
        <module name="ConstantName"/>
        <module name="LocalVariableName"/>
        <module name="MethodName"/>
        <module name="PackageName"/>
        <module name="ParameterName"/>
        <module name="TypeName"/>
        <module name="LineLength">
            <property name="max" value="120"/>
        </module>
    </module>
</module>
```

### 2. 系统监控方案

#### 应用监控配置
```yaml
# application.yml - 监控配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: software-assessment-system

# 自定义监控指标
@Component
public class CustomMetrics {
    
    private final Counter calculationCounter;
    private final Timer calculationTimer;
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        this.calculationCounter = Counter.builder("calculations.total")
            .description("Total number of calculations performed")
            .register(meterRegistry);
            
        this.calculationTimer = Timer.builder("calculations.duration")
            .description("Calculation execution time")
            .register(meterRegistry);
    }
    
    public void recordCalculation(Runnable calculation) {
        calculationCounter.increment();
        calculationTimer.recordCallable(() -> {
            calculation.run();
            return null;
        });
    }
}
```

## 十、开发规范和流程

### 1. Git分支管理策略

#### GitFlow分支模型
```
main (生产环境)
├── develop (开发环境)
│   ├── feature/function-point-calculation    # 功能点计算功能
│   ├── feature/cost-calculation             # 成本计算功能
│   ├── feature/approval-workflow            # 审批工作流功能
│   └── feature/report-generation            # 报告生成功能
├── release/v1.0.0                          # 发布分支
└── hotfix/urgent-bug-fix                    # 热修复分支
```

#### 提交消息规范
```
格式：<type>(<scope>): <subject>

类型 (type)：
- feat: 新功能
- fix: 错误修复
- docs: 文档更新
- style: 代码格式调整
- refactor: 代码重构
- test: 测试相关
- chore: 构建工具或辅助工具的变动

示例：
feat(calculation): 实现NESMA功能点计算引擎
fix(auth): 修复JWT令牌过期处理逻辑
docs(api): 更新API文档说明
```

### 2. 代码审查流程

#### Pull Request模板
```markdown
## 变更说明
<!-- 简要描述本次变更的内容和目标 -->

## 变更类型
- [ ] 新功能 (feature)
- [ ] 错误修复 (bug fix)
- [ ] 代码重构 (refactoring)
- [ ] 文档更新 (documentation)
- [ ] 其他 (other)

## 测试情况
- [ ] 单元测试已通过
- [ ] 集成测试已通过
- [ ] 手动测试已完成
- [ ] 代码审查已完成

## 影响范围
<!-- 描述变更可能影响的模块和功能 -->

## 检查清单
- [ ] 代码符合项目编码规范
- [ ] 已添加必要的注释和文档
- [ ] 已添加或更新相关测试
- [ ] 无安全漏洞和性能问题
- [ ] 与设计规范保持一致
```

### 3. CI/CD流程设计

#### GitHub Actions配置
```yaml
# .github/workflows/ci.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  frontend-test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - uses: actions/setup-node@v3
      with:
        node-version: '18'
        cache: 'npm'
        cache-dependency-path: frontend/package-lock.json
    
    - name: Install dependencies
      working-directory: ./frontend
      run: npm ci
    
    - name: Run linting
      working-directory: ./frontend
      run: npm run lint
    
    - name: Run tests
      working-directory: ./frontend
      run: npm run test:unit
    
    - name: Build application
      working-directory: ./frontend
      run: npm run build

  backend-test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Run tests
      working-directory: ./backend
      run: mvn clean test
    
    - name: Run code quality checks
      working-directory: ./backend
      run: mvn checkstyle:check spotbugs:check
    
    - name: Build application
      working-directory: ./backend
      run: mvn clean package -DskipTests

  deploy:
    needs: [frontend-test, backend-test]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
    - name: Deploy to production
      run: |
        # 部署脚本
        echo "Deploying to production environment"
```

## 技术架构总结

本技术架构设计方案完全符合政府项目要求，具有以下特点：

**技术特色：**
1. **稳定可靠** - 选用成熟的企业级技术栈
2. **安全合规** - 完整的安全防护和审计机制
3. **高性能** - 多级缓存和数据库优化策略
4. **可扩展** - 微服务架构，支持水平扩展
5. **易维护** - 清晰的代码结构和开发规范

**核心优势：**
1. **精确计算** - 严格按照PDF指南实现NESMA计算算法
2. **流程控制** - 完整的多级审批工作流支持
3. **数据安全** - 多层次的安全防护和数据加密
4. **运维友好** - 容器化部署和完善的监控体系
5. **开发高效** - 标准化的开发流程和工具支持

这个技术架构为软件规模评估系统提供了坚实的技术基础，确保系统能够满足政府部门的各项要求。