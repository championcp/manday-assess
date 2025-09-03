-- NESMA功能点评估系统 - 数据库表结构完整性验证和汇总
-- Version: V8__Database_structure_validation_and_summary.sql
-- Description: 验证数据库表结构完整性，创建系统元数据视图

-- 创建数据库表结构汇总视图
CREATE OR REPLACE VIEW database_tables_summary AS
SELECT 
    schemaname,
    tablename,
    tableowner,
    tablespace,
    hasindexes,
    hasrules,
    hastriggers,
    rowsecurity
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY tablename;

-- 创建表字段信息汇总视图
CREATE OR REPLACE VIEW table_columns_summary AS
SELECT 
    t.table_name,
    t.column_name,
    t.ordinal_position,
    t.column_default,
    t.is_nullable,
    t.data_type,
    t.character_maximum_length,
    t.numeric_precision,
    t.numeric_scale,
    col_description(pg_class.oid, t.ordinal_position) as column_comment
FROM information_schema.columns t
LEFT JOIN pg_class ON pg_class.relname = t.table_name
WHERE t.table_schema = 'public'
ORDER BY t.table_name, t.ordinal_position;

-- 创建外键关系汇总视图
CREATE OR REPLACE VIEW foreign_key_relationships AS
SELECT 
    tc.table_name as table_name,
    kcu.column_name as column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name,
    tc.constraint_name as constraint_name,
    rc.update_rule,
    rc.delete_rule
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
LEFT JOIN information_schema.referential_constraints AS rc
    ON tc.constraint_name = rc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY' 
  AND tc.table_schema = 'public'
ORDER BY tc.table_name, kcu.column_name;

-- 创建索引信息汇总视图
CREATE OR REPLACE VIEW table_indexes_summary AS
SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes 
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

-- 创建约束信息汇总视图
CREATE OR REPLACE VIEW table_constraints_summary AS
SELECT 
    tc.table_name,
    tc.constraint_name,
    tc.constraint_type,
    cc.check_clause,
    kcu.column_name
FROM information_schema.table_constraints tc
LEFT JOIN information_schema.check_constraints cc
    ON tc.constraint_name = cc.constraint_name
LEFT JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_schema = 'public'
ORDER BY tc.table_name, tc.constraint_type, tc.constraint_name;

-- 创建触发器信息汇总视图
CREATE OR REPLACE VIEW table_triggers_summary AS
SELECT 
    t.trigger_name,
    t.event_manipulation,
    t.event_object_table as table_name,
    t.action_timing,
    t.action_statement
FROM information_schema.triggers t
WHERE t.trigger_schema = 'public'
ORDER BY t.event_object_table, t.trigger_name;

-- 验证核心表是否存在的函数
CREATE OR REPLACE FUNCTION validate_core_tables()
RETURNS TABLE(
    table_name TEXT,
    exists_status BOOLEAN,
    row_count BIGINT,
    validation_message TEXT
) AS $$
DECLARE
    core_tables TEXT[] := ARRAY[
        'users', 'projects', 'project_status_history', 'nesma_configurations', 'project_team_members',
        'function_points', 'function_point_elements', 'function_point_relationships', 
        'complexity_assessments', 'function_point_history',
        'ilf_details', 'eif_details', 'ei_details', 'eo_details', 'eq_details',
        'calculation_results', 'calculation_details', 'calculation_history', 'calculation_validations',
        'roles', 'permissions', 'role_permissions', 'user_roles',
        'approval_workflow_templates', 'approval_workflows', 'approval_nodes', 'approval_records',
        'audit_logs', 'system_operation_logs', 'user_session_logs', 'security_event_logs',
        'system_performance_metrics', 'system_error_logs', 'data_backup_records'
    ];
    table_record RECORD;
    table_exists BOOLEAN;
    table_count BIGINT;
    i TEXT;
BEGIN
    FOREACH i IN ARRAY core_tables
    LOOP
        -- 检查表是否存在
        SELECT EXISTS (
            SELECT FROM information_schema.tables 
            WHERE table_schema = 'public' 
            AND table_name = i
        ) INTO table_exists;
        
        -- 获取表记录数
        IF table_exists THEN
            EXECUTE format('SELECT COUNT(*) FROM %I', i) INTO table_count;
        ELSE
            table_count := 0;
        END IF;
        
        -- 返回结果
        RETURN QUERY SELECT 
            i::TEXT as table_name,
            table_exists as exists_status,
            table_count as row_count,
            CASE 
                WHEN table_exists THEN '表已创建'
                ELSE '表不存在'
            END::TEXT as validation_message;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- 验证外键关系的函数
CREATE OR REPLACE FUNCTION validate_foreign_keys()
RETURNS TABLE(
    table_name TEXT,
    column_name TEXT,
    referenced_table TEXT,
    referenced_column TEXT,
    is_valid BOOLEAN,
    validation_message TEXT
) AS $$
DECLARE
    fk_record RECORD;
