package gov.changsha.finance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一API响应结果封装（标准化增强版本）
 * 
 * 符合政府级API规范要求，提供完整的错误信息和可追溯性
 * 
 * @param <T> 数据类型
 * @author 开发团队
 * @version 1.1.0
 * @since 2025-09-03
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 响应状态码
     */
    private int code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳（毫秒）
     */
    private long timestamp;
    
    /**
     * 格式化的时间字符串（便于阅读）
     */
    private String datetime;

    /**
     * 是否成功标识
     */
    private boolean success;
    
    /**
     * 错误ID（用于问题追踪，仅错误时返回）
     */
    private String errorId;
    
    /**
     * 错误类型（仅错误时返回）
     */
    private String errorType;
    
    /**
     * 详细错误信息（仅开发环境返回）
     */
    private String details;
    
    /**
     * 请求路径（便于调试）
     */
    private String path;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
        this.datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = code >= 200 && code < 300;
        this.timestamp = System.currentTimeMillis();
        this.datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 创建成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }

    /**
     * 创建成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "操作成功", null);
    }

    /**
     * 创建成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 创建失败响应（带数据）
     */
    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    /**
     * 创建失败响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 创建失败响应（默认500错误）
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }
    
    /**
     * 创建标准化错误响应（包含错误ID等扩展信息）
     */
    public static <T> ApiResponse<T> error(int code, String message, String errorId, String errorType) {
        ApiResponse<T> response = new ApiResponse<>(code, message, null);
        response.setErrorId(errorId);
        response.setErrorType(errorType);
        return response;
    }
    
    /**
     * 创建标准化错误响应（包含完整错误信息）
     */
    public static <T> ApiResponse<T> error(int code, String message, String errorId, String errorType, String details, String path) {
        ApiResponse<T> response = new ApiResponse<>(code, message, null);
        response.setErrorId(errorId);
        response.setErrorType(errorType);
        response.setDetails(details);
        response.setPath(path);
        return response;
    }

    // Builder 模式支持
    public ApiResponse<T> withErrorId(String errorId) {
        this.errorId = errorId;
        return this;
    }
    
    public ApiResponse<T> withErrorType(String errorType) {
        this.errorType = errorType;
        return this;
    }
    
    public ApiResponse<T> withDetails(String details) {
        this.details = details;
        return this;
    }
    
    public ApiResponse<T> withPath(String path) {
        this.path = path;
        return this;
    }

    // Getters and Setters
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
        this.success = code >= 200 && code < 300;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        this.datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public String getDatetime() {
        return datetime;
    }
    
    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", success=" + success +
                ", timestamp=" + timestamp +
                ", datetime='" + datetime + '\'' +
                ", errorId='" + errorId + '\'' +
                ", errorType='" + errorType + '\'' +
                '}';
    }
}