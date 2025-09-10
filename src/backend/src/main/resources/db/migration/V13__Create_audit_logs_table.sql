-- 创建审计日志表
-- Version: V12__Create_audit_logs_table.sql
-- Description: 创建audit_logs表以支持审计日志功能

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    
    -- 用户信息
    user_id BIGINT,
    username VARCHAR(100),
    real_name VARCHAR(100),
    
    -- 操作信息
    operation VARCHAR(50) NOT NULL,
    module VARCHAR(50) NOT NULL,
    operation_desc VARCHAR(500) NOT NULL,
    
    -- 业务信息
    business_type VARCHAR(50),
    business_id VARCHAR(100),
    business_data TEXT,
    
    -- 操作结果
    operation_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    result_message VARCHAR(1000),
    
    -- 时间和网络信息
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    
    -- 请求信息
    request_uri VARCHAR(500),
    http_method VARCHAR(10),
    request_params TEXT,
    
    -- 其他信息
    duration BIGINT,
    exception_info TEXT,
    risk_level VARCHAR(20) DEFAULT 'LOW',
    session_id VARCHAR(100),
    signature VARCHAR(500),
    
    -- 约束
    CONSTRAINT chk_operation_status CHECK (operation_status IN ('SUCCESS', 'FAILED', 'PARTIAL')),
    CONSTRAINT chk_risk_level CHECK (risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

-- 创建索引以提高查询性能
CREATE INDEX idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_operation ON audit_logs(operation);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_ip ON audit_logs(ip_address);
CREATE INDEX idx_audit_module ON audit_logs(module);
CREATE INDEX idx_audit_status ON audit_logs(operation_status);
CREATE INDEX idx_audit_risk_level ON audit_logs(risk_level);

-- 添加表注释
COMMENT ON TABLE audit_logs IS '审计日志表 - 记录系统中所有重要操作和事件';
COMMENT ON COLUMN audit_logs.operation IS '操作类型';
COMMENT ON COLUMN audit_logs.operation_status IS '操作状态：SUCCESS-成功, FAILED-失败, PARTIAL-部分成功';
COMMENT ON COLUMN audit_logs.risk_level IS '风险等级：LOW-低风险, MEDIUM-中等风险, HIGH-高风险, CRITICAL-严重风险';