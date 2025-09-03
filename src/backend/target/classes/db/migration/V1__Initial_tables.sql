-- 创建基础数据表
-- Version: V1__Initial_tables.sql
-- Description: 初始化基础数据表结构

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    full_name VARCHAR(100),
    department VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT false
);

-- 为用户表创建索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_deleted ON users(deleted);

-- 创建项目表
CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    client_name VARCHAR(200),
    project_type VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- 为项目表创建索引
CREATE INDEX idx_projects_name ON projects(name);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_created_by ON projects(created_by);
CREATE INDEX idx_projects_deleted ON projects(deleted);

-- 插入默认管理员用户
INSERT INTO users (username, password, email, full_name, department, role, enabled)
VALUES ('admin', '$2a$10$9.rJZ8Z8QGQbZ8Z8QGQbZOH8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8Z8', 
        'admin@changsha.gov.cn', '系统管理员', '财政评审中心', 'ADMIN', true)
ON CONFLICT (username) DO NOTHING;

-- 添加注释
COMMENT ON TABLE users IS '用户表';
COMMENT ON TABLE projects IS '项目表';

COMMENT ON COLUMN users.username IS '用户名';
COMMENT ON COLUMN users.password IS '密码（BCrypt加密）';
COMMENT ON COLUMN users.role IS '用户角色：ADMIN, USER';
COMMENT ON COLUMN projects.status IS '项目状态：DRAFT, IN_PROGRESS, COMPLETED, ARCHIVED';