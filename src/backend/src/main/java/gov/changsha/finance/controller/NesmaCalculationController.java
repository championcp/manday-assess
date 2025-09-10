package gov.changsha.finance.controller;

import gov.changsha.finance.dto.ApiResponse;
import gov.changsha.finance.entity.CalculationResult;
import gov.changsha.finance.entity.Project;
import gov.changsha.finance.repository.ProjectRepository;
import gov.changsha.finance.service.NesmaCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * NESMA计算控制器
 * 提供NESMA功能点评估和计算服务
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-09
 */
@RestController
@RequestMapping("/api/nesma")
public class NesmaCalculationController {

    private static final Logger logger = LoggerFactory.getLogger(NesmaCalculationController.class);

    @Autowired
    private NesmaCalculationService nesmaCalculationService;
    
    @Autowired
    private ProjectRepository projectRepository;

    /**
     * 执行NESMA功能点计算
     */
    @PostMapping("/calculate/{projectId}")
    public ApiResponse<Map<String, Object>> calculateNesmaFunctionPoints(@PathVariable Long projectId) {
        try {
            logger.info("开始执行NESMA计算，项目ID: {}", projectId);
            
            // 验证项目是否存在
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                return ApiResponse.error("项目不存在，ID: " + projectId);
            }
            
            // 执行NESMA计算
            long startTime = System.currentTimeMillis();
            CalculationResult result = nesmaCalculationService.calculateNesmaFunctionPoints(projectId);
            long duration = System.currentTimeMillis() - startTime;
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("projectId", projectId);
            responseData.put("projectName", project.getProjectName());
            responseData.put("calculationId", result.getId());
            responseData.put("totalFunctionPoints", result.getTotalFunctionPoints());
            responseData.put("adjustedFunctionPoints", result.getAdjustedFunctionPoints());
            responseData.put("estimatedPersonMonths", result.getEstimatedPersonMonths());
            responseData.put("estimatedCost", result.getEstimatedCost());
            responseData.put("calculationDuration", duration);
            responseData.put("calculationStatus", result.getCalculationStatus());
            responseData.put("calculatedAt", result.getCreatedAt());
            responseData.put("remarks", result.getRemarks());
            
            logger.info("NESMA计算完成，项目ID: {}, 耗时: {}ms", projectId, duration);
            
            return ApiResponse.success("NESMA计算完成", responseData);
            
        } catch (Exception e) {
            logger.error("NESMA计算失败，项目ID: {}, 错误: {}", projectId, e.getMessage(), e);
            return ApiResponse.error("NESMA计算失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目的计算历史
     */
    @GetMapping("/history/{projectId}")
    public ApiResponse<Object> getCalculationHistory(@PathVariable Long projectId) {
        try {
            // 验证项目是否存在
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project == null) {
                return ApiResponse.error("项目不存在，ID: " + projectId);
            }
            
            // 获取计算历史（这里简化为获取最新的计算结果）
            // 实际实现中可以从CalculationResult表中查询历史记录
            Map<String, Object> historyData = new HashMap<>();
            historyData.put("projectId", projectId);
            historyData.put("message", "计算历史功能待完善");
            
            return ApiResponse.success("获取计算历史成功", historyData);
            
        } catch (Exception e) {
            logger.error("获取计算历史失败，项目ID: {}, 错误: {}", projectId, e.getMessage(), e);
            return ApiResponse.error("获取计算历史失败: " + e.getMessage());
        }
    }

    /**
     * 批量计算多个项目的NESMA功能点
     */
    @PostMapping("/batch-calculate")
    public ApiResponse<Map<String, Object>> batchCalculateNesmaFunctionPoints(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            java.util.List<Long> projectIds = (java.util.List<Long>) request.get("projectIds");
            
            if (projectIds == null || projectIds.isEmpty()) {
                return ApiResponse.error("项目ID列表不能为空");
            }
            
            logger.info("开始批量NESMA计算，项目数量: {}", projectIds.size());
            
            Map<String, Object> batchResults = new HashMap<>();
            java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
            int successCount = 0;
            int failureCount = 0;
            
            long totalStartTime = System.currentTimeMillis();
            
            for (Long projectId : projectIds) {
                try {
                    // 验证项目是否存在
                    Project project = projectRepository.findById(projectId).orElse(null);
                    if (project == null) {
                        Map<String, Object> failedResult = new HashMap<>();
                        failedResult.put("projectId", projectId);
                        failedResult.put("success", false);
                        failedResult.put("error", "项目不存在");
                        results.add(failedResult);
                        failureCount++;
                        continue;
                    }
                    
                    // 执行计算
                    long startTime = System.currentTimeMillis();
                    CalculationResult result = nesmaCalculationService.calculateNesmaFunctionPoints(projectId);
                    long duration = System.currentTimeMillis() - startTime;
                    
                    // 构建结果
                    Map<String, Object> successResult = new HashMap<>();
                    successResult.put("projectId", projectId);
                    successResult.put("projectName", project.getProjectName());
                    successResult.put("success", true);
                    successResult.put("totalFunctionPoints", result.getTotalFunctionPoints());
                    successResult.put("adjustedFunctionPoints", result.getAdjustedFunctionPoints());
                    successResult.put("estimatedPersonMonths", result.getEstimatedPersonMonths());
                    successResult.put("estimatedCost", result.getEstimatedCost());
                    successResult.put("calculationDuration", duration);
                    
                    results.add(successResult);
                    successCount++;
                    
                    logger.info("项目{}计算完成，耗时: {}ms", projectId, duration);
                    
                } catch (Exception e) {
                    Map<String, Object> failedResult = new HashMap<>();
                    failedResult.put("projectId", projectId);
                    failedResult.put("success", false);
                    failedResult.put("error", e.getMessage());
                    results.add(failedResult);
                    failureCount++;
                    
                    logger.error("项目{}计算失败: {}", projectId, e.getMessage());
                }
            }
            
            long totalDuration = System.currentTimeMillis() - totalStartTime;
            
            // 构建批量结果摘要
            batchResults.put("totalProjects", projectIds.size());
            batchResults.put("successCount", successCount);
            batchResults.put("failureCount", failureCount);
            batchResults.put("totalDuration", totalDuration);
            batchResults.put("results", results);
            
            logger.info("批量NESMA计算完成，成功: {}, 失败: {}, 总耗时: {}ms", 
                    successCount, failureCount, totalDuration);
            
            return ApiResponse.success("批量NESMA计算完成", batchResults);
            
        } catch (Exception e) {
            logger.error("批量NESMA计算失败: {}", e.getMessage(), e);
            return ApiResponse.error("批量NESMA计算失败: " + e.getMessage());
        }
    }

    /**
     * 获取NESMA计算性能统计
     */
    @GetMapping("/performance-stats")
    public ApiResponse<Map<String, Object>> getPerformanceStats() {
        try {
            // 这里可以实现性能统计逻辑
            // 例如：平均计算时间、最大功能点数量、系统负载等
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("message", "性能统计功能待完善");
            stats.put("systemStatus", "运行正常");
            stats.put("timestamp", java.time.LocalDateTime.now());
            
            return ApiResponse.success("获取性能统计成功", stats);
            
        } catch (Exception e) {
            logger.error("获取性能统计失败: {}", e.getMessage(), e);
            return ApiResponse.error("获取性能统计失败: " + e.getMessage());
        }
    }
}