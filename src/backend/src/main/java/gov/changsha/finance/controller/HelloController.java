package gov.changsha.finance.controller;

import gov.changsha.finance.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello World控制器
 * 用于验证系统基础功能
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-03
 */
@RestController
@RequestMapping("/api/public")
public class HelloController {

    /**
     * Hello World接口
     */
    @GetMapping("/hello")
    public ApiResponse<String> hello() {
        return ApiResponse.success("长沙市财政评审中心软件规模评估系统启动成功！");
    }

    /**
     * 系统状态检查
     */
    @GetMapping("/status")
    public ApiResponse<String> status() {
        return ApiResponse.success("系统运行状态", "正常");
    }
}