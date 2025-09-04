package gov.changsha.finance.service;

import gov.changsha.finance.entity.*;
import gov.changsha.finance.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NESMA功能点计算服务简单测试
 * 验证基本计算功能
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NESMA功能点计算服务基础测试")
class NesmaCalculationServiceSimpleTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private VafCalculationService vafCalculationService;

    @InjectMocks
    private NesmaCalculationService nesmaCalculationService;

    private Project testProject;
    private List<FunctionPoint> testFunctionPoints;

    @BeforeEach
    void setUp() {
        // 初始化测试项目
        testProject = new Project();
        testProject.setId(1L);
        testProject.setProjectName("测试项目");
        
        // 初始化测试功能点
        testFunctionPoints = new ArrayList<FunctionPoint>();
        testProject.setFunctionPoints(testFunctionPoints);
        
        // 设置VAF计算服务的默认Mock行为 - 返回1.0 (无调整)
        lenient().when(vafCalculationService.calculateVaf(any(Project.class))).thenReturn(new BigDecimal("1.0000"));
    }

    @Test
    @DisplayName("基本NESMA功能点计算测试")
    void testBasicNesmaCalculation() {
        // 准备测试数据
        addTestFunctionPoint("ILF", "FP001", "用户信息表", 15, 2, 0);
        addTestFunctionPoint("EI", "FP002", "用户登录", 3, 0, 1);
        
        // Mock repository行为
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        // 执行计算
        CalculationResult result = nesmaCalculationService.calculateNesmaFunctionPoints(1L);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getProjectId());
        assertEquals("NESMA_CALCULATION", result.getCalculationType());
        assertEquals("COMPLETED", result.getCalculationStatus());
        
        // 验证功能点总数计算
        // ILF(15 DET, 2 RET) → MEDIUM → 10.0000
        // EI(3 DET, 1 FTR) → LOW → 3.0000
        // 总计: 13.0000
        BigDecimal expectedTotal = new BigDecimal("13.0000");
        assertEquals(0, expectedTotal.compareTo(result.getTotalFunctionPoints()));
        
        // 验证人月数和成本计算
        assertTrue(result.getEstimatedPersonMonths().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(result.getEstimatedCost().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("项目不存在异常测试")
    void testProjectNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            nesmaCalculationService.calculateNesmaFunctionPoints(999L);
        });
        
        assertTrue(exception.getMessage().contains("项目不存在"));
    }

    @Test
    @DisplayName("功能点列表为空异常测试")
    void testEmptyFunctionPointList() {
        testProject.setFunctionPoints(new ArrayList<FunctionPoint>());
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            nesmaCalculationService.calculateNesmaFunctionPoints(1L);
        });
        
        assertTrue(exception.getMessage().contains("项目功能点数据为空"));
    }

    @Test
    @DisplayName("ILF复杂度判定测试")
    void testIlfComplexityDetermination() {
        // LOW复杂度
        addTestFunctionPoint("ILF", "ILF_LOW", "低复杂度ILF", 15, 1, 0);
        // MEDIUM复杂度
        addTestFunctionPoint("ILF", "ILF_MED", "中复杂度ILF", 30, 1, 0);
        // HIGH复杂度
        addTestFunctionPoint("ILF", "ILF_HIGH", "高复杂度ILF", 60, 1, 0);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        CalculationResult result = nesmaCalculationService.calculateNesmaFunctionPoints(1L);
        
        // 验证计算结果: LOW=7 + MEDIUM=10 + HIGH=15 = 32
        BigDecimal expected = new BigDecimal("32.0000");
        assertEquals(0, expected.compareTo(result.getTotalFunctionPoints()));
    }

    @Test
    @DisplayName("EI复杂度判定测试")
    void testEiComplexityDetermination() {
        // LOW复杂度
        addTestFunctionPoint("EI", "EI_LOW", "低复杂度EI", 3, 0, 1);
        // MEDIUM复杂度
        addTestFunctionPoint("EI", "EI_MED", "中复杂度EI", 8, 0, 1);
        // HIGH复杂度
        addTestFunctionPoint("EI", "EI_HIGH", "高复杂度EI", 20, 0, 1);
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        CalculationResult result = nesmaCalculationService.calculateNesmaFunctionPoints(1L);
        
        // 验证计算结果: LOW=3 + MEDIUM=4 + HIGH=6 = 13
        BigDecimal expected = new BigDecimal("13.0000");
        assertEquals(0, expected.compareTo(result.getTotalFunctionPoints()));
    }

    @Test
    @DisplayName("五种功能点类型权重测试")
    void testAllFunctionPointTypes() {
        // 添加五种类型的低复杂度功能点
        addTestFunctionPoint("ILF", "ILF1", "内部逻辑文件", 10, 1, 0);  // LOW = 7
        addTestFunctionPoint("EIF", "EIF1", "外部接口文件", 8, 1, 0);   // LOW = 5
        addTestFunctionPoint("EI", "EI1", "外部输入", 3, 0, 1);        // LOW = 3
        addTestFunctionPoint("EO", "EO1", "外部输出", 4, 0, 1);        // LOW = 4
        addTestFunctionPoint("EQ", "EQ1", "外部查询", 2, 0, 1);        // LOW = 3
        
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        
        CalculationResult result = nesmaCalculationService.calculateNesmaFunctionPoints(1L);
        
        // 验证计算结果: 7+5+3+4+3 = 22
        BigDecimal expected = new BigDecimal("22.0000");
        assertEquals(0, expected.compareTo(result.getTotalFunctionPoints()));
    }

    /**
     * 辅助方法：添加测试功能点
     */
    private void addTestFunctionPoint(String type, String code, String name, 
                                    Integer det, Integer ret, Integer ftr) {
        FunctionPoint fp = new FunctionPoint(code, name, type, 1L);
        fp.setId((long) (testFunctionPoints.size() + 1));
        fp.setDetCount(det);
        fp.setRetCount(ret);
        fp.setFtrCount(ftr);
        fp.setStatus("CONFIRMED");
        testFunctionPoints.add(fp);
    }
}