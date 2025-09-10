package gov.changsha.finance.service;

import gov.changsha.finance.entity.Permission;
import gov.changsha.finance.repository.PermissionRepository;
import gov.changsha.finance.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限管理服务
 * 提供权限的CRUD操作和树结构管理功能
 * 符合RBAC权限管理模型
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-09
 */
@Service
@Transactional
public class PermissionService {
    
    private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private AuditLogService auditLogService; // 将在后续创建
    
    /**
     * 创建权限
     */
    public Permission createPermission(Permission permission) {
        // 验证权限代码唯一性
        if (permissionRepository.existsByCode(permission.getCode())) {
            throw new IllegalArgumentException("权限代码已存在: " + permission.getCode());
        }
        
        // 如果有父权限，验证父权限存在并设置层级
        if (permission.getParentId() != null) {
            Optional<Permission> parentOpt = permissionRepository.findById(permission.getParentId());
            if (!parentOpt.isPresent()) {
                throw new IllegalArgumentException("父权限不存在: " + permission.getParentId());
            }
            Permission parent = parentOpt.get();
            permission.setLevel(parent.getLevel() + 1);
        } else {
            permission.setLevel(1);
        }
        
        Permission savedPermission = permissionRepository.save(permission);
        
        // 记录创建权限的审计日志
        auditLogService.recordPermissionCreate(savedPermission);
        
        logger.info("创建权限成功 - 权限: {} ({})", savedPermission.getName(), savedPermission.getCode());
        return savedPermission;
    }
    
    /**
     * 更新权限信息
     */
    public Permission updatePermission(Long permissionId, Permission permissionUpdates) {
        Optional<Permission> permissionOpt = permissionRepository.findById(permissionId);
        if (!permissionOpt.isPresent()) {
            throw new IllegalArgumentException("权限不存在: " + permissionId);
        }
        
        Permission existingPermission = permissionOpt.get();
        Permission originalPermission = clonePermission(existingPermission); // 用于审计日志
        
        // 更新允许修改的字段
        if (permissionUpdates.getName() != null) {
            existingPermission.setName(permissionUpdates.getName());
        }
        
        if (permissionUpdates.getDescription() != null) {
            existingPermission.setDescription(permissionUpdates.getDescription());
        }
        
        if (permissionUpdates.getResourcePath() != null) {
            existingPermission.setResourcePath(permissionUpdates.getResourcePath());
        }
        
        if (permissionUpdates.getHttpMethod() != null) {
            existingPermission.setHttpMethod(permissionUpdates.getHttpMethod());
        }
        
        if (permissionUpdates.getStatus() != null) {
            existingPermission.setStatus(permissionUpdates.getStatus());
        }
        
        if (permissionUpdates.getSortOrder() != null) {
            existingPermission.setSortOrder(permissionUpdates.getSortOrder());
        }
        
        if (permissionUpdates.getIcon() != null) {
            existingPermission.setIcon(permissionUpdates.getIcon());
        }
        
        Permission savedPermission = permissionRepository.save(existingPermission);
        
        // 记录更新权限的审计日志
        auditLogService.recordPermissionUpdate(originalPermission, savedPermission);
        
        logger.info("更新权限成功 - 权限: {} ({})", savedPermission.getName(), savedPermission.getCode());
        return savedPermission;
    }
    
    /**
     * 删除权限
     */
    public boolean deletePermission(Long permissionId) {
        Optional<Permission> permissionOpt = permissionRepository.findById(permissionId);
        if (!permissionOpt.isPresent()) {
            throw new IllegalArgumentException("权限不存在: " + permissionId);
        }
        
        Permission permission = permissionOpt.get();
        
        // 检查是否有子权限
        List<Permission> children = permissionRepository.findByParentIdOrderBySortOrderAsc(permissionId);
        if (!children.isEmpty()) {
            throw new IllegalArgumentException("存在子权限，无法删除");
        }
        
        // 检查是否有角色使用此权限
        if (!permission.getRoles().isEmpty()) {
            throw new IllegalArgumentException("权限正在被角色使用，无法删除");
        }
        
        permissionRepository.delete(permission);
        
        // 记录删除权限的审计日志
        auditLogService.recordPermissionDelete(permission);
        
        logger.info("删除权限成功 - 权限: {} ({})", permission.getName(), permission.getCode());
        return true;
    }
    
    /**
     * 获取权限树结构
     */
    @Transactional(readOnly = true)
    public List<Permission> getPermissionTree() {
        List<Permission> allPermissions = permissionRepository.findPermissionTree();
        return buildPermissionTree(allPermissions);
    }
    
    /**
     * 获取指定模块的权限树
     */
    @Transactional(readOnly = true)
    public List<Permission> getPermissionTreeByModule(String module) {
        List<Permission> permissions = permissionRepository.findByModule(module)
                .stream()
                .filter(Permission::isActive)
                .collect(Collectors.toList());
        return buildPermissionTree(permissions);
    }
    
