package gov.changsha.finance.service;

import gov.changsha.finance.entity.Permission;
import gov.changsha.finance.entity.Role;
import gov.changsha.finance.entity.User;
import gov.changsha.finance.repository.PermissionRepository;
import gov.changsha.finance.repository.RoleRepository;
import gov.changsha.finance.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色管理服务
 * 提供角色的CRUD操作和权限分配功能
 * 符合RBAC权限管理模型
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-09
 */
@Service
@Transactional
public class RoleService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuditLogService auditLogService; // 将在后续创建
    
    /**
     * 创建角色
     */
    public Role createRole(Role role) {
        // 验证角色代码唯一性
        if (roleRepository.existsByCode(role.getCode())) {
            throw new IllegalArgumentException("角色代码已存在: " + role.getCode());
        }
        
        // 验证角色名称唯一性
        if (roleRepository.existsByName(role.getName())) {
            throw new IllegalArgumentException("角色名称已存在: " + role.getName());
        }
        
        Role savedRole = roleRepository.save(role);
        
        // 记录创建角色的审计日志
        auditLogService.recordRoleCreate(savedRole);
        
        logger.info("创建角色成功 - 角色: {} ({})", savedRole.getName(), savedRole.getCode());
        return savedRole;
    }
    
    /**
     * 更新角色信息
     */
    public Role updateRole(Long roleId, Role roleUpdates) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            throw new IllegalArgumentException("角色不存在: " + roleId);
        }
        
        Role existingRole = roleOpt.get();
        
        // 系统角色不允许修改关键属性
        if (existingRole.isSystemRole()) {
            throw new IllegalArgumentException("系统角色不允许修改");
        }
        
        Role originalRole = cloneRole(existingRole); // 用于审计日志
        
        // 更新允许修改的字段
        if (roleUpdates.getName() != null && !roleUpdates.getName().equals(existingRole.getName())) {
            if (roleRepository.existsByName(roleUpdates.getName())) {
                throw new IllegalArgumentException("角色名称已存在: " + roleUpdates.getName());
            }
            existingRole.setName(roleUpdates.getName());
        }
        
        if (roleUpdates.getDescription() != null) {
            existingRole.setDescription(roleUpdates.getDescription());
        }
        
        if (roleUpdates.getStatus() != null) {
            existingRole.setStatus(roleUpdates.getStatus());
        }
        
        if (roleUpdates.getSortOrder() != null) {
            existingRole.setSortOrder(roleUpdates.getSortOrder());
        }
        
        Role savedRole = roleRepository.save(existingRole);
        
        // 记录更新角色的审计日志
        auditLogService.recordRoleUpdate(originalRole, savedRole);
        
        logger.info("更新角色成功 - 角色: {} ({})", savedRole.getName(), savedRole.getCode());
        return savedRole;
    }
    
    /**
     * 删除角色
     */
    public boolean deleteRole(Long roleId) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            throw new IllegalArgumentException("角色不存在: " + roleId);
        }
        
        Role role = roleOpt.get();
        
        // 系统角色不允许删除
        if (role.isSystemRole()) {
            throw new IllegalArgumentException("系统角色不允许删除");
        }
        
        // 检查是否有用户使用此角色
        List<User> usersWithRole = userRepository.findByRoleCode(role.getCode());
        if (!usersWithRole.isEmpty()) {
            throw new IllegalArgumentException("角色正在被使用，无法删除");
        }
        
        roleRepository.delete(role);
        
        // 记录删除角色的审计日志
        auditLogService.recordRoleDelete(role);
        
        logger.info("删除角色成功 - 角色: {} ({})", role.getName(), role.getCode());
        return true;
    }
    
    /**
     * 为角色分配权限
     */
    public Role assignPermissionsToRole(Long roleId, Set<Long> permissionIds) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            throw new IllegalArgumentException("角色不存在: " + roleId);
        }
        
        Role role = roleOpt.get();
        Set<String> originalPermissions = role.getPermissionCodes(); // 用于审计日志
        
        // 获取所有指定的权限
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new IllegalArgumentException("部分权限不存在");
        }
        
        // 清除现有权限并设置新权限
        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);
        
        Role savedRole = roleRepository.save(role);
        
        // 记录权限分配的审计日志
        Set<String> newPermissions = savedRole.getPermissionCodes();
        auditLogService.recordPermissionAssignment(role, originalPermissions, newPermissions);
        
        logger.info("角色权限分配成功 - 角色: {} ({}), 权限数量: {}", 
                  savedRole.getName(), savedRole.getCode(), permissions.size());
        return savedRole;
    }
    
    /**
     * 为角色添加权限
     */
    public Role addPermissionsToRole(Long roleId, Set<Long> permissionIds) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            throw new IllegalArgumentException("角色不存在: " + roleId);
        }
        
        Role role = roleOpt.get();
        Set<String> originalPermissions = role.getPermissionCodes();
        
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        role.getPermissions().addAll(permissions);
        
        Role savedRole = roleRepository.save(role);
        
        // 记录权限添加的审计日志
        Set<String> newPermissions = savedRole.getPermissionCodes();
        auditLogService.recordPermissionAssignment(role, originalPermissions, newPermissions);
        
        logger.info("角色权限添加成功 - 角色: {} ({})", savedRole.getName(), savedRole.getCode());
        return savedRole;
    }
    
    /**
     * 从角色移除权限
     */
    public Role removePermissionsFromRole(Long roleId, Set<Long> permissionIds) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (!roleOpt.isPresent()) {
            throw new IllegalArgumentException("角色不存在: " + roleId);
        }
        
        Role role = roleOpt.get();
        Set<String> originalPermissions = role.getPermissionCodes();
        
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        role.getPermissions().removeAll(permissions);
        
        Role savedRole = roleRepository.save(role);
        
        // 记录权限移除的审计日志
        Set<String> newPermissions = savedRole.getPermissionCodes();
        auditLogService.recordPermissionAssignment(role, originalPermissions, newPermissions);
        
        logger.info("角色权限移除成功 - 角色: {} ({})", savedRole.getName(), savedRole.getCode());
        return savedRole;
    }
    
    /**
     * 获取角色的所有权限
     */
    @Transactional(readOnly = true)
    public List<Permission> getRolePermissions(Long roleId) {
        List<Permission> permissions = permissionRepository.findByRoleId(roleId);
        return permissions.stream()
                .filter(Permission::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户的所有角色
     */
    @Transactional(readOnly = true)
    public List<Role> getUserRoles(Long userId) {
        return roleRepository.findByUserId(userId);
    }
    
    /**
     * 获取用户的所有权限（通过角色）
     */
    @Transactional(readOnly = true)
    public Set<String> getUserPermissions(Long userId) {
        List<Permission> permissions = permissionRepository.findByUserId(userId);
        return permissions.stream()
                .filter(Permission::isActive)
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }
    
    /**
     * 检查用户是否有指定角色
     */
    @Transactional(readOnly = true)
    public boolean userHasRole(Long userId, String roleCode) {
        List<Role> userRoles = roleRepository.findByUserId(userId);
        return userRoles.stream().anyMatch(role -> role.getCode().equals(roleCode));
    }
    
    /**
     * 检查用户是否有指定权限
     */
    @Transactional(readOnly = true)
    public boolean userHasPermission(Long userId, String permissionCode) {
        Set<String> userPermissions = getUserPermissions(userId);
        return userPermissions.contains(permissionCode);
    }
    
    /**
     * 获取所有活跃角色
     */
    @Transactional(readOnly = true)
    public List<Role> getActiveRoles() {
        return roleRepository.findActiveRoles();
    }
    
    /**
     * 获取系统预定义角色
     */
    @Transactional(readOnly = true)
    public List<Role> getSystemRoles() {
        return roleRepository.findSystemRoles();
    }
    
    /**
     * 初始化系统默认角色和权限
     */
    public void initializeSystemRoles() {
        logger.info("开始初始化系统角色和权限...");
        
        // 创建系统管理员角色
        createRoleIfNotExists("ADMIN", "系统管理员", "拥有系统所有权限的超级管理员", Role.RoleType.SYSTEM);
        
        // 创建政府评审员角色
        createRoleIfNotExists("REVIEWER", "政府评审员", "负责项目评审和NESMA计算的评审人员", Role.RoleType.SYSTEM);
        
        // 创建审计员角色
        createRoleIfNotExists("AUDITOR", "审计员", "负责系统审计和日志查看的审计人员", Role.RoleType.SYSTEM);
        
        // 创建普通用户角色
        createRoleIfNotExists("USER", "普通用户", "系统普通用户，具有基础功能权限", Role.RoleType.SYSTEM);
        
        logger.info("系统角色初始化完成");
    }
    
    /**
     * 创建角色（如果不存在）
     */
    private void createRoleIfNotExists(String code, String name, String description, Role.RoleType roleType) {
        if (!roleRepository.existsByCode(code)) {
            Role role = new Role(name, code, description, roleType);
            role.setStatus(Role.RoleStatus.ACTIVE);
            roleRepository.save(role);
            logger.info("创建系统角色: {} ({})", name, code);
        }
    }
    
    /**
     * 克隆角色对象（用于审计日志）
     */
    private Role cloneRole(Role original) {
        Role clone = new Role();
        clone.setId(original.getId());
        clone.setName(original.getName());
        clone.setCode(original.getCode());
        clone.setDescription(original.getDescription());
        clone.setRoleType(original.getRoleType());
        clone.setStatus(original.getStatus());
        clone.setSortOrder(original.getSortOrder());
        return clone;
    }
}