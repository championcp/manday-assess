package gov.changsha.finance.repository;

import gov.changsha.finance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 * 提供用户信息的CRUD操作和查询功能
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-09
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据工号查找用户
     */
    Optional<User> findByEmployeeId(String employeeId);
    
    /**
     * 根据用户名、邮箱或工号查找用户（支持多种登录方式）
     */
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier OR u.employeeId = :identifier")
    Optional<User> findByUsernameOrEmailOrEmployeeId(@Param("identifier") String username, 
                                                   @Param("identifier") String email, 
                                                   @Param("identifier") String employeeId);
    
    /**
     * 检查用户名是否已存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否已存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 检查工号是否已存在
     */
    boolean existsByEmployeeId(String employeeId);
    
    /**
     * 根据部门查找用户列表
     */
    List<User> findByDepartment(String department);
    
    /**
     * 根据账户状态查找用户列表
     */
    List<User> findByAccountStatus(User.AccountStatus accountStatus);
    
    /**
     * 查找活跃用户列表
     */
    @Query("SELECT u FROM User u WHERE u.accountStatus = 'ACTIVE'")
    List<User> findActiveUsers();
    
    /**
     * 查找被锁定的用户列表
     */
    @Query("SELECT u FROM User u WHERE u.accountStatus = 'LOCKED'")
    List<User> findLockedUsers();
    
    /**
     * 查找指定时间后登录的用户
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt > :since")
    List<User> findUsersLoggedInSince(@Param("since") LocalDateTime since);
    
    /**
     * 查找密码即将过期的用户（30天内）
     */
    @Query("SELECT u FROM User u WHERE u.passwordExpiresAt BETWEEN CURRENT_TIMESTAMP AND :expiryDate")
    List<User> findUsersWithPasswordExpiringSoon(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * 查找密码已过期的用户
     */
    @Query("SELECT u FROM User u WHERE u.passwordExpiresAt < CURRENT_TIMESTAMP")
    List<User> findUsersWithExpiredPassword();
    
    /**
     * 根据真实姓名模糊查询用户
     */
    @Query("SELECT u FROM User u WHERE u.realName LIKE %:name%")
    List<User> findByRealNameContaining(@Param("name") String name);
    
    /**
     * 查找指定角色的用户
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.code = :roleCode")
    List<User> findByRoleCode(@Param("roleCode") String roleCode);
    
    /**
     * 查找具有指定权限的用户
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r JOIN r.permissions p WHERE p.code = :permissionCode")
    List<User> findByPermissionCode(@Param("permissionCode") String permissionCode);
    
    /**
     * 统计活跃用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.accountStatus = 'ACTIVE'")
    long countActiveUsers();
    
    /**
     * 统计指定部门的用户数量
     */
    long countByDepartment(String department);
}