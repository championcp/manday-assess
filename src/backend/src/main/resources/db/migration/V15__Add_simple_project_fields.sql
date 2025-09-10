-- 添加SimpleProject实体所需的缺失字段
-- Version: V15__Add_simple_project_fields.sql
-- Description: 为SimpleProject实体添加业务相关字段

-- 添加SimpleProject实体需要的字段
ALTER TABLE projects 
ADD COLUMN IF NOT EXISTS client_name VARCHAR(100),
ADD COLUMN IF NOT EXISTS technology_platform VARCHAR(100),
ADD COLUMN IF NOT EXISTS business_domain VARCHAR(100),
ADD COLUMN IF NOT EXISTS complexity_level VARCHAR(50),
ADD COLUMN IF NOT EXISTS total_function_points DECIMAL(12,2),
ADD COLUMN IF NOT EXISTS total_development_hours DECIMAL(12,2),
ADD COLUMN IF NOT EXISTS total_cost_estimate DECIMAL(19,4),
ADD COLUMN IF NOT EXISTS current_version INTEGER DEFAULT 1;

-- 更新索引
CREATE INDEX IF NOT EXISTS idx_project_client_name ON projects(client_name);
CREATE INDEX IF NOT EXISTS idx_project_technology_platform ON projects(technology_platform);
CREATE INDEX IF NOT EXISTS idx_project_business_domain ON projects(business_domain);
CREATE INDEX IF NOT EXISTS idx_project_complexity_level ON projects(complexity_level);

-- 添加字段注释
COMMENT ON COLUMN projects.client_name IS '客户名称';
COMMENT ON COLUMN projects.technology_platform IS '技术平台';
COMMENT ON COLUMN projects.business_domain IS '业务领域';
COMMENT ON COLUMN projects.complexity_level IS '复杂度等级: LOW-低, MEDIUM-中, HIGH-高';
COMMENT ON COLUMN projects.total_function_points IS '总功能点数';
COMMENT ON COLUMN projects.total_development_hours IS '总开发工时';
COMMENT ON COLUMN projects.total_cost_estimate IS '总成本估算';
COMMENT ON COLUMN projects.current_version IS '当前版本';