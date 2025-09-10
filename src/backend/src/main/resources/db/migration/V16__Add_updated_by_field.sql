-- 添加SimpleProject实体所需的updated_by字段
-- Version: V16__Add_updated_by_field.sql
-- Description: 为projects表添加updated_by字段

-- 添加缺失的updated_by字段
ALTER TABLE projects 
ADD COLUMN IF NOT EXISTS updated_by BIGINT;

-- 为现有数据设置默认值（使用created_by的值）
UPDATE projects SET updated_by = created_by WHERE updated_by IS NULL;

-- 设置非空约束
ALTER TABLE projects ALTER COLUMN updated_by SET NOT NULL;

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_project_updated_by ON projects(updated_by);

-- 添加字段注释
COMMENT ON COLUMN projects.updated_by IS '更新人ID';