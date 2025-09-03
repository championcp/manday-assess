-- NESMA功能点评估系统 - 计算结果和历史记录表
-- Version: V5__Calculation_results_and_history.sql
-- Description: 创建NESMA计算结果存储和历史跟踪相关表

-- 创建NESMA计算结果主表
CREATE TABLE IF NOT EXISTS calculation_results (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    calculation_name VARCHAR(200) NOT NULL,
    calculation_description TEXT,
    calculation_type VARCHAR(30) NOT NULL,
    calculation_method VARCHAR(50) NOT NULL DEFAULT 'NESMA_2.1',
    calculation_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    calculated_by BIGINT NOT NULL,
    
    -- NESMA计算统计
    total_ilf_count INTEGER NOT NULL DEFAULT 0,
    total_eif_count INTEGER NOT NULL DEFAULT 0,
    total_ei_count INTEGER NOT NULL DEFAULT 0,
    total_eo_count INTEGER NOT NULL DEFAULT 0,
    total_eq_count INTEGER NOT NULL DEFAULT 0,
    
    -- 各类型功能点分数
    ilf_function_points DECIMAL(19,4) NOT NULL DEFAULT 0,
    eif_function_points DECIMAL(19,4) NOT NULL DEFAULT 0,
    ei_function_points DECIMAL(19,4) NOT NULL DEFAULT 0,
    eo_function_points DECIMAL(19,4) NOT NULL DEFAULT 0,
    eq_function_points DECIMAL(19,4) NOT NULL DEFAULT 0,
    
    -- 复杂度分布统计
    low_complexity_count INTEGER NOT NULL DEFAULT 0,
    medium_complexity_count INTEGER NOT NULL DEFAULT 0,
    high_complexity_count INTEGER NOT NULL DEFAULT 0,
    
    -- 总计算结果
    unadjusted_function_points DECIMAL(19,4) NOT NULL DEFAULT 0,
    adjustment_factor DECIMAL(19,4) NOT NULL DEFAULT 1.0,
    adjusted_function_points DECIMAL(19,4) NOT NULL DEFAULT 0,
    
    -- 成本估算
    development_hours_estimate DECIMAL(19,4),
    development_cost_estimate DECIMAL(19,4),
    hourly_rate DECIMAL(19,4),
    
    -- 质量度量
    complexity_distribution JSONB,
    risk_factors JSONB,
    quality_metrics JSONB,
    assumptions JSONB,
    
    -- 审核状态
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    review_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT,
    approved_at TIMESTAMP WITH TIME ZONE,
    approval_notes TEXT,
    
    -- 版本控制
    version INTEGER NOT NULL DEFAULT 1,
    is_baseline BOOLEAN NOT NULL DEFAULT false,
    baseline_date TIMESTAMP WITH TIME ZONE,
    previous_version_id BIGINT,
    
    -- 审计字段
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP WITH TIME ZONE,
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (calculated_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id),
    FOREIGN KEY (previous_version_id) REFERENCES calculation_results(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- 为计算结果表创建索引
CREATE INDEX idx_calculation_results_project_id ON calculation_results(project_id);
CREATE INDEX idx_calculation_results_calculation_type ON calculation_results(calculation_type);
CREATE INDEX idx_calculation_results_status ON calculation_results(status);
CREATE INDEX idx_calculation_results_review_status ON calculation_results(review_status);
CREATE INDEX idx_calculation_results_calculated_by ON calculation_results(calculated_by);
CREATE INDEX idx_calculation_results_calculation_date ON calculation_results(calculation_date);
CREATE INDEX idx_calculation_results_is_baseline ON calculation_results(is_baseline);
CREATE INDEX idx_calculation_results_deleted_at ON calculation_results(deleted_at);

-- 添加约束
ALTER TABLE calculation_results ADD CONSTRAINT chk_calculation_results_calculation_type 
    CHECK (calculation_type IN ('INITIAL_ESTIMATE', 'DETAILED_ESTIMATE', 'REVISED_ESTIMATE', 'FINAL_CALCULATION', 'POST_IMPLEMENTATION'));

ALTER TABLE calculation_results ADD CONSTRAINT chk_calculation_results_status 
    CHECK (status IN ('DRAFT', 'IN_REVIEW', 'APPROVED', 'REJECTED', 'ARCHIVED'));

ALTER TABLE calculation_results ADD CONSTRAINT chk_calculation_results_review_status 
    CHECK (review_status IN ('PENDING', 'IN_REVIEW', 'APPROVED', 'REJECTED', 'REQUIRES_REVISION'));

-- 创建计算详情表（记录每个功能点的计算过程）
CREATE TABLE IF NOT EXISTS calculation_details (
    id BIGSERIAL PRIMARY KEY,
    calculation_result_id BIGINT NOT NULL,
    function_point_id BIGINT NOT NULL,
    fp_type VARCHAR(10) NOT NULL,
    fp_name VARCHAR(200) NOT NULL,
    complexity_level VARCHAR(20) NOT NULL,
    
    -- 详细计算参数
    ret_count INTEGER DEFAULT 0,
    det_count INTEGER DEFAULT 0,
    ftr_count INTEGER DEFAULT 0,
    
    -- 计算步骤
    base_weight DECIMAL(19,4) NOT NULL,
    adjustment_factors JSONB,
    adjusted_weight DECIMAL(19,4) NOT NULL,
    quantity DECIMAL(19,4) NOT NULL DEFAULT 1,
    calculated_value DECIMAL(19,4) NOT NULL,
    
    -- 计算说明
    calculation_notes TEXT,
    complexity_justification TEXT,
    manual_adjustments JSONB,
    
    -- 计算时间
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (calculation_result_id) REFERENCES calculation_results(id) ON DELETE CASCADE,
    FOREIGN KEY (function_point_id) REFERENCES function_points(id),
    UNIQUE(calculation_result_id, function_point_id)
);

-- 为计算详情表创建索引
CREATE INDEX idx_calculation_details_calculation_result_id ON calculation_details(calculation_result_id);
CREATE INDEX idx_calculation_details_function_point_id ON calculation_details(function_point_id);
CREATE INDEX idx_calculation_details_fp_type ON calculation_details(fp_type);
CREATE INDEX idx_calculation_details_complexity_level ON calculation_details(complexity_level);

-- 创建计算历史表
CREATE TABLE IF NOT EXISTS calculation_history (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    calculation_result_id BIGINT NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    action_description TEXT,
    
    -- 变更前后对比
    previous_values JSONB,
    new_values JSONB,
    changes_summary JSONB,
    
    -- 影响分析
    impact_assessment TEXT,
    affected_function_points JSONB,
    cost_impact DECIMAL(19,4) DEFAULT 0,
    schedule_impact_days INTEGER DEFAULT 0,
    
    -- 变更原因
    change_reason VARCHAR(100),
    change_justification TEXT,
    business_impact TEXT,
    
    -- 审计信息
    performed_by BIGINT NOT NULL,
    performed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    review_comments TEXT,
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (calculation_result_id) REFERENCES calculation_results(id) ON DELETE CASCADE,
    FOREIGN KEY (performed_by) REFERENCES users(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id)
);

-- 为计算历史表创建索引
CREATE INDEX idx_calculation_history_project_id ON calculation_history(project_id);
CREATE INDEX idx_calculation_history_calculation_result_id ON calculation_history(calculation_result_id);
CREATE INDEX idx_calculation_history_action_type ON calculation_history(action_type);
CREATE INDEX idx_calculation_history_performed_by ON calculation_history(performed_by);
CREATE INDEX idx_calculation_history_performed_at ON calculation_history(performed_at);

-- 添加约束
ALTER TABLE calculation_history ADD CONSTRAINT chk_calculation_history_action_type 
    CHECK (action_type IN ('CREATE', 'UPDATE', 'DELETE', 'APPROVE', 'REJECT', 'BASELINE', 'RECALCULATE', 'ADJUST'));

-- 创建计算验证表（存储计算结果的验证信息）
CREATE TABLE IF NOT EXISTS calculation_validations (
    id BIGSERIAL PRIMARY KEY,
    calculation_result_id BIGINT NOT NULL,
    validation_type VARCHAR(30) NOT NULL,
    validation_rule_name VARCHAR(100) NOT NULL,
    validation_description TEXT,
    
    -- 验证结果
    is_valid BOOLEAN NOT NULL DEFAULT true,
    validation_result JSONB,
    validation_message TEXT,
    severity_level VARCHAR(20) NOT NULL DEFAULT 'INFO',
    
    -- 验证参数
    expected_value DECIMAL(19,4),
    actual_value DECIMAL(19,4),
    tolerance_percentage DECIMAL(19,4) DEFAULT 5.0,
    
    -- 验证上下文
    validation_context JSONB,
    related_function_points JSONB,
    
    -- 处理状态
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    resolution_notes TEXT,
    resolved_by BIGINT,
    resolved_at TIMESTAMP WITH TIME ZONE,
    
    -- 审计信息
    validated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    validated_by BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (calculation_result_id) REFERENCES calculation_results(id) ON DELETE CASCADE,
    FOREIGN KEY (validated_by) REFERENCES users(id),
    FOREIGN KEY (resolved_by) REFERENCES users(id)
);

-- 为验证表创建索引
CREATE INDEX idx_calculation_validations_calculation_result_id ON calculation_validations(calculation_result_id);
CREATE INDEX idx_calculation_validations_validation_type ON calculation_validations(validation_type);
CREATE INDEX idx_calculation_validations_is_valid ON calculation_validations(is_valid);
CREATE INDEX idx_calculation_validations_severity_level ON calculation_validations(severity_level);
CREATE INDEX idx_calculation_validations_status ON calculation_validations(status);

-- 添加约束
ALTER TABLE calculation_validations ADD CONSTRAINT chk_calculation_validations_validation_type 
    CHECK (validation_type IN ('COMPLEXITY_CHECK', 'TOTAL_SUM_CHECK', 'BUSINESS_RULE_CHECK', 'RANGE_CHECK', 'CONSISTENCY_CHECK', 'PEER_REVIEW'));

ALTER TABLE calculation_validations ADD CONSTRAINT chk_calculation_validations_severity_level 
    CHECK (severity_level IN ('INFO', 'WARNING', 'ERROR', 'CRITICAL'));

ALTER TABLE calculation_validations ADD CONSTRAINT chk_calculation_validations_status 
    CHECK (status IN ('PENDING', 'RESOLVED', 'IGNORED', 'ESCALATED'));

-- 创建计算结果摘要视图
CREATE OR REPLACE VIEW calculation_summary AS
SELECT 
    cr.id,
    cr.project_id,
    p.name as project_name,
    p.project_code,
    cr.calculation_name,
    cr.calculation_type,
    cr.calculation_date,
    u.full_name as calculated_by_name,
    cr.status,
    cr.review_status,
    cr.total_ilf_count + cr.total_eif_count + cr.total_ei_count + cr.total_eo_count + cr.total_eq_count as total_fp_count,
    cr.unadjusted_function_points,
    cr.adjustment_factor,
    cr.adjusted_function_points,
    cr.development_hours_estimate,
    cr.development_cost_estimate,
    cr.is_baseline,
    cr.version,
    CASE 
        WHEN cr.approved_by IS NOT NULL THEN a.full_name 
        ELSE NULL 
    END as approved_by_name,
    cr.approved_at
FROM calculation_results cr
JOIN projects p ON cr.project_id = p.id
JOIN users u ON cr.calculated_by = u.id
LEFT JOIN users a ON cr.approved_by = a.id
WHERE cr.deleted_at IS NULL
  AND p.deleted = false;

-- 创建触发器：自动更新项目的计算结果汇总
CREATE OR REPLACE FUNCTION update_project_calculation_summary()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        -- 更新项目表中的汇总信息
        UPDATE projects 
        SET 
            total_function_points = NEW.adjusted_function_points,
            total_development_hours = NEW.development_hours_estimate,
            total_cost_estimate = NEW.development_cost_estimate,
            updated_at = CURRENT_TIMESTAMP,
            updated_by = NEW.updated_by
        WHERE id = NEW.project_id;
        
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        -- 如果删除了计算结果，清除项目汇总信息
        UPDATE projects 
        SET 
            total_function_points = 0,
            total_development_hours = 0,
            total_cost_estimate = 0,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = OLD.project_id;
        
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
DROP TRIGGER IF EXISTS trigger_update_project_calculation_summary ON calculation_results;
CREATE TRIGGER trigger_update_project_calculation_summary
    AFTER INSERT OR UPDATE OR DELETE ON calculation_results
    FOR EACH ROW
    EXECUTE FUNCTION update_project_calculation_summary();

-- 创建触发器：自动记录计算历史
CREATE OR REPLACE FUNCTION record_calculation_history()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        INSERT INTO calculation_history (
            project_id, calculation_result_id, action_type, action_description,
            previous_values, new_values, performed_by
        ) VALUES (
            NEW.project_id,
            NEW.id,
            CASE 
                WHEN OLD.status != NEW.status THEN 'UPDATE'
                WHEN OLD.adjusted_function_points != NEW.adjusted_function_points THEN 'RECALCULATE'
                ELSE 'UPDATE'
            END,
            '计算结果更新',
            to_jsonb(OLD.*),
            to_jsonb(NEW.*),
            COALESCE(NEW.updated_by, NEW.created_by)
        );
        RETURN NEW;
    ELSIF TG_OP = 'INSERT' THEN
        INSERT INTO calculation_history (
            project_id, calculation_result_id, action_type, action_description,
            new_values, performed_by
        ) VALUES (
            NEW.project_id, NEW.id, 'CREATE', '创建计算结果',
            to_jsonb(NEW.*), NEW.created_by
        );
        RETURN NEW;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
DROP TRIGGER IF EXISTS trigger_record_calculation_history ON calculation_results;
CREATE TRIGGER trigger_record_calculation_history
    AFTER INSERT OR UPDATE ON calculation_results
    FOR EACH ROW
    EXECUTE FUNCTION record_calculation_history();

-- 添加表注释
COMMENT ON TABLE calculation_results IS 'NESMA计算结果主表';
COMMENT ON TABLE calculation_details IS '计算详情表（记录每个功能点的计算过程）';
COMMENT ON TABLE calculation_history IS '计算历史表';
COMMENT ON TABLE calculation_validations IS '计算验证表';

-- 添加重要字段注释
COMMENT ON COLUMN calculation_results.calculation_type IS '计算类型：INITIAL_ESTIMATE, DETAILED_ESTIMATE, REVISED_ESTIMATE, FINAL_CALCULATION, POST_IMPLEMENTATION';
COMMENT ON COLUMN calculation_results.unadjusted_function_points IS '未调整功能点总数';
COMMENT ON COLUMN calculation_results.adjustment_factor IS '调整因子';
COMMENT ON COLUMN calculation_results.adjusted_function_points IS '调整后功能点总数';
COMMENT ON COLUMN calculation_results.is_baseline IS '是否为基线版本';

COMMENT ON COLUMN calculation_details.ret_count IS 'RET数量（记录元素类型）';
COMMENT ON COLUMN calculation_details.det_count IS 'DET数量（数据元素类型）';
COMMENT ON COLUMN calculation_details.ftr_count IS 'FTR数量（文件类型引用）';

COMMENT ON COLUMN calculation_validations.validation_type IS '验证类型：COMPLEXITY_CHECK, TOTAL_SUM_CHECK, BUSINESS_RULE_CHECK, RANGE_CHECK, CONSISTENCY_CHECK, PEER_REVIEW';
COMMENT ON COLUMN calculation_validations.severity_level IS '严重程度：INFO, WARNING, ERROR, CRITICAL';

COMMENT ON VIEW calculation_summary IS '计算结果摘要视图';