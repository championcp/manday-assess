-- NESMA功能点评估系统 - NESMA功能点核心表结构
-- Version: V3__NESMA_function_points_core.sql
-- Description: 创建NESMA功能点评估的核心表结构

-- 创建功能点主表
CREATE TABLE IF NOT EXISTS function_points (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    fp_type VARCHAR(10) NOT NULL,
    fp_name VARCHAR(200) NOT NULL,
    fp_description TEXT,
    business_purpose TEXT,
    complexity_level VARCHAR(20) NOT NULL,
    complexity_weight DECIMAL(19,4) NOT NULL,
    adjusted_complexity_weight DECIMAL(19,4),
    function_point_count DECIMAL(19,4) NOT NULL DEFAULT 1,
    calculated_fp_value DECIMAL(19,4) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version INTEGER NOT NULL DEFAULT 1,
    parent_fp_id BIGINT,
    sort_order INTEGER DEFAULT 0,
    is_baseline BOOLEAN NOT NULL DEFAULT false,
    baseline_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_fp_id) REFERENCES function_points(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 为功能点主表创建索引
CREATE INDEX idx_function_points_project_id ON function_points(project_id);
CREATE INDEX idx_function_points_fp_type ON function_points(fp_type);
CREATE INDEX idx_function_points_complexity_level ON function_points(complexity_level);
CREATE INDEX idx_function_points_status ON function_points(status);
CREATE INDEX idx_function_points_is_baseline ON function_points(is_baseline);
CREATE INDEX idx_function_points_parent_fp_id ON function_points(parent_fp_id);
CREATE INDEX idx_function_points_deleted_at ON function_points(deleted_at);
CREATE INDEX idx_function_points_fp_name ON function_points(fp_name);

-- 添加功能点主表约束
ALTER TABLE function_points ADD CONSTRAINT chk_function_points_fp_type 
    CHECK (fp_type IN ('ILF', 'EIF', 'EI', 'EO', 'EQ'));

ALTER TABLE function_points ADD CONSTRAINT chk_function_points_complexity_level 
    CHECK (complexity_level IN ('LOW', 'MEDIUM', 'HIGH'));

ALTER TABLE function_points ADD CONSTRAINT chk_function_points_status 
    CHECK (status IN ('DRAFT', 'IN_REVIEW', 'APPROVED', 'REJECTED', 'ARCHIVED'));

-- 创建数据元素表（用于存储RET、DET、FTR等）
CREATE TABLE IF NOT EXISTS function_point_elements (
    id BIGSERIAL PRIMARY KEY,
    function_point_id BIGINT NOT NULL,
    element_type VARCHAR(20) NOT NULL,
    element_name VARCHAR(200) NOT NULL,
    element_description TEXT,
    element_count INTEGER NOT NULL DEFAULT 1,
    is_key_field BOOLEAN NOT NULL DEFAULT false,
    data_type VARCHAR(50),
    field_length INTEGER,
    is_mandatory BOOLEAN NOT NULL DEFAULT false,
    business_rules TEXT,
    validation_rules TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (function_point_id) REFERENCES function_points(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 为数据元素表创建索引
CREATE INDEX idx_function_point_elements_function_point_id ON function_point_elements(function_point_id);
CREATE INDEX idx_function_point_elements_element_type ON function_point_elements(element_type);
CREATE INDEX idx_function_point_elements_element_name ON function_point_elements(element_name);
CREATE INDEX idx_function_point_elements_deleted_at ON function_point_elements(deleted_at);

-- 添加数据元素表约束
ALTER TABLE function_point_elements ADD CONSTRAINT chk_function_point_elements_element_type 
    CHECK (element_type IN ('RET', 'DET', 'FTR', 'INPUT_DATA', 'OUTPUT_DATA', 'QUERY_DATA'));

-- 创建功能点关系表（处理功能点之间的依赖关系）
CREATE TABLE IF NOT EXISTS function_point_relationships (
    id BIGSERIAL PRIMARY KEY,
    source_fp_id BIGINT NOT NULL,
    target_fp_id BIGINT NOT NULL,
    relationship_type VARCHAR(30) NOT NULL,
    relationship_description TEXT,
    dependency_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (source_fp_id) REFERENCES function_points(id) ON DELETE CASCADE,
    FOREIGN KEY (target_fp_id) REFERENCES function_points(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    UNIQUE(source_fp_id, target_fp_id, relationship_type)
);

-- 为关系表创建索引
CREATE INDEX idx_function_point_relationships_source_fp_id ON function_point_relationships(source_fp_id);
CREATE INDEX idx_function_point_relationships_target_fp_id ON function_point_relationships(target_fp_id);
CREATE INDEX idx_function_point_relationships_relationship_type ON function_point_relationships(relationship_type);
CREATE INDEX idx_function_point_relationships_deleted_at ON function_point_relationships(deleted_at);

-- 添加关系表约束
ALTER TABLE function_point_relationships ADD CONSTRAINT chk_function_point_relationships_type 
    CHECK (relationship_type IN ('DEPENDS_ON', 'TRIGGERS', 'INCLUDES', 'EXTENDS', 'USES_DATA', 'PROVIDES_DATA'));

ALTER TABLE function_point_relationships ADD CONSTRAINT chk_function_point_relationships_dependency_level 
    CHECK (dependency_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'));

-- 防止自引用
ALTER TABLE function_point_relationships ADD CONSTRAINT chk_function_point_relationships_no_self_reference 
    CHECK (source_fp_id != target_fp_id);

-- 创建功能点复杂度评估记录表
CREATE TABLE IF NOT EXISTS complexity_assessments (
    id BIGSERIAL PRIMARY KEY,
    function_point_id BIGINT NOT NULL,
    assessment_type VARCHAR(30) NOT NULL,
    ret_count INTEGER DEFAULT 0,
    det_count INTEGER DEFAULT 0,
    ftr_count INTEGER DEFAULT 0,
    input_complexity_score DECIMAL(19,4) DEFAULT 0,
    output_complexity_score DECIMAL(19,4) DEFAULT 0,
    processing_complexity_score DECIMAL(19,4) DEFAULT 0,
    total_complexity_score DECIMAL(19,4) NOT NULL DEFAULT 0,
    complexity_level VARCHAR(20) NOT NULL,
    complexity_justification TEXT,
    assessed_by BIGINT NOT NULL,
    assessment_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_final BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    FOREIGN KEY (function_point_id) REFERENCES function_points(id) ON DELETE CASCADE,
    FOREIGN KEY (assessed_by) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 为复杂度评估表创建索引
CREATE INDEX idx_complexity_assessments_function_point_id ON complexity_assessments(function_point_id);
CREATE INDEX idx_complexity_assessments_assessment_type ON complexity_assessments(assessment_type);
CREATE INDEX idx_complexity_assessments_complexity_level ON complexity_assessments(complexity_level);
CREATE INDEX idx_complexity_assessments_assessed_by ON complexity_assessments(assessed_by);
CREATE INDEX idx_complexity_assessments_assessment_date ON complexity_assessments(assessment_date);
CREATE INDEX idx_complexity_assessments_deleted_at ON complexity_assessments(deleted_at);

-- 添加复杂度评估表约束
ALTER TABLE complexity_assessments ADD CONSTRAINT chk_complexity_assessments_assessment_type 
    CHECK (assessment_type IN ('INITIAL', 'REVISED', 'FINAL', 'PEER_REVIEW', 'EXPERT_REVIEW'));

ALTER TABLE complexity_assessments ADD CONSTRAINT chk_complexity_assessments_complexity_level 
    CHECK (complexity_level IN ('LOW', 'MEDIUM', 'HIGH'));

-- 创建功能点变更历史表
CREATE TABLE IF NOT EXISTS function_point_history (
    id BIGSERIAL PRIMARY KEY,
    function_point_id BIGINT NOT NULL,
    change_type VARCHAR(20) NOT NULL,
    previous_values JSONB,
    new_values JSONB,
    change_reason TEXT,
    change_impact TEXT,
    changed_by BIGINT NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version_before INTEGER NOT NULL,
    version_after INTEGER NOT NULL,
    is_major_change BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (function_point_id) REFERENCES function_points(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id)
);

-- 为变更历史表创建索引
CREATE INDEX idx_function_point_history_function_point_id ON function_point_history(function_point_id);
CREATE INDEX idx_function_point_history_change_type ON function_point_history(change_type);
CREATE INDEX idx_function_point_history_changed_at ON function_point_history(changed_at);
CREATE INDEX idx_function_point_history_changed_by ON function_point_history(changed_by);
CREATE INDEX idx_function_point_history_is_major_change ON function_point_history(is_major_change);

-- 添加变更历史表约束
ALTER TABLE function_point_history ADD CONSTRAINT chk_function_point_history_change_type 
    CHECK (change_type IN ('CREATE', 'UPDATE', 'DELETE', 'COMPLEXITY_CHANGE', 'STATUS_CHANGE', 'BASELINE'));

-- 创建触发器函数：自动记录功能点变更历史
CREATE OR REPLACE FUNCTION record_function_point_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        -- 记录更新操作
        INSERT INTO function_point_history (
            function_point_id, change_type, previous_values, new_values,
            changed_by, version_before, version_after, is_major_change,
            change_reason
        ) VALUES (
            NEW.id,
            CASE 
                WHEN OLD.complexity_level != NEW.complexity_level THEN 'COMPLEXITY_CHANGE'
                WHEN OLD.status != NEW.status THEN 'STATUS_CHANGE'
                ELSE 'UPDATE'
            END,
            to_jsonb(OLD.*),
            to_jsonb(NEW.*),
            COALESCE(NEW.updated_by, NEW.created_by),
            OLD.version,
            NEW.version,
            (OLD.complexity_level != NEW.complexity_level OR OLD.status != NEW.status),
            '系统自动记录变更'
        );
        RETURN NEW;
    ELSIF TG_OP = 'INSERT' THEN
        -- 记录创建操作
        INSERT INTO function_point_history (
            function_point_id, change_type, new_values,
            changed_by, version_before, version_after, is_major_change
        ) VALUES (
            NEW.id, 'CREATE', to_jsonb(NEW.*),
            NEW.created_by, 0, NEW.version, true
        );
        RETURN NEW;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
DROP TRIGGER IF EXISTS trigger_record_function_point_changes ON function_points;
CREATE TRIGGER trigger_record_function_point_changes
    AFTER INSERT OR UPDATE ON function_points
    FOR EACH ROW
    EXECUTE FUNCTION record_function_point_changes();

-- 添加表注释
COMMENT ON TABLE function_points IS 'NESMA功能点主表';
COMMENT ON TABLE function_point_elements IS '功能点数据元素表';
COMMENT ON TABLE function_point_relationships IS '功能点关系表';
COMMENT ON TABLE complexity_assessments IS '功能点复杂度评估记录表';
COMMENT ON TABLE function_point_history IS '功能点变更历史表';

-- 添加字段注释
COMMENT ON COLUMN function_points.fp_type IS '功能点类型：ILF, EIF, EI, EO, EQ';
COMMENT ON COLUMN function_points.complexity_level IS '复杂度等级：LOW, MEDIUM, HIGH';
COMMENT ON COLUMN function_points.complexity_weight IS '复杂度权重值';
COMMENT ON COLUMN function_points.calculated_fp_value IS '计算得出的功能点值';
COMMENT ON COLUMN function_points.is_baseline IS '是否为基线版本';
COMMENT ON COLUMN function_points.parent_fp_id IS '父功能点ID（用于功能点分解）';

COMMENT ON COLUMN function_point_elements.element_type IS '元素类型：RET, DET, FTR, INPUT_DATA, OUTPUT_DATA, QUERY_DATA';
COMMENT ON COLUMN function_point_elements.is_key_field IS '是否为关键字段';

COMMENT ON COLUMN function_point_relationships.relationship_type IS '关系类型：DEPENDS_ON, TRIGGERS, INCLUDES, EXTENDS, USES_DATA, PROVIDES_DATA';
COMMENT ON COLUMN function_point_relationships.dependency_level IS '依赖级别：LOW, MEDIUM, HIGH, CRITICAL';

COMMENT ON COLUMN complexity_assessments.ret_count IS 'RET数量（记录元素类型）';
COMMENT ON COLUMN complexity_assessments.det_count IS 'DET数量（数据元素类型）';
COMMENT ON COLUMN complexity_assessments.ftr_count IS 'FTR数量（文件类型引用）';
COMMENT ON COLUMN complexity_assessments.is_final IS '是否为最终评估结果';