# BigDecimal数据精度规格说明书

## 📋 文档信息
- **文档类型：** 技术规格说明书 (Technical Specification)
- **适用范围：** NESMA核心计算引擎
- **创建时间：** 2025-09-03
- **创建者：** Product Owner
- **审核者：** Developer Engineer (待审核)
- **质量等级：** 政府级项目 - 零误差要求

## 🎯 精度要求概述

### 核心原则
基于政府投资信息化项目的严格要求，所有数值计算必须：
1. **绝对精度** - 零浮点精度误差
2. **可重现性** - 相同输入产生完全相同的输出
3. **审计可追溯** - 每个计算步骤都可验证
4. **标准合规** - 完全符合《长沙市财政评审中心政府投资信息化项目评审指南》

## 🔢 BigDecimal配置要求

### 基础配置参数
```java
/**
 * NESMA计算专用BigDecimal配置
 * 政府项目专用 - 不得随意修改
 */
public class NesmaPrecisionConfig {
    
    /** 
     * 标准精度：小数点后4位
     * 足以处理功能点计算和成本估算的所有场景
     */
    public static final int STANDARD_SCALE = 4;
    
    /**
     * 高精度：小数点后8位
     * 用于中间计算，避免精度丢失
     */
    public static final int HIGH_PRECISION_SCALE = 8;
    
    /**
     * 标准舍入模式：四舍五入
     * 符合财政计算惯例
     */
    public static final RoundingMode STANDARD_ROUNDING = RoundingMode.HALF_UP;
    
    /**
     * 创建标准精度BigDecimal
     */
    public static BigDecimal createStandard(String value) {
        return new BigDecimal(value).setScale(STANDARD_SCALE, STANDARD_ROUNDING);
    }
    
    /**
     * 创建高精度BigDecimal（中间计算用）
     */
    public static BigDecimal createHighPrecision(String value) {
        return new BigDecimal(value).setScale(HIGH_PRECISION_SCALE, STANDARD_ROUNDING);
    }
}
```

### 精度等级分类

#### 1. 标准精度场景 (Scale=4)
**适用范围：**
- 最终功能点结果
- 成本估算结果  
- 调整因子计算结果
- 用户界面展示数据

**配置示例：**
```java
BigDecimal finalResult = calculation.setScale(4, RoundingMode.HALF_UP);
```

#### 2. 高精度场景 (Scale=8)
**适用范围：**
- 中间计算过程
- 复杂调整因子计算
- 多步骤计算的累积误差控制

**配置示例：**
```java
BigDecimal intermediateResult = calculation.setScale(8, RoundingMode.HALF_UP);
```

#### 3. 整数场景 (Scale=0)
**适用范围：**
- 功能点数量统计
- DET/RET/FTR计数
- 复杂度等级枚举值

**配置示例：**
```java
BigDecimal countValue = new BigDecimal(count).setScale(0, RoundingMode.UNNECESSARY);
```

## 📊 具体计算场景精度配置

### 功能点基础计算
```java
/**
 * ILF功能点计算示例
 * 确保每个步骤都使用正确的精度
 */
public BigDecimal calculateILFPoints() {
    // 1. 数量统计 - 整数精度
    BigDecimal simpleCount = new BigDecimal("3").setScale(0);
    BigDecimal averageCount = new BigDecimal("2").setScale(0);
    BigDecimal complexCount = new BigDecimal("1").setScale(0);
    
    // 2. 权重应用 - 标准精度
    BigDecimal simpleWeight = new BigDecimal("7.0000").setScale(STANDARD_SCALE);
    BigDecimal averageWeight = new BigDecimal("10.0000").setScale(STANDARD_SCALE);
    BigDecimal complexWeight = new BigDecimal("15.0000").setScale(STANDARD_SCALE);
    
    // 3. 中间计算 - 高精度
    BigDecimal simplePoints = simpleCount.multiply(simpleWeight)
        .setScale(HIGH_PRECISION_SCALE, STANDARD_ROUNDING);
    BigDecimal averagePoints = averageCount.multiply(averageWeight)
        .setScale(HIGH_PRECISION_SCALE, STANDARD_ROUNDING);
    BigDecimal complexPoints = complexCount.multiply(complexWeight)
        .setScale(HIGH_PRECISION_SCALE, STANDARD_ROUNDING);
    
    // 4. 最终结果 - 标准精度
    return simplePoints.add(averagePoints).add(complexPoints)
        .setScale(STANDARD_SCALE, STANDARD_ROUNDING);
}
```

