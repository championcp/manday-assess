-- 完整数据库重建脚本
-- Version: V11__Complete_database_rebuild.sql
-- Description: 完全重建所有表结构以匹配实体类定义

-- =====================================================
-- 第一步：删除所有现有表（危险操作，仅用于修复）
-- =====================================================

-- 删除外键约束和表
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS project_function_points CASCADE;
DROP TABLE IF EXISTS function_point_histories CASCADE;
DROP TABLE IF EXISTS project_status_histories CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;

-- 删除主表
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS function_points CASCADE;
DROP TABLE IF EXISTS simple_function_points CASCADE;
DROP TABLE IF EXISTS vaf_factors CASCADE;
DROP TABLE IF EXISTS ei_details CASCADE;
DROP TABLE IF EXISTS eif_details CASCADE;

-- =====================================================
-- 第二步：重新创建所有表
-- =====================================================

-- 创建用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    employee_id VARCHAR(50) UNIQUE,
    department VARCHAR(100),
    position VARCHAR(100),
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMP,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_at TIMESTAMP,
    password_expires_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    
    CONSTRAINT chk_account_status CHECK (account_status IN ('ACTIVE', 'INACTIVE', 'LOCKED', 'SUSPENDED'))
);

-- 创建角色表
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500),
    role_type VARCHAR(20) NOT NULL DEFAULT 'BUSINESS',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    
    CONSTRAINT chk_role_type CHECK (role_type IN ('SYSTEM', 'BUSINESS', 'CUSTOM')),
    CONSTRAINT chk_role_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

-- 创建权限表
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    resource VARCHAR(200),
    action VARCHAR(50),
    permission_type VARCHAR(20) NOT NULL DEFAULT 'FUNCTIONAL',
    parent_id BIGINT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    
    CONSTRAINT chk_permission_type CHECK (permission_type IN ('FUNCTIONAL', 'DATA', 'MENU')),
    FOREIGN KEY (parent_id) REFERENCES permissions(id)
);

-- 创建项目表
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    
    name VARCHAR(200) NOT NULL,
    description TEXT,
    client_name VARCHAR(200),
    project_type VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by BIGINT NOT NULL,
    
    CONSTRAINT chk_project_status CHECK (status IN ('DRAFT', 'IN_PROGRESS', 'COMPLETED', 'ARCHIVED')),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- =====================================================
-- 第三步：创建关联表
-- =====================================================

-- 用户角色关联表
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 角色权限关联表
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- =====================================================
-- 第四步：创建索引
-- =====================================================

-- 用户表索引
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_employee_id ON users(employee_id);
CREATE INDEX idx_user_account_status ON users(account_status);
CREATE INDEX idx_user_deleted ON users(deleted);

-- 角色表索引
CREATE INDEX idx_role_name ON roles(name);
CREATE INDEX idx_role_code ON roles(code);
CREATE INDEX idx_role_deleted ON roles(deleted);

-- 权限表索引
CREATE INDEX idx_permission_name ON permissions(name);
CREATE INDEX idx_permission_code ON permissions(code);
CREATE INDEX idx_permission_deleted ON permissions(deleted);

-- 项目表索引
CREATE INDEX idx_project_name ON projects(name);
CREATE INDEX idx_project_status ON projects(status);
CREATE INDEX idx_project_created_by ON projects(created_by);
CREATE INDEX idx_project_deleted ON projects(deleted);

-- 关联表索引
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- =====================================================
-- 第五步：插入初始数据
-- =====================================================

-- 插入默认角色
INSERT INTO roles (name, code, description, role_type, status, sort_order, created_by, updated_by) VALUES
('系统管理员', 'ADMIN', '系统最高权限管理员', 'SYSTEM', 'ACTIVE', 1, 'system', 'system'),
('项目经理', 'PROJECT_MANAGER', '项目管理权限', 'BUSINESS', 'ACTIVE', 2, 'system', 'system'),
('评估人员', 'ASSESSOR', '软件规模评估权限', 'BUSINESS', 'ACTIVE', 3, 'system', 'system'),
('普通用户', 'USER', '基础查看权限', 'BUSINESS', 'ACTIVE', 4, 'system', 'system');

-- 插入默认权限
INSERT INTO permissions (name, code, description, resource, action, permission_type, sort_order, created_by, updated_by) VALUES
('系统管理', 'SYSTEM_ADMIN', '系统管理权限', '/admin/**', '*', 'FUNCTIONAL', 1, 'system', 'system'),
('用户管理', 'USER_MANAGE', '用户管理权限', '/api/users/**', '*', 'FUNCTIONAL', 2, 'system', 'system'),
('项目管理', 'PROJECT_MANAGE', '项目管理权限', '/api/projects/**', '*', 'FUNCTIONAL', 3, 'system', 'system'),
('功能点评估', 'FUNCTION_POINT_ASSESS', '功能点评估权限', '/api/function-points/**', '*', 'FUNCTIONAL', 4, 'system', 'system'),
('报表查看', 'REPORT_VIEW', '报表查看权限', '/api/reports/**', 'GET', 'FUNCTIONAL', 5, 'system', 'system');

-- 插入管理员用户
INSERT INTO users (username, password_hash, real_name, email, department, account_status, created_by, updated_by) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9b2UgdHOT4.Ea1u', '系统管理员', 'admin@changsha.gov.cn', '财政评审中心', 'ACTIVE', 'system', 'system');

-- 给管理员分配管理员角色
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- 给管理员角色分配所有权限
INSERT INTO role_permissions (role_id, permission_id) VALUES 
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5);

-- =====================================================
-- 第六步：添加表注释
-- =====================================================

COMMENT ON TABLE users IS '用户表';
COMMENT ON TABLE roles IS '角色表';
COMMENT ON TABLE permissions IS '权限表';
COMMENT ON TABLE projects IS '项目表';
COMMENT ON TABLE user_roles IS '用户角色关联表';
COMMENT ON TABLE role_permissions IS '角色权限关联表';

COMMENT ON COLUMN users.username IS '用户名';
COMMENT ON COLUMN users.password_hash IS '密码哈希值';
COMMENT ON COLUMN users.account_status IS '账户状态：ACTIVE-活跃, INACTIVE-非活跃, LOCKED-锁定, SUSPENDED-暂停';
COMMENT ON COLUMN roles.role_type IS '角色类型：SYSTEM-系统角色, BUSINESS-业务角色, CUSTOM-自定义角色';
COMMENT ON COLUMN roles.status IS '角色状态：ACTIVE-活跃, INACTIVE-非活跃';
COMMENT ON COLUMN projects.status IS '项目状态：DRAFT-草稿, IN_PROGRESS-进行中, COMPLETED-已完成, ARCHIVED-已归档';