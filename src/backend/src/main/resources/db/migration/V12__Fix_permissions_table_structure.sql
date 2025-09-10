-- 修复permissions表结构以匹配Permission实体类
-- Version: V12__Fix_permissions_table_structure.sql
-- Description: 修复permissions表字段名称和结构，使其与Permission.java实体类完全匹配

-- =====================================================
-- 第一步：修改现有字段名称和类型
-- =====================================================

-- 重命名字段以匹配实体类
ALTER TABLE permissions RENAME COLUMN resource TO resource_path;
ALTER TABLE permissions RENAME COLUMN action TO http_method;

-- =====================================================
-- 第二步：添加缺失的字段
-- =====================================================

-- 添加module字段
ALTER TABLE permissions ADD COLUMN module VARCHAR(50) NOT NULL DEFAULT 'SYSTEM';

-- 添加status字段
ALTER TABLE permissions ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- 添加icon字段
ALTER TABLE permissions ADD COLUMN icon VARCHAR(100);

-- 添加level字段
ALTER TABLE permissions ADD COLUMN level INTEGER NOT NULL DEFAULT 1;

-- =====================================================
-- 第三步：更新permission_type枚举值
-- =====================================================

-- 先移除现有约束
ALTER TABLE permissions DROP CONSTRAINT IF EXISTS chk_permission_type;

-- 更新现有数据的permission_type值以匹配实体类枚举
UPDATE permissions SET permission_type = 'API' WHERE permission_type = 'FUNCTIONAL';
-- MENU和DATA保持不变，因为它们在实体类中也存在

-- 添加新的约束以匹配实体类枚举
ALTER TABLE permissions ADD CONSTRAINT chk_permission_type 
    CHECK (permission_type IN ('MENU', 'BUTTON', 'API', 'DATA'));

-- 添加status字段的约束
ALTER TABLE permissions ADD CONSTRAINT chk_permission_status 
    CHECK (status IN ('ACTIVE', 'INACTIVE'));

-- =====================================================
-- 第四步：更新现有数据
-- =====================================================

-- 更新现有权限记录，设置合适的module值
UPDATE permissions SET module = 'SYSTEM' WHERE code IN ('SYSTEM_ADMIN', 'USER_MANAGE');
UPDATE permissions SET module = 'PROJECT' WHERE code IN ('PROJECT_MANAGE', 'FUNCTION_POINT_ASSESS');
UPDATE permissions SET module = 'REPORT' WHERE code = 'REPORT_VIEW';

-- 设置合适的level值
UPDATE permissions SET level = 1 WHERE parent_id IS NULL;

-- =====================================================
-- 第五步：创建新的索引
-- =====================================================

-- 为新字段创建索引
CREATE INDEX idx_permission_module ON permissions(module);
CREATE INDEX idx_permission_status ON permissions(status);
CREATE INDEX idx_permission_level ON permissions(level);
CREATE INDEX idx_permission_parent_id ON permissions(parent_id);

-- =====================================================
-- 第六步：更新表和字段注释
-- =====================================================

COMMENT ON COLUMN permissions.resource_path IS '资源路径（URL或路由）';
COMMENT ON COLUMN permissions.http_method IS 'HTTP方法（GET, POST, PUT, DELETE等）';
COMMENT ON COLUMN permissions.module IS '所属模块';
COMMENT ON COLUMN permissions.status IS '权限状态：ACTIVE-活跃, INACTIVE-非活跃';
COMMENT ON COLUMN permissions.icon IS '图标样式类';
COMMENT ON COLUMN permissions.level IS '权限层级';
COMMENT ON COLUMN permissions.permission_type IS '权限类型：MENU-菜单, BUTTON-按钮, API-接口, DATA-数据权限';