-- 添加用户表缺失的审计字段
-- Version: V10__Add_missing_user_fields.sql
-- Description: 添加created_by, updated_by, password_expires_at字段

-- 添加缺失的审计字段到users表
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS created_by VARCHAR(50),
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(50),
ADD COLUMN IF NOT EXISTS password_expires_at TIMESTAMP;

-- 为现有数据设置默认值
UPDATE users SET created_by = 'system' WHERE created_by IS NULL;
UPDATE users SET updated_by = 'system' WHERE updated_by IS NULL;

-- 为新字段添加注释
COMMENT ON COLUMN users.created_by IS '创建人';
COMMENT ON COLUMN users.updated_by IS '更新人';
COMMENT ON COLUMN users.password_expires_at IS '密码过期时间';