package gov.changsha.finance.controller;

import gov.changsha.finance.dto.ApiResponse;
import gov.changsha.finance.entity.SimpleFunctionPoint;
import gov.changsha.finance.entity.SimpleProject;
import gov.changsha.finance.repository.SimpleFunctionPointRepository;
import gov.changsha.finance.repository.SimpleProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 简化功能点管理控制器
 * 提供功能点的CRUD操作和查询功能
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-09
 */
@RestController
@RequestMapping("/api/simple-function-points")
public class SimpleFunctionPointController {

    @Autowired
    private SimpleFunctionPointRepository functionPointRepository;
    
    @Autowired
    private SimpleProjectRepository projectRepository;

    /**
     * 为项目创建功能点
     */
    @PostMapping("/project/{projectId}")
    public ApiResponse<SimpleFunctionPoint> createFunctionPoint(@PathVariable Long projectId, 
                                                               @RequestBody Map<String, Object> request) {
        try {
            // 验证项目是否存在
            SimpleProject project = projectRepository.findByIdAndDeletedFalse(projectId);
            if (project == null) {
                return ApiResponse.error("项目不存在，ID: " + projectId);
            }
            
            // 创建功能点
            SimpleFunctionPoint functionPoint = new SimpleFunctionPoint();
            functionPoint.setProjectId(projectId);
            functionPoint.setFpName((String) request.get("functionName"));
            functionPoint.setFpDescription((String) request.get("functionDescription"));
            functionPoint.setFpType((String) request.get("functionPointType"));
            
            // 设置复杂度参数（暂存在内存中，实际应存储到complexity_assessments表）
            if (request.containsKey("detCount")) {
                Object detCountObj = request.get("detCount");
                if (detCountObj instanceof Number) {
                    functionPoint.setDetCount(((Number) detCountObj).intValue());
                }
            }
            
            if (request.containsKey("retCount")) {
                Object retCountObj = request.get("retCount");
                if (retCountObj instanceof Number) {
                    functionPoint.setRetCount(((Number) retCountObj).intValue());
                }
            }
            
            if (request.containsKey("ftrCount")) {
                Object ftrCountObj = request.get("ftrCount");
                if (ftrCountObj instanceof Number) {
                    functionPoint.setFtrCount(((Number) ftrCountObj).intValue());
                }
            }
            
            // 设置默认值
            functionPoint.setComplexityLevel("MEDIUM");
            functionPoint.setComplexityWeight(BigDecimal.valueOf(4.0));
            functionPoint.setCalculatedFpValue(BigDecimal.valueOf(4.0));
            functionPoint.setCreatedBy(1L); // 默认用户ID
            
            // 保存功能点
            functionPoint = functionPointRepository.save(functionPoint);
            
            return ApiResponse.success("创建功能点成功", functionPoint);
            
        } catch (Exception e) {
            return ApiResponse.error("创建功能点失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目的所有功能点
     */
    @GetMapping("/project/{projectId}")
    public ApiResponse<List<SimpleFunctionPoint>> getProjectFunctionPoints(@PathVariable Long projectId) {
        try {
            List<SimpleFunctionPoint> functionPoints = functionPointRepository.findByProjectIdAndDeletedAtIsNull(projectId);
            return ApiResponse.success("获取功能点列表成功", functionPoints);
        } catch (Exception e) {
            return ApiResponse.error("获取功能点列表失败: " + e.getMessage());
        }
    }

    /**
     * 批量创建功能点
     */
    @PostMapping("/project/{projectId}/batch")
    public ApiResponse<List<SimpleFunctionPoint>> batchCreateFunctionPoints(@PathVariable Long projectId,
                                                                           @RequestBody List<Map<String, Object>> functionPointsData) {
        try {
            // 验证项目是否存在
            SimpleProject project = projectRepository.findByIdAndDeletedFalse(projectId);
            if (project == null) {
                return ApiResponse.error("项目不存在，ID: " + projectId);
            }
            
            List<SimpleFunctionPoint> createdFunctionPoints = new java.util.ArrayList<>();
            
            for (Map<String, Object> fpData : functionPointsData) {
                SimpleFunctionPoint functionPoint = new SimpleFunctionPoint();
                functionPoint.setProjectId(projectId);
                functionPoint.setFpName((String) fpData.get("functionName"));
                functionPoint.setFpDescription((String) fpData.get("functionDescription"));
                functionPoint.setFpType((String) fpData.get("functionPointType"));
                
                // 设置复杂度参数
                if (fpData.containsKey("detCount")) {
                    Object detCountObj = fpData.get("detCount");
                    if (detCountObj instanceof Number) {
                        functionPoint.setDetCount(((Number) detCountObj).intValue());
                    }
                }
                
                if (fpData.containsKey("retCount")) {
                    Object retCountObj = fpData.get("retCount");
                    if (retCountObj instanceof Number) {
                        functionPoint.setRetCount(((Number) retCountObj).intValue());
                    }
                }
                
                if (fpData.containsKey("ftrCount")) {
                    Object ftrCountObj = fpData.get("ftrCount");
                    if (ftrCountObj instanceof Number) {
                        functionPoint.setFtrCount(((Number) ftrCountObj).intValue());
                    }
                }
                
                // 设置默认值
                functionPoint.setComplexityLevel("MEDIUM");
                functionPoint.setComplexityWeight(BigDecimal.valueOf(4.0));
                functionPoint.setCalculatedFpValue(BigDecimal.valueOf(4.0));
                functionPoint.setCreatedBy(1L);
                
                createdFunctionPoints.add(functionPoint);
            }
            
            // 批量保存
            List<SimpleFunctionPoint> savedFunctionPoints = functionPointRepository.saveAll(createdFunctionPoints);
            
            return ApiResponse.success("批量创建功能点成功", savedFunctionPoints);
            
        } catch (Exception e) {
            return ApiResponse.error("批量创建功能点失败: " + e.getMessage());
        }
    }

    /**
     * 删除功能点（软删除）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteFunctionPoint(@PathVariable Long id) {
        try {
            SimpleFunctionPoint functionPoint = functionPointRepository.findById(id).orElse(null);
            if (functionPoint == null || functionPoint.getDeletedAt() != null) {
                return ApiResponse.error("功能点不存在，ID: " + id);
            }
            
            // 软删除
            functionPoint.delete(1L);
            functionPointRepository.save(functionPoint);
            
            return ApiResponse.success("删除功能点成功", "功能点ID: " + id);
            
        } catch (Exception e) {
            return ApiResponse.error("删除功能点失败: " + e.getMessage());
        }
    }

    /**
     * 获取项目功能点统计
     */
    @GetMapping("/project/{projectId}/stats")
    public ApiResponse<Map<String, Object>> getProjectFunctionPointStats(@PathVariable Long projectId) {
        try {
            long totalCount = functionPointRepository.countByProjectId(projectId);
            BigDecimal totalValue = functionPointRepository.getTotalFunctionPointValueByProjectId(projectId);
            List<Object[]> typeStats = functionPointRepository.countByFunctionPointTypeForProject(projectId);
            List<Object[]> complexityStats = functionPointRepository.getComplexityStatsForProject(projectId);
            
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalCount", totalCount);
            stats.put("totalValue", totalValue);
            stats.put("typeStats", typeStats);
            stats.put("complexityStats", complexityStats);
            stats.put("projectId", projectId);
            
            return ApiResponse.success("获取功能点统计成功", stats);
            
        } catch (Exception e) {
            return ApiResponse.error("获取功能点统计失败: " + e.getMessage());
        }
    }
}