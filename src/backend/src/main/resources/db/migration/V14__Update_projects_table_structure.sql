-- 更新项目表结构以匹配实体类
-- Version: V14__Update_projects_table_structure.sql
-- Description: 添加Project实体类中定义的缺失字段

-- 添加缺失的字段
ALTER TABLE projects 
ADD COLUMN IF NOT EXISTS project_code VARCHAR(50) UNIQUE,
ADD COLUMN IF NOT EXISTS priority VARCHAR(20),
ADD COLUMN IF NOT EXISTS budget_amount DECIMAL(19,4),
ADD COLUMN IF NOT EXISTS estimated_person_months DECIMAL(8,2),
ADD COLUMN IF NOT EXISTS start_date TIMESTAMP,
ADD COLUMN IF NOT EXISTS end_date TIMESTAMP,
ADD COLUMN IF NOT EXISTS department_id BIGINT,
ADD COLUMN IF NOT EXISTS client_department VARCHAR(100),
ADD COLUMN IF NOT EXISTS client_contact_person VARCHAR(50),
ADD COLUMN IF NOT EXISTS client_contact_phone VARCHAR(20),
ADD COLUMN IF NOT EXISTS client_contact_email VARCHAR(100),
ADD COLUMN IF NOT EXISTS technical_requirements TEXT,
ADD COLUMN IF NOT EXISTS business_requirements TEXT,
ADD COLUMN IF NOT EXISTS project_manager_id BIGINT,
ADD COLUMN IF NOT EXISTS review_status VARCHAR(30) DEFAULT 'NOT_REVIEWED',
ADD COLUMN IF NOT EXISTS review_comments TEXT,
ADD COLUMN IF NOT EXISTS reviewer_id BIGINT,
ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS approval_status VARCHAR(30) DEFAULT 'PENDING',
ADD COLUMN IF NOT EXISTS approved_by BIGINT,
ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS completion_percentage DECIMAL(5,2) DEFAULT 0.00,
ADD COLUMN IF NOT EXISTS actual_person_months DECIMAL(8,2),
ADD COLUMN IF NOT EXISTS actual_budget DECIMAL(19,4),
ADD COLUMN IF NOT EXISTS risk_level VARCHAR(20) DEFAULT 'LOW',
ADD COLUMN IF NOT EXISTS risk_assessment TEXT,
ADD COLUMN IF NOT EXISTS attachments TEXT,
ADD COLUMN IF NOT EXISTS remarks TEXT,
ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1;

-- 修改列名以匹配实体类映射
ALTER TABLE projects RENAME COLUMN name TO project_name;
ALTER TABLE projects RENAME COLUMN description TO project_description; 
ALTER TABLE projects RENAME COLUMN status TO project_status;

-- 添加外键约束
-- ALTER TABLE projects ADD CONSTRAINT fk_projects_department_id 
--   FOREIGN KEY (department_id) REFERENCES departments(id);
-- ALTER TABLE projects ADD CONSTRAINT fk_projects_project_manager_id 
--   FOREIGN KEY (project_manager_id) REFERENCES users(id);
-- ALTER TABLE projects ADD CONSTRAINT fk_projects_reviewer_id 
--   FOREIGN KEY (reviewer_id) REFERENCES users(id);
-- ALTER TABLE projects ADD CONSTRAINT fk_projects_approved_by 
--   FOREIGN KEY (approved_by) REFERENCES users(id);

-- 更新索引
CREATE INDEX IF NOT EXISTS idx_project_code ON projects(project_code);
CREATE INDEX IF NOT EXISTS idx_project_priority ON projects(priority);
CREATE INDEX IF NOT EXISTS idx_project_status ON projects(project_status);
CREATE INDEX IF NOT EXISTS idx_project_review_status ON projects(review_status);
CREATE INDEX IF NOT EXISTS idx_project_approval_status ON projects(approval_status);
CREATE INDEX IF NOT EXISTS idx_project_department_id ON projects(department_id);
CREATE INDEX IF NOT EXISTS idx_project_manager_id ON projects(project_manager_id);
CREATE INDEX IF NOT EXISTS idx_project_start_date ON projects(start_date);
CREATE INDEX IF NOT EXISTS idx_project_end_date ON projects(end_date);

-- 为现有数据生成project_code
UPDATE projects SET project_code = 'PROJ-' || LPAD(id::text, 6, '0') WHERE project_code IS NULL;

-- 添加约束
ALTER TABLE projects ALTER COLUMN project_code SET NOT NULL;

-- 更新表注释
COMMENT ON COLUMN projects.project_code IS '项目编号';
COMMENT ON COLUMN projects.priority IS '项目优先级: HIGH-高, MEDIUM-中, LOW-低';
COMMENT ON COLUMN projects.budget_amount IS '预算金额';
COMMENT ON COLUMN projects.estimated_person_months IS '预计人月数';
COMMENT ON COLUMN projects.completion_percentage IS '完成百分比';
COMMENT ON COLUMN projects.risk_level IS '风险等级: HIGH-高, MEDIUM-中, LOW-低';