### UFP计算精度控制
```java
/**
 * 未调整功能点(UFP)计算
 * 每种功能点类型保持独立精度控制
 */
public BigDecimal calculateUFP(FunctionPointSummary summary) {
    // 各功能点类型计算结果都使用标准精度
    BigDecimal ilfPoints = calculateILFPoints().setScale(STANDARD_SCALE, STANDARD_ROUNDING);
    BigDecimal eifPoints = calculateEIFPoints().setScale(STANDARD_SCALE, STANDARD_ROUNDING);
    BigDecimal eiPoints = calculateEIPoints().setScale(STANDARD_SCALE, STANDARD_ROUNDING);
    BigDecimal eoPoints = calculateEOPoints().setScale(STANDARD_SCALE, STANDARD_ROUNDING);
    BigDecimal eqPoints = calculateEQPoints().setScale(STANDARD_SCALE, STANDARD_ROUNDING);
    
    // UFP总计算 - 标准精度
    return ilfPoints.add(eifPoints).add(eiPoints).add(eoPoints).add(eqPoints)
        .setScale(STANDARD_SCALE, STANDARD_ROUNDING);
}
```

### 调整因子计算精度控制
```java
/**
 * 技术复杂度调整因子(VAF)计算
 * 精度要求特别严格
 */
public BigDecimal calculateVAF(List<ComplexityFactor> factors) {
    // 基础值 - 高精度
    BigDecimal baseValue = new BigDecimal("0.65000000").setScale(HIGH_PRECISION_SCALE);
    
    // 因子总和计算 - 整数精度转高精度
    int totalInfluence = factors.stream().mapToInt(ComplexityFactor::getScore).sum();
    BigDecimal influenceDecimal = new BigDecimal(totalInfluence).setScale(HIGH_PRECISION_SCALE);
    
    // 调整系数 - 高精度
    BigDecimal adjustmentCoeff = new BigDecimal("0.01000000").setScale(HIGH_PRECISION_SCALE);
    
    // VAF计算 - 最终结果标准精度
    BigDecimal vaf = baseValue.add(influenceDecimal.multiply(adjustmentCoeff))
        .setScale(STANDARD_SCALE, STANDARD_ROUNDING);
    
    // VAF范围校验 [0.65, 1.35]
    BigDecimal minVAF = new BigDecimal("0.6500").setScale(STANDARD_SCALE);
    BigDecimal maxVAF = new BigDecimal("1.3500").setScale(STANDARD_SCALE);
    
    if (vaf.compareTo(minVAF) < 0 || vaf.compareTo(maxVAF) > 0) {
        throw new IllegalArgumentException("VAF超出标准范围[0.65, 1.35]: " + vaf);
    }
    
    return vaf;
}
```

### 复用度调整精度控制
```java
/**
 * 复用度调整计算
 * 分数计算需要特别注意精度
 */
public BigDecimal applyReuseAdjustment(BigDecimal afp, ReuseLevel level) {
    BigDecimal reuseFactor;
    
    switch (level) {
        case HIGH:
            // 高复用：1/3 = 0.3333
            reuseFactor = BigDecimal.ONE.divide(new BigDecimal("3"), 
                HIGH_PRECISION_SCALE, STANDARD_ROUNDING);
            break;
        case MEDIUM:
            // 中复用：2/3 = 0.6667
            reuseFactor = new BigDecimal("2").divide(new BigDecimal("3"),
                HIGH_PRECISION_SCALE, STANDARD_ROUNDING);
            break;
        case LOW:
            // 低复用：1 = 1.0000
            reuseFactor = BigDecimal.ONE.setScale(HIGH_PRECISION_SCALE);
            break;
        default:
            throw new IllegalArgumentException("不支持的复用等级: " + level);
    }
    
    // 最终结果 - 标准精度
    return afp.multiply(reuseFactor).setScale(STANDARD_SCALE, STANDARD_ROUNDING);
}
```

