-- 修复用户表结构，添加缺失字段
-- Version: V9__Fix_users_table_structure.sql
-- Description: 修复用户表结构，使其与User实体类匹配

-- 添加缺失的字段到users表
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255),
ADD COLUMN IF NOT EXISTS real_name VARCHAR(100),
ADD COLUMN IF NOT EXISTS phone VARCHAR(20),
ADD COLUMN IF NOT EXISTS employee_id VARCHAR(50) UNIQUE,
ADD COLUMN IF NOT EXISTS position VARCHAR(100),
ADD COLUMN IF NOT EXISTS account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS locked_at TIMESTAMP;

-- 数据迁移：将现有的password迁移到password_hash
UPDATE users SET password_hash = password WHERE password_hash IS NULL;
UPDATE users SET real_name = full_name WHERE real_name IS NULL;

-- 为新字段创建索引
CREATE INDEX IF NOT EXISTS idx_user_employee_id ON users(employee_id);
CREATE INDEX IF NOT EXISTS idx_user_account_status ON users(account_status);

-- 添加约束检查账户状态值
ALTER TABLE users ADD CONSTRAINT chk_account_status 
CHECK (account_status IN ('ACTIVE', 'INACTIVE', 'LOCKED', 'SUSPENDED'));

-- 更新现有管理员用户的account_status
UPDATE users SET account_status = 'ACTIVE' WHERE username = 'admin' AND account_status IS NULL;

-- 插入或更新管理员用户（使用正确的密码哈希）
INSERT INTO users (username, password, password_hash, email, real_name, department, account_status, created_at, updated_at)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2UgdHOT4.Ea1u', 
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2UgdHOT4.Ea1u', 
        'admin@changsha.gov.cn', '系统管理员', '财政评审中心', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO UPDATE SET
    password = EXCLUDED.password,
    password_hash = EXCLUDED.password_hash,
    email = EXCLUDED.email,
    real_name = EXCLUDED.real_name,
    department = EXCLUDED.department,
    account_status = EXCLUDED.account_status,
    updated_at = CURRENT_TIMESTAMP;