    /**
     * 构建权限树结构
     */
    private List<Permission> buildPermissionTree(List<Permission> permissions) {
        Map<Long, Permission> permissionMap = new HashMap<>();
        List<Permission> rootPermissions = new ArrayList<>();
        
        // 构建权限映射
        for (Permission permission : permissions) {
            permissionMap.put(permission.getId(), permission);
            permission.getChildren().clear(); // 清空现有子节点
        }
        
        // 构建树结构
        for (Permission permission : permissions) {
            if (permission.isRoot()) {
                rootPermissions.add(permission);
            } else {
                Permission parent = permissionMap.get(permission.getParentId());
                if (parent != null) {
                    parent.getChildren().add(permission);
                }
            }
        }
        
        return rootPermissions;
    }
    
    /**
     * 获取菜单权限列表
     */
    @Transactional(readOnly = true)
    public List<Permission> getMenuPermissions() {
        return permissionRepository.findMenuPermissions();
    }
    
    /**
     * 获取API权限列表
     */
    @Transactional(readOnly = true)
    public List<Permission> getApiPermissions() {
        return permissionRepository.findApiPermissions();
    }
    
    /**
     * 获取按钮权限列表
     */
    @Transactional(readOnly = true)
    public List<Permission> getButtonPermissions() {
        return permissionRepository.findButtonPermissions();
    }
    
    /**
     * 根据资源路径和HTTP方法查找权限
     */
    @Transactional(readOnly = true)
    public Optional<Permission> getPermissionByResource(String resourcePath, String httpMethod) {
        return permissionRepository.findByResourcePathAndHttpMethod(resourcePath, httpMethod);
    }
    
    /**
     * 获取用户可访问的菜单权限
     */
    @Transactional(readOnly = true)
    public List<Permission> getUserMenuPermissions(Long userId) {
        List<Permission> allPermissions = permissionRepository.findByUserId(userId);
        List<Permission> menuPermissions = allPermissions.stream()
                .filter(p -> p.isMenu() && p.isActive())
                .collect(Collectors.toList());
        
        return buildPermissionTree(menuPermissions);
    }
    
    /**
     * 初始化系统权限
     */
    public void initializeSystemPermissions() {
        logger.info("开始初始化系统权限...");
        
        // 系统管理模块权限
        createPermissionIfNotExists("SYSTEM_ADMIN", "系统管理", Permission.PermissionType.MENU, "SYSTEM", 
                                   null, null, null, 1, "系统管理模块");
        
        Long systemAdminId = permissionRepository.findByCode("SYSTEM_ADMIN").get().getId();
        
        createPermissionIfNotExists("USER_MANAGE", "用户管理", Permission.PermissionType.MENU, "SYSTEM",
                                   systemAdminId, "/admin/users", null, 2, "用户管理页面");
        createPermissionIfNotExists("ROLE_MANAGE", "角色管理", Permission.PermissionType.MENU, "SYSTEM",
                                   systemAdminId, "/admin/roles", null, 3, "角色管理页面");
        createPermissionIfNotExists("PERMISSION_MANAGE", "权限管理", Permission.PermissionType.MENU, "SYSTEM",
                                   systemAdminId, "/admin/permissions", null, 4, "权限管理页面");
        
        // NESMA评估模块权限
        createPermissionIfNotExists("NESMA_MODULE", "NESMA评估", Permission.PermissionType.MENU, "NESMA",
                                   null, null, null, 10, "NESMA评估模块");
        
        Long nesmaModuleId = permissionRepository.findByCode("NESMA_MODULE").get().getId();
        
        createPermissionIfNotExists("PROJECT_MANAGE", "项目管理", Permission.PermissionType.MENU, "NESMA",
                                   nesmaModuleId, "/nesma/projects", null, 11, "项目管理页面");
        createPermissionIfNotExists("NESMA_CALCULATE", "NESMA计算", Permission.PermissionType.MENU, "NESMA",
                                   nesmaModuleId, "/nesma/calculate", null, 12, "NESMA计算页面");
        createPermissionIfNotExists("REPORT_VIEW", "报告查看", Permission.PermissionType.MENU, "NESMA",
                                   nesmaModuleId, "/nesma/reports", null, 13, "报告查看页面");
        
        // API权限
        createApiPermissions();
        
        // 按钮权限
        createButtonPermissions();
        
        logger.info("系统权限初始化完成");
    }
    