## 🧪 精度验证测试用例

### 测试用例1：基础精度验证
```java
@Test
public void testBasicPrecisionControl() {
    // 测试数据
    BigDecimal input = new BigDecimal("123.456789");
    
    // 标准精度测试
    BigDecimal standardResult = input.setScale(STANDARD_SCALE, STANDARD_ROUNDING);
    assertEquals("123.4568", standardResult.toString());
    
    // 高精度测试
    BigDecimal highPrecisionResult = input.setScale(HIGH_PRECISION_SCALE, STANDARD_ROUNDING);
    assertEquals("123.45678900", highPrecisionResult.toString());
}
```

### 测试用例2：累积误差控制验证
```java
@Test
public void testAccumulationErrorControl() {
    // 模拟多次小数计算的累积
    BigDecimal sum = BigDecimal.ZERO;
    
    for (int i = 0; i < 1000; i++) {
        BigDecimal value = new BigDecimal("0.001").setScale(HIGH_PRECISION_SCALE);
        sum = sum.add(value);
    }
    
    // 最终结果应该精确等于1.000
    BigDecimal finalResult = sum.setScale(STANDARD_SCALE, STANDARD_ROUNDING);
    assertEquals("1.0000", finalResult.toString());
}
```

### 测试用例3：PDF指南案例精度验证
```java
@Test
public void testPDFGuidelineCasePrecision() {
    // 基于PDF指南实际案例的精度验证
    // 案例：UFP=129, VAF=0.97, AFP=125.13
    
    BigDecimal ufp = new BigDecimal("129.0000").setScale(STANDARD_SCALE);
    BigDecimal vaf = new BigDecimal("0.9700").setScale(STANDARD_SCALE);
    
    BigDecimal afp = ufp.multiply(vaf).setScale(STANDARD_SCALE, STANDARD_ROUNDING);
    
    // 验证结果必须精确匹配PDF指南
    assertEquals("125.1300", afp.toString());
}
```

## 📋 数据库精度配置

### JPA实体配置
```java
@Entity
@Table(name = "calculation_results")
public class CalculationResult {
    
    /**
     * 功能点相关字段 - 标准精度
     */
    @Column(name = "ufp_points", precision = 19, scale = 4, nullable = false)
    private BigDecimal ufpPoints;
    
    @Column(name = "afp_points", precision = 19, scale = 4, nullable = false)
    private BigDecimal afpPoints;
    
    @Column(name = "final_points", precision = 19, scale = 4, nullable = false)
    private BigDecimal finalPoints;
    
    /**
     * 调整因子 - 标准精度
     */
    @Column(name = "vaf_factor", precision = 19, scale = 4, nullable = false)
    private BigDecimal vafFactor;
    
    /**
     * 复用调整因子 - 高精度存储
     */
    @Column(name = "reuse_factor", precision = 19, scale = 8, nullable = false)
    private BigDecimal reuseFactor;
    
    /**
     * 成本相关 - 标准精度
     */
    @Column(name = "development_cost", precision = 19, scale = 4)
    private BigDecimal developmentCost;
}
```

### PostgreSQL数据类型映射
```sql
-- 标准精度字段
ufp_points DECIMAL(19,4) NOT NULL,
afp_points DECIMAL(19,4) NOT NULL,
final_points DECIMAL(19,4) NOT NULL,
vaf_factor DECIMAL(19,4) NOT NULL,

-- 高精度字段  
reuse_factor DECIMAL(19,8) NOT NULL,

-- 成本字段
development_cost DECIMAL(19,4),

-- 约束检查
CONSTRAINT chk_vaf_range CHECK (vaf_factor >= 0.6500 AND vaf_factor <= 1.3500),
CONSTRAINT chk_positive_points CHECK (ufp_points >= 0 AND afp_points >= 0 AND final_points >= 0)
```

## 🚨 常见精度陷阱和预防措施

