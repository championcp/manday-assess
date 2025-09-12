package gov.changsha.finance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.changsha.finance.entity.AuditLog;
import gov.changsha.finance.entity.Permission;
import gov.changsha.finance.entity.Role;
import gov.changsha.finance.entity.User;
import gov.changsha.finance.repository.AuditLogRepository;
import gov.changsha.finance.security.jwt.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 审计日志服务
 * 提供完整的审计日志记录和查询功能
 * 符合政府级安全审计要求
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-09
 */
@Service
@Transactional
public class AuditLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);
    
    private static final String SIGNATURE_ALGORITHM = "HmacSHA256";
    private static final String SIGNATURE_KEY = "manday-assess-audit-signature-key-2025"; // 生产环境应从配置文件读取
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 记录用户登录
     */
    public void recordLogin(User user, String ipAddress, String userAgent) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.LOGIN,
            "AUTH",
            "用户登录系统"
        );
        
        setUserInfo(auditLog, user);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLog.setRiskLevel(AuditLog.RiskLevel.LOW);
        auditLog.setBusinessType("User");
        auditLog.setBusinessId(user.getId().toString());
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("loginTime", LocalDateTime.now());
            data.put("userInfo", createSafeUserInfo(user));
            auditLog.setBusinessData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            logger.warn("记录登录审计日志数据时出错", e);
        }
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录登录失败
     */
    public void recordFailedLogin(User user, String ipAddress, String reason) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.LOGIN_FAILED,
            "AUTH",
            "用户登录失败: " + reason
        );
        
        setUserInfo(auditLog, user);
        auditLog.setIpAddress(ipAddress);
        auditLog.setOperationStatus(AuditLog.OperationStatus.FAILED);
        auditLog.setResultMessage(reason);
        auditLog.setRiskLevel(AuditLog.RiskLevel.MEDIUM);
        auditLog.setBusinessType("User");
        auditLog.setBusinessId(user.getId().toString());
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录未知用户登录尝试
     */
    public void recordUnknownUserLogin(String username, String ipAddress, String reason) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.LOGIN_FAILED,
            "AUTH",
            "未知用户登录尝试: " + username
        );
        
        auditLog.setUsername(username);
        auditLog.setIpAddress(ipAddress);
        auditLog.setOperationStatus(AuditLog.OperationStatus.FAILED);
        auditLog.setResultMessage(reason);
        auditLog.setRiskLevel(AuditLog.RiskLevel.HIGH);
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录用户登出
     */
    public void recordLogout(User user, String ipAddress) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.LOGOUT,
            "AUTH",
            "用户登出系统"
        );
        
        setUserInfo(auditLog, user);
        auditLog.setIpAddress(ipAddress);
        auditLog.setRiskLevel(AuditLog.RiskLevel.LOW);
        auditLog.setBusinessType("User");
        auditLog.setBusinessId(user.getId().toString());
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录用户注册
     */
    public void recordRegistration(User user, String ipAddress, String clientInfo) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.REGISTER,
            "AUTH",
            "用户注册新账户"
        );
        
        setUserInfo(auditLog, user);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(clientInfo);
        auditLog.setRiskLevel(AuditLog.RiskLevel.MEDIUM); // 注册是中风险操作
        auditLog.setBusinessType("User");
        auditLog.setBusinessId(user.getId().toString());
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("registrationTime", LocalDateTime.now());
            data.put("userInfo", createSafeUserInfo(user));
            auditLog.setBusinessData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            logger.warn("记录用户注册审计日志数据时出错", e);
        }
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录账户解锁
     */
    public void recordAccountUnlock(User user) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.USER_UNLOCK,
            "USER_MANAGEMENT",
            "管理员解锁用户账户"
        );
        
        setCurrentUserInfo(auditLog);
        auditLog.setRiskLevel(AuditLog.RiskLevel.MEDIUM);
        auditLog.setBusinessType("User");
        auditLog.setBusinessId(user.getId().toString());
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("targetUser", createSafeUserInfo(user));
            data.put("action", "unlock");
            auditLog.setBusinessData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            logger.warn("记录账户解锁审计日志数据时出错", e);
        }
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录账户锁定
     */
    public void recordAccountLock(User user, String reason) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.USER_LOCK,
            "USER_MANAGEMENT",
            "管理员锁定用户账户"
        );
        
        setCurrentUserInfo(auditLog);
        auditLog.setRiskLevel(AuditLog.RiskLevel.MEDIUM);
        auditLog.setResultMessage(reason);
        auditLog.setBusinessType("User");
        auditLog.setBusinessId(user.getId().toString());
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("targetUser", createSafeUserInfo(user));
            data.put("action", "lock");
            data.put("reason", reason);
            auditLog.setBusinessData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            logger.warn("记录账户锁定审计日志数据时出错", e);
        }
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录角色创建
     */
    public void recordRoleCreate(Role role) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.ROLE_CREATE,
            "ROLE_MANAGEMENT",
            "创建角色: " + role.getName()
        );
        
        setCurrentUserInfo(auditLog);
        auditLog.setRiskLevel(AuditLog.RiskLevel.MEDIUM);
        auditLog.setBusinessType("Role");
        auditLog.setBusinessId(role.getId().toString());
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("roleInfo", createSafeRoleInfo(role));
            auditLog.setBusinessData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            logger.warn("记录角色创建审计日志数据时出错", e);
        }
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录角色更新
     */
    public void recordRoleUpdate(Role originalRole, Role updatedRole) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.ROLE_UPDATE,
            "ROLE_MANAGEMENT",
            "更新角色: " + updatedRole.getName()
        );
        
        setCurrentUserInfo(auditLog);
        auditLog.setRiskLevel(AuditLog.RiskLevel.MEDIUM);
        auditLog.setBusinessType("Role");
        auditLog.setBusinessId(updatedRole.getId().toString());
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("originalRole", createSafeRoleInfo(originalRole));
            data.put("updatedRole", createSafeRoleInfo(updatedRole));
            auditLog.setBusinessData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            logger.warn("记录角色更新审计日志数据时出错", e);
        }
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录角色删除
     */
    public void recordRoleDelete(Role role) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.ROLE_DELETE,
            "ROLE_MANAGEMENT",
            "删除角色: " + role.getName()
        );
        
        setCurrentUserInfo(auditLog);
        auditLog.setRiskLevel(AuditLog.RiskLevel.HIGH);
        auditLog.setBusinessType("Role");
        auditLog.setBusinessId(role.getId().toString());
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("deletedRole", createSafeRoleInfo(role));
            auditLog.setBusinessData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            logger.warn("记录角色删除审计日志数据时出错", e);
        }
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录权限分配
     */
    public void recordPermissionAssignment(Role role, Set<String> originalPermissions, Set<String> newPermissions) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.PERMISSION_ASSIGN,
            "PERMISSION_MANAGEMENT",
            "角色权限变更: " + role.getName()
        );
        
        setCurrentUserInfo(auditLog);
        auditLog.setRiskLevel(AuditLog.RiskLevel.HIGH);
        auditLog.setBusinessType("Role");
        auditLog.setBusinessId(role.getId().toString());
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("roleInfo", createSafeRoleInfo(role));
            data.put("originalPermissions", originalPermissions);
            data.put("newPermissions", newPermissions);
            
            Set<String> added = new HashSet<>(newPermissions);
            added.removeAll(originalPermissions);
            data.put("addedPermissions", added);
            
            Set<String> removed = new HashSet<>(originalPermissions);
            removed.removeAll(newPermissions);
            data.put("removedPermissions", removed);
            
            auditLog.setBusinessData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            logger.warn("记录权限分配审计日志数据时出错", e);
        }
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录权限创建
     */
    public void recordPermissionCreate(Permission permission) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.PERMISSION_CREATE,
            "PERMISSION_MANAGEMENT",
            "创建权限: " + permission.getName()
        );
        
        setCurrentUserInfo(auditLog);
        auditLog.setRiskLevel(AuditLog.RiskLevel.MEDIUM);
        auditLog.setBusinessType("Permission");
        auditLog.setBusinessId(permission.getId().toString());
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("permissionInfo", createSafePermissionInfo(permission));
            auditLog.setBusinessData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            logger.warn("记录权限创建审计日志数据时出错", e);
        }
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录权限更新
     */
    public void recordPermissionUpdate(Permission originalPermission, Permission updatedPermission) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.PERMISSION_UPDATE,
            "PERMISSION_MANAGEMENT",
            "更新权限: " + updatedPermission.getName()
        );
        
        setCurrentUserInfo(auditLog);
        auditLog.setRiskLevel(AuditLog.RiskLevel.MEDIUM);
        auditLog.setBusinessType("Permission");
        auditLog.setBusinessId(updatedPermission.getId().toString());
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("originalPermission", createSafePermissionInfo(originalPermission));
            data.put("updatedPermission", createSafePermissionInfo(updatedPermission));
            auditLog.setBusinessData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            logger.warn("记录权限更新审计日志数据时出错", e);
        }
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录权限删除
     */
    public void recordPermissionDelete(Permission permission) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.PERMISSION_DELETE,
            "PERMISSION_MANAGEMENT",
            "删除权限: " + permission.getName()
        );
        
        setCurrentUserInfo(auditLog);
        auditLog.setRiskLevel(AuditLog.RiskLevel.HIGH);
        auditLog.setBusinessType("Permission");
        auditLog.setBusinessId(permission.getId().toString());
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("deletedPermission", createSafePermissionInfo(permission));
            auditLog.setBusinessData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            logger.warn("记录权限删除审计日志数据时出错", e);
        }
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录NESMA计算操作
     */
    public void recordNesmaCalculation(String projectId, Map<String, Object> calculationData) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.NESMA_CALCULATE,
            "NESMA",
            "执行NESMA功能点计算"
        );
        
        setCurrentUserInfo(auditLog);
        auditLog.setRiskLevel(AuditLog.RiskLevel.MEDIUM);
        auditLog.setBusinessType("Project");
        auditLog.setBusinessId(projectId);
        
        try {
            auditLog.setBusinessData(objectMapper.writeValueAsString(calculationData));
        } catch (Exception e) {
            logger.warn("记录NESMA计算审计日志数据时出错", e);
        }
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录数据导出操作
     */
    public void recordDataExport(String dataType, String exportFormat, int recordCount) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.DATA_EXPORT,
            "DATA_MANAGEMENT",
            String.format("导出%s数据，格式：%s，记录数：%d", dataType, exportFormat, recordCount)
        );
        
        setCurrentUserInfo(auditLog);
        auditLog.setRiskLevel(AuditLog.RiskLevel.HIGH); // 数据导出为高风险操作
        
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("dataType", dataType);
            data.put("exportFormat", exportFormat);
            data.put("recordCount", recordCount);
            data.put("exportTime", LocalDateTime.now());
            auditLog.setBusinessData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            logger.warn("记录数据导出审计日志数据时出错", e);
        }
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 记录系统异常
     */
    public void recordSystemException(String operation, Exception exception) {
        AuditLog auditLog = new AuditLog(
            AuditLog.OperationType.OTHER,
            "SYSTEM",
            "系统异常: " + operation
        );
        
        setCurrentUserInfo(auditLog);
        auditLog.setOperationStatus(AuditLog.OperationStatus.FAILED);
        auditLog.setRiskLevel(AuditLog.RiskLevel.CRITICAL);
        auditLog.setResultMessage(exception.getMessage());
        auditLog.setExceptionInfo(getStackTrace(exception));
        
        generateSignature(auditLog);
        saveAuditLog(auditLog);
    }
    
    /**
     * 设置用户信息
     */
    private void setUserInfo(AuditLog auditLog, User user) {
        if (user != null) {
            auditLog.setUserId(user.getId());
            auditLog.setUsername(user.getUsername());
            auditLog.setRealName(user.getRealName());
        }
    }
    
    /**
     * 设置当前用户信息
     */
    private void setCurrentUserInfo(AuditLog auditLog) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                auditLog.setUserId(userPrincipal.getId());
                auditLog.setUsername(userPrincipal.getUsername());
                auditLog.setRealName(userPrincipal.getRealName());
            }
        } catch (Exception e) {
            logger.warn("设置当前用户信息时出错", e);
        }
    }
    
    /**
     * 创建安全的用户信息（脱敏）
     */
    private Map<String, Object> createSafeUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("email", maskEmail(user.getEmail()));
        userInfo.put("employeeId", user.getEmployeeId());
        userInfo.put("department", user.getDepartment());
        userInfo.put("position", user.getPosition());
        userInfo.put("accountStatus", user.getAccountStatus());
        // 密码等敏感信息不记录
        return userInfo;
    }
    
    /**
     * 创建安全的角色信息
     */
    private Map<String, Object> createSafeRoleInfo(Role role) {
        Map<String, Object> roleInfo = new HashMap<>();
        roleInfo.put("id", role.getId());
        roleInfo.put("name", role.getName());
        roleInfo.put("code", role.getCode());
        roleInfo.put("description", role.getDescription());
        roleInfo.put("roleType", role.getRoleType());
        roleInfo.put("status", role.getStatus());
        return roleInfo;
    }
    
    /**
     * 创建安全的权限信息
     */
    private Map<String, Object> createSafePermissionInfo(Permission permission) {
        Map<String, Object> permissionInfo = new HashMap<>();
        permissionInfo.put("id", permission.getId());
        permissionInfo.put("code", permission.getCode());
        permissionInfo.put("name", permission.getName());
        permissionInfo.put("description", permission.getDescription());
        permissionInfo.put("permissionType", permission.getPermissionType());
        permissionInfo.put("module", permission.getModule());
        permissionInfo.put("resourcePath", permission.getResourcePath());
        permissionInfo.put("httpMethod", permission.getHttpMethod());
        permissionInfo.put("status", permission.getStatus());
        return permissionInfo;
    }
    
    /**
     * 邮箱脱敏
     */
    private String maskEmail(String email) {
        if (email == null || email.length() <= 3) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
    
    /**
     * 获取异常堆栈信息
     */
    private String getStackTrace(Exception exception) {
        StringBuilder sb = new StringBuilder();
        sb.append(exception.toString()).append("\n");
        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * 生成数字签名（防篡改）
     */
    private void generateSignature(AuditLog auditLog) {
        try {
            String data = String.format("%s:%s:%s:%s:%s:%s",
                auditLog.getUsername(),
                auditLog.getOperation(),
                auditLog.getModule(),
                auditLog.getOperationDesc(),
                auditLog.getTimestamp(),
                auditLog.getIpAddress()
            );
            
            Mac mac = Mac.getInstance(SIGNATURE_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(SIGNATURE_KEY.getBytes(StandardCharsets.UTF_8), SIGNATURE_ALGORITHM);
            mac.init(secretKey);
            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            auditLog.setSignature(Base64.getEncoder().encodeToString(signature));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("生成审计日志签名失败", e);
        }
    }
    
    /**
     * 验证数字签名
     */
    public boolean verifySignature(AuditLog auditLog) {
        try {
            String data = String.format("%s:%s:%s:%s:%s:%s",
                auditLog.getUsername(),
                auditLog.getOperation(),
                auditLog.getModule(),
                auditLog.getOperationDesc(),
                auditLog.getTimestamp(),
                auditLog.getIpAddress()
            );
            
            Mac mac = Mac.getInstance(SIGNATURE_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(SIGNATURE_KEY.getBytes(StandardCharsets.UTF_8), SIGNATURE_ALGORITHM);
            mac.init(secretKey);
            byte[] expectedSignature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            String expectedSignatureStr = Base64.getEncoder().encodeToString(expectedSignature);
            return expectedSignatureStr.equals(auditLog.getSignature());
        } catch (Exception e) {
            logger.error("验证审计日志签名失败", e);
            return false;
        }
    }
    
    /**
     * 保存审计日志
     */
    private void saveAuditLog(AuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // 审计日志保存失败不应影响业务操作，只记录错误
            logger.error("保存审计日志失败", e);
        }
    }
    
    /**
     * 获取审计日志列表
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogs(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.findByTimestampBetween(startTime, endTime);
    }
    
    /**
     * 获取用户操作历史
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getUserOperationHistory(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }
    
    /**
     * 获取高风险操作记录
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getHighRiskOperations() {
        return auditLogRepository.findHighRiskOperations();
    }
    
    /**
     * 清理过期审计日志
     */
    @Transactional
    public void cleanupOldLogs(LocalDateTime beforeTime) {
        try {
            auditLogRepository.deleteLogsBefore(beforeTime);
            logger.info("清理{}之前的审计日志完成", beforeTime);
        } catch (Exception e) {
            logger.error("清理过期审计日志失败", e);
        }
    }
    
    /**
     * 记录系统操作
     */
    public void recordSystemOperation(String operationType, String description, 
                                    String businessType, String businessId) {
        try {
            AuditLog.OperationType opType = AuditLog.OperationType.valueOf(operationType);
            AuditLog auditLog = new AuditLog(opType, "SYSTEM", description);
            
            setCurrentUserInfo(auditLog);
            auditLog.setRiskLevel(AuditLog.RiskLevel.MEDIUM);
            
            if (businessType != null) {
                auditLog.setBusinessType(businessType);
            }
            if (businessId != null) {
                auditLog.setBusinessId(businessId);
            }
            
            generateSignature(auditLog);
            saveAuditLog(auditLog);
            
        } catch (Exception e) {
            logger.error("记录系统操作审计日志失败", e);
        }
    }
    
    /**
     * 记录系统操作（带结果消息）
     */
    public void recordSystemOperation(String operationType, String description, 
                                    String businessType, String businessId, String resultMessage) {
        try {
            AuditLog.OperationType opType = AuditLog.OperationType.valueOf(operationType);
            AuditLog auditLog = new AuditLog(opType, "SYSTEM", description);
            
            setCurrentUserInfo(auditLog);
            auditLog.setRiskLevel(AuditLog.RiskLevel.MEDIUM);
            auditLog.setResultMessage(resultMessage);
            
            if (businessType != null) {
                auditLog.setBusinessType(businessType);
            }
            if (businessId != null) {
                auditLog.setBusinessId(businessId);
            }
            
            generateSignature(auditLog);
            saveAuditLog(auditLog);
            
        } catch (Exception e) {
            logger.error("记录系统操作审计日志失败", e);
        }
    }
}