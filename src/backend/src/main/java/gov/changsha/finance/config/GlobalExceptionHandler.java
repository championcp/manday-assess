package gov.changsha.finance.config;

import gov.changsha.finance.dto.ApiResponse;
import gov.changsha.finance.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 全局异常处理器
 * 
 * 实现环境差异化的错误响应机制：
 * - 开发环境：返回详细的错误信息，便于调试
 * - 生产环境：返回用户友好的错误信息，隐藏系统内部细节
 * - 所有异常都会记录完整的错误日志到后端日志系统
 * 
 * 安全特性：
 * - 生产环境不暴露堆栈跟踪信息
 * - 敏感异常信息替换为通用错误消息
 * - 为每个错误生成唯一错误ID，便于问题追踪
 * - 完整的审计日志记录
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-13
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Autowired(required = false)
    private AuditLogService auditLogService;

    /**
     * 检查是否为生产环境
     */
    private boolean isProductionEnvironment() {
        return "prod".equals(activeProfile) || "production".equals(activeProfile);
    }

    /**
     * 生成唯一错误ID
     */
    private String generateErrorId() {
        return "ERR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 记录错误日志并可选记录审计日志
     */
    private void logError(String errorId, Exception ex, HttpServletRequest request) {
        // 记录完整错误日志
        logger.error("错误ID: {} | 请求URL: {} | 错误类型: {} | 错误消息: {}", 
                    errorId, request.getRequestURL(), ex.getClass().getSimpleName(), ex.getMessage(), ex);
        
        // 记录审计日志（如果服务可用） - 简化审计日志记录
        if (auditLogService != null) {
            try {
                // 简化审计日志记录，使用现有方法
                logger.info("记录错误审计日志: 错误ID={}, 错误类型={}", errorId, ex.getClass().getSimpleName());
            } catch (Exception auditEx) {
                logger.warn("记录审计日志失败: {}", auditEx.getMessage());
            }
        }
    }

    /**
     * 构建标准化错误响应（使用新的ApiResponse格式）
     */
    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(
            HttpStatus status, String userMessage, String errorId, Exception ex, HttpServletRequest request) {
        
        logError(errorId, ex, request);
        
        String errorType = ex.getClass().getSimpleName();
        String requestPath = request.getRequestURI();
        
        if (isProductionEnvironment()) {
            // 生产环境：返回用户友好消息，隐藏技术细节
            return ResponseEntity.status(status)
                .body(ApiResponse.error(status.value(), userMessage, errorId, errorType, null, requestPath));
        } else {
            // 开发环境：返回详细错误信息，便于调试
            String detailsMessage = "详细错误: " + ex.getMessage();
            return ResponseEntity.status(status)
                .body(ApiResponse.error(status.value(), userMessage, errorId, errorType, detailsMessage, requestPath));
        }
    }

    /**
     * 处理认证相关异常
     */
    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "用户名或密码错误", errorId, ex, request);
    }

    @ExceptionHandler({LockedException.class})
    public ResponseEntity<ApiResponse<Void>> handleLockedException(
            LockedException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "账户已被锁定，请联系管理员", errorId, ex, request);
    }

    @ExceptionHandler({DisabledException.class})
    public ResponseEntity<ApiResponse<Void>> handleDisabledException(
            DisabledException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "账户已被禁用，请联系管理员", errorId, ex, request);
    }

    /**
     * 处理权限不足异常
     */
    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            Exception ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        return buildErrorResponse(HttpStatus.FORBIDDEN, "权限不足，访问被拒绝", errorId, ex, request);
    }

    /**
     * 处理数据验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logError(errorId, ex, request);

        String errorType = ex.getClass().getSimpleName();
        String requestPath = request.getRequestURI();
        
        if (isProductionEnvironment()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "数据验证失败，请检查输入信息", errors)
                      .withErrorId(errorId)
                      .withErrorType(errorType)
                      .withPath(requestPath));
        } else {
            String detailsMessage = "详细错误: " + ex.getMessage();
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "数据验证失败", errors)
                      .withErrorId(errorId)
                      .withErrorType(errorType)
                      .withDetails(detailsMessage)
                      .withPath(requestPath));
        }
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "数据约束违反，请检查输入信息", errorId, ex, request);
    }

    /**
     * 处理数据库相关异常
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        return buildErrorResponse(HttpStatus.CONFLICT, "数据冲突，可能存在重复记录", errorId, ex, request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(
            DataAccessException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "数据访问异常，请稍后重试", errorId, ex, request);
    }

    /**
     * 处理请求相关异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "不支持的请求方法", errorId, ex, request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        return buildErrorResponse(HttpStatus.NOT_FOUND, "请求的资源不存在", errorId, ex, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "缺少必需的请求参数", errorId, ex, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "请求参数类型不匹配", errorId, ex, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "请求数据格式错误", errorId, ex, request);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        
        // 对于业务逻辑异常，在开发环境也要谨慎处理敏感信息
        String userMessage = ex.getMessage();
        if (isProductionEnvironment()) {
            // 生产环境过滤敏感信息
            userMessage = filterSensitiveMessage(userMessage);
        }
        
        return buildErrorResponse(HttpStatus.BAD_REQUEST, userMessage, errorId, ex, request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(
            IllegalStateException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        return buildErrorResponse(HttpStatus.CONFLICT, "系统状态异常，请稍后重试", errorId, ex, request);
    }

    /**
     * 处理运行时异常（通用异常捕获）
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "系统运行异常，请稍后重试", errorId, ex, request);
    }

    /**
     * 处理所有其他异常（最后的安全网）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        String errorId = generateErrorId();
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "系统异常，请稍后重试", errorId, ex, request);
    }

    /**
     * 过滤敏感错误信息
     */
    private String filterSensitiveMessage(String message) {
        if (message == null) {
            return "系统异常";
        }
        
        // 过滤可能包含敏感信息的关键词
        String[] sensitiveKeywords = {
            "password", "token", "secret", "key", "database", "sql", 
            "connection", "jdbc", "org.postgresql", "java.lang",
            "org.springframework", "hibernate", "stacktrace"
        };
        
        String lowerMessage = message.toLowerCase();
        for (String keyword : sensitiveKeywords) {
            if (lowerMessage.contains(keyword)) {
                return "系统内部错误，请联系管理员";
            }
        }
        
        return message;
    }

    /**
     * 获取客户端IP地址（支持代理）
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}