    /**
     * 创建API权限
     */
    private void createApiPermissions() {
        // 用户管理API权限
        createPermissionIfNotExists("API_USER_CREATE", "创建用户", Permission.PermissionType.API, "SYSTEM",
                                   null, "/api/admin/users", "POST", 100, "创建用户API权限");
        createPermissionIfNotExists("API_USER_UPDATE", "更新用户", Permission.PermissionType.API, "SYSTEM",
                                   null, "/api/admin/users/*", "PUT", 101, "更新用户API权限");
        createPermissionIfNotExists("API_USER_DELETE", "删除用户", Permission.PermissionType.API, "SYSTEM",
                                   null, "/api/admin/users/*", "DELETE", 102, "删除用户API权限");
        createPermissionIfNotExists("API_USER_VIEW", "查看用户", Permission.PermissionType.API, "SYSTEM",
                                   null, "/api/admin/users/**", "GET", 103, "查看用户API权限");
        
        // 角色管理API权限
        createPermissionIfNotExists("API_ROLE_CREATE", "创建角色", Permission.PermissionType.API, "SYSTEM",
                                   null, "/api/admin/roles", "POST", 110, "创建角色API权限");
        createPermissionIfNotExists("API_ROLE_UPDATE", "更新角色", Permission.PermissionType.API, "SYSTEM",
                                   null, "/api/admin/roles/*", "PUT", 111, "更新角色API权限");
        createPermissionIfNotExists("API_ROLE_DELETE", "删除角色", Permission.PermissionType.API, "SYSTEM",
                                   null, "/api/admin/roles/*", "DELETE", 112, "删除角色API权限");
        
        // NESMA相关API权限
        createPermissionIfNotExists("API_PROJECT_CREATE", "创建项目", Permission.PermissionType.API, "NESMA",
                                   null, "/api/projects", "POST", 200, "创建项目API权限");
        createPermissionIfNotExists("API_PROJECT_UPDATE", "更新项目", Permission.PermissionType.API, "NESMA",
                                   null, "/api/projects/*", "PUT", 201, "更新项目API权限");
        createPermissionIfNotExists("API_NESMA_CALCULATE", "执行NESMA计算", Permission.PermissionType.API, "NESMA",
                                   null, "/api/nesma/calculate", "POST", 210, "执行NESMA计算API权限");
    }
    
    /**
     * 创建按钮权限
     */
    private void createButtonPermissions() {
        createPermissionIfNotExists("BTN_USER_CREATE", "新增用户按钮", Permission.PermissionType.BUTTON, "SYSTEM",
                                   null, null, null, 300, "新增用户按钮权限");
        createPermissionIfNotExists("BTN_USER_EDIT", "编辑用户按钮", Permission.PermissionType.BUTTON, "SYSTEM",
                                   null, null, null, 301, "编辑用户按钮权限");
        createPermissionIfNotExists("BTN_USER_DELETE", "删除用户按钮", Permission.PermissionType.BUTTON, "SYSTEM",
                                   null, null, null, 302, "删除用户按钮权限");
        createPermissionIfNotExists("BTN_PROJECT_CREATE", "新增项目按钮", Permission.PermissionType.BUTTON, "NESMA",
                                   null, null, null, 310, "新增项目按钮权限");
        createPermissionIfNotExists("BTN_NESMA_CALCULATE", "NESMA计算按钮", Permission.PermissionType.BUTTON, "NESMA",
                                   null, null, null, 311, "NESMA计算按钮权限");
    }
    
    /**
     * 创建权限（如果不存在）
     */
    private void createPermissionIfNotExists(String code, String name, Permission.PermissionType type, 
                                           String module, Long parentId, String resourcePath, 
                                           String httpMethod, Integer sortOrder, String description) {
        if (!permissionRepository.existsByCode(code)) {
            Permission permission = new Permission();
            permission.setCode(code);
            permission.setName(name);
            permission.setPermissionType(type);
            permission.setModule(module);
            permission.setParentId(parentId);
            permission.setResourcePath(resourcePath);
            permission.setHttpMethod(httpMethod);
            permission.setSortOrder(sortOrder);
            permission.setDescription(description);
            permission.setStatus(Permission.PermissionStatus.ACTIVE);
            
            if (parentId != null) {
                Optional<Permission> parent = permissionRepository.findById(parentId);
                if (parent.isPresent()) {
                    permission.setLevel(parent.get().getLevel() + 1);
                } else {
                    permission.setLevel(1);
                }
            } else {
                permission.setLevel(1);
            }
            
            permissionRepository.save(permission);
            logger.info("创建系统权限: {} ({})", name, code);
        }
    }
    
    /**
     * 克隆权限对象（用于审计日志）
     */
    private Permission clonePermission(Permission original) {
        Permission clone = new Permission();
        clone.setId(original.getId());
        clone.setCode(original.getCode());
        clone.setName(original.getName());
        clone.setDescription(original.getDescription());
        clone.setPermissionType(original.getPermissionType());
        clone.setModule(original.getModule());
        clone.setResourcePath(original.getResourcePath());
        clone.setHttpMethod(original.getHttpMethod());
        clone.setParentId(original.getParentId());
        clone.setLevel(original.getLevel());
        clone.setSortOrder(original.getSortOrder());
        clone.setStatus(original.getStatus());
        clone.setIcon(original.getIcon());
        return clone;
    }
}