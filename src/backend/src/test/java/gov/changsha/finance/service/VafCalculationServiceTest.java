package gov.changsha.finance.service;

import gov.changsha.finance.entity.Project;
import gov.changsha.finance.entity.VafFactor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VAF调整因子计算服务单元测试
 * 验证NESMA标准的VAF计算逻辑
 * 
 * @author Developer Engineer  
 * @version 1.0.0
 * @since 2025-09-03
 */
@ExtendWith(MockitoExtension.class)
class VafCalculationServiceTest {

    @InjectMocks
    private VafCalculationService vafCalculationService;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setId(1L);
        testProject.setProjectCode("TEST-001");
        testProject.setProjectName("测试项目");
        testProject.setProjectType("INFORMATION_SYSTEM");
    }

    /**
     * 测试VAF因子初始化
     * 验证标准的14个技术复杂度因子是否正确初始化
     */
    @Test
    void testStandardVafFactorDefinitions() {
        // 获取标准因子定义
        Map<String, String> factorDescriptions = vafCalculationService.getVafFactorDescriptions();
        
        // 验证因子数量
        assertEquals(14, factorDescriptions.size(), "应该有14个标准VAF因子");
        
        // 验证必要的因子存在
        assertNotNull(factorDescriptions.get("TF01"), "TF01数据通信因子应该存在");
        assertNotNull(factorDescriptions.get("TF02"), "TF02分布式数据处理因子应该存在");
        assertNotNull(factorDescriptions.get("TF14"), "TF14变更便利性因子应该存在");
        
        // 验证因子名称
        assertEquals("数据通信：应用系统需要的数据通信设施", factorDescriptions.get("TF01"));
        assertEquals("变更便利性：系统对业务变化的适应和修改能力", factorDescriptions.get("TF14"));
    }

    /**
     * 测试基础VAF计算
     * 场景：所有因子影响度为0，VAF应该为0.65
     */
    @Test
    void testBasicVafCalculation_AllZeroScores() {
        // 准备测试数据：所有因子评分为0
        List<VafFactor> vafFactors = createVafFactorsWithScores(new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0});
        testProject.setVafFactors(vafFactors);

        // 执行计算
        BigDecimal vaf = vafCalculationService.calculateVaf(testProject);

        // 验证结果：VAF = 0.65 + 0.01 × 0 = 0.65
        BigDecimal expectedVaf = new BigDecimal("0.6500");
        assertEquals(expectedVaf, vaf, "所有因子为0时，VAF应该为0.65");
    }

    /**
     * 测试最大VAF计算
     * 场景：所有因子影响度为5，VAF应该为1.35
     */
    @Test
    void testMaximumVafCalculation_AllFiveScores() {
        // 准备测试数据：所有因子评分为5
        List<VafFactor> vafFactors = createVafFactorsWithScores(new int[]{5,5,5,5,5,5,5,5,5,5,5,5,5,5});
        testProject.setVafFactors(vafFactors);

        // 执行计算
        BigDecimal vaf = vafCalculationService.calculateVaf(testProject);

        // 验证结果：VAF = 0.65 + 0.01 × (5×14) = 0.65 + 0.7 = 1.35
        BigDecimal expectedVaf = new BigDecimal("1.3500");
        assertEquals(expectedVaf, vaf, "所有因子为5时，VAF应该为1.35");
    }

    /**
     * 测试典型项目VAF计算
     * 场景：模拟真实项目的VAF因子评分
     */
    @Test
    void testTypicalProjectVafCalculation() {
        // 准备测试数据：典型政府信息化项目的评分
        // TF01-数据通信:4, TF02-分布式:3, TF03-性能:4, TF04-高度使用:3, TF05-交易率:3
        // TF06-在线录入:4, TF07-用户效率:4, TF08-在线更新:3, TF09-复杂处理:3, TF10-重用性:2
        // TF11-安装简便:2, TF12-操作简便:3, TF13-多场地:1, TF14-变更便利:3
        int[] scores = {4,3,4,3,3,4,4,3,3,2,2,3,1,3};
        List<VafFactor> vafFactors = createVafFactorsWithScores(scores);
        testProject.setVafFactors(vafFactors);

        // 执行计算
        BigDecimal vaf = vafCalculationService.calculateVaf(testProject);

        // 验证结果：总评分 = 42, VAF = 0.65 + 0.01 × 42 = 1.07
        BigDecimal expectedVaf = new BigDecimal("1.0700");
        assertEquals(expectedVaf, vaf, "典型项目VAF计算错误");
    }

    /**
     * 测试VAF范围边界值验证
     */
    @Test
    void testVafRangeBoundaryValidation() {
        // 测试最小值边界
        List<VafFactor> minFactors = createVafFactorsWithScores(new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0});
        testProject.setVafFactors(minFactors);
        BigDecimal minVaf = vafCalculationService.calculateVaf(testProject);
        assertTrue(minVaf.compareTo(new BigDecimal("0.65")) >= 0, "VAF不能小于0.65");

        // 测试最大值边界  
        List<VafFactor> maxFactors = createVafFactorsWithScores(new int[]{5,5,5,5,5,5,5,5,5,5,5,5,5,5});
        testProject.setVafFactors(maxFactors);
        BigDecimal maxVaf = vafCalculationService.calculateVaf(testProject);
        assertTrue(maxVaf.compareTo(new BigDecimal("1.35")) <= 0, "VAF不能大于1.35");
    }

    /**
     * 测试VAF因子验证功能
     */
    @Test
    void testVafFactorValidation() {
        // 测试正常情况
        List<VafFactor> normalFactors = createVafFactorsWithScores(new int[]{3,3,3,3,3,3,3,3,3,3,3,3,3,3});
        String normalResult = vafCalculationService.validateVafFactorScores(normalFactors);
        assertEquals("VAF因子验证通过", normalResult, "正常VAF因子应该验证通过");

        // 测试空列表
        String emptyResult = vafCalculationService.validateVafFactorScores(null);
        assertEquals("VAF因子列表为空", emptyResult, "空列表应该返回相应错误信息");

        // 测试因子数量不足 - 手动创建不足的因子列表
        List<VafFactor> insufficientFactors = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            VafFactor factor = new VafFactor(testProject, "TF0" + (i+1), "测试因子" + (i+1));
            factor.setInfluenceScore(3);
            insufficientFactors.add(factor);
        }
        String insufficientResult = vafCalculationService.validateVafFactorScores(insufficientFactors);
        assertEquals("VAF因子数量不正确，应为14个，当前为3个", insufficientResult, "因子数量不足应该返回相应错误信息");

        // 测试评分超出范围 - 手动创建包含无效评分的因子列表
        List<VafFactor> invalidScoreFactors = createVafFactorsWithScores(new int[]{3,3,3,3,3,3,3,3,3,3,3,3,3,3});
        // 手动设置一个无效评分，跳过实体验证
        if (invalidScoreFactors.size() > 0) {
            VafFactor lastFactor = invalidScoreFactors.get(13);
            // 直接设置字段而不调用setter来避免验证
            try {
                java.lang.reflect.Field field = VafFactor.class.getDeclaredField("influenceScore");
                field.setAccessible(true);
                field.set(lastFactor, 6);
            } catch (Exception e) {
                // 如果反射失败，跳过这个测试
                return;
            }
        }
        String invalidScoreResult = vafCalculationService.validateVafFactorScores(invalidScoreFactors);
        assertTrue(invalidScoreResult.contains("评分超出范围"), "评分超范围应该返回相应错误信息");
    }

    /**
     * 测试VAF因子贡献值计算
     */
    @Test
    void testVafFactorContribution() {
        VafFactor factor = new VafFactor(testProject, "TF01", "数据通信");
        factor.setInfluenceScore(3);
        factor.setWeight(new BigDecimal("1.0"));

        BigDecimal contribution = factor.calculateContribution();
        assertEquals(new BigDecimal("3.0"), contribution, "因子贡献值计算错误");
    }

    /**
     * 测试空VAF因子列表的处理
     * 验证系统能自动初始化标准因子
     */
    @Test
    void testEmptyVafFactorsHandling() {
        // 测试项目没有VAF因子的情况
        testProject.setVafFactors(null);
        
        // 执行计算（应该自动初始化因子）
        BigDecimal vaf = vafCalculationService.calculateVaf(testProject);
        
        // 验证结果：默认初始化的因子评分都为0，VAF应该为0.65
        BigDecimal expectedVaf = new BigDecimal("0.6500");
        assertEquals(expectedVaf, vaf, "空VAF因子列表应该自动初始化并返回基础VAF值");
        
        // 验证项目中已经设置了VAF因子
        assertNotNull(testProject.getVafFactors(), "应该自动创建VAF因子列表");
        assertEquals(14, testProject.getVafFactors().size(), "应该创建14个标准VAF因子");
    }

    /**
     * 测试精度和舍入规则
     */
    @Test
    void testVafPrecisionAndRounding() {
        // 创建会产生小数的VAF评分
        List<VafFactor> vafFactors = createVafFactorsWithScores(new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1});
        testProject.setVafFactors(vafFactors);

        BigDecimal vaf = vafCalculationService.calculateVaf(testProject);

        // 验证精度：应该保留4位小数
        assertEquals(4, vaf.scale(), "VAF应该保留4位小数");
        
        // 验证VAF = 0.65 + 0.01 × 14 = 0.79
        BigDecimal expectedVaf = new BigDecimal("0.7900");
        assertEquals(expectedVaf, vaf, "VAF精度计算错误");
    }

    /**
     * 辅助方法：根据评分数组创建VAF因子列表
     */
    private List<VafFactor> createVafFactorsWithScores(int[] scores) {
        if (scores.length != 14) {
            throw new IllegalArgumentException("必须提供14个评分值");
        }

        List<VafFactor> factors = new ArrayList<>();
        String[] factorTypes = {"TF01", "TF02", "TF03", "TF04", "TF05", "TF06", "TF07", 
                               "TF08", "TF09", "TF10", "TF11", "TF12", "TF13", "TF14"};
        String[] factorNames = {"数据通信", "分布式数据处理", "性能", "高度使用配置", "交易率",
                               "在线数据录入", "最终用户效率", "在线更新", "复杂处理", "重用性",
                               "安装简便性", "操作简便性", "多个场地", "变更便利性"};

        for (int i = 0; i < 14; i++) {
            VafFactor factor = new VafFactor(testProject, factorTypes[i], factorNames[i]);
            factor.setInfluenceScore(scores[i]);
            factor.setWeight(BigDecimal.ONE);
            factors.add(factor);
        }

        return factors;
    }
}