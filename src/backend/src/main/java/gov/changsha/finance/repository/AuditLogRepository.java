package gov.changsha.finance.repository;

import gov.changsha.finance.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志数据访问层
 * 提供审计日志的查询和统计功能
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-09
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * 根据用户ID查找审计日志
     */
    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);
    
    /**
     * 根据用户名查找审计日志
     */
    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);
    
    /**
     * 根据操作类型查找审计日志
     */
    List<AuditLog> findByOperationOrderByTimestampDesc(AuditLog.OperationType operation);
    
    /**
     * 根据模块查找审计日志
     */
    List<AuditLog> findByModuleOrderByTimestampDesc(String module);
    
    /**
     * 根据操作状态查找审计日志
     */
    List<AuditLog> findByOperationStatusOrderByTimestampDesc(AuditLog.OperationStatus operationStatus);
    
    /**
     * 根据风险等级查找审计日志
     */
    List<AuditLog> findByRiskLevelOrderByTimestampDesc(AuditLog.RiskLevel riskLevel);
    
    /**
     * 根据IP地址查找审计日志
     */
    List<AuditLog> findByIpAddressOrderByTimestampDesc(String ipAddress);
    
    /**
     * 根据时间范围查找审计日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp DESC")
    List<AuditLog> findByTimestampBetween(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找指定时间后的审计日志
     */
    List<AuditLog> findByTimestampAfterOrderByTimestampDesc(LocalDateTime timestamp);
    
    /**
     * 查找登录相关的审计日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.operation IN ('LOGIN', 'LOGOUT', 'LOGIN_FAILED') ORDER BY a.timestamp DESC")
    List<AuditLog> findLoginRelatedLogs();
    
    /**
     * 查找失败的操作日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.operationStatus = 'FAILED' ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedOperations();
    
    /**
     * 查找高风险操作日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.riskLevel IN ('HIGH', 'CRITICAL') ORDER BY a.timestamp DESC")
    List<AuditLog> findHighRiskOperations();
    
    /**
     * 根据业务对象查找审计日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.businessType = :businessType AND a.businessId = :businessId ORDER BY a.timestamp DESC")
    List<AuditLog> findByBusinessObject(@Param("businessType") String businessType, 
                                       @Param("businessId") String businessId);
    
    /**
     * 查找指定用户的登录历史
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.operation = 'LOGIN' AND a.operationStatus = 'SUCCESS' ORDER BY a.timestamp DESC")
    List<AuditLog> findUserLoginHistory(@Param("userId") Long userId);
    
    /**
     * 查找指定IP的登录尝试
     */
    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress AND a.operation IN ('LOGIN', 'LOGIN_FAILED') ORDER BY a.timestamp DESC")
    List<AuditLog> findLoginAttemptsByIp(@Param("ipAddress") String ipAddress);
    
    /**
     * 统计指定时间范围内的操作数量
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.timestamp BETWEEN :startTime AND :endTime")
    long countByTimestampBetween(@Param("startTime") LocalDateTime startTime, 
                                @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定用户的操作数量
     */
    long countByUserId(Long userId);
    
    /**
     * 统计指定操作类型的数量
     */
    long countByOperation(AuditLog.OperationType operation);
    
    /**
     * 统计失败操作的数量
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.operationStatus = 'FAILED'")
    long countFailedOperations();
    
    /**
     * 统计高风险操作的数量
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.riskLevel IN ('HIGH', 'CRITICAL')")
    long countHighRiskOperations();
    
    /**
     * 查找指定时间范围内的用户活跃度统计
     */
    @Query("SELECT a.userId, a.username, COUNT(a) as operationCount FROM AuditLog a " +
           "WHERE a.timestamp BETWEEN :startTime AND :endTime AND a.userId IS NOT NULL " +
           "GROUP BY a.userId, a.username ORDER BY operationCount DESC")
    List<Object[]> findUserActivityStatistics(@Param("startTime") LocalDateTime startTime, 
                                             @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找指定时间范围内的操作类型统计
     */
    @Query("SELECT a.operation, COUNT(a) as operationCount FROM AuditLog a " +
           "WHERE a.timestamp BETWEEN :startTime AND :endTime " +
           "GROUP BY a.operation ORDER BY operationCount DESC")
    List<Object[]> findOperationTypeStatistics(@Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找指定时间范围内的IP访问统计
     */
    @Query("SELECT a.ipAddress, COUNT(a) as accessCount FROM AuditLog a " +
           "WHERE a.timestamp BETWEEN :startTime AND :endTime AND a.ipAddress IS NOT NULL " +
           "GROUP BY a.ipAddress ORDER BY accessCount DESC")
    List<Object[]> findIpAccessStatistics(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找可疑活动（短时间内大量失败操作）
     */
    @Query("SELECT a.ipAddress, a.username, COUNT(a) as failureCount FROM AuditLog a " +
           "WHERE a.timestamp >= :since AND a.operationStatus = 'FAILED' " +
           "GROUP BY a.ipAddress, a.username " +
           "HAVING COUNT(a) >= :threshold ORDER BY failureCount DESC")
    List<Object[]> findSuspiciousActivity(@Param("since") LocalDateTime since, 
                                         @Param("threshold") Long threshold);
    
    /**
     * 删除指定时间之前的审计日志（用于日志轮转）
     */
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :beforeTime")
    void deleteLogsBefore(@Param("beforeTime") LocalDateTime beforeTime);
}