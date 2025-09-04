package gov.changsha.finance.service;

import gov.changsha.finance.entity.Project;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 复用度调整计算服务
 * 基于软件复用程度对AFP进行调整，计算最终功能点
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-04
 */
@Service
public class ReuseAdjustmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReuseAdjustmentService.class);
    
    /** 高精度小数位数 */
    private static final int DECIMAL_SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    /** 高复用度系数：1/3 ≈ 0.3333 */
    private static final BigDecimal HIGH_REUSE_COEFFICIENT = 
            BigDecimal.ONE.divide(new BigDecimal("3"), DECIMAL_SCALE, ROUNDING_MODE);
    
    /** 中复用度系数：2/3 ≈ 0.6667 */
    private static final BigDecimal MEDIUM_REUSE_COEFFICIENT = 
            new BigDecimal("2").divide(new BigDecimal("3"), DECIMAL_SCALE, ROUNDING_MODE);
    
    /** 低复用度和无复用系数：1.0000 */
    private static final BigDecimal LOW_NO_REUSE_COEFFICIENT = BigDecimal.ONE;
    
    /**
     * 复用等级枚举
     */
    public enum ReuseLevel {
        HIGH("高复用度", HIGH_REUSE_COEFFICIENT, 
             "项目中大部分功能(>70%)可以复用现有系统或组件，只需少量定制开发"),
        MEDIUM("中复用度", MEDIUM_REUSE_COEFFICIENT, 
               "项目中部分功能(30-70%)可以复用，需要适度的定制和集成开发"),
        LOW("低复用度", LOW_NO_REUSE_COEFFICIENT, 
            "项目中少量功能(<30%)可以复用，主要为全新开发"),
        NONE("无复用", LOW_NO_REUSE_COEFFICIENT, 
             "项目完全为全新开发，无法复用任何现有系统或组件");
        
        private final String displayName;
        private final BigDecimal coefficient;
        private final String description;
        
        ReuseLevel(String displayName, BigDecimal coefficient, String description) {
            this.displayName = displayName;
            this.coefficient = coefficient;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public BigDecimal getCoefficient() { return coefficient; }
        public String getDescription() { return description; }
    }
    
    /**
     * 复用度调整详细结果
     */
    public static class ReuseAdjustmentDetail {
        private final BigDecimal originalAfp;
        private final ReuseLevel reuseLevel;
        private final BigDecimal reuseCoefficient;
        private final BigDecimal finalFunctionPoints;
        private final String calculationProcess;
        private final Map<String, String> adjustmentNotes;
        
        public ReuseAdjustmentDetail(BigDecimal originalAfp, ReuseLevel reuseLevel, 
                                   BigDecimal reuseCoefficient, BigDecimal finalFunctionPoints,
                                   String calculationProcess, Map<String, String> adjustmentNotes) {
            this.originalAfp = originalAfp;
            this.reuseLevel = reuseLevel;
            this.reuseCoefficient = reuseCoefficient;
            this.finalFunctionPoints = finalFunctionPoints;
            this.calculationProcess = calculationProcess;
            this.adjustmentNotes = adjustmentNotes;
        }
        
        // Getter methods
        public BigDecimal getOriginalAfp() { return originalAfp; }
        public ReuseLevel getReuseLevel() { return reuseLevel; }
        public BigDecimal getReuseCoefficient() { return reuseCoefficient; }
        public BigDecimal getFinalFunctionPoints() { return finalFunctionPoints; }
        public String getCalculationProcess() { return calculationProcess; }
        public Map<String, String> getAdjustmentNotes() { return adjustmentNotes; }
    }
    
    /**
     * 应用复用度调整
     * 
     * @param afp 调整后功能点(AFP)
     * @param reuseLevel 复用等级
     * @return 最终功能点
     */
    public BigDecimal applyReuseAdjustment(BigDecimal afp, ReuseLevel reuseLevel) {
        if (afp == null) {
            throw new IllegalArgumentException("AFP不能为空");
        }
        
        if (reuseLevel == null) {
            logger.warn("复用等级为空，使用默认无复用等级");
            reuseLevel = ReuseLevel.NONE;
        }
        
        BigDecimal coefficient = reuseLevel.getCoefficient();
        BigDecimal finalFunctionPoints = afp.multiply(coefficient);
        
        logger.info("复用度调整完成 - 原始AFP: {}, 复用等级: {}, 系数: {}, 最终功能点: {}", 
                afp, reuseLevel.getDisplayName(), coefficient, finalFunctionPoints);
        
        return finalFunctionPoints.setScale(DECIMAL_SCALE, ROUNDING_MODE);
    }
    
    /**
     * 计算复用度调整的详细信息
     * 
     * @param afp 调整后功能点(AFP)
     * @param reuseLevel 复用等级
     * @return 复用度调整详细结果
     */
    public ReuseAdjustmentDetail calculateWithDetail(BigDecimal afp, ReuseLevel reuseLevel) {
        if (afp == null) {
            throw new IllegalArgumentException("AFP不能为空");
        }
        
        if (reuseLevel == null) {
            reuseLevel = ReuseLevel.NONE;
        }
        
        BigDecimal coefficient = reuseLevel.getCoefficient();
        BigDecimal finalFunctionPoints = afp.multiply(coefficient).setScale(DECIMAL_SCALE, ROUNDING_MODE);
        
        // 构建计算过程说明
        String calculationProcess = String.format(
            "复用度调整计算过程:\n" +
            "1. 输入AFP: %s\n" +
            "2. 选择复用等级: %s\n" +
            "3. 复用系数: %s\n" +
            "4. 计算公式: 最终功能点 = AFP × 复用系数\n" +
            "5. 计算过程: %s × %s = %s\n" +
            "6. 最终结果: %s 功能点",
            afp, reuseLevel.getDisplayName(), coefficient,
            afp, coefficient, finalFunctionPoints, finalFunctionPoints
        );
        
        // 构建调整说明
        Map<String, String> adjustmentNotes = new LinkedHashMap<>();
        adjustmentNotes.put("复用等级", reuseLevel.getDisplayName());
        adjustmentNotes.put("复用系数", coefficient.toString());
        adjustmentNotes.put("适用场景", reuseLevel.getDescription());
        adjustmentNotes.put("调整依据", "基于《长沙市财政评审中心政府投资信息化项目评审指南》");
        adjustmentNotes.put("计算精度", DECIMAL_SCALE + "位小数，四舍五入");
        
        // 添加影响分析
        BigDecimal reductionAmount = afp.subtract(finalFunctionPoints);
        BigDecimal reductionPercentage = reductionAmount.divide(afp, 4, ROUNDING_MODE).multiply(new BigDecimal("100"));
        
        adjustmentNotes.put("工作量减少", reductionAmount + " 功能点");
        adjustmentNotes.put("减少比例", reductionPercentage.setScale(2, ROUNDING_MODE) + "%");
        
        return new ReuseAdjustmentDetail(afp, reuseLevel, coefficient, finalFunctionPoints, 
                                       calculationProcess, adjustmentNotes);
    }
    
    /**
     * 获取支持的复用等级列表
     * 
     * @return 复用等级列表
     */
    public List<ReuseLevel> getSupportedReuseLevels() {
        return Arrays.asList(ReuseLevel.values());
    }
    
    /**
     * 验证复用等级是否有效
     * 
     * @param reuseLevel 复用等级
     * @return 是否有效
     */
    public boolean isValidReuseLevel(ReuseLevel reuseLevel) {
        return reuseLevel != null && Arrays.asList(ReuseLevel.values()).contains(reuseLevel);
    }
    
    /**
     * 根据字符串获取复用等级
     * 
     * @param levelName 等级名称
     * @return 复用等级
     */
    public ReuseLevel parseReuseLevel(String levelName) {
        if (levelName == null || levelName.trim().isEmpty()) {
            return ReuseLevel.NONE;
        }
        
        String normalizedName = levelName.trim().toUpperCase();
        
        try {
            return ReuseLevel.valueOf(normalizedName);
        } catch (IllegalArgumentException e) {
            // 尝试通过显示名称匹配
            for (ReuseLevel level : ReuseLevel.values()) {
                if (level.getDisplayName().equals(levelName.trim())) {
                    return level;
                }
            }
            
            logger.warn("无法解析复用等级: {}, 使用默认无复用等级", levelName);
            return ReuseLevel.NONE;
        }
    }
    
    /**
     * 获取复用等级的推荐建议
     * 
     * @param project 项目对象
     * @return 推荐的复用等级和建议说明
     */
    public Map<String, Object> getReuseLevelRecommendation(Project project) {
        Map<String, Object> recommendation = new LinkedHashMap<>();
        
        // 基于项目特征进行复用度评估
        ReuseLevel recommendedLevel = ReuseLevel.LOW; // 默认建议低复用度
        String reason = "基于项目特征的综合评估";
        
        // 如果项目有VAF因子，检查TF10（重用性）的评分
        if (project.getVafFactors() != null && !project.getVafFactors().isEmpty()) {
            project.getVafFactors().stream()
                   .filter(factor -> "TF10".equals(factor.getFactorType()))
                   .findFirst()
                   .ifPresent(reuseabilityFactor -> {
                       int score = reuseabilityFactor.getInfluenceScore();
                       
                       if (score >= 4) {
                           recommendation.put("recommendedLevel", ReuseLevel.HIGH);
                           recommendation.put("reason", "VAF重用性因子评分较高(" + score + "分)，建议选择高复用度");
                       } else if (score >= 2) {
                           recommendation.put("recommendedLevel", ReuseLevel.MEDIUM);
                           recommendation.put("reason", "VAF重用性因子评分中等(" + score + "分)，建议选择中复用度");
                       } else {
                           recommendation.put("recommendedLevel", ReuseLevel.LOW);
                           recommendation.put("reason", "VAF重用性因子评分较低(" + score + "分)，建议选择低复用度或无复用");
                       }
                   });
        } else {
            recommendation.put("recommendedLevel", recommendedLevel);
            recommendation.put("reason", reason);
        }
        
        // 添加选择指导
        List<String> selectionGuidance = Arrays.asList(
            "1. 高复用度适用：基于现有成熟平台扩展，复用度>70%",
            "2. 中复用度适用：部分功能可复用，需要适度定制，复用度30-70%",
            "3. 低复用度适用：主要为新开发，少量复用，复用度<30%",
            "4. 无复用适用：全新系统开发，完全无法复用现有组件"
        );
        
        recommendation.put("selectionGuidance", selectionGuidance);
        recommendation.put("impactAnalysis", getReuseLevelImpactAnalysis());
        
        return recommendation;
    }
    
    /**
     * 获取复用等级影响分析
     */
    private Map<ReuseLevel, String> getReuseLevelImpactAnalysis() {
        Map<ReuseLevel, String> impactAnalysis = new LinkedHashMap<>();
        
        impactAnalysis.put(ReuseLevel.HIGH, 
            "选择高复用度将使最终功能点约为AFP的33%，大幅减少开发工作量和成本");
        impactAnalysis.put(ReuseLevel.MEDIUM, 
            "选择中复用度将使最终功能点约为AFP的67%，中等程度减少开发工作量");
        impactAnalysis.put(ReuseLevel.LOW, 
            "选择低复用度最终功能点等于AFP，不减少开发工作量");
        impactAnalysis.put(ReuseLevel.NONE, 
            "选择无复用最终功能点等于AFP，按全新开发计算工作量");
        
        return impactAnalysis;
    }
}