### 陷阱1：浮点数隐式转换
```java
// ❌ 错误：会产生浮点精度误差
BigDecimal wrong = new BigDecimal(0.1);

// ✅ 正确：使用字符串构造器
BigDecimal correct = new BigDecimal("0.1");
```

### 陷阱2：除法运算未指定精度
```java
// ❌ 错误：可能抛出ArithmeticException
BigDecimal result = dividend.divide(divisor);

// ✅ 正确：明确指定精度和舍入模式
BigDecimal result = dividend.divide(divisor, STANDARD_SCALE, STANDARD_ROUNDING);
```

### 陷阱3：比较运算使用equals
```java
// ❌ 错误：精度不同时会返回false
if (value1.equals(value2)) { ... }

// ✅ 正确：使用compareTo进行数值比较
if (value1.compareTo(value2) == 0) { ... }
```

### 陷阱4：JSON序列化精度丢失
```java
// ✅ 配置Jackson正确处理BigDecimal
@JsonSerialize(using = BigDecimalSerializer.class)
@JsonDeserialize(using = BigDecimalDeserializer.class)
@Column(precision = 19, scale = 4)
private BigDecimal calculatedValue;
```

## 📊 性能优化建议

### BigDecimal性能优化
1. **缓存常用值** - 缓存0, 1, 10等常用BigDecimal实例
2. **避免重复创建** - 复用已有的BigDecimal对象
3. **精度按需设置** - 根据场景选择合适的精度等级
4. **批量计算优化** - 减少频繁的精度转换操作

### 内存使用优化
```java
/**
 * BigDecimal常量池
 * 避免重复创建相同的BigDecimal对象
 */
public class BigDecimalConstants {
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(STANDARD_SCALE);
    public static final BigDecimal ONE = BigDecimal.ONE.setScale(STANDARD_SCALE);
    public static final BigDecimal TEN = BigDecimal.TEN.setScale(STANDARD_SCALE);
    
    // 功能点权重常量
    public static final BigDecimal ILF_SIMPLE = new BigDecimal("7.0000");
    public static final BigDecimal ILF_AVERAGE = new BigDecimal("10.0000");
    public static final BigDecimal ILF_COMPLEX = new BigDecimal("15.0000");
    
    // VAF相关常量
    public static final BigDecimal VAF_BASE = new BigDecimal("0.6500");
    public static final BigDecimal VAF_COEFFICIENT = new BigDecimal("0.0100");
}
```

## ✅ 验收检查清单

### 开发阶段检查
- [ ] 所有数值计算都使用BigDecimal而非float/double
- [ ] 精度配置严格按照本规格执行
- [ ] 舍入模式统一使用HALF_UP
- [ ] 数据库字段精度配置正确

### 测试阶段检查  
- [ ] PDF指南案例精度验证100%通过
- [ ] 边界条件精度测试通过
- [ ] 累积误差控制测试通过
- [ ] 性能测试满足要求

### 部署阶段检查
- [ ] 生产环境BigDecimal配置正确
- [ ] 数据库精度设置验证
- [ ] JSON序列化/反序列化精度保持
- [ ] 审计日志记录计算精度信息

## 🔍 监控和审计要求

### 精度监控指标
1. **计算精度偏差** - 监控计算结果与预期的精度偏差
2. **性能影响** - 监控BigDecimal对系统性能的影响
3. **内存使用** - 监控BigDecimal对象的内存占用
4. **错误率** - 监控精度相关的计算错误

### 审计日志要求
```java
/**
 * 精度审计日志示例
 */
public class PrecisionAuditLogger {
    
    public void logCalculation(String operation, BigDecimal input, BigDecimal output, int scale) {
        AuditLog.info()
            .setOperation(operation)
            .setInputValue(input.toString())
            .setOutputValue(output.toString())
            .setScale(scale)
            .setRoundingMode(STANDARD_ROUNDING.toString())
            .setTimestamp(LocalDateTime.now())
            .log("NESMA计算精度审计");
    }
}
```

---

**文档维护者：** Product Owner  
**技术审核：** Developer Engineer (待审核)  
**最后更新：** 2025-09-03  
**重要提醒：** 政府项目精度要求，任何偏差都可能影响项目验收！