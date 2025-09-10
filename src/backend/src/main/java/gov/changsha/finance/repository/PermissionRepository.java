package gov.changsha.finance.repository;

import gov.changsha.finance.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限数据访问层
 * 提供权限信息的CRUD操作和查询功能
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-09
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    /**
     * 根据权限代码查找权限
     */
    Optional<Permission> findByCode(String code);
    
    /**
     * 根据权限名称查找权限
     */
    Optional<Permission> findByName(String name);
    
    /**
     * 检查权限代码是否已存在
     */
    boolean existsByCode(String code);
    
    /**
     * 根据权限类型查找权限列表
     */
    List<Permission> findByPermissionType(Permission.PermissionType permissionType);
    
    /**
     * 根据所属模块查找权限列表
     */
    List<Permission> findByModule(String module);
    
    /**
     * 根据权限状态查找权限列表
     */
    List<Permission> findByStatus(Permission.PermissionStatus status);
    
    /**
     * 查找活跃权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.status = 'ACTIVE' ORDER BY p.module, p.sortOrder, p.name")
    List<Permission> findActivePermissions();
    
    /**
     * 查找菜单权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.permissionType = 'MENU' AND p.status = 'ACTIVE' ORDER BY p.sortOrder, p.name")
    List<Permission> findMenuPermissions();
    
    /**
     * 查找API权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.permissionType = 'API' AND p.status = 'ACTIVE' ORDER BY p.module, p.name")
    List<Permission> findApiPermissions();
    
    /**
     * 查找按钮权限列表
     */
    @Query("SELECT p FROM Permission p WHERE p.permissionType = 'BUTTON' AND p.status = 'ACTIVE' ORDER BY p.module, p.name")
    List<Permission> findButtonPermissions();
    
    /**
     * 根据父权限ID查找子权限列表
     */
    List<Permission> findByParentIdOrderBySortOrderAsc(Long parentId);
    
    /**
     * 查找根权限列表（parentId为null）
     */
    @Query("SELECT p FROM Permission p WHERE p.parentId IS NULL AND p.status = 'ACTIVE' ORDER BY p.sortOrder, p.name")
    List<Permission> findRootPermissions();
    
    /**
     * 根据资源路径和HTTP方法查找权限
     */
    @Query("SELECT p FROM Permission p WHERE p.resourcePath = :resourcePath AND p.httpMethod = :httpMethod")
    Optional<Permission> findByResourcePathAndHttpMethod(@Param("resourcePath") String resourcePath, 
                                                        @Param("httpMethod") String httpMethod);
    
    /**
     * 根据角色ID查找角色拥有的权限
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据用户ID查找用户拥有的权限（通过角色）
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN r.users u WHERE u.id = :userId")
    List<Permission> findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据模块和权限类型查找权限
     */
    @Query("SELECT p FROM Permission p WHERE p.module = :module AND p.permissionType = :permissionType AND p.status = 'ACTIVE' ORDER BY p.sortOrder, p.name")
    List<Permission> findByModuleAndPermissionType(@Param("module") String module, 
                                                  @Param("permissionType") Permission.PermissionType permissionType);
    
    /**
     * 查找指定层级的权限
     */
    @Query("SELECT p FROM Permission p WHERE p.level = :level AND p.status = 'ACTIVE' ORDER BY p.sortOrder, p.name")
    List<Permission> findByLevel(@Param("level") Integer level);
    
    /**
     * 统计指定模块的权限数量
     */
    long countByModule(String module);
    
    /**
     * 统计指定类型的权限数量
     */
    long countByPermissionType(Permission.PermissionType permissionType);
    
    /**
     * 统计活跃权限数量
     */
    @Query("SELECT COUNT(p) FROM Permission p WHERE p.status = 'ACTIVE'")
    long countActivePermissions();
    
    /**
     * 查找权限树结构（递归查询）
     */
    @Query("SELECT p FROM Permission p WHERE p.status = 'ACTIVE' ORDER BY p.level, p.sortOrder, p.name")
    List<Permission> findPermissionTree();
}