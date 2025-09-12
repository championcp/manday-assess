-- 修复项目权限配置 - 最终版本
-- Version: V19__Fix_project_permissions_final.sql  
-- Description: Issue #9 最终修复 - 绕过Flyway缓存，彻底解决权限配置问题

-- =====================================================
-- 1. 简单安全的权限数据插入（使用正确字段名）
-- =====================================================

-- 直接插入项目管理相关权限，使用正确的字段名称
INSERT INTO permissions (
    name, 
    code, 
    description, 
    resource_path, 
    http_method, 
    permission_type, 
    module,
    level,
    status,
    sort_order, 
    created_by, 
    updated_by
) VALUES
('项目创建权限', 'PROJECT_CREATE', '创建新项目的权限', '/api/projects/**', 'POST', 'API', 'PROJECT', 2, 'ACTIVE', 10, 'system', 'system'),
('项目查看权限', 'PROJECT_READ', '查看项目详情的权限', '/api/projects/**', 'GET', 'API', 'PROJECT', 2, 'ACTIVE', 11, 'system', 'system'),
('项目更新权限', 'PROJECT_UPDATE', '更新项目信息的权限', '/api/projects/**', 'PUT', 'API', 'PROJECT', 2, 'ACTIVE', 12, 'system', 'system'),
('项目删除权限', 'PROJECT_DELETE', '删除项目的权限', '/api/projects/**', 'DELETE', 'API', 'PROJECT', 2, 'ACTIVE', 13, 'system', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    resource_path = EXCLUDED.resource_path,
    http_method = EXCLUDED.http_method,
    updated_at = CURRENT_TIMESTAMP;

-- =====================================================
-- 2. 确保ADMIN和PROJECT_MANAGER角色存在且有正确权限
-- =====================================================

-- 给ADMIN角色分配项目管理权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'ADMIN' 
  AND p.code IN ('PROJECT_MANAGE', 'PROJECT_CREATE', 'PROJECT_READ', 'PROJECT_UPDATE', 'PROJECT_DELETE')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 给PROJECT_MANAGER角色分配项目管理权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'PROJECT_MANAGER'
  AND p.code IN ('PROJECT_MANAGE', 'PROJECT_CREATE', 'PROJECT_READ', 'PROJECT_UPDATE')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- =====================================================
-- 3. 确保admin用户有正确角色
-- =====================================================

-- 确保admin用户拥有ADMIN角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' 
  AND r.code = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- =====================================================
-- 4. 修复完成通知
-- =====================================================

-- 输出修复结果
DO $$
DECLARE
    admin_perms INTEGER;
    pm_perms INTEGER;
BEGIN
    -- 统计管理员项目相关权限数量
    SELECT COUNT(*) INTO admin_perms
    FROM users u
    JOIN user_roles ur ON u.id = ur.user_id
    JOIN roles r ON ur.role_id = r.id
    JOIN role_permissions rp ON r.id = rp.role_id
    JOIN permissions p ON rp.permission_id = p.id
    WHERE u.username = 'admin'
      AND p.code LIKE '%PROJECT%';
    
    -- 统计PROJECT_MANAGER角色权限数量
    SELECT COUNT(*) INTO pm_perms
    FROM roles r
    JOIN role_permissions rp ON r.id = rp.role_id
    JOIN permissions p ON rp.permission_id = p.id
    WHERE r.code = 'PROJECT_MANAGER'
      AND p.code LIKE '%PROJECT%';
    
    RAISE NOTICE '==========================================';
    RAISE NOTICE 'V19 权限修复完成！';
    RAISE NOTICE 'admin用户项目权限数量: %', admin_perms;
    RAISE NOTICE 'PROJECT_MANAGER角色权限数量: %', pm_perms;
    
    IF admin_perms > 0 AND pm_perms > 0 THEN
        RAISE NOTICE '✅ Issue #9 成功修复 - 权限配置正确';
    ELSE
        RAISE NOTICE '⚠️  权限配置可能仍存在问题，需要进一步检查';
    END IF;
    
    RAISE NOTICE '==========================================';
END$$;