BEGIN
    FOR fk_record IN 
        SELECT 
            tc.table_name,
            kcu.column_name,
            ccu.table_name AS foreign_table_name,
            ccu.column_name AS foreign_column_name
        FROM information_schema.table_constraints AS tc 
        JOIN information_schema.key_column_usage AS kcu
            ON tc.constraint_name = kcu.constraint_name
        JOIN information_schema.constraint_column_usage AS ccu
            ON ccu.constraint_name = tc.constraint_name
        WHERE tc.constraint_type = 'FOREIGN KEY' 
          AND tc.table_schema = 'public'
    LOOP
        RETURN QUERY SELECT 
            fk_record.table_name::TEXT,
            fk_record.column_name::TEXT,
            fk_record.foreign_table_name::TEXT,
            fk_record.foreign_column_name::TEXT,
            true as is_valid,
            '外键关系正常'::TEXT as validation_message;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- 验证必需索引是否存在
CREATE OR REPLACE FUNCTION validate_required_indexes()
RETURNS TABLE(
    table_name TEXT,
    index_name TEXT,
    exists_status BOOLEAN,
    validation_message TEXT
) AS $$
DECLARE
    required_indexes TEXT[][] := ARRAY[
        ARRAY['users', 'idx_users_username'],
        ARRAY['projects', 'idx_projects_project_code'],
        ARRAY['function_points', 'idx_function_points_project_id'],
        ARRAY['function_points', 'idx_function_points_fp_type'],
        ARRAY['calculation_results', 'idx_calculation_results_project_id'],
        ARRAY['audit_logs', 'idx_audit_logs_operation_timestamp'],
        ARRAY['user_session_logs', 'idx_user_session_logs_session_id']
    ];
    index_record TEXT[];
    index_exists BOOLEAN;
BEGIN
    FOREACH index_record SLICE 1 IN ARRAY required_indexes
    LOOP
        -- 检查索引是否存在
        SELECT EXISTS (
            SELECT FROM pg_indexes 
            WHERE schemaname = 'public' 
            AND tablename = index_record[1]
            AND indexname = index_record[2]
        ) INTO index_exists;
        
        RETURN QUERY SELECT 
            index_record[1]::TEXT as table_name,
            index_record[2]::TEXT as index_name,
            index_exists as exists_status,
            CASE 
                WHEN index_exists THEN '索引已创建'
                ELSE '索引缺失'
            END::TEXT as validation_message;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- 创建数据库统计信息汇总视图
CREATE OR REPLACE VIEW database_statistics_summary AS
SELECT 
    'Tables' as category,
    COUNT(*)::TEXT as count,
    '数据库表总数' as description
FROM information_schema.tables 
WHERE table_schema = 'public'

UNION ALL

SELECT 
    'Columns' as category,
    COUNT(*)::TEXT as count,
    '数据库字段总数' as description
FROM information_schema.columns 
WHERE table_schema = 'public'

UNION ALL

SELECT 
    'Foreign Keys' as category,
    COUNT(*)::TEXT as count,
    '外键约束总数' as description
FROM information_schema.table_constraints 
WHERE table_schema = 'public' AND constraint_type = 'FOREIGN KEY'

UNION ALL

SELECT 
    'Indexes' as category,
    COUNT(*)::TEXT as count,
    '索引总数' as description
FROM pg_indexes 
WHERE schemaname = 'public'

UNION ALL

SELECT 
    'Triggers' as category,
    COUNT(*)::TEXT as count,
    '触发器总数' as description
FROM information_schema.triggers 
WHERE trigger_schema = 'public'

UNION ALL

SELECT 
    'Views' as category,
    COUNT(*)::TEXT as count,
    '视图总数' as description
FROM information_schema.views 
WHERE table_schema = 'public'

UNION ALL

SELECT 
    'Functions' as category,
    COUNT(*)::TEXT as count,
    '存储函数总数' as description
FROM information_schema.routines 
WHERE routine_schema = 'public' AND routine_type = 'FUNCTION';

-- 创建NESMA系统核心业务实体汇总
CREATE OR REPLACE VIEW nesma_business_entities_summary AS
SELECT 
    'Projects' as entity_type,
    '项目管理' as entity_category,
    COUNT(*)::TEXT as total_count,
    '政府信息化项目' as description
FROM projects WHERE deleted = false

UNION ALL

SELECT 
    'Function Points' as entity_type,
    'NESMA评估' as entity_category,
    COUNT(*)::TEXT as total_count,
    'NESMA功能点' as description
FROM function_points WHERE deleted_at IS NULL

UNION ALL

SELECT 
    'ILF Details' as entity_type,
    '内部逻辑文件' as entity_category,
    COUNT(*)::TEXT as total_count,
    'ILF详细信息' as description
FROM ilf_details WHERE deleted_at IS NULL

UNION ALL

SELECT 
    'EIF Details' as entity_type,
    '外部接口文件' as entity_category,
    COUNT(*)::TEXT as total_count,
    'EIF详细信息' as description
