-- NESMA功能点评估系统 - 项目管理和状态管理相关表
-- Version: V2__Project_management_tables.sql
-- Description: 扩展项目管理功能，添加项目状态历史和基础配置表

-- 扩展项目表，添加NESMA评估必需字段
ALTER TABLE projects ADD COLUMN IF NOT EXISTS project_code VARCHAR(50) UNIQUE;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS start_date DATE;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS end_date DATE;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS budget_amount DECIMAL(19,4);
ALTER TABLE projects ADD COLUMN IF NOT EXISTS technology_platform VARCHAR(200);
ALTER TABLE projects ADD COLUMN IF NOT EXISTS business_domain VARCHAR(200);
ALTER TABLE projects ADD COLUMN IF NOT EXISTS complexity_level VARCHAR(20) DEFAULT 'MEDIUM';
ALTER TABLE projects ADD COLUMN IF NOT EXISTS total_function_points DECIMAL(19,4) DEFAULT 0;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS total_development_hours DECIMAL(19,4) DEFAULT 0;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS total_cost_estimate DECIMAL(19,4) DEFAULT 0;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS current_version INTEGER DEFAULT 1;
ALTER TABLE projects ADD COLUMN IF NOT EXISTS updated_by BIGINT;

-- 添加外键约束
ALTER TABLE projects ADD CONSTRAINT fk_projects_updated_by 
    FOREIGN KEY (updated_by) REFERENCES users(id);

-- 为新字段创建索引
CREATE INDEX IF NOT EXISTS idx_projects_project_code ON projects(project_code);
CREATE INDEX IF NOT EXISTS idx_projects_complexity_level ON projects(complexity_level);
CREATE INDEX IF NOT EXISTS idx_projects_start_date ON projects(start_date);

-- 添加检查约束
ALTER TABLE projects ADD CONSTRAINT chk_projects_status 
    CHECK (status IN ('DRAFT', 'IN_PROGRESS', 'UNDER_REVIEW', 'APPROVED', 'COMPLETED', 'ARCHIVED', 'REJECTED'));

ALTER TABLE projects ADD CONSTRAINT chk_projects_complexity_level 
    CHECK (complexity_level IN ('LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH'));

