package gov.changsha.finance.security;

import gov.changsha.finance.config.GlobalExceptionHandler;
import gov.changsha.finance.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 全局异常处理器安全性测试
 * 验证错误信息泄露修复效果
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-13
 */
@SpringBootTest
@ActiveProfiles("dev") // 测试开发环境行为
class GlobalExceptionHandlerSecurityTest {

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    /**
     * 测试开发环境下的错误信息处理
     */
    @Test
    void testDevEnvironmentErrorHandling() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        
        // 创建一个包含敏感信息的异常
        RuntimeException sensitiveException = new RuntimeException(
            "Database connection failed: jdbc:postgresql://localhost:5432/secret_db with username=admin");
        
        // 测试异常处理
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleRuntimeException(
            sensitiveException, request);
        
        // 验证响应
        assertNotNull(response);
        assertEquals(500, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        
        // 开发环境应该包含详细信息但有错误ID
        String message = response.getBody().getMessage();
        assertTrue(message.contains("错误ID:"));
        assertTrue(message.contains("ERR-"));
    }

    /**
     * 测试IllegalArgumentException的处理
     */
    @Test
    void testIllegalArgumentExceptionHandling() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/validation");
        
        // 创建业务逻辑异常
        IllegalArgumentException businessException = new IllegalArgumentException("用户名已存在");
        
        // 测试异常处理
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleIllegalArgumentException(
            businessException, request);
        
        // 验证响应
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        
        // 验证错误消息包含错误ID
        String message = response.getBody().getMessage();
        assertTrue(message.contains("错误ID:"));
        assertTrue(message.contains("用户名已存在"));
    }

    /**
     * 测试数据验证异常处理
     */
    @Test
    void testConstraintViolationExceptionHandling() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/create");
        
        // 创建约束违反异常
        RuntimeException constraintException = new RuntimeException("约束违反");
        
        // 测试异常处理
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleRuntimeException(
            constraintException, request);
        
        // 验证响应
        assertNotNull(response);
        assertEquals(500, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        
        // 验证包含错误ID且不泄露系统信息
        String message = response.getBody().getMessage();
        assertTrue(message.contains("错误ID:"));
        assertFalse(message.toLowerCase().contains("stacktrace"));
        assertFalse(message.toLowerCase().contains("database"));
    }
}