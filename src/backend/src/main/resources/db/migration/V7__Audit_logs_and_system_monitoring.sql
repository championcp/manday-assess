-- NESMA功能点评估系统 - 审计日志和系统监控表
-- Version: V7__Audit_logs_and_system_monitoring.sql
-- Description: 创建审计日志、系统监控和安全相关表

-- 创建审计日志表
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    
    -- 基本信息
    log_type VARCHAR(30) NOT NULL,
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id BIGINT,
    resource_name VARCHAR(200),
    
    -- 操作者信息
    user_id BIGINT,
    username VARCHAR(50),
    user_role VARCHAR(50),
    session_id VARCHAR(100),
    
    -- 操作详情
    operation_description TEXT,
    old_values JSONB,
    new_values JSONB,
    changes_summary JSONB,
    
    -- 请求信息
    request_method VARCHAR(10),
    request_url TEXT,
    request_params JSONB,
    request_body TEXT,
    response_status INTEGER,
    response_body TEXT,
    
    -- 技术信息
    ip_address INET,
    user_agent TEXT,
    client_info JSONB,
    server_info JSONB,
    
    -- 业务信息
    business_context JSONB,
    related_entities JSONB,
    impact_assessment TEXT,
    
    -- 安全信息
    security_level VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    risk_level VARCHAR(20) NOT NULL DEFAULT 'LOW',
    is_sensitive_operation BOOLEAN NOT NULL DEFAULT false,
    
    -- 时间信息
    operation_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processing_duration_ms BIGINT,
    
    -- 结果信息
    operation_result VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message TEXT,
    error_code VARCHAR(50),
    stack_trace TEXT,
    
    -- 索引时间
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建系统操作日志表
CREATE TABLE IF NOT EXISTS system_operation_logs (
    id BIGSERIAL PRIMARY KEY,
    
    -- 操作信息
    operation_type VARCHAR(30) NOT NULL,
    operation_name VARCHAR(100) NOT NULL,
    operation_description TEXT,
    
    -- 系统信息
    service_name VARCHAR(50) NOT NULL,
    module_name VARCHAR(50) NOT NULL,
    function_name VARCHAR(100),
    
    -- 执行信息
    execution_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    start_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP WITH TIME ZONE,
    duration_ms BIGINT,
    
    -- 资源使用
    cpu_usage DECIMAL(5,2),
    memory_usage_mb DECIMAL(10,2),
    disk_io_mb DECIMAL(10,2),
    network_io_mb DECIMAL(10,2),
    
    -- 业务指标
    records_processed INTEGER DEFAULT 0,
    transactions_processed INTEGER DEFAULT 0,
    data_volume_mb DECIMAL(10,2) DEFAULT 0,
    
    -- 错误信息
    error_message TEXT,
    error_code VARCHAR(50),
    error_details JSONB,
    
    -- 附加信息
    additional_info JSONB,
    trace_id VARCHAR(100),
    correlation_id VARCHAR(100),
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建用户会话日志表
CREATE TABLE IF NOT EXISTS user_session_logs (
    id BIGSERIAL PRIMARY KEY,
    
    -- 会话信息
    session_id VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    
    -- 登录信息
    login_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP WITH TIME ZONE,
    session_duration_minutes INTEGER,
    
    -- 客户端信息
    ip_address INET NOT NULL,
    user_agent TEXT,
    browser_info JSONB,
    device_info JSONB,
    location_info JSONB,
    
    -- 会话状态
    session_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    logout_reason VARCHAR(50),
    is_concurrent_session BOOLEAN NOT NULL DEFAULT false,
    
    -- 活动统计
    page_views INTEGER DEFAULT 0,
    actions_performed INTEGER DEFAULT 0,
    api_calls_made INTEGER DEFAULT 0,
    last_activity_time TIMESTAMP WITH TIME ZONE,
    
    -- 安全信息
    authentication_method VARCHAR(30) NOT NULL DEFAULT 'PASSWORD',
    failed_login_attempts INTEGER DEFAULT 0,
    is_suspicious_activity BOOLEAN NOT NULL DEFAULT false,
    security_alerts JSONB,
    
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建安全事件日志表
CREATE TABLE IF NOT EXISTS security_event_logs (
    id BIGSERIAL PRIMARY KEY,
    
    -- 事件基本信息
    event_type VARCHAR(30) NOT NULL,
    event_category VARCHAR(30) NOT NULL,
    event_name VARCHAR(100) NOT NULL,
    event_description TEXT,
    
    -- 严重程度
    severity_level VARCHAR(20) NOT NULL,
    risk_level VARCHAR(20) NOT NULL DEFAULT 'LOW',
    
    -- 涉及对象
    affected_user_id BIGINT,
    affected_username VARCHAR(50),
    affected_resource_type VARCHAR(50),
    affected_resource_id BIGINT,
    
    -- 事件详情
    event_details JSONB NOT NULL,
    attack_indicators JSONB,
    mitigation_actions JSONB,
    
    -- 来源信息
    source_ip INET,
    source_country VARCHAR(100),
    source_user_agent TEXT,
    
    -- 检测信息
    detection_method VARCHAR(50),
    detection_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    detection_system VARCHAR(50),
    
    -- 响应信息
    response_status VARCHAR(20) NOT NULL DEFAULT 'DETECTED',
    response_actions JSONB,
    response_time TIMESTAMP WITH TIME ZONE,
    resolved_time TIMESTAMP WITH TIME ZONE,
    resolved_by BIGINT,
    
    -- 影响评估
    business_impact TEXT,
    technical_impact TEXT,
    data_sensitivity_level VARCHAR(20),
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (affected_user_id) REFERENCES users(id),
    FOREIGN KEY (resolved_by) REFERENCES users(id)
);

-- 创建系统性能监控表
CREATE TABLE IF NOT EXISTS system_performance_metrics (
    id BIGSERIAL PRIMARY KEY,
    
    -- 监控基本信息
    metric_name VARCHAR(100) NOT NULL,
    metric_category VARCHAR(50) NOT NULL,
    metric_type VARCHAR(30) NOT NULL,
    
    -- 指标值
    metric_value DECIMAL(19,4) NOT NULL,
    metric_unit VARCHAR(20),
    threshold_value DECIMAL(19,4),
    is_threshold_exceeded BOOLEAN NOT NULL DEFAULT false,
    
    -- 时间信息
    measurement_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    measurement_interval_seconds INTEGER DEFAULT 60,
    
    -- 系统信息
    server_name VARCHAR(100),
    service_name VARCHAR(50),
    component_name VARCHAR(50),
    
    -- 聚合统计
    min_value DECIMAL(19,4),
    max_value DECIMAL(19,4),
    avg_value DECIMAL(19,4),
    percentile_95 DECIMAL(19,4),
    sample_count INTEGER DEFAULT 1,
    
    -- 附加信息
    tags JSONB,
    additional_data JSONB,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建系统错误日志表
CREATE TABLE IF NOT EXISTS system_error_logs (
    id BIGSERIAL PRIMARY KEY,
    
    -- 错误基本信息
    error_type VARCHAR(30) NOT NULL,
    error_category VARCHAR(30) NOT NULL,
    error_level VARCHAR(20) NOT NULL,
    error_code VARCHAR(50),
    error_message TEXT NOT NULL,
    
    -- 发生位置
    service_name VARCHAR(50) NOT NULL,
    class_name VARCHAR(200),
    method_name VARCHAR(100),
    line_number INTEGER,
    
    -- 错误详情
    stack_trace TEXT,
    root_cause TEXT,
    error_context JSONB,
    
    -- 用户相关
    user_id BIGINT,
    session_id VARCHAR(100),
    
    -- 请求相关
    request_id VARCHAR(100),
    request_url TEXT,
    request_method VARCHAR(10),
    request_params JSONB,
    
    -- 环境信息
    environment VARCHAR(20) NOT NULL DEFAULT 'PRODUCTION',
    server_name VARCHAR(100),
    application_version VARCHAR(50),
    
    -- 影响信息
    is_user_facing BOOLEAN NOT NULL DEFAULT true,
    business_impact TEXT,
    resolution_status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by BIGINT,
    resolution_notes TEXT,
    
    -- 时间信息
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    first_occurrence TIMESTAMP WITH TIME ZONE,
    last_occurrence TIMESTAMP WITH TIME ZONE,
    occurrence_count INTEGER DEFAULT 1,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (resolved_by) REFERENCES users(id)
);

-- 创建数据备份记录表
CREATE TABLE IF NOT EXISTS data_backup_records (
    id BIGSERIAL PRIMARY KEY,
    
    -- 备份基本信息
    backup_name VARCHAR(200) NOT NULL,
    backup_type VARCHAR(30) NOT NULL,
    backup_scope VARCHAR(50) NOT NULL,
    backup_description TEXT,
    
    -- 备份配置
    backup_strategy VARCHAR(50) NOT NULL,
    retention_days INTEGER NOT NULL DEFAULT 30,
    compression_enabled BOOLEAN NOT NULL DEFAULT true,
    encryption_enabled BOOLEAN NOT NULL DEFAULT true,
    
    -- 备份执行
    backup_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    duration_minutes INTEGER,
    
    -- 备份结果
    backup_size_mb DECIMAL(12,2),
    compressed_size_mb DECIMAL(12,2),
    files_count INTEGER DEFAULT 0,
    tables_count INTEGER DEFAULT 0,
    records_count BIGINT DEFAULT 0,
    
    -- 存储信息
    storage_location TEXT,
    storage_type VARCHAR(30) NOT NULL,
    backup_file_path TEXT,
    checksum_value VARCHAR(200),
    
    -- 验证信息
    is_verified BOOLEAN NOT NULL DEFAULT false,
    verification_status VARCHAR(20),
    verification_time TIMESTAMP WITH TIME ZONE,
    verification_result TEXT,
    
    -- 错误信息
    error_message TEXT,
    error_details JSONB,
    
    -- 执行者信息
    initiated_by BIGINT,
    is_scheduled BOOLEAN NOT NULL DEFAULT false,
    schedule_name VARCHAR(100),
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (initiated_by) REFERENCES users(id)
);

-- 为所有表创建索引
-- 审计日志索引
CREATE INDEX idx_audit_logs_log_type ON audit_logs(log_type);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_resource_type ON audit_logs(resource_type);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_operation_timestamp ON audit_logs(operation_timestamp);
CREATE INDEX idx_audit_logs_operation_result ON audit_logs(operation_result);
CREATE INDEX idx_audit_logs_security_level ON audit_logs(security_level);
CREATE INDEX idx_audit_logs_ip_address ON audit_logs(ip_address);
CREATE INDEX idx_audit_logs_session_id ON audit_logs(session_id);

-- 系统操作日志索引
CREATE INDEX idx_system_operation_logs_operation_type ON system_operation_logs(operation_type);
CREATE INDEX idx_system_operation_logs_service_name ON system_operation_logs(service_name);
CREATE INDEX idx_system_operation_logs_execution_status ON system_operation_logs(execution_status);
CREATE INDEX idx_system_operation_logs_start_time ON system_operation_logs(start_time);

-- 用户会话日志索引
CREATE INDEX idx_user_session_logs_session_id ON user_session_logs(session_id);
CREATE INDEX idx_user_session_logs_user_id ON user_session_logs(user_id);
CREATE INDEX idx_user_session_logs_login_time ON user_session_logs(login_time);
CREATE INDEX idx_user_session_logs_session_status ON user_session_logs(session_status);
CREATE INDEX idx_user_session_logs_ip_address ON user_session_logs(ip_address);

-- 安全事件日志索引
CREATE INDEX idx_security_event_logs_event_type ON security_event_logs(event_type);
CREATE INDEX idx_security_event_logs_severity_level ON security_event_logs(severity_level);
CREATE INDEX idx_security_event_logs_detection_time ON security_event_logs(detection_time);
CREATE INDEX idx_security_event_logs_affected_user_id ON security_event_logs(affected_user_id);
CREATE INDEX idx_security_event_logs_source_ip ON security_event_logs(source_ip);
CREATE INDEX idx_security_event_logs_response_status ON security_event_logs(response_status);

-- 性能监控索引
CREATE INDEX idx_system_performance_metrics_metric_name ON system_performance_metrics(metric_name);
CREATE INDEX idx_system_performance_metrics_measurement_time ON system_performance_metrics(measurement_time);
CREATE INDEX idx_system_performance_metrics_is_threshold_exceeded ON system_performance_metrics(is_threshold_exceeded);

-- 错误日志索引
CREATE INDEX idx_system_error_logs_error_type ON system_error_logs(error_type);
CREATE INDEX idx_system_error_logs_error_level ON system_error_logs(error_level);
CREATE INDEX idx_system_error_logs_occurred_at ON system_error_logs(occurred_at);
CREATE INDEX idx_system_error_logs_resolution_status ON system_error_logs(resolution_status);
CREATE INDEX idx_system_error_logs_user_id ON system_error_logs(user_id);

-- 备份记录索引
CREATE INDEX idx_data_backup_records_backup_type ON data_backup_records(backup_type);
CREATE INDEX idx_data_backup_records_backup_status ON data_backup_records(backup_status);
CREATE INDEX idx_data_backup_records_start_time ON data_backup_records(start_time);
CREATE INDEX idx_data_backup_records_is_scheduled ON data_backup_records(is_scheduled);

-- 添加约束条件
ALTER TABLE audit_logs ADD CONSTRAINT chk_audit_logs_log_type 
    CHECK (log_type IN ('USER_ACTION', 'SYSTEM_EVENT', 'DATA_CHANGE', 'SECURITY_EVENT', 'API_ACCESS', 'FILE_ACCESS'));

ALTER TABLE audit_logs ADD CONSTRAINT chk_audit_logs_security_level 
    CHECK (security_level IN ('LOW', 'NORMAL', 'HIGH', 'CRITICAL'));

ALTER TABLE audit_logs ADD CONSTRAINT chk_audit_logs_risk_level 
    CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'));

ALTER TABLE audit_logs ADD CONSTRAINT chk_audit_logs_operation_result 
    CHECK (operation_result IN ('SUCCESS', 'FAILURE', 'PARTIAL', 'CANCELLED'));

ALTER TABLE system_operation_logs ADD CONSTRAINT chk_system_operation_logs_execution_status 
    CHECK (execution_status IN ('SUCCESS', 'FAILURE', 'RUNNING', 'CANCELLED', 'TIMEOUT'));

ALTER TABLE user_session_logs ADD CONSTRAINT chk_user_session_logs_session_status 
    CHECK (session_status IN ('ACTIVE', 'EXPIRED', 'TERMINATED', 'TIMEOUT', 'FORCED_LOGOUT'));

ALTER TABLE security_event_logs ADD CONSTRAINT chk_security_event_logs_event_type 
    CHECK (event_type IN ('LOGIN_FAILURE', 'UNAUTHORIZED_ACCESS', 'SUSPICIOUS_ACTIVITY', 'DATA_BREACH', 'MALWARE_DETECTION', 'PRIVILEGE_ESCALATION'));

ALTER TABLE security_event_logs ADD CONSTRAINT chk_security_event_logs_severity_level 
    CHECK (severity_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'));

ALTER TABLE security_event_logs ADD CONSTRAINT chk_security_event_logs_response_status 
    CHECK (response_status IN ('DETECTED', 'INVESTIGATING', 'CONTAINED', 'RESOLVED', 'FALSE_POSITIVE'));

ALTER TABLE system_performance_metrics ADD CONSTRAINT chk_system_performance_metrics_metric_type 
    CHECK (metric_type IN ('COUNTER', 'GAUGE', 'HISTOGRAM', 'TIMER'));

ALTER TABLE system_error_logs ADD CONSTRAINT chk_system_error_logs_error_level 
    CHECK (error_level IN ('DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL'));

ALTER TABLE system_error_logs ADD CONSTRAINT chk_system_error_logs_resolution_status 
    CHECK (resolution_status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'WONT_FIX'));

ALTER TABLE data_backup_records ADD CONSTRAINT chk_data_backup_records_backup_type 
    CHECK (backup_type IN ('FULL', 'INCREMENTAL', 'DIFFERENTIAL', 'TRANSACTION_LOG'));

ALTER TABLE data_backup_records ADD CONSTRAINT chk_data_backup_records_backup_status 
    CHECK (backup_status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED'));

-- 创建审计日志分区表（按月分区）
-- 注意：PostgreSQL 10+支持声明式分区
-- CREATE TABLE audit_logs_y2024m01 PARTITION OF audit_logs
--     FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

-- 创建自动清理过期日志的函数
CREATE OR REPLACE FUNCTION cleanup_expired_logs()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER := 0;
    temp_count INTEGER;
BEGIN
    -- 清理6个月前的审计日志
    DELETE FROM audit_logs 
    WHERE created_at < CURRENT_DATE - INTERVAL '6 months';
    GET DIAGNOSTICS temp_count = ROW_COUNT;
    deleted_count := deleted_count + temp_count;
    
    -- 清理3个月前的系统操作日志
    DELETE FROM system_operation_logs 
    WHERE created_at < CURRENT_DATE - INTERVAL '3 months';
    GET DIAGNOSTICS temp_count = ROW_COUNT;
    deleted_count := deleted_count + temp_count;
    
    -- 清理1个月前的用户会话日志
    DELETE FROM user_session_logs 
    WHERE login_time < CURRENT_DATE - INTERVAL '1 month';
    GET DIAGNOSTICS temp_count = ROW_COUNT;
    deleted_count := deleted_count + temp_count;
    
    -- 清理3个月前的性能指标
    DELETE FROM system_performance_metrics 
    WHERE created_at < CURRENT_DATE - INTERVAL '3 months';
    GET DIAGNOSTICS temp_count = ROW_COUNT;
    deleted_count := deleted_count + temp_count;
    
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- 创建日志统计视图
CREATE OR REPLACE VIEW audit_log_summary AS
SELECT 
    DATE(created_at) as log_date,
    log_type,
    COUNT(*) as total_logs,
    COUNT(CASE WHEN operation_result = 'SUCCESS' THEN 1 END) as success_count,
    COUNT(CASE WHEN operation_result = 'FAILURE' THEN 1 END) as failure_count,
    COUNT(CASE WHEN security_level IN ('HIGH', 'CRITICAL') THEN 1 END) as high_security_count,
    COUNT(DISTINCT user_id) as unique_users,
    COUNT(DISTINCT ip_address) as unique_ips
FROM audit_logs
WHERE created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY DATE(created_at), log_type
ORDER BY log_date DESC, log_type;

-- 添加表注释
COMMENT ON TABLE audit_logs IS '审计日志表';
COMMENT ON TABLE system_operation_logs IS '系统操作日志表';
COMMENT ON TABLE user_session_logs IS '用户会话日志表';
COMMENT ON TABLE security_event_logs IS '安全事件日志表';
COMMENT ON TABLE system_performance_metrics IS '系统性能监控表';
COMMENT ON TABLE system_error_logs IS '系统错误日志表';
COMMENT ON TABLE data_backup_records IS '数据备份记录表';

-- 添加重要字段注释
COMMENT ON COLUMN audit_logs.log_type IS '日志类型：USER_ACTION, SYSTEM_EVENT, DATA_CHANGE, SECURITY_EVENT, API_ACCESS, FILE_ACCESS';
COMMENT ON COLUMN audit_logs.security_level IS '安全级别：LOW, NORMAL, HIGH, CRITICAL';
COMMENT ON COLUMN audit_logs.operation_result IS '操作结果：SUCCESS, FAILURE, PARTIAL, CANCELLED';

COMMENT ON COLUMN security_event_logs.event_type IS '事件类型：LOGIN_FAILURE, UNAUTHORIZED_ACCESS, SUSPICIOUS_ACTIVITY, DATA_BREACH, MALWARE_DETECTION, PRIVILEGE_ESCALATION';
COMMENT ON COLUMN security_event_logs.severity_level IS '严重程度：LOW, MEDIUM, HIGH, CRITICAL';

COMMENT ON COLUMN system_error_logs.error_level IS '错误级别：DEBUG, INFO, WARN, ERROR, FATAL';
COMMENT ON COLUMN system_error_logs.resolution_status IS '解决状态：OPEN, IN_PROGRESS, RESOLVED, CLOSED, WONT_FIX';

COMMENT ON VIEW audit_log_summary IS '审计日志统计摘要视图';