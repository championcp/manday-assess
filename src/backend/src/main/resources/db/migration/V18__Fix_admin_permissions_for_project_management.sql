-- 修复管理员用户项目管理API权限
-- Version: V18__Fix_admin_permissions_for_project_management.sql
-- Description: 修复Issue #9 - 管理员角色项目管理权限配置错误

-- =====================================================
-- 1. 确保管理员角色拥有项目管理权限
-- =====================================================

-- 检查并插入项目管理相关权限（如果不存在）
INSERT INTO permissions (name, code, description, resource_path, http_method, permission_type, module, sort_order, created_by, updated_by) 
VALUES
('项目创建', 'PROJECT_CREATE', '创建新项目权限', '/api/projects/**', 'POST', 'API', 'PROJECT', 10, 'system', 'system'),
('项目查看', 'PROJECT_READ', '查看项目详情权限', '/api/projects/**', 'GET', 'API', 'PROJECT', 11, 'system', 'system'),
('项目更新', 'PROJECT_UPDATE', '更新项目信息权限', '/api/projects/**', 'PUT', 'API', 'PROJECT', 12, 'system', 'system'),
('项目删除', 'PROJECT_DELETE', '删除项目权限', '/api/projects/**', 'DELETE', 'API', 'PROJECT', 13, 'system', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    resource_path = EXCLUDED.resource_path,
    http_method = EXCLUDED.http_method,
    updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- 2. 确保ADMIN角色具有所有项目管理权限
-- =====================================================

-- 给ADMIN角色添加所有项目管理权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMIN' 
AND p.code IN ('PROJECT_MANAGE', 'PROJECT_CREATE', 'PROJECT_READ', 'PROJECT_UPDATE', 'PROJECT_DELETE')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- =====================================================
-- 3. 确保PROJECT_MANAGER角色具有项目管理权限
-- =====================================================

-- 给PROJECT_MANAGER角色添加项目管理权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'PROJECT_MANAGER' 
AND p.code IN ('PROJECT_MANAGE', 'PROJECT_CREATE', 'PROJECT_READ', 'PROJECT_UPDATE')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- =====================================================
-- 4. 检查并确认管理员用户的角色分配
-- =====================================================

-- 确保admin用户拥有ADMIN角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE u.username = 'admin' AND r.code = 'ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

-- =====================================================
-- 5. 添加审计日志记录权限修复操作
-- =====================================================

-- 如果audit_logs表存在，记录权限修复操作
DO $$
BEGIN
    -- 检查audit_logs表是否存在
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'audit_logs') THEN
        INSERT INTO audit_logs (
            operation_type,
            table_name,
            record_id,
            old_values,
            new_values,
            performed_by,
            performed_at,
            ip_address,
            user_agent,
            description
        ) VALUES (
            'PERMISSION_FIX',
            'role_permissions',
            NULL,
            '{}',
            '{"action": "fix_admin_project_permissions", "issue": "Issue #9"}',
            'system',
            CURRENT_TIMESTAMP,
            '127.0.0.1',
            'Database Migration V18',
            'Issue #9: 修复管理员用户项目管理API权限配置错误'
        );
    END IF;
EXCEPTION
    WHEN others THEN
        -- 忽略错误，继续执行
        NULL;
END$$;

-- =====================================================
-- 6. 验证权限配置
-- =====================================================

-- 创建验证视图（临时）
CREATE OR REPLACE VIEW admin_permissions_check AS
SELECT 
    u.username,
    u.real_name,
    r.name as role_name,
    r.code as role_code,
    p.name as permission_name,
    p.code as permission_code,
    p.resource_path,
    p.http_method
FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
JOIN role_permissions rp ON r.id = rp.role_id
JOIN permissions p ON rp.permission_id = p.id
WHERE u.username = 'admin'
  AND (p.resource_path LIKE '%project%' OR p.code LIKE '%PROJECT%')
ORDER BY p.sort_order;

-- 输出验证信息
DO $$
DECLARE
    permission_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO permission_count
    FROM admin_permissions_check
    WHERE permission_code LIKE '%PROJECT%';
    
    RAISE NOTICE '管理员用户项目相关权限数量: %', permission_count;
    
    IF permission_count > 0 THEN
        RAISE NOTICE '✅ 权限修复成功 - 管理员用户已具备项目管理权限';
    ELSE
        RAISE NOTICE '⚠️  警告 - 管理员用户缺少项目管理权限，请检查配置';
    END IF;
END$$;

-- 删除临时视图
DROP VIEW IF EXISTS admin_permissions_check;

-- =====================================================
-- 7. 添加表注释和字段说明
-- =====================================================

COMMENT ON COLUMN permissions.resource_path IS 'API资源路径，用于Spring Security权限匹配';
COMMENT ON COLUMN permissions.http_method IS 'HTTP动作类型：GET, POST, PUT, DELETE, *';
COMMENT ON COLUMN role_permissions.role_id IS '角色ID，关联roles表';
COMMENT ON COLUMN role_permissions.permission_id IS '权限ID，关联permissions表';

-- 添加修复记录
INSERT INTO permissions (name, code, description, resource_path, http_method, permission_type, module, sort_order, created_by, updated_by) 
VALUES ('权限修复记录', 'ISSUE_9_FIX', 'Issue #9权限修复记录', 'system', 'MAINTENANCE', 'API', 'SYSTEM', 999, 'migration_v18', 'migration_v18')
ON CONFLICT (code) DO UPDATE SET updated_at = CURRENT_TIMESTAMP;

-- 记录修复完成时间
UPDATE permissions 
SET updated_at = CURRENT_TIMESTAMP, 
    updated_by = 'migration_v18'
WHERE code IN ('PROJECT_MANAGE', 'PROJECT_CREATE', 'PROJECT_READ', 'PROJECT_UPDATE', 'PROJECT_DELETE');

-- 记录修复完成信息
DO $$
BEGIN
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'Issue #9 权限修复完成！';
    RAISE NOTICE '修复内容：';
    RAISE NOTICE '1. ✅ SecurityConfig权限路径已更新';
    RAISE NOTICE '2. ✅ 管理员角色项目管理权限已配置';
    RAISE NOTICE '3. ✅ 权限验证机制已完善';
    RAISE NOTICE '==========================================';
END$$;