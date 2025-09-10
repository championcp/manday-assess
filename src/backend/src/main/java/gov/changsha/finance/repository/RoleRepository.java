package gov.changsha.finance.repository;

import gov.changsha.finance.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色数据访问层
 * 提供角色信息的CRUD操作和查询功能
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-09
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * 根据角色名称查找角色
     */
    Optional<Role> findByName(String name);
    
    /**
     * 根据角色代码查找角色
     */
    Optional<Role> findByCode(String code);
    
    /**
     * 检查角色名称是否已存在
     */
    boolean existsByName(String name);
    
    /**
     * 检查角色代码是否已存在
     */
    boolean existsByCode(String code);
    
    /**
     * 根据角色类型查找角色列表
     */
    List<Role> findByRoleType(Role.RoleType roleType);
    
    /**
     * 根据角色状态查找角色列表
     */
    List<Role> findByStatus(Role.RoleStatus status);
    
    /**
     * 查找活跃角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.status = 'ACTIVE' ORDER BY r.sortOrder, r.name")
    List<Role> findActiveRoles();
    
    /**
     * 查找系统角色列表（不可删除）
     */
    @Query("SELECT r FROM Role r WHERE r.roleType = 'SYSTEM' ORDER BY r.sortOrder, r.name")
    List<Role> findSystemRoles();
    
    /**
     * 查找业务角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.roleType = 'BUSINESS' AND r.status = 'ACTIVE' ORDER BY r.sortOrder, r.name")
    List<Role> findBusinessRoles();
    
    /**
     * 查找自定义角色列表
     */
    @Query("SELECT r FROM Role r WHERE r.roleType = 'CUSTOM' AND r.status = 'ACTIVE' ORDER BY r.sortOrder, r.name")
    List<Role> findCustomRoles();
    
    /**
     * 根据用户ID查找用户拥有的角色
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据权限代码查找拥有该权限的角色
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.code = :permissionCode")
    List<Role> findByPermissionCode(@Param("permissionCode") String permissionCode);
    
    /**
     * 查找包含指定权限的所有角色
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.id = :permissionId")
    List<Role> findByPermissionId(@Param("permissionId") Long permissionId);
    
    /**
     * 按排序权重查找所有活跃角色
     */
    @Query("SELECT r FROM Role r WHERE r.status = 'ACTIVE' ORDER BY r.sortOrder ASC, r.name ASC")
    List<Role> findAllActiveOrderBySortOrder();
    
    /**
     * 统计指定类型的角色数量
     */
    long countByRoleType(Role.RoleType roleType);
    
    /**
     * 统计活跃角色数量
     */
    @Query("SELECT COUNT(r) FROM Role r WHERE r.status = 'ACTIVE'")
    long countActiveRoles();
}