package gov.changsha.finance.controller;

import gov.changsha.finance.dto.ApiResponse;
import gov.changsha.finance.entity.SimpleFunctionPoint;
import gov.changsha.finance.repository.SimpleFunctionPointRepository;
import gov.changsha.finance.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简化NESMA计算控制器
 * 提供基础的NESMA功能点评估和计算服务
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-09
 */
@RestController
@RequestMapping("/api/simple-nesma")
public class SimpleNesmaCalculationController {

    private static final Logger logger = LoggerFactory.getLogger(SimpleNesmaCalculationController.class);
    private static final int DECIMAL_SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Autowired
    private SimpleFunctionPointRepository functionPointRepository;
    
    @Autowired
    private ProjectRepository projectRepository;

    /**
     * 执行简化NESMA功能点计算
     */
    @PostMapping("/calculate/{projectId}")
    public ApiResponse<Map<String, Object>> calculateNesmaFunctionPoints(@PathVariable Long projectId) {
        try {
            logger.info("开始执行简化NESMA计算，项目ID: {}", projectId);
            
            // 验证项目是否存在
            if (!projectRepository.existsById(projectId)) {
                return ApiResponse.error("项目不存在，ID: " + projectId);
            }
            
            // 获取项目功能点
            List<SimpleFunctionPoint> functionPoints = functionPointRepository.findByProjectIdAndDeletedAtIsNull(projectId);
            if (functionPoints.isEmpty()) {
                return ApiResponse.error("项目功能点数据为空，无法执行计算");
            }
            
            long startTime = System.currentTimeMillis();
            
            // 执行计算
            Map<String, Object> calculationResult = performSimpleNesmaCalculation(projectId, functionPoints);
            
            long duration = System.currentTimeMillis() - startTime;
            calculationResult.put("calculationDuration", duration);
            
            logger.info("简化NESMA计算完成，项目ID: {}, 耗时: {}ms", projectId, duration);
            
            return ApiResponse.success("NESMA计算完成", calculationResult);
            
        } catch (Exception e) {
            logger.error("简化NESMA计算失败，项目ID: {}, 错误: {}", projectId, e.getMessage(), e);
            return ApiResponse.error("NESMA计算失败: " + e.getMessage());
        }
    }

    /**
     * 执行简化的NESMA计算
     */
    private Map<String, Object> performSimpleNesmaCalculation(Long projectId, List<SimpleFunctionPoint> functionPoints) {
        
        // 1. 计算每个功能点的复杂度和权重
        BigDecimal totalFunctionPoints = BigDecimal.ZERO;
        
        for (SimpleFunctionPoint fp : functionPoints) {
            // 确定复杂度等级
            String complexity = determineComplexity(fp);
            fp.setComplexityLevel(complexity);
            
            // 获取权重
            BigDecimal weight = getWeight(fp.getFpType(), complexity);
            fp.setComplexityWeight(weight);
            fp.setCalculatedFpValue(weight);
            
            totalFunctionPoints = totalFunctionPoints.add(weight);
            
            // 更新数据库中的功能点
            functionPointRepository.save(fp);
        }
        
        // 2. 应用VAF调整（简化为固定调整因子）
        BigDecimal vafFactor = BigDecimal.valueOf(1.0); // 简化：不调整
        BigDecimal adjustedFunctionPoints = totalFunctionPoints.multiply(vafFactor);
        
        // 3. 计算人月和成本
        BigDecimal personMonths = calculatePersonMonths(adjustedFunctionPoints);
        BigDecimal cost = calculateCost(personMonths);
        
        // 4. 构建结果
        Map<String, Object> result = new HashMap<>();
        result.put("projectId", projectId);
        result.put("functionPointCount", functionPoints.size());
        result.put("totalFunctionPoints", totalFunctionPoints.setScale(DECIMAL_SCALE, ROUNDING_MODE));
        result.put("adjustedFunctionPoints", adjustedFunctionPoints.setScale(DECIMAL_SCALE, ROUNDING_MODE));
        result.put("estimatedPersonMonths", personMonths.setScale(DECIMAL_SCALE, ROUNDING_MODE));
        result.put("estimatedCost", cost.setScale(2, ROUNDING_MODE));
        result.put("calculatedAt", LocalDateTime.now());
        result.put("calculationStatus", "COMPLETED");
        
        // 5. 添加详细统计
        Map<String, Integer> typeStats = new HashMap<>();
        Map<String, BigDecimal> valueStats = new HashMap<>();
        
        for (SimpleFunctionPoint fp : functionPoints) {
            String type = fp.getFpType();
            typeStats.put(type, typeStats.getOrDefault(type, 0) + 1);
            valueStats.put(type, valueStats.getOrDefault(type, BigDecimal.ZERO).add(fp.getCalculatedFpValue()));
        }
        
        result.put("typeStatistics", typeStats);
        result.put("valueStatistics", valueStats);
        
        return result;
    }

    /**
     * 确定功能点复杂度
     */
    private String determineComplexity(SimpleFunctionPoint fp) {
        String type = fp.getFpType();
        Integer det = fp.getDetCount() != null ? fp.getDetCount() : 1;
        Integer ret = fp.getRetCount() != null ? fp.getRetCount() : 1;
        Integer ftr = fp.getFtrCount() != null ? fp.getFtrCount() : 1;
        
        if ("ILF".equals(type) || "EIF".equals(type)) {
            return determineDataComplexity(det, ret);
        } else if ("EI".equals(type) || "EQ".equals(type)) {
            return determineTransactionComplexity(det, ftr, true);
        } else if ("EO".equals(type)) {
            return determineTransactionComplexity(det, ftr, false);
        }
        
        return "MEDIUM";
    }
    
