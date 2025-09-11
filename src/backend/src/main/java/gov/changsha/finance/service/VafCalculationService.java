package gov.changsha.finance.service;

import gov.changsha.finance.entity.Project;
import gov.changsha.finance.entity.VafFactor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * VAF (Value Adjustment Factor) 调整因子计算服务
 * 实现NESMA标准的14个技术复杂度因子评估和VAF计算
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-03
 */
@Service
public class VafCalculationService {
    
    private static final Logger logger = LoggerFactory.getLogger(VafCalculationService.class);
    
    /** VAF计算的精度 */
    private static final int VAF_SCALE = 4;
    private static final RoundingMode VAF_ROUNDING = RoundingMode.HALF_UP;
    
    /** VAF基础值：0.65 */
    private static final BigDecimal VAF_BASE = new BigDecimal("0.65");
    
    /** VAF调整系数：0.01 */
    private static final BigDecimal VAF_COEFFICIENT = new BigDecimal("0.01");
    
    /** VAF最小值：0.65 */
    private static final BigDecimal VAF_MIN = new BigDecimal("0.65");
    
    /** VAF最大值：1.35 */
    private static final BigDecimal VAF_MAX = new BigDecimal("1.35");
    
    /**
     * 计算项目的VAF调整因子
     * VAF = 0.65 + 0.01 × Σ(影响度评分)
     * 
     * @param project 项目对象
     * @return VAF值，范围[0.65, 1.35]
     */
    public BigDecimal calculateVaf(Project project) {
        logger.info("开始计算VAF调整因子，项目ID: {}", project.getId());
        
        try {
            // 1. 获取或初始化VAF因子
            List<VafFactor> vafFactors = getOrInitializeVafFactors(project);
            
            // 2. 计算总影响度评分
            BigDecimal totalInfluenceScore = calculateTotalInfluenceScore(vafFactors);
            
            // 3. 计算VAF值
            BigDecimal vaf = VAF_BASE.add(VAF_COEFFICIENT.multiply(totalInfluenceScore));
            
            // 4. 确保VAF值在有效范围内
            vaf = validateVafRange(vaf);
            
            logger.info("VAF计算完成，项目ID: {}, 总影响评分: {}, VAF值: {}", 
                    project.getId(), totalInfluenceScore, vaf);
            
            return vaf.setScale(VAF_SCALE, VAF_ROUNDING);
            
        } catch (Exception e) {
            logger.error("VAF计算失败，项目ID: {}, 错误: {}", project.getId(), e.getMessage(), e);
            // 计算失败时返回默认VAF值1.0
            return BigDecimal.ONE.setScale(VAF_SCALE, VAF_ROUNDING);
        }
    }
    
    /**
     * 获取或初始化项目的14个标准VAF因子
     */
    private List<VafFactor> getOrInitializeVafFactors(Project project) {
        List<VafFactor> vafFactors = null;
        
        try {
            // 尝试获取VAF因子，但如果失败就使用默认值
            vafFactors = project.getVafFactors();
        } catch (Exception e) {
            logger.warn("无法获取项目{}的VAF因子，将使用默认值。错误: {}", project.getId(), e.getMessage());
            vafFactors = null;
        }
        
        if (vafFactors == null || vafFactors.isEmpty()) {
            logger.debug("项目{}的VAF因子为空，初始化标准因子", project.getId());
            vafFactors = initializeStandardVafFactors(project);
            // 不再设置到project中，避免关联关系问题
        }
        
        // 确保14个因子完整
        if (vafFactors.size() != 14) {
            logger.warn("项目{}的VAF因子数量不完整，当前数量: {}，重新初始化", 
                    project.getId(), vafFactors.size());
            vafFactors = initializeStandardVafFactors(project);
        }
        
        return vafFactors;
    }
    
    /**
     * 初始化14个标准VAF因子
     * 基于NESMA标准定义的技术复杂度因子
     */
    private List<VafFactor> initializeStandardVafFactors(Project project) {
        List<VafFactor> factors = new ArrayList<>();
        Map<String, String> standardFactors = getStandardVafFactorDefinitions();
        
        for (Map.Entry<String, String> entry : standardFactors.entrySet()) {
            VafFactor factor = new VafFactor(project, entry.getKey(), entry.getValue());
            factors.add(factor);
        }
        
        logger.debug("已初始化{}个标准VAF因子", factors.size());
        return factors;
    }
    
