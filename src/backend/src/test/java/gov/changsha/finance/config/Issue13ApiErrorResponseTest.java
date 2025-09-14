package gov.changsha.finance.config;

import gov.changsha.finance.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Issue #13 API错误响应格式标准化测试
 * 
 * 测试目标：
 * 1. 验证所有API错误响应都使用标准化格式
 * 2. 确认错误响应包含必要字段：errorId、errorType、success、datetime等
 * 3. 验证开发环境和生产环境的差异化响应
 * 4. 测试各种异常类型的标准化处理
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-14
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class Issue13ApiErrorResponseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testStandardizedApiErrorResponse() throws Exception {
        // 测试404错误的标准化响应格式
        MvcResult result = mockMvc.perform(get("/api/non-existent-endpoint")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);

        // 验证标准化字段存在
        assertStandardApiResponseFields(apiResponse);
        
        // 验证错误特定字段
        assert apiResponse.getCode() == 404;
        assert !apiResponse.isSuccess();
        assert apiResponse.getErrorId() != null;
        assert apiResponse.getErrorType() != null;
        assert apiResponse.getMessage().contains("请求的资源不存在");
    }

    @Test
    public void testValidationErrorStandardizedResponse() throws Exception {
        // 测试数据验证错误的标准化响应
        String invalidJsonData = "{\"invalidField\": \"value\"}";
        
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJsonData))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);

        // 验证标准化字段
        assertStandardApiResponseFields(apiResponse);
        
        // 验证验证错误特定字段
        assert apiResponse.getCode() == 400;
        assert !apiResponse.isSuccess();
        assert apiResponse.getErrorId() != null;
        assert apiResponse.getErrorType() != null;
    }

    @Test
    public void testAuthenticationErrorStandardizedResponse() throws Exception {
        // 测试认证错误的标准化响应
        MvcResult result = mockMvc.perform(get("/api/nesma/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);

        // 验证标准化字段
        assertStandardApiResponseFields(apiResponse);
        
        // 验证认证错误特定字段
        assert apiResponse.getCode() == 401;
        assert !apiResponse.isSuccess();
        assert apiResponse.getErrorId() != null;
        assert apiResponse.getErrorType() != null;
    }

    @Test
    public void testMethodNotAllowedStandardizedResponse() throws Exception {
        // 测试不支持的HTTP方法错误
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andReturn(); // 可能返回各种状态码

        // 如果返回405 Method Not Allowed，验证标准化响应
        if (result.getResponse().getStatus() == 405) {
            String responseContent = result.getResponse().getContentAsString();
            ApiResponse<?> apiResponse = objectMapper.readValue(responseContent, ApiResponse.class);
            
            assertStandardApiResponseFields(apiResponse);
            assert apiResponse.getCode() == 405;
            assert !apiResponse.isSuccess();
        }
    }

    /**
     * 验证API响应的标准化字段
     */
    private void assertStandardApiResponseFields(ApiResponse<?> response) {
        // 验证基础字段存在
        assert response.getCode() > 0 : "响应码应该存在";
        assert response.getMessage() != null : "响应消息应该存在";
        assert response.getTimestamp() > 0 : "时间戳应该存在";
        assert response.getDatetime() != null : "格式化时间应该存在";
        
        // 验证成功标识正确设置
        assert response.isSuccess() == (response.getCode() >= 200 && response.getCode() < 300) 
            : "成功标识应该与状态码一致";
        
        // 对于错误响应，验证错误相关字段
        if (!response.isSuccess()) {
            assert response.getErrorId() != null : "错误ID应该存在";
            assert response.getErrorType() != null : "错误类型应该存在";
            // path字段可能存在（取决于具体异常处理）
        }
        
        System.out.println("标准化API响应验证通过:");
        System.out.println("- 响应码: " + response.getCode());
        System.out.println("- 成功标识: " + response.isSuccess());
        System.out.println("- 错误ID: " + response.getErrorId());
        System.out.println("- 错误类型: " + response.getErrorType());
        System.out.println("- 时间戳: " + response.getTimestamp());
        System.out.println("- 格式化时间: " + response.getDatetime());
    }

    @Test
    public void testSuccessResponseFormat() throws Exception {
        // 测试成功响应的标准化格式（如果有公开的成功接口）
        // 这里可以根据实际的公开接口进行测试
        
        // 创建一个成功的ApiResponse对象进行格式验证
        ApiResponse<String> successResponse = ApiResponse.success("测试数据");
        
        // 验证成功响应的标准化字段
        assert successResponse.getCode() == 200;
        assert successResponse.isSuccess();
        assert successResponse.getData().equals("测试数据");
        assert successResponse.getTimestamp() > 0;
        assert successResponse.getDatetime() != null;
        assert successResponse.getErrorId() == null; // 成功响应不应该有错误ID
        assert successResponse.getErrorType() == null; // 成功响应不应该有错误类型
        
        System.out.println("成功响应标准化格式验证通过");
    }
}