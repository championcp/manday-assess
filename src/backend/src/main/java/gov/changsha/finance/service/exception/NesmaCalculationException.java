package gov.changsha.finance.service.exception;

/**
 * NESMA计算专用异常类
 * 
 * 用于封装NESMA功能点计算过程中的各种异常情况，
 * 提供更精确的错误信息和异常分类，便于问题定位和处理
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-03
 */
public class NesmaCalculationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    /**
     * 异常类型枚举
     */
    public enum ErrorType {
        /** 项目数据不存在或无效 */
        PROJECT_NOT_FOUND("PROJECT_NOT_FOUND", "项目数据错误"),
        
        /** 功能点数据不完整或无效 */
        INVALID_FUNCTION_POINT("INVALID_FUNCTION_POINT", "功能点数据错误"),
        
        /** 复杂度判定失败 */
        COMPLEXITY_DETERMINATION_FAILED("COMPLEXITY_DETERMINATION_FAILED", "复杂度判定失败"),
        
        /** 权重配置错误 */
        WEIGHT_CONFIGURATION_ERROR("WEIGHT_CONFIGURATION_ERROR", "权重配置错误"),
        
        /** VAF计算异常 */
        VAF_CALCULATION_ERROR("VAF_CALCULATION_ERROR", "VAF计算异常"),
        
        /** 数值计算精度异常 */
        PRECISION_CALCULATION_ERROR("PRECISION_CALCULATION_ERROR", "数值计算异常"),
        
        /** 性能超时异常 */
        CALCULATION_TIMEOUT("CALCULATION_TIMEOUT", "计算超时"),
        
        /** 系统资源不足 */
        INSUFFICIENT_RESOURCES("INSUFFICIENT_RESOURCES", "系统资源不足"),
        
        /** 数据验证失败 */
        DATA_VALIDATION_ERROR("DATA_VALIDATION_ERROR", "数据验证失败"),
        
        /** 未知系统错误 */
        SYSTEM_ERROR("SYSTEM_ERROR", "系统错误");
        
        private final String code;
        private final String description;
        
        ErrorType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /** 错误类型 */
    private final ErrorType errorType;
    
    /** 错误代码 */
    private final String errorCode;
    
    /** 项目ID */
    private final Long projectId;
    
    /** 功能点ID */
    private final Long functionPointId;
    
    /** 详细错误信息 */
    private final String details;
    
    /**
     * 构造函数 - 基本异常
     * 
     * @param errorType 错误类型
     * @param message 错误消息
     */
    public NesmaCalculationException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
        this.errorCode = errorType.getCode();
        this.projectId = null;
        this.functionPointId = null;
        this.details = message;
    }
    
    /**
     * 构造函数 - 带项目ID的异常
     * 
     * @param errorType 错误类型
     * @param message 错误消息
     * @param projectId 项目ID
     */
    public NesmaCalculationException(ErrorType errorType, String message, Long projectId) {
        super(message);
        this.errorType = errorType;
        this.errorCode = errorType.getCode();
        this.projectId = projectId;
        this.functionPointId = null;
        this.details = message;
    }
    
    /**
     * 构造函数 - 带项目ID和功能点ID的异常
     * 
     * @param errorType 错误类型
     * @param message 错误消息
     * @param projectId 项目ID
     * @param functionPointId 功能点ID
     */
    public NesmaCalculationException(ErrorType errorType, String message, 
                                   Long projectId, Long functionPointId) {
        super(message);
        this.errorType = errorType;
        this.errorCode = errorType.getCode();
        this.projectId = projectId;
        this.functionPointId = functionPointId;
        this.details = message;
    }
    
    /**
     * 构造函数 - 带详细信息的异常
     * 
     * @param errorType 错误类型
     * @param message 错误消息
     * @param projectId 项目ID
     * @param functionPointId 功能点ID
     * @param details 详细错误信息
     */
    public NesmaCalculationException(ErrorType errorType, String message, 
                                   Long projectId, Long functionPointId, String details) {
        super(message);
        this.errorType = errorType;
        this.errorCode = errorType.getCode();
        this.projectId = projectId;
        this.functionPointId = functionPointId;
        this.details = details;
    }
    
    /**
     * 构造函数 - 带原因异常
     * 
     * @param errorType 错误类型
     * @param message 错误消息
     * @param cause 原因异常
     */
    public NesmaCalculationException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.errorCode = errorType.getCode();
        this.projectId = null;
        this.functionPointId = null;
        this.details = message;
    }
    
    /**
     * 构造函数 - 完整参数异常
     * 
     * @param errorType 错误类型
     * @param message 错误消息
     * @param projectId 项目ID
     * @param functionPointId 功能点ID
     * @param details 详细错误信息
     * @param cause 原因异常
     */
    public NesmaCalculationException(ErrorType errorType, String message, 
                                   Long projectId, Long functionPointId, 
                                   String details, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.errorCode = errorType.getCode();
        this.projectId = projectId;
        this.functionPointId = functionPointId;
        this.details = details;
    }
    
    // Getter方法
    
    public ErrorType getErrorType() {
        return errorType;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Long getProjectId() {
        return projectId;
    }
    
    public Long getFunctionPointId() {
        return functionPointId;
    }
    
    public String getDetails() {
        return details;
    }
    
    /**
     * 获取完整的错误描述
     */
    public String getFullErrorDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(errorCode).append("] ");
        sb.append(errorType.getDescription()).append(": ");
        sb.append(getMessage());
        
        if (projectId != null) {
            sb.append(" (项目ID: ").append(projectId).append(")");
        }
        
        if (functionPointId != null) {
            sb.append(" (功能点ID: ").append(functionPointId).append(")");
        }
        
        if (details != null && !details.equals(getMessage())) {
            sb.append(" - 详细信息: ").append(details);
        }
        
        return sb.toString();
    }
    
    /**
     * 静态工厂方法：创建项目不存在异常
     */
    public static NesmaCalculationException projectNotFound(Long projectId) {
        return new NesmaCalculationException(
            ErrorType.PROJECT_NOT_FOUND,
            "项目不存在或已删除",
            projectId
        );
    }
    
    /**
     * 静态工厂方法：创建功能点数据无效异常
     */
    public static NesmaCalculationException invalidFunctionPoint(Long projectId, Long functionPointId, String reason) {
        return new NesmaCalculationException(
            ErrorType.INVALID_FUNCTION_POINT,
            "功能点数据无效: " + reason,
            projectId,
            functionPointId
        );
    }
    
    /**
     * 静态工厂方法：创建复杂度判定失败异常
     */
    public static NesmaCalculationException complexityDeterminationFailed(Long projectId, Long functionPointId, 
                                                                        String functionPointType, String reason) {
        return new NesmaCalculationException(
            ErrorType.COMPLEXITY_DETERMINATION_FAILED,
            String.format("功能点类型 %s 的复杂度判定失败: %s", functionPointType, reason),
            projectId,
            functionPointId
        );
    }
    
    /**
     * 静态工厂方法：创建权重配置错误异常
     */
    public static NesmaCalculationException weightConfigurationError(String functionPointType, 
                                                                   String complexityLevel, String reason) {
        return new NesmaCalculationException(
            ErrorType.WEIGHT_CONFIGURATION_ERROR,
            String.format("功能点类型 %s，复杂度 %s 的权重配置错误: %s", 
                         functionPointType, complexityLevel, reason)
        );
    }
    
    /**
     * 静态工厂方法：创建VAF计算异常
     */
    public static NesmaCalculationException vafCalculationError(Long projectId, String reason) {
        return new NesmaCalculationException(
            ErrorType.VAF_CALCULATION_ERROR,
            "VAF值计算失败: " + reason,
            projectId
        );
    }
    
    /**
     * 静态工厂方法：创建计算超时异常
     */
    public static NesmaCalculationException calculationTimeout(Long projectId, long timeoutMs) {
        return new NesmaCalculationException(
            ErrorType.CALCULATION_TIMEOUT,
            String.format("计算超时，超过 %d 毫秒限制", timeoutMs),
            projectId
        );
    }
    
    /**
     * 静态工厂方法：创建数据验证失败异常
     */
    public static NesmaCalculationException dataValidationError(String fieldName, Object value, String reason) {
        return new NesmaCalculationException(
            ErrorType.DATA_VALIDATION_ERROR,
            String.format("字段 %s 的值 %s 验证失败: %s", fieldName, value, reason)
        );
    }
    
    /**
     * 静态工厂方法：创建精度计算异常
     */
    public static NesmaCalculationException precisionCalculationError(String operation, String reason) {
        return new NesmaCalculationException(
            ErrorType.PRECISION_CALCULATION_ERROR,
            String.format("数值计算异常，操作: %s，原因: %s", operation, reason)
        );
    }
    
    /**
     * 静态工厂方法：创建系统资源不足异常
     */
    public static NesmaCalculationException insufficientResources(String resource, String details) {
        return new NesmaCalculationException(
            ErrorType.INSUFFICIENT_RESOURCES,
            String.format("系统资源不足: %s - %s", resource, details)
        );
    }
    
    @Override
    public String toString() {
        return "NesmaCalculationException{" +
                "errorType=" + errorType +
                ", errorCode='" + errorCode + '\'' +
                ", projectId=" + projectId +
                ", functionPointId=" + functionPointId +
                ", message='" + getMessage() + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}