    /**
     * 获取NESMA标准的14个技术复杂度因子定义
     */
    private Map<String, String> getStandardVafFactorDefinitions() {
        Map<String, String> factors = new LinkedHashMap<>();
        
        factors.put("TF01", "数据通信");
        factors.put("TF02", "分布式数据处理");
        factors.put("TF03", "性能");
        factors.put("TF04", "高度使用配置");
        factors.put("TF05", "交易率");
        factors.put("TF06", "在线数据录入");
        factors.put("TF07", "最终用户效率");
        factors.put("TF08", "在线更新");
        factors.put("TF09", "复杂处理");
        factors.put("TF10", "重用性");
        factors.put("TF11", "安装简便性");
        factors.put("TF12", "操作简便性");
        factors.put("TF13", "多个场地");
        factors.put("TF14", "变更便利性");
        
        return factors;
    }
    
    /**
     * 计算所有VAF因子的总影响度评分
     */
    private BigDecimal calculateTotalInfluenceScore(List<VafFactor> vafFactors) {
        BigDecimal totalScore = BigDecimal.ZERO;
        
        for (VafFactor factor : vafFactors) {
            BigDecimal contribution = factor.calculateContribution();
            totalScore = totalScore.add(contribution);
            
            logger.debug("VAF因子 {}: 评分={}, 权重={}, 贡献={}", 
                    factor.getFactorType(), 
                    factor.getInfluenceScore(), 
                    factor.getWeight(), 
                    contribution);
        }
        
        return totalScore;
    }
    
    /**
     * 验证VAF值是否在有效范围内
     * NESMA标准：VAF范围为[0.65, 1.35]
     */
    private BigDecimal validateVafRange(BigDecimal vaf) {
        if (vaf.compareTo(VAF_MIN) < 0) {
            logger.warn("计算的VAF值{}小于最小值{}，调整为最小值", vaf, VAF_MIN);
            return VAF_MIN;
        }
        
        if (vaf.compareTo(VAF_MAX) > 0) {
            logger.warn("计算的VAF值{}大于最大值{}，调整为最大值", vaf, VAF_MAX);
            return VAF_MAX;
        }
        
        return vaf;
    }
    
    /**
     * 获取VAF因子的详细评估说明
     */
    public Map<String, String> getVafFactorDescriptions() {
        Map<String, String> descriptions = new LinkedHashMap<>();
        
        descriptions.put("TF01", "数据通信：应用系统需要的数据通信设施");
        descriptions.put("TF02", "分布式数据处理：分布式处理和数据传输功能");
        descriptions.put("TF03", "性能：应用系统的响应时间和吞吐量要求");
        descriptions.put("TF04", "高度使用配置：计算机资源的高度利用要求");
        descriptions.put("TF05", "交易率：每日/每月的交易处理量");
        descriptions.put("TF06", "在线数据录入：在线实时数据输入比例");
        descriptions.put("TF07", "最终用户效率：系统对最终用户的易用性要求");
        descriptions.put("TF08", "在线更新：主文件的在线更新比例");
        descriptions.put("TF09", "复杂处理：系统内部复杂的逻辑处理");
        descriptions.put("TF10", "重用性：应用代码和组件的重用性设计");
        descriptions.put("TF11", "安装简便性：系统的安装和部署复杂度");
        descriptions.put("TF12", "操作简便性：系统的日常操作和维护复杂度");
        descriptions.put("TF13", "多个场地：系统需要在多个地点安装和运行");
        descriptions.put("TF14", "变更便利性：系统对业务变化的适应和修改能力");
        
        return descriptions;
    }
    
    /**
     * 验证VAF因子评分的合理性
     * 
     * @param factors VAF因子列表
     * @return 验证结果信息
     */
    public String validateVafFactorScores(List<VafFactor> factors) {
        if (factors == null || factors.isEmpty()) {
            return "VAF因子列表为空";
        }
        
        if (factors.size() != 14) {
            return "VAF因子数量不正确，应为14个，当前为" + factors.size() + "个";
        }
        
        for (VafFactor factor : factors) {
            if (factor.getInfluenceScore() < 0 || factor.getInfluenceScore() > 5) {
                return "VAF因子" + factor.getFactorType() + "的评分超出范围[0-5]";
            }
        }
        
        return "VAF因子验证通过";
    }
}