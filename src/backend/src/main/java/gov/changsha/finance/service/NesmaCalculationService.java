package gov.changsha.finance.service;

import gov.changsha.finance.entity.*;
import gov.changsha.finance.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * NESMA功能点计算核心服务 - 简化版本
 * 兼容Java 1.8，确保编译通过
 * 
 * @author Developer Engineer  
 * @version 1.0.0
 * @since 2025-09-03
 */
@Service
@Transactional
public class NesmaCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(NesmaCalculationService.class);
    
    private static final int DECIMAL_SCALE = 4;
    private static final int COST_DECIMAL_SCALE = 2; // 成本精度：2位小数（货币标准）
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private VafCalculationService vafCalculationService;
    
    /**
     * 执行NESMA功能点计算
     */
    public CalculationResult calculateNesmaFunctionPoints(Long projectId) {
        logger.info("开始执行NESMA功能点计算，项目ID: {}", projectId);
        
        try {
            // 1. 验证项目
            Project project = validateProject(projectId);
            
            // 2. 创建结果记录
            CalculationResult result = new CalculationResult(projectId, "NESMA_CALCULATION");
            
            // 3. 获取功能点列表
            List<FunctionPoint> functionPoints = project.getFunctionPoints();
            if (functionPoints == null || functionPoints.isEmpty()) {
                throw new RuntimeException("项目功能点数据为空，无法执行计算");
            }
            
            // 4. 计算总功能点
            BigDecimal totalFunctionPoints = calculateTotal(functionPoints);
            
            // 5. VAF调整
            BigDecimal adjustedFunctionPoints = applyVafAdjustment(project, totalFunctionPoints);
            
            // 6. 计算人月和成本
            BigDecimal personMonths = calculatePersonMonths(adjustedFunctionPoints);
            BigDecimal cost = calculateCost(personMonths);
            
            // 7. 设置结果
            result.setTotalFunctionPoints(totalFunctionPoints.setScale(DECIMAL_SCALE, ROUNDING_MODE));
            result.setAdjustedFunctionPoints(adjustedFunctionPoints.setScale(DECIMAL_SCALE, ROUNDING_MODE));
            result.setEstimatedPersonMonths(personMonths.setScale(DECIMAL_SCALE, ROUNDING_MODE));
            result.setEstimatedCost(cost.setScale(COST_DECIMAL_SCALE, ROUNDING_MODE));
            
            result.completeCalculation();
            
            logger.info("NESMA计算完成，项目ID: {}, 总功能点: {}", projectId, totalFunctionPoints);
            
            return result;
            
        } catch (Exception e) {
            logger.error("NESMA计算失败，项目ID: {}, 错误: {}", projectId, e.getMessage(), e);
            
            CalculationResult failedResult = new CalculationResult(projectId, "NESMA_CALCULATION");
            failedResult.markCalculationFailed();
            failedResult.setRemarks("计算失败: " + e.getMessage());
            
            throw new RuntimeException("NESMA计算执行失败: " + e.getMessage(), e);
        }
    }
    
    private Project validateProject(Long projectId) {
        if (projectId == null) {
            throw new RuntimeException("项目ID不能为空");
        }
        
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            throw new RuntimeException("项目不存在，项目ID: " + projectId);
        }
        
        return project;
    }
    
    private BigDecimal calculateTotal(List<FunctionPoint> functionPoints) {
        BigDecimal total = BigDecimal.ZERO;
        
        for (FunctionPoint fp : functionPoints) {
            // 验证功能点
            if (fp.getFunctionPointType() == null) {
                throw new RuntimeException("功能点类型不能为空");
            }
            
            // 确定复杂度
            String complexity = determineComplexity(fp);
            fp.setComplexityLevel(complexity);
            
            // 获取权重
            BigDecimal weight = getWeight(fp.getFunctionPointType(), complexity);
            fp.setComplexityWeight(weight);
            fp.setFunctionPointValue(weight);
            
            total = total.add(weight);
        }
        
        return total;
    }
    
    private String determineComplexity(FunctionPoint fp) {
        String type = fp.getFunctionPointType();
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
    
    private BigDecimal getWeight(String type, String complexity) {
        Map<String, BigDecimal> weights = new HashMap<String, BigDecimal>();
        
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
    
    private BigDecimal applyVafAdjustment(Project project, BigDecimal totalFunctionPoints) {
        try {
            // 计算VAF调整因子
            BigDecimal vaf = vafCalculationService.calculateVaf(project);
            
            // 应用VAF调整
            BigDecimal adjustedFunctionPoints = totalFunctionPoints.multiply(vaf);
            
            logger.info("VAF调整完成 - 项目ID: {}, 原始功能点: {}, VAF: {}, 调整后功能点: {}",
                    project.getId(), totalFunctionPoints, vaf, adjustedFunctionPoints);
            
            return adjustedFunctionPoints.setScale(DECIMAL_SCALE, ROUNDING_MODE);
            
        } catch (Exception e) {
            logger.error("VAF调整失败，项目ID: {}, 错误: {}, 使用原始功能点数", 
                    project.getId(), e.getMessage(), e);
            
            // VAF计算失败时，返回原始功能点数确保计算能继续进行
            return totalFunctionPoints;
        }
    }
    
    private BigDecimal calculatePersonMonths(BigDecimal adjustedFunctionPoints) {
        // 政府标准：7.01功能点/人月，即1人月 = 7.01功能点
        // 因此转换率 = 1 / 7.01 = 0.1427人月/功能点
        BigDecimal productivityRate = new BigDecimal("7.01"); // 7.01功能点/人月
        BigDecimal conversionRate = BigDecimal.ONE.divide(productivityRate, DECIMAL_SCALE, ROUNDING_MODE);
        return adjustedFunctionPoints.multiply(conversionRate).setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }
    
    private BigDecimal calculateCost(BigDecimal personMonths) {
        // 政府标准：18000元/人月
        BigDecimal monthlyRate = new BigDecimal("18000.00");
        return personMonths.multiply(monthlyRate).setScale(COST_DECIMAL_SCALE, ROUNDING_MODE);
    }
}