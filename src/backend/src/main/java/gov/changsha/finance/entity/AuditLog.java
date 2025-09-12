package gov.changsha.finance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 审计日志实体类
 * 记录系统中所有重要操作和事件
 * 符合政府级安全审计要求
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-09
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_operation", columnList = "operation"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_ip", columnList = "ip_address"),
    @Index(name = "idx_audit_module", columnList = "module"),
    @Index(name = "idx_audit_status", columnList = "operation_status")
})
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 操作用户ID（可为空，如系统操作）
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 操作用户名
     */
    @Column(name = "username", length = 100)
    private String username;
    
    /**
     * 用户真实姓名
     */
    @Column(name = "real_name", length = 100)
    private String realName;
    
    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false, length = 50)
    private OperationType operation;
    
    /**
     * 操作模块
     */
    @Column(name = "module", nullable = false, length = 50)
    private String module;
    
    /**
     * 操作描述
     */
    @Column(name = "operation_desc", nullable = false, length = 500)
    private String operationDesc;
    
    /**
     * 业务对象类型（如User、Role、Project等）
     */
    @Column(name = "business_type", length = 50)
    private String businessType;
    
    /**
     * 业务对象ID
     */
    @Column(name = "business_id")
    private String businessId;
    
    /**
     * 业务数据（JSON格式，存储操作前后的数据变化）
     */
    @Lob
    @Column(name = "business_data")
    private String businessData;
    
    /**
     * 操作状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_status", nullable = false, length = 20)
    private OperationStatus operationStatus;
    
    /**
     * 操作结果消息
     */
    @Column(name = "result_message", length = 1000)
    private String resultMessage;
    
    /**
     * 操作时间
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * 客户端IP地址
     */
    @Column(name = "ip_address", length = 45) // 支持IPv6
    private String ipAddress;
    
    /**
     * 用户代理信息
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    /**
     * 请求URI
     */
    @Column(name = "request_uri", length = 500)
    private String requestUri;
    
    /**
     * HTTP方法
     */
    @Column(name = "http_method", length = 10)
    private String httpMethod;
    
    /**
     * 请求参数（敏感信息需要脱敏）
     */
    @Lob
    @Column(name = "request_params")
    private String requestParams;
    
    /**
     * 操作耗时（毫秒）
     */
    @Column(name = "duration")
    private Long duration;
    
    /**
     * 异常信息（如果操作失败）
     */
    @Lob
    @Column(name = "exception_info")
    private String exceptionInfo;
    
    /**
     * 风险等级
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 20)
    private RiskLevel riskLevel = RiskLevel.LOW;
    
    /**
     * 会话ID
     */
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    /**
     * 数字签名（防篡改）
     */
    @Column(name = "signature", length = 500)
    private String signature;
    
    /**
     * 操作类型枚举
     */
    public enum OperationType {
        // 认证相关
        LOGIN("登录"),
        LOGOUT("登出"),
        LOGIN_FAILED("登录失败"),
        REGISTER("用户注册"),
        
        // 用户管理
        USER_CREATE("创建用户"),
        USER_UPDATE("更新用户"),
        USER_DELETE("删除用户"),
        USER_ENABLE("启用用户"),
        USER_DISABLE("禁用用户"),
        USER_LOCK("锁定用户"),
        USER_UNLOCK("解锁用户"),
        PASSWORD_CHANGE("修改密码"),
        PASSWORD_RESET("重置密码"),
        
        // 角色权限管理
        ROLE_CREATE("创建角色"),
        ROLE_UPDATE("更新角色"),
        ROLE_DELETE("删除角色"),
        ROLE_ASSIGN("分配角色"),
        ROLE_REVOKE("撤销角色"),
        PERMISSION_CREATE("创建权限"),
        PERMISSION_UPDATE("更新权限"),
        PERMISSION_DELETE("删除权限"),
        PERMISSION_ASSIGN("分配权限"),
        
        // NESMA业务操作
        PROJECT_CREATE("创建项目"),
        PROJECT_UPDATE("更新项目"),
        PROJECT_DELETE("删除项目"),
        NESMA_CALCULATE("NESMA计算"),
        REPORT_GENERATE("生成报告"),
        REPORT_EXPORT("导出报告"),
        
        // 系统管理
        SYSTEM_CONFIG("系统配置"),
        DATA_BACKUP("数据备份"),
        DATA_RESTORE("数据恢复"),
        SYSTEM_MONITOR("系统监控"),
        
        // 数据操作
        DATA_EXPORT("数据导出"),
        DATA_IMPORT("数据导入"),
        DATA_DELETE("数据删除"),
        
        // 其他
        OTHER("其他操作");
        
        private final String description;
        
        OperationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 操作状态枚举
     */
    public enum OperationStatus {
        SUCCESS("成功"),
        FAILED("失败"),
        PARTIAL("部分成功");
        
        private final String description;
        
        OperationStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        LOW("低风险"),
        MEDIUM("中等风险"),
        HIGH("高风险"),
        CRITICAL("严重风险");
        
        private final String description;
        
        RiskLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 构造函数
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }
    
    public AuditLog(OperationType operation, String module, String operationDesc) {
        this();
        this.operation = operation;
        this.module = module;
        this.operationDesc = operationDesc;
        this.operationStatus = OperationStatus.SUCCESS;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    public OperationType getOperation() {
        return operation;
    }
    
    public void setOperation(OperationType operation) {
        this.operation = operation;
    }
    
    public String getModule() {
        return module;
    }
    
    public void setModule(String module) {
        this.module = module;
    }
    
    public String getOperationDesc() {
        return operationDesc;
    }
    
    public void setOperationDesc(String operationDesc) {
        this.operationDesc = operationDesc;
    }
    
    public String getBusinessType() {
        return businessType;
    }
    
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }
    
    public String getBusinessId() {
        return businessId;
    }
    
    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }
    
    public String getBusinessData() {
        return businessData;
    }
    
    public void setBusinessData(String businessData) {
        this.businessData = businessData;
    }
    
    public OperationStatus getOperationStatus() {
        return operationStatus;
    }
    
    public void setOperationStatus(OperationStatus operationStatus) {
        this.operationStatus = operationStatus;
    }
    
    public String getResultMessage() {
        return resultMessage;
    }
    
    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getRequestUri() {
        return requestUri;
    }
    
    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }
    
    public String getHttpMethod() {
        return httpMethod;
    }
    
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    
    public String getRequestParams() {
        return requestParams;
    }
    
    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }
    
    public Long getDuration() {
        return duration;
    }
    
    public void setDuration(Long duration) {
        this.duration = duration;
    }
    
    public String getExceptionInfo() {
        return exceptionInfo;
    }
    
    public void setExceptionInfo(String exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }
    
    public RiskLevel getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(id, auditLog.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", operation=" + operation +
                ", module='" + module + '\'' +
                ", operationDesc='" + operationDesc + '\'' +
                ", operationStatus=" + operationStatus +
                ", timestamp=" + timestamp +
                ", ipAddress='" + ipAddress + '\'' +
                ", riskLevel=" + riskLevel +
                '}';
    }
}