-- 创建项目状态变更历史表
CREATE TABLE IF NOT EXISTS project_status_history (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    change_reason TEXT,
    changed_by BIGINT NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 为项目状态历史表创建索引
CREATE INDEX idx_project_status_history_project_id ON project_status_history(project_id);
CREATE INDEX idx_project_status_history_changed_at ON project_status_history(changed_at);
CREATE INDEX idx_project_status_history_new_status ON project_status_history(new_status);
CREATE INDEX idx_project_status_history_deleted_at ON project_status_history(deleted_at);

-- 创建NESMA评估配置表
CREATE TABLE IF NOT EXISTS nesma_configurations (
    id BIGSERIAL PRIMARY KEY,
    config_name VARCHAR(100) NOT NULL UNIQUE,
    config_type VARCHAR(50) NOT NULL,
    config_value TEXT NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 为配置表创建索引
CREATE INDEX idx_nesma_configurations_config_type ON nesma_configurations(config_type);
CREATE INDEX idx_nesma_configurations_is_active ON nesma_configurations(is_active);
CREATE INDEX idx_nesma_configurations_deleted_at ON nesma_configurations(deleted_at);

-- 添加检查约束
ALTER TABLE nesma_configurations ADD CONSTRAINT chk_nesma_configurations_config_type 
    CHECK (config_type IN ('COMPLEXITY_WEIGHT', 'HOUR_RATE', 'ADJUSTMENT_FACTOR', 'VALIDATION_RULE', 'BUSINESS_RULE'));

-- 创建项目团队成员表
CREATE TABLE IF NOT EXISTS project_team_members (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    join_date DATE NOT NULL DEFAULT CURRENT_DATE,
    leave_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    responsibilities TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    UNIQUE(project_id, user_id, role, join_date)
);

-- 为团队成员表创建索引
CREATE INDEX idx_project_team_members_project_id ON project_team_members(project_id);
CREATE INDEX idx_project_team_members_user_id ON project_team_members(user_id);
CREATE INDEX idx_project_team_members_role ON project_team_members(role);
CREATE INDEX idx_project_team_members_is_active ON project_team_members(is_active);
CREATE INDEX idx_project_team_members_deleted_at ON project_team_members(deleted_at);

-- 添加检查约束
ALTER TABLE project_team_members ADD CONSTRAINT chk_project_team_members_role 
    CHECK (role IN ('PROJECT_MANAGER', 'TECHNICAL_LEAD', 'BUSINESS_ANALYST', 'DEVELOPER', 'TESTER', 'REVIEWER'));

-- 插入默认NESMA配置数据
INSERT INTO nesma_configurations (config_name, config_type, config_value, description, created_by) VALUES
('ILF_LOW_COMPLEXITY_WEIGHT', 'COMPLEXITY_WEIGHT', '7', 'ILF低复杂度权重', 1),
('ILF_MEDIUM_COMPLEXITY_WEIGHT', 'COMPLEXITY_WEIGHT', '10', 'ILF中复杂度权重', 1),
('ILF_HIGH_COMPLEXITY_WEIGHT', 'COMPLEXITY_WEIGHT', '15', 'ILF高复杂度权重', 1),
('EIF_LOW_COMPLEXITY_WEIGHT', 'COMPLEXITY_WEIGHT', '5', 'EIF低复杂度权重', 1),
('EIF_MEDIUM_COMPLEXITY_WEIGHT', 'COMPLEXITY_WEIGHT', '7', 'EIF中复杂度权重', 1),
('EIF_HIGH_COMPLEXITY_WEIGHT', 'COMPLEXITY_WEIGHT', '10', 'EIF高复杂度权重', 1),
('EI_LOW_COMPLEXITY_WEIGHT', 'COMPLEXITY_WEIGHT', '3', 'EI低复杂度权重', 1),
('EI_MEDIUM_COMPLEXITY_WEIGHT', 'COMPLEXITY_WEIGHT', '4', 'EI中复杂度权重', 1),
('EI_HIGH_COMPLEXITY_WEIGHT', 'COMPLEXITY_WEIGHT', '6', 'EI高复杂度权重', 1),
('EO_LOW_COMPLEXITY_WEIGHT', 'COMPLEXITY_WEIGHT', '4', 'EO低复杂度权重', 1),
('EO_MEDIUM_COMPLEXITY_WEIGHT', 'COMPLEXITY_WEIGHT', '5', 'EO中复杂度权重', 1),
('EO_HIGH_COMPLEXITY_WEIGHT', 'COMPLEXITY_WEIGHT', '7', 'EO高复杂度权重', 1),
('EQ_LOW_COMPLEXITY_WEIGHT', 'COMPLEXITY_WEIGHT', '3', 'EQ低复杂度权重', 1),
('EQ_MEDIUM_COMPLEXITY_WEIGHT', 'COMPLEXITY_WEIGHT', '4', 'EQ中复杂度权重', 1),
('EQ_HIGH_COMPLEXITY_WEIGHT', 'COMPLEXITY_WEIGHT', '6', 'EQ高复杂度权重', 1),
('DEFAULT_HOUR_RATE', 'HOUR_RATE', '800.00', '默认人日单价（元）', 1),
('DEVELOPMENT_HOUR_RATE', 'HOUR_RATE', '1200.00', '开发人员人日单价（元）', 1),
('SENIOR_HOUR_RATE', 'HOUR_RATE', '1500.00', '高级开发人员人日单价（元）', 1)
ON CONFLICT (config_name) DO NOTHING;

-- 更新用户表，添加更多角色支持
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_role;
ALTER TABLE users ADD CONSTRAINT chk_users_role 
    CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'PROJECT_MANAGER', 'EVALUATOR', 'REVIEWER', 'USER'));

-- 添加注释
COMMENT ON TABLE project_status_history IS '项目状态变更历史表';
COMMENT ON TABLE nesma_configurations IS 'NESMA评估配置表';
COMMENT ON TABLE project_team_members IS '项目团队成员表';

COMMENT ON COLUMN projects.project_code IS '项目编码';
COMMENT ON COLUMN projects.complexity_level IS '项目复杂度等级：LOW, MEDIUM, HIGH, VERY_HIGH';
COMMENT ON COLUMN projects.total_function_points IS 'NESMA功能点总数';
COMMENT ON COLUMN projects.total_development_hours IS '预估开发工时';
COMMENT ON COLUMN projects.total_cost_estimate IS '预估总成本';
COMMENT ON COLUMN projects.current_version IS '当前版本号';

COMMENT ON COLUMN project_status_history.previous_status IS '变更前状态';
COMMENT ON COLUMN project_status_history.new_status IS '变更后状态';
COMMENT ON COLUMN project_status_history.change_reason IS '状态变更原因';
COMMENT ON COLUMN project_status_history.version IS '状态变更版本号';

COMMENT ON COLUMN nesma_configurations.config_type IS '配置类型：COMPLEXITY_WEIGHT, HOUR_RATE, ADJUSTMENT_FACTOR, VALIDATION_RULE, BUSINESS_RULE';
COMMENT ON COLUMN nesma_configurations.config_value IS '配置值（JSON格式）';

COMMENT ON COLUMN project_team_members.role IS '团队角色：PROJECT_MANAGER, TECHNICAL_LEAD, BUSINESS_ANALYST, DEVELOPER, TESTER, REVIEWER';
COMMENT ON COLUMN project_team_members.responsibilities IS '具体职责描述';