-- 创建function_points主表
-- Version: V17__Create_function_points_table.sql
-- Description: 创建功能点主表以支持SimpleFunctionPoint实体

-- 创建功能点主表
CREATE TABLE function_points (
    id BIGSERIAL PRIMARY KEY,
    
    -- 项目关联
    project_id BIGINT NOT NULL,
    
    -- 功能点基本信息
    fp_type VARCHAR(10) NOT NULL,
    fp_name VARCHAR(200) NOT NULL,
    fp_description TEXT,
    business_purpose TEXT,
    
    -- 复杂度信息
    complexity_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    complexity_weight DECIMAL(19,4) NOT NULL DEFAULT 4.0,
    adjusted_complexity_weight DECIMAL(19,4),
    function_point_count DECIMAL(19,4) NOT NULL DEFAULT 1.0,
    calculated_fp_value DECIMAL(19,4) NOT NULL DEFAULT 0.0,
    
    -- 状态和版本管理
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version INTEGER NOT NULL DEFAULT 1,
    parent_fp_id BIGINT,
    sort_order INTEGER DEFAULT 0,
    is_baseline BOOLEAN NOT NULL DEFAULT FALSE,
    baseline_date TIMESTAMP,
    
    -- 审计字段
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    
    -- 外键约束
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (parent_fp_id) REFERENCES function_points(id),
    
    -- 检查约束
    CONSTRAINT chk_fp_type CHECK (fp_type IN ('ILF', 'EIF', 'EI', 'EO', 'EQ')),
    CONSTRAINT chk_complexity_level CHECK (complexity_level IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT chk_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED', 'ARCHIVED'))
);

-- 创建索引
CREATE INDEX idx_function_points_project_id ON function_points(project_id);
CREATE INDEX idx_function_points_fp_type ON function_points(fp_type);
CREATE INDEX idx_function_points_status ON function_points(status);
CREATE INDEX idx_function_points_created_at ON function_points(created_at);
CREATE INDEX idx_function_points_deleted_at ON function_points(deleted_at);
CREATE INDEX idx_function_points_parent_fp_id ON function_points(parent_fp_id);

-- 添加注释
COMMENT ON TABLE function_points IS '功能点主表 - 存储NESMA功能点基本信息';
COMMENT ON COLUMN function_points.fp_type IS '功能点类型: ILF-内部逻辑文件, EIF-外部接口文件, EI-外部输入, EO-外部输出, EQ-外部询问';
COMMENT ON COLUMN function_points.complexity_level IS '复杂度级别: LOW-低, MEDIUM-中, HIGH-高';
COMMENT ON COLUMN function_points.status IS '状态: DRAFT-草稿, PENDING-待审, APPROVED-已批准, REJECTED-已拒绝, ARCHIVED-已归档';