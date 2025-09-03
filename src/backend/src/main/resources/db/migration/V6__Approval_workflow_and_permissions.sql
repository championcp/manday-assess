-- NESMA功能点评估系统 - 审批流程和权限管理表
-- Version: V6__Approval_workflow_and_permissions.sql
-- Description: 创建审批流程、权限管理和用户角色相关表

-- 创建角色表
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    role_code VARCHAR(20) NOT NULL UNIQUE,
    role_description TEXT,
    is_system_role BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    level_hierarchy INTEGER NOT NULL DEFAULT 0,
    department VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 创建权限表
CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    permission_name VARCHAR(100) NOT NULL UNIQUE,
    permission_code VARCHAR(50) NOT NULL UNIQUE,
    permission_description TEXT,
    resource_type VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    is_system_permission BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 创建角色权限关联表
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    is_granted BOOLEAN NOT NULL DEFAULT true,
    granted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by BIGINT NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    revoked_by BIGINT,
    notes TEXT,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES users(id),
    FOREIGN KEY (revoked_by) REFERENCES users(id),
    UNIQUE(role_id, permission_id)
);

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    assignment_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id),
    UNIQUE(user_id, role_id)
);

-- 创建审批工作流模板表
CREATE TABLE IF NOT EXISTS approval_workflow_templates (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(200) NOT NULL UNIQUE,
    template_description TEXT,
    workflow_type VARCHAR(50) NOT NULL,
    applicable_to VARCHAR(100) NOT NULL,
    is_sequential BOOLEAN NOT NULL DEFAULT true,
    is_active BOOLEAN NOT NULL DEFAULT true,
    auto_start_conditions JSONB,
    completion_conditions JSONB,
    escalation_rules JSONB,
    notification_settings JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 创建审批工作流实例表
CREATE TABLE IF NOT EXISTS approval_workflows (
    id BIGSERIAL PRIMARY KEY,
    workflow_template_id BIGINT NOT NULL,
    workflow_name VARCHAR(200) NOT NULL,
    workflow_description TEXT,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    target_data JSONB,
    
    -- 工作流状态
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    
    -- 时间信息
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    deadline TIMESTAMP WITH TIME ZONE,
    
    -- 发起信息
    initiated_by BIGINT NOT NULL,
    initiation_reason TEXT,
    
    -- 当前处理信息
    current_node_id BIGINT,
    current_approver_id BIGINT,
    next_approvers JSONB,
    
    -- 结果信息
    final_decision VARCHAR(20),
    final_decision_reason TEXT,
    overall_comments TEXT,
    
    -- 审计信息
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    FOREIGN KEY (workflow_template_id) REFERENCES approval_workflow_templates(id),
    FOREIGN KEY (initiated_by) REFERENCES users(id),
    FOREIGN KEY (current_approver_id) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 创建审批节点表
CREATE TABLE IF NOT EXISTS approval_nodes (
    id BIGSERIAL PRIMARY KEY,
    workflow_template_id BIGINT NOT NULL,
    node_name VARCHAR(100) NOT NULL,
    node_description TEXT,
    node_type VARCHAR(30) NOT NULL,
    node_order INTEGER NOT NULL,
    
    -- 审批配置
    required_approvers_count INTEGER NOT NULL DEFAULT 1,
    approval_type VARCHAR(30) NOT NULL DEFAULT 'ANY',
    approver_roles JSONB,
    approver_users JSONB,
    
    -- 条件配置
    activation_conditions JSONB,
    skip_conditions JSONB,
    auto_approval_conditions JSONB,
    
    -- 时间配置
    timeout_hours INTEGER DEFAULT 72,
    escalation_rules JSONB,
    reminder_settings JSONB,
    
    -- 其他配置
    allow_delegate BOOLEAN NOT NULL DEFAULT true,
    allow_parallel_approval BOOLEAN NOT NULL DEFAULT false,
    required_attachments JSONB,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    FOREIGN KEY (workflow_template_id) REFERENCES approval_workflow_templates(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    UNIQUE(workflow_template_id, node_order)
);

-- 创建审批记录表
CREATE TABLE IF NOT EXISTS approval_records (
    id BIGSERIAL PRIMARY KEY,
    workflow_id BIGINT NOT NULL,
    node_id BIGINT NOT NULL,
    approver_id BIGINT NOT NULL,
    delegate_approver_id BIGINT,
    
    -- 审批决策
    decision VARCHAR(20) NOT NULL,
    decision_reason TEXT,
    comments TEXT,
    attachments JSONB,
    
    -- 审批时间
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    decided_at TIMESTAMP WITH TIME ZONE,
    deadline TIMESTAMP WITH TIME ZONE,
    
    -- 状态信息
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    is_escalated BOOLEAN NOT NULL DEFAULT false,
    escalation_level INTEGER DEFAULT 0,
    
    -- 通知信息
    notification_sent BOOLEAN NOT NULL DEFAULT false,
    reminder_count INTEGER NOT NULL DEFAULT 0,
    last_reminder_sent TIMESTAMP WITH TIME ZONE,
    
    -- 审计信息
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (workflow_id) REFERENCES approval_workflows(id) ON DELETE CASCADE,
    FOREIGN KEY (node_id) REFERENCES approval_nodes(id),
    FOREIGN KEY (approver_id) REFERENCES users(id),
    FOREIGN KEY (delegate_approver_id) REFERENCES users(id)
);

-- 创建所有表的索引
CREATE INDEX idx_roles_role_code ON roles(role_code);
CREATE INDEX idx_roles_is_active ON roles(is_active);
CREATE INDEX idx_roles_deleted_at ON roles(deleted_at);

CREATE INDEX idx_permissions_permission_code ON permissions(permission_code);
CREATE INDEX idx_permissions_resource_type ON permissions(resource_type);
CREATE INDEX idx_permissions_action_type ON permissions(action_type);
CREATE INDEX idx_permissions_deleted_at ON permissions(deleted_at);

CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);
CREATE INDEX idx_role_permissions_is_granted ON role_permissions(is_granted);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_user_roles_is_active ON user_roles(is_active);

CREATE INDEX idx_approval_workflow_templates_workflow_type ON approval_workflow_templates(workflow_type);
CREATE INDEX idx_approval_workflow_templates_is_active ON approval_workflow_templates(is_active);
CREATE INDEX idx_approval_workflow_templates_deleted_at ON approval_workflow_templates(deleted_at);

CREATE INDEX idx_approval_workflows_target_type_id ON approval_workflows(target_type, target_id);
CREATE INDEX idx_approval_workflows_status ON approval_workflows(status);
CREATE INDEX idx_approval_workflows_initiated_by ON approval_workflows(initiated_by);
CREATE INDEX idx_approval_workflows_current_approver_id ON approval_workflows(current_approver_id);
CREATE INDEX idx_approval_workflows_started_at ON approval_workflows(started_at);
CREATE INDEX idx_approval_workflows_deadline ON approval_workflows(deadline);
CREATE INDEX idx_approval_workflows_deleted_at ON approval_workflows(deleted_at);

CREATE INDEX idx_approval_nodes_workflow_template_id ON approval_nodes(workflow_template_id);
CREATE INDEX idx_approval_nodes_node_order ON approval_nodes(node_order);
CREATE INDEX idx_approval_nodes_node_type ON approval_nodes(node_type);
CREATE INDEX idx_approval_nodes_deleted_at ON approval_nodes(deleted_at);

CREATE INDEX idx_approval_records_workflow_id ON approval_records(workflow_id);
CREATE INDEX idx_approval_records_approver_id ON approval_records(approver_id);
CREATE INDEX idx_approval_records_decision ON approval_records(decision);
CREATE INDEX idx_approval_records_status ON approval_records(status);
CREATE INDEX idx_approval_records_assigned_at ON approval_records(assigned_at);
CREATE INDEX idx_approval_records_deadline ON approval_records(deadline);

-- 添加约束条件
ALTER TABLE roles ADD CONSTRAINT chk_roles_level_hierarchy CHECK (level_hierarchy >= 0);

ALTER TABLE permissions ADD CONSTRAINT chk_permissions_resource_type 
    CHECK (resource_type IN ('PROJECT', 'FUNCTION_POINT', 'CALCULATION', 'USER', 'ROLE', 'SYSTEM', 'REPORT'));

ALTER TABLE permissions ADD CONSTRAINT chk_permissions_action_type 
    CHECK (action_type IN ('CREATE', 'READ', 'UPDATE', 'DELETE', 'APPROVE', 'REVIEW', 'EXPORT', 'IMPORT', 'EXECUTE'));

ALTER TABLE approval_workflow_templates ADD CONSTRAINT chk_approval_workflow_templates_workflow_type 
    CHECK (workflow_type IN ('PROJECT_APPROVAL', 'CALCULATION_APPROVAL', 'USER_MANAGEMENT', 'SYSTEM_CONFIGURATION'));

ALTER TABLE approval_workflow_templates ADD CONSTRAINT chk_approval_workflow_templates_applicable_to 
    CHECK (applicable_to IN ('PROJECTS', 'CALCULATION_RESULTS', 'FUNCTION_POINTS', 'USER_ACCOUNTS', 'SYSTEM_SETTINGS'));

ALTER TABLE approval_workflows ADD CONSTRAINT chk_approval_workflows_status 
    CHECK (status IN ('PENDING', 'IN_PROGRESS', 'APPROVED', 'REJECTED', 'CANCELLED', 'EXPIRED'));

ALTER TABLE approval_workflows ADD CONSTRAINT chk_approval_workflows_priority 
    CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'));

ALTER TABLE approval_workflows ADD CONSTRAINT chk_approval_workflows_target_type 
    CHECK (target_type IN ('PROJECT', 'CALCULATION_RESULT', 'FUNCTION_POINT', 'USER', 'SYSTEM_CONFIG'));

ALTER TABLE approval_workflows ADD CONSTRAINT chk_approval_workflows_final_decision 
    CHECK (final_decision IS NULL OR final_decision IN ('APPROVED', 'REJECTED', 'CANCELLED'));

ALTER TABLE approval_nodes ADD CONSTRAINT chk_approval_nodes_node_type 
    CHECK (node_type IN ('START', 'APPROVAL', 'REVIEW', 'NOTIFICATION', 'CONDITION', 'END'));

ALTER TABLE approval_nodes ADD CONSTRAINT chk_approval_nodes_approval_type 
    CHECK (approval_type IN ('ANY', 'ALL', 'MAJORITY', 'WEIGHTED'));

ALTER TABLE approval_records ADD CONSTRAINT chk_approval_records_decision 
    CHECK (decision IN ('APPROVED', 'REJECTED', 'RETURNED', 'DELEGATED', 'CANCELLED'));

ALTER TABLE approval_records ADD CONSTRAINT chk_approval_records_status 
    CHECK (status IN ('PENDING', 'IN_REVIEW', 'COMPLETED', 'EXPIRED', 'ESCALATED'));

-- 插入默认角色
INSERT INTO roles (role_name, role_code, role_description, is_system_role, level_hierarchy, created_by) VALUES
('超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', true, 0, 1),
('系统管理员', 'ADMIN', '系统管理员，负责用户和系统配置管理', true, 1, 1),
('项目经理', 'PROJECT_MANAGER', '项目经理，负责项目管理和评估过程管控', false, 2, 1),
('评估专家', 'EVALUATOR', 'NESMA评估专家，负责功能点评估和计算', false, 3, 1),
('评审员', 'REVIEWER', '评审员，负责评估结果审核', false, 3, 1),
('普通用户', 'USER', '普通用户，只能查看和使用基本功能', false, 4, 1)
ON CONFLICT (role_code) DO NOTHING;

-- 插入默认权限
INSERT INTO permissions (permission_name, permission_code, permission_description, resource_type, action_type, is_system_permission, created_by) VALUES
-- 项目权限
('创建项目', 'PROJECT_CREATE', '创建新项目', 'PROJECT', 'CREATE', false, 1),
('查看项目', 'PROJECT_READ', '查看项目信息', 'PROJECT', 'READ', false, 1),
('修改项目', 'PROJECT_UPDATE', '修改项目信息', 'PROJECT', 'UPDATE', false, 1),
('删除项目', 'PROJECT_DELETE', '删除项目', 'PROJECT', 'DELETE', false, 1),
('审批项目', 'PROJECT_APPROVE', '审批项目', 'PROJECT', 'APPROVE', false, 1),

-- 功能点权限
('创建功能点', 'FP_CREATE', '创建功能点', 'FUNCTION_POINT', 'CREATE', false, 1),
('查看功能点', 'FP_READ', '查看功能点', 'FUNCTION_POINT', 'READ', false, 1),
('修改功能点', 'FP_UPDATE', '修改功能点', 'FUNCTION_POINT', 'UPDATE', false, 1),
('删除功能点', 'FP_DELETE', '删除功能点', 'FUNCTION_POINT', 'DELETE', false, 1),

-- 计算权限
('执行计算', 'CALC_EXECUTE', '执行NESMA计算', 'CALCULATION', 'EXECUTE', false, 1),
('查看计算结果', 'CALC_READ', '查看计算结果', 'CALCULATION', 'READ', false, 1),
('审核计算结果', 'CALC_REVIEW', '审核计算结果', 'CALCULATION', 'REVIEW', false, 1),
('审批计算结果', 'CALC_APPROVE', '审批计算结果', 'CALCULATION', 'APPROVE', false, 1),

-- 用户管理权限
('用户管理', 'USER_MANAGE', '用户管理', 'USER', 'UPDATE', true, 1),
('角色管理', 'ROLE_MANAGE', '角色管理', 'ROLE', 'UPDATE', true, 1),

-- 系统权限
('系统配置', 'SYSTEM_CONFIG', '系统配置管理', 'SYSTEM', 'UPDATE', true, 1),
('导出报表', 'REPORT_EXPORT', '导出报表', 'REPORT', 'EXPORT', false, 1)
ON CONFLICT (permission_code) DO NOTHING;

-- 分配默认角色权限
WITH role_permission_mapping AS (
    SELECT r.id as role_id, p.id as permission_id
    FROM roles r
    CROSS JOIN permissions p
    WHERE (r.role_code = 'SUPER_ADMIN') -- 超级管理员拥有所有权限
    
    UNION ALL
    
    SELECT r.id as role_id, p.id as permission_id
    FROM roles r
    CROSS JOIN permissions p
    WHERE r.role_code = 'ADMIN' 
    AND p.permission_code IN ('PROJECT_READ', 'PROJECT_CREATE', 'PROJECT_UPDATE', 
                             'FP_READ', 'CALC_READ', 'USER_MANAGE', 'ROLE_MANAGE', 
                             'SYSTEM_CONFIG', 'REPORT_EXPORT')
    
    UNION ALL
    
    SELECT r.id as role_id, p.id as permission_id
    FROM roles r
    CROSS JOIN permissions p
    WHERE r.role_code = 'PROJECT_MANAGER' 
    AND p.permission_code IN ('PROJECT_CREATE', 'PROJECT_READ', 'PROJECT_UPDATE', 'PROJECT_APPROVE',
                             'FP_CREATE', 'FP_READ', 'FP_UPDATE', 
                             'CALC_EXECUTE', 'CALC_READ', 'CALC_REVIEW', 'REPORT_EXPORT')
    
    UNION ALL
    
    SELECT r.id as role_id, p.id as permission_id
    FROM roles r
    CROSS JOIN permissions p
    WHERE r.role_code = 'EVALUATOR' 
    AND p.permission_code IN ('PROJECT_READ', 'FP_CREATE', 'FP_READ', 'FP_UPDATE',
                             'CALC_EXECUTE', 'CALC_READ', 'REPORT_EXPORT')
    
    UNION ALL
    
    SELECT r.id as role_id, p.id as permission_id
    FROM roles r
    CROSS JOIN permissions p
    WHERE r.role_code = 'REVIEWER' 
    AND p.permission_code IN ('PROJECT_READ', 'FP_READ', 'CALC_READ', 'CALC_REVIEW', 'CALC_APPROVE')
    
    UNION ALL
    
    SELECT r.id as role_id, p.id as permission_id
    FROM roles r
    CROSS JOIN permissions p
    WHERE r.role_code = 'USER' 
    AND p.permission_code IN ('PROJECT_READ', 'FP_READ', 'CALC_READ')
)
INSERT INTO role_permissions (role_id, permission_id, granted_by)
SELECT role_id, permission_id, 1
FROM role_permission_mapping
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 为默认管理员用户分配角色
INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, 1
FROM users u
CROSS JOIN roles r
WHERE u.username = 'admin' AND r.role_code = 'SUPER_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- 创建默认审批工作流模板
INSERT INTO approval_workflow_templates (template_name, template_description, workflow_type, applicable_to, created_by) VALUES
('项目评估审批流程', '项目NESMA功能点评估结果审批流程', 'PROJECT_APPROVAL', 'PROJECTS', 1),
('计算结果审核流程', 'NESMA计算结果审核确认流程', 'CALCULATION_APPROVAL', 'CALCULATION_RESULTS', 1)
ON CONFLICT (template_name) DO NOTHING;

-- 添加表注释
COMMENT ON TABLE roles IS '角色表';
COMMENT ON TABLE permissions IS '权限表';
COMMENT ON TABLE role_permissions IS '角色权限关联表';
COMMENT ON TABLE user_roles IS '用户角色关联表';
COMMENT ON TABLE approval_workflow_templates IS '审批工作流模板表';
COMMENT ON TABLE approval_workflows IS '审批工作流实例表';
COMMENT ON TABLE approval_nodes IS '审批节点表';
COMMENT ON TABLE approval_records IS '审批记录表';

-- 添加重要字段注释
COMMENT ON COLUMN roles.level_hierarchy IS '角色层级，数字越小权限越高';
COMMENT ON COLUMN permissions.resource_type IS '资源类型：PROJECT, FUNCTION_POINT, CALCULATION, USER, ROLE, SYSTEM, REPORT';
COMMENT ON COLUMN permissions.action_type IS '操作类型：CREATE, READ, UPDATE, DELETE, APPROVE, REVIEW, EXPORT, IMPORT, EXECUTE';
COMMENT ON COLUMN approval_workflows.target_type IS '审批对象类型';
COMMENT ON COLUMN approval_workflows.target_id IS '审批对象ID';
COMMENT ON COLUMN approval_nodes.approval_type IS '审批类型：ANY(任一), ALL(全部), MAJORITY(多数), WEIGHTED(加权)';