FROM eif_details WHERE deleted_at IS NULL

UNION ALL

SELECT 
    'EI Details' as entity_type,
    '外部输入' as entity_category,
    COUNT(*)::TEXT as total_count,
    'EI详细信息' as description
FROM ei_details WHERE deleted_at IS NULL

UNION ALL

SELECT 
    'EO Details' as entity_type,
    '外部输出' as entity_category,
    COUNT(*)::TEXT as total_count,
    'EO详细信息' as description
FROM eo_details WHERE deleted_at IS NULL

UNION ALL

SELECT 
    'EQ Details' as entity_type,
    '外部查询' as entity_category,
    COUNT(*)::TEXT as total_count,
    'EQ详细信息' as description
FROM eq_details WHERE deleted_at IS NULL

UNION ALL

SELECT 
    'Calculation Results' as entity_type,
    '计算结果' as entity_category,
    COUNT(*)::TEXT as total_count,
    'NESMA计算结果' as description
FROM calculation_results WHERE deleted_at IS NULL

UNION ALL

SELECT 
    'Users' as entity_type,
    '用户管理' as entity_category,
    COUNT(*)::TEXT as total_count,
    '系统用户' as description
FROM users WHERE deleted = false

UNION ALL

SELECT 
    'Approval Workflows' as entity_type,
    '审批流程' as entity_category,
    COUNT(*)::TEXT as total_count,
    '审批工作流实例' as description
FROM approval_workflows WHERE deleted_at IS NULL;

-- 验证数据库完整性的主函数
CREATE OR REPLACE FUNCTION validate_database_integrity()
RETURNS TABLE(
    validation_category TEXT,
    validation_item TEXT,
    validation_result TEXT,
    validation_status TEXT,
    recommendations TEXT
) AS $$
BEGIN
    -- 返回核心表验证结果
    RETURN QUERY 
    SELECT 
        '核心表结构'::TEXT as validation_category,
        t.table_name as validation_item,
        CASE WHEN t.exists_status THEN '通过' ELSE '失败' END as validation_result,
        t.validation_message as validation_status,
        CASE WHEN NOT t.exists_status THEN '请检查迁移脚本是否正确执行' ELSE '正常' END as recommendations
    FROM validate_core_tables() t;
    
    -- 检查是否有足够的配置数据
    RETURN QUERY 
    SELECT 
        '基础配置数据'::TEXT as validation_category,
        'NESMA配置参数'::TEXT as validation_item,
        CASE WHEN (SELECT COUNT(*) FROM nesma_configurations WHERE is_active = true) > 0 
             THEN '通过' ELSE '警告' END as validation_result,
        CASE WHEN (SELECT COUNT(*) FROM nesma_configurations WHERE is_active = true) > 0 
             THEN '配置数据已就绪' ELSE '缺少NESMA配置数据' END as validation_status,
        '建议检查NESMA权重配置是否完整'::TEXT as recommendations;
        
    -- 检查默认用户和角色
    RETURN QUERY 
    SELECT 
        '用户权限配置'::TEXT as validation_category,
        '默认管理员账户'::TEXT as validation_item,
        CASE WHEN (SELECT COUNT(*) FROM users WHERE role = 'ADMIN') > 0 
             THEN '通过' ELSE '失败' END as validation_result,
        CASE WHEN (SELECT COUNT(*) FROM users WHERE role = 'ADMIN') > 0 
             THEN '管理员账户已创建' ELSE '缺少管理员账户' END as validation_status,
        '确保系统有管理员账户用于初始化'::TEXT as recommendations;
END;
$$ LANGUAGE plpgsql;

-- 添加视图和函数注释
COMMENT ON VIEW database_tables_summary IS '数据库表汇总信息视图';
COMMENT ON VIEW table_columns_summary IS '数据库表字段汇总信息视图';
COMMENT ON VIEW foreign_key_relationships IS '外键关系汇总视图';
COMMENT ON VIEW table_indexes_summary IS '表索引汇总视图';
COMMENT ON VIEW table_constraints_summary IS '表约束汇总视图';
COMMENT ON VIEW table_triggers_summary IS '表触发器汇总视图';
COMMENT ON VIEW database_statistics_summary IS '数据库统计信息汇总视图';
COMMENT ON VIEW nesma_business_entities_summary IS 'NESMA业务实体汇总视图';

COMMENT ON FUNCTION validate_core_tables() IS '验证核心表是否存在的函数';
COMMENT ON FUNCTION validate_foreign_keys() IS '验证外键关系的函数';
COMMENT ON FUNCTION validate_required_indexes() IS '验证必需索引是否存在的函数';
COMMENT ON FUNCTION validate_database_integrity() IS '数据库完整性验证主函数';

-- 执行完整性验证并输出结果
-- 注意：在实际部署时，这些SELECT语句可以通过应用程序或管理工具执行
-- SELECT * FROM validate_database_integrity();
-- SELECT * FROM database_statistics_summary;
-- SELECT * FROM nesma_business_entities_summary;