    private String determineDataComplexity(int det, int ret) {
        if (ret <= 1) {
            if (det <= 19) return "LOW";
            else if (det <= 50) return "MEDIUM";
            else return "HIGH";
        } else if (ret <= 5) {
            if (det <= 19) return "MEDIUM";
            else return "HIGH";
        } else {
            return "HIGH";
        }
    }
    
    private String determineTransactionComplexity(int det, int ftr, boolean isEiOrEq) {
        int detLow = isEiOrEq ? 4 : 5;
        int detMedium = isEiOrEq ? 15 : 19;
        
        if (ftr <= 1) {
            if (det <= detLow) return "LOW";
            else if (det <= detMedium) return "MEDIUM";
            else return "HIGH";
        } else if (ftr == 2) {
            if (det <= detLow) return "MEDIUM";
            else return "HIGH";
        } else {
            return "HIGH";
        }
    }

    /**
     * 获取功能点权重
     */
    private BigDecimal getWeight(String type, String complexity) {
        Map<String, BigDecimal> weights = new HashMap<>();
        
        if ("ILF".equals(type)) {
            weights.put("LOW", new BigDecimal("7.0000"));
            weights.put("MEDIUM", new BigDecimal("10.0000"));
            weights.put("HIGH", new BigDecimal("15.0000"));
        } else if ("EIF".equals(type)) {
            weights.put("LOW", new BigDecimal("5.0000"));
            weights.put("MEDIUM", new BigDecimal("7.0000"));
            weights.put("HIGH", new BigDecimal("10.0000"));
        } else if ("EI".equals(type)) {
            weights.put("LOW", new BigDecimal("3.0000"));
            weights.put("MEDIUM", new BigDecimal("4.0000"));
            weights.put("HIGH", new BigDecimal("6.0000"));
        } else if ("EO".equals(type)) {
            weights.put("LOW", new BigDecimal("4.0000"));
            weights.put("MEDIUM", new BigDecimal("5.0000"));
            weights.put("HIGH", new BigDecimal("7.0000"));
        } else if ("EQ".equals(type)) {
            weights.put("LOW", new BigDecimal("3.0000"));
            weights.put("MEDIUM", new BigDecimal("4.0000"));
            weights.put("HIGH", new BigDecimal("6.0000"));
        } else {
            throw new RuntimeException("不支持的功能点类型: " + type);
        }
        
        BigDecimal weight = weights.get(complexity);
        if (weight == null) {
            throw new RuntimeException("不支持的复杂度等级: " + complexity);
        }
        
        return weight;
    }

    /**
     * 计算人月
     */
    private BigDecimal calculatePersonMonths(BigDecimal adjustedFunctionPoints) {
        // 政府标准：7.01功能点/人月
        BigDecimal productivityRate = new BigDecimal("7.01");
        BigDecimal conversionRate = BigDecimal.ONE.divide(productivityRate, DECIMAL_SCALE, ROUNDING_MODE);
        return adjustedFunctionPoints.multiply(conversionRate);
    }
    
    /**
     * 计算成本
     */
    private BigDecimal calculateCost(BigDecimal personMonths) {
        // 政府标准：18000元/人月
        BigDecimal monthlyRate = new BigDecimal("18000.00");
        return personMonths.multiply(monthlyRate);
    }

    /**
     * 批量计算多个项目的NESMA功能点
     */
    @PostMapping("/batch-calculate")
    public ApiResponse<Map<String, Object>> batchCalculateNesmaFunctionPoints(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> projectIds = (List<Long>) request.get("projectIds");
            
            if (projectIds == null || projectIds.isEmpty()) {
                return ApiResponse.error("项目ID列表不能为空");
            }
            
            logger.info("开始批量简化NESMA计算，项目数量: {}", projectIds.size());
            
            Map<String, Object> batchResults = new HashMap<>();
            List<Map<String, Object>> results = new java.util.ArrayList<>();
            int successCount = 0;
            int failureCount = 0;
            
            long totalStartTime = System.currentTimeMillis();
            
            for (Long projectId : projectIds) {
                try {
                    // 验证项目是否存在
                    if (!projectRepository.existsById(projectId)) {
                        Map<String, Object> failedResult = new HashMap<>();
                        failedResult.put("projectId", projectId);
                        failedResult.put("success", false);
                        failedResult.put("error", "项目不存在");
                        results.add(failedResult);
                        failureCount++;
                        continue;
                    }
                    
                    // 获取功能点
                    List<SimpleFunctionPoint> functionPoints = functionPointRepository.findByProjectIdAndDeletedAtIsNull(projectId);
                    if (functionPoints.isEmpty()) {
                        Map<String, Object> failedResult = new HashMap<>();
                        failedResult.put("projectId", projectId);
                        failedResult.put("success", false);
                        failedResult.put("error", "项目功能点数据为空");
                        results.add(failedResult);
                        failureCount++;
                        continue;
                    }
                    
                    // 执行计算
                    Map<String, Object> calculationResult = performSimpleNesmaCalculation(projectId, functionPoints);
                    calculationResult.put("success", true);
                    
                    results.add(calculationResult);
                    successCount++;
                    
                    logger.info("项目{}计算完成", projectId);
                    
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
            
            logger.info("批量简化NESMA计算完成，成功: {}, 失败: {}, 总耗时: {}ms", 
                    successCount, failureCount, totalDuration);
            
            return ApiResponse.success("批量NESMA计算完成", batchResults);
            
        } catch (Exception e) {
            logger.error("批量简化NESMA计算失败: {}", e.getMessage(), e);
            return ApiResponse.error("批量NESMA计算失败: " + e.getMessage());
        }
    }
}