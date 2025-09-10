# NESMA功能点评估算法 - UI测试用例

**项目名称**: 长沙市财政评审中心软件规模评估系统  
**测试版本**: Sprint 2 NESMA核心算法验证  
**测试工具**: Chrome MCP Server + 手工验证  
**制定时间**: 2025年9月7日  
**核心目标**: 验证NESMA功能点评估算法的准确性和政府标准合规性

---

## 🎯 测试核心目标

### 关键验收标准
- ✅ **100%准确性** - 所有计算结果与《长沙市财政评审中心政府投资信息化项目评审指南》完全一致
- ✅ **零偏差容忍** - 不允许任何数值误差或舍入偏差
- ✅ **全场景覆盖** - 覆盖所有5种功能点类型和复杂度组合
- ✅ **边界条件验证** - 所有边界值判定必须准确
- ✅ **BigDecimal精度** - 保持4位小数精度

---

## 🧪 核心算法测试场景

### 场景一：ILF（内部逻辑文件）功能点计算验证

**测试目标**: 验证ILF功能点识别和计算的100%准确性

#### 测试步骤
1. **导航到NESMA计算页面**
   ```javascript
   await chrome.navigate('http://localhost:5173/nesma-calculate');
   await chrome.waitForElement('.nesma-calculator');
   ```

2. **测试ILF简单复杂度计算**
   ```javascript
   // 测试数据：DET=15, RET=1, 预期结果：简单(7点)
   await chrome.fill('input[data-testid="ilf-det"]', '15');
   await chrome.fill('input[data-testid="ilf-ret"]', '1');
   await chrome.click('button[data-testid="calculate-ilf"]');
   
   // 验证复杂度判定
   const complexity = await chrome.getText('.ilf-complexity-result');
   assert(complexity === '简单');
   
   // 验证功能点数
   const points = await chrome.getText('.ilf-points-result');
   assert(points === '7.0000');
   ```

3. **测试ILF一般复杂度计算**
   ```javascript
   // 测试数据：DET=25, RET=3, 预期结果：一般(10点)
   await chrome.fill('input[data-testid="ilf-det"]', '25');
   await chrome.fill('input[data-testid="ilf-ret"]', '3');
   await chrome.click('button[data-testid="calculate-ilf"]');
   
   const complexity = await chrome.getText('.ilf-complexity-result');
   assert(complexity === '一般');
   
   const points = await chrome.getText('.ilf-points-result');
   assert(points === '10.0000');
   ```

4. **测试ILF复杂复杂度计算**
   ```javascript
   // 测试数据：DET=55, RET=8, 预期结果：复杂(15点)
   await chrome.fill('input[data-testid="ilf-det"]', '55');
   await chrome.fill('input[data-testid="ilf-ret"]', '8');
   await chrome.click('button[data-testid="calculate-ilf"]');
   
   const complexity = await chrome.getText('.ilf-complexity-result');
   assert(complexity === '复杂');
   
   const points = await chrome.getText('.ilf-points-result');
   assert(points === '15.0000');
   ```

**验收标准**: 
- ✅ 复杂度判定100%准确
- ✅ 功能点计算100%准确
- ✅ BigDecimal精度保持4位小数

---

### 场景二：EIF（外部接口文件）功能点计算验证

**测试目标**: 验证EIF功能点识别和计算的准确性

#### 测试步骤
1. **切换到EIF计算界面**
   ```javascript
   await chrome.click('tab[data-testid="eif-tab"]');
   await chrome.waitForElement('.eif-calculator');
   ```

2. **测试EIF各复杂度计算**
   ```javascript
   // 简单EIF: DET=12, RET=1, 预期：简单(5点)
   await chrome.fill('input[data-testid="eif-det"]', '12');
   await chrome.fill('input[data-testid="eif-ret"]', '1');
   await chrome.click('button[data-testid="calculate-eif"]');
   assert(await chrome.getText('.eif-points-result') === '5.0000');
   
   // 一般EIF: DET=30, RET=4, 预期：一般(7点)
   await chrome.fill('input[data-testid="eif-det"]', '30');
   await chrome.fill('input[data-testid="eif-ret"]', '4');
   await chrome.click('button[data-testid="calculate-eif"]');
   assert(await chrome.getText('.eif-points-result') === '7.0000');
   
   // 复杂EIF: DET=60, RET=10, 预期：复杂(10点)
   await chrome.fill('input[data-testid="eif-det"]', '60');
   await chrome.fill('input[data-testid="eif-ret"]', '10');
   await chrome.click('button[data-testid="calculate-eif"]');
   assert(await chrome.getText('.eif-points-result') === '10.0000');
   ```

---

### 场景三：EI（外部输入）功能点计算验证

**测试目标**: 验证EI功能点识别和计算的准确性

#### 测试步骤
```javascript
await chrome.click('tab[data-testid="ei-tab"]');

// 简单EI: DET=10, FTR=1, 预期：简单(3点)
await chrome.fill('input[data-testid="ei-det"]', '10');
await chrome.fill('input[data-testid="ei-ftr"]', '1');
await chrome.click('button[data-testid="calculate-ei"]');
assert(await chrome.getText('.ei-points-result') === '3.0000');

// 一般EI: DET=18, FTR=2, 预期：一般(4点)
await chrome.fill('input[data-testid="ei-det"]', '18');
await chrome.fill('input[data-testid="ei-ftr"]', '2');
await chrome.click('button[data-testid="calculate-ei"]');
assert(await chrome.getText('.ei-points-result') === '4.0000');

// 复杂EI: DET=25, FTR=5, 预期：复杂(6点)
await chrome.fill('input[data-testid="ei-det"]', '25');
await chrome.fill('input[data-testid="ei-ftr"]', '5');
await chrome.click('button[data-testid="calculate-ei"]');
assert(await chrome.getText('.ei-points-result') === '6.0000');
```

---

### 场景四：EO（外部输出）功能点计算验证

**测试目标**: 验证EO功能点识别和计算的准确性

#### 测试步骤
```javascript
await chrome.click('tab[data-testid="eo-tab"]');

// 简单EO: DET=15, FTR=1, 预期：简单(4点)
await chrome.fill('input[data-testid="eo-det"]', '15');
await chrome.fill('input[data-testid="eo-ftr"]', '1');
await chrome.click('button[data-testid="calculate-eo"]');
assert(await chrome.getText('.eo-points-result') === '4.0000');

// 一般EO: DET=22, FTR=3, 预期：一般(5点)
await chrome.fill('input[data-testid="eo-det"]', '22');
await chrome.fill('input[data-testid="eo-ftr"]', '3');
await chrome.click('button[data-testid="calculate-eo"]');
assert(await chrome.getText('.eo-points-result') === '5.0000');

// 复杂EO: DET=30, FTR=6, 预期：复杂(7点)
await chrome.fill('input[data-testid="eo-det"]', '30');
await chrome.fill('input[data-testid="eo-ftr"]', '6');
await chrome.click('button[data-testid="calculate-eo"]');
assert(await chrome.getText('.eo-points-result') === '7.0000');
```

---

### 场景五：EQ（外部查询）功能点计算验证

**测试目标**: 验证EQ功能点识别和计算的准确性

#### 测试步骤
```javascript
await chrome.click('tab[data-testid="eq-tab"]');

// 简单EQ: DET=12, FTR=1, 预期：简单(3点)
await chrome.fill('input[data-testid="eq-det"]', '12');
await chrome.fill('input[data-testid="eq-ftr"]', '1');
await chrome.click('button[data-testid="calculate-eq"]');
assert(await chrome.getText('.eq-points-result') === '3.0000');

// 一般EQ: DET=20, FTR=3, 预期：一般(4点)
await chrome.fill('input[data-testid="eq-det"]', '20');
await chrome.fill('input[data-testid="eq-ftr"]', '3');
await chrome.click('button[data-testid="calculate-eq"]');
assert(await chrome.getText('.eq-points-result') === '4.0000');

// 复杂EQ: DET=28, FTR=5, 预期：复杂(6点)
await chrome.fill('input[data-testid="eq-det"]', '28');
await chrome.fill('input[data-testid="eq-ftr"]', '5');
await chrome.click('button[data-testid="calculate-eq"]');
assert(await chrome.getText('.eq-points-result') === '6.0000');
```

---

## 🔍 边界条件测试场景

### 场景六：复杂度判定边界值测试

**测试目标**: 验证复杂度判定矩阵的边界条件处理

#### ILF边界测试
```javascript
await chrome.click('tab[data-testid="ilf-tab"]');

// 边界测试：DET=19, RET=1 -> 简单(7点)
await chrome.fill('input[data-testid="ilf-det"]', '19');
await chrome.fill('input[data-testid="ilf-ret"]', '1');
await chrome.click('button[data-testid="calculate-ilf"]');
assert(await chrome.getText('.ilf-complexity-result') === '简单');
assert(await chrome.getText('.ilf-points-result') === '7.0000');

// 边界测试：DET=20, RET=1 -> 简单(7点) 
await chrome.fill('input[data-testid="ilf-det"]', '20');
await chrome.fill('input[data-testid="ilf-ret"]', '1');
await chrome.click('button[data-testid="calculate-ilf"]');
assert(await chrome.getText('.ilf-complexity-result') === '简单');
assert(await chrome.getText('.ilf-points-result') === '7.0000');

// 边界测试：DET=20, RET=2 -> 一般(10点)
await chrome.fill('input[data-testid="ilf-det"]', '20');
await chrome.fill('input[data-testid="ilf-ret"]', '2');
await chrome.click('button[data-testid="calculate-ilf"]');
assert(await chrome.getText('.ilf-complexity-result') === '一般');
assert(await chrome.getText('.ilf-points-result') === '10.0000');

// 边界测试：DET=50, RET=6 -> 复杂(15点)
await chrome.fill('input[data-testid="ilf-det"]', '50');
await chrome.fill('input[data-testid="ilf-ret"]', '6');
await chrome.click('button[data-testid="calculate-ilf"]');
assert(await chrome.getText('.ilf-complexity-result') === '复杂');
assert(await chrome.getText('.ilf-points-result') === '15.0000');

// 边界测试：DET=51, RET=6 -> 复杂(15点)
await chrome.fill('input[data-testid="ilf-det"]', '51');
await chrome.fill('input[data-testid="ilf-ret"]', '6');
await chrome.click('button[data-testid="calculate-ilf"]');
assert(await chrome.getText('.ilf-complexity-result') === '复杂');
assert(await chrome.getText('.ilf-points-result') === '15.0000');
```

**验收标准**:
- ✅ 所有边界值判定100%准确
- ✅ 临界点处理无误差
- ✅ 边界两侧结果正确区分

---

## 🧮 综合计算验证场景

### 场景七：UFP（未调整功能点）综合计算测试

**测试目标**: 验证UFP综合计算的准确性

#### 完整项目计算测试
```javascript
// 导航到项目综合计算页面
await chrome.navigate('http://localhost:5173/nesma-calculate');
await chrome.click('tab[data-testid="comprehensive-calc"]');

// 输入完整项目数据
const projectData = {
  ILF: {simple: 3, average: 2, complex: 1},  // 3×7 + 2×10 + 1×15 = 56
  EIF: {simple: 2, average: 1, complex: 0},  // 2×5 + 1×7 + 0×10 = 17  
  EI:  {simple: 5, average: 3, complex: 2},  // 5×3 + 3×4 + 2×6 = 39
  EO:  {simple: 4, average: 2, complex: 1},  // 4×4 + 2×5 + 1×7 = 33
  EQ:  {simple: 6, average: 1, complex: 0}   // 6×3 + 1×4 + 0×6 = 22
};

// 填入ILF数据
await chrome.fill('input[data-testid="ilf-simple"]', '3');
await chrome.fill('input[data-testid="ilf-average"]', '2');
await chrome.fill('input[data-testid="ilf-complex"]', '1');

// 填入EIF数据
await chrome.fill('input[data-testid="eif-simple"]', '2');
await chrome.fill('input[data-testid="eif-average"]', '1');
await chrome.fill('input[data-testid="eif-complex"]', '0');

// 填入EI数据
await chrome.fill('input[data-testid="ei-simple"]', '5');
await chrome.fill('input[data-testid="ei-average"]', '3');
await chrome.fill('input[data-testid="ei-complex"]', '2');

// 填入EO数据
await chrome.fill('input[data-testid="eo-simple"]', '4');
await chrome.fill('input[data-testid="eo-average"]', '2');
await chrome.fill('input[data-testid="eo-complex"]', '1');

// 填入EQ数据
await chrome.fill('input[data-testid="eq-simple"]', '6');
await chrome.fill('input[data-testid="eq-average"]', '1');
await chrome.fill('input[data-testid="eq-complex"]', '0');

// 执行UFP计算
await chrome.click('button[data-testid="calculate-ufp"]');

// 验证各项计算结果
assert(await chrome.getText('.ilf-total-points') === '56.0000');
assert(await chrome.getText('.eif-total-points') === '17.0000');
assert(await chrome.getText('.ei-total-points') === '39.0000');
assert(await chrome.getText('.eo-total-points') === '33.0000');
assert(await chrome.getText('.eq-total-points') === '22.0000');

// 验证UFP总计
assert(await chrome.getText('.ufp-total') === '167.0000');
```

**预期结果**: UFP = 56+17+39+33+22 = 167.0000

---

### 场景八：VAF（价值调整因子）计算测试

**测试目标**: 验证技术复杂度调整因子计算

#### VAF计算测试
```javascript
await chrome.click('tab[data-testid="vaf-calculation"]');

// 输入14个影响因子评分
const vafFactors = {
  F1: 4,  // 数据通信
  F2: 3,  // 分布式数据处理
  F3: 4,  // 性能
  F4: 3,  // 高度使用的配置
  F5: 3,  // 事务率
  F6: 5,  // 在线数据输入
  F7: 4,  // 最终用户效率
  F8: 3,  // 在线更新
  F9: 4,  // 复杂处理
  F10: 3, // 可重用性
  F11: 2, // 安装简易性
  F12: 3, // 操作简易性
  F13: 2, // 多站点
  F14: 4  // 变更便利性
};

// 填入各影响因子评分
for (let i = 1; i <= 14; i++) {
  await chrome.fill(`input[data-testid="vaf-f${i}"]`, vafFactors[`F${i}`].toString());
}

// 执行VAF计算
await chrome.click('button[data-testid="calculate-vaf"]');

// 验证影响度总和：4+3+4+3+3+5+4+3+4+3+2+3+2+4 = 47
assert(await chrome.getText('.vaf-sum-total') === '47');

// 验证VAF计算：0.65 + 0.01 × 47 = 1.12
assert(await chrome.getText('.vaf-result') === '1.1200');
```

**预期结果**: VAF = 0.65 + 0.01 × 47 = 1.1200

---

### 场景九：AFP（调整功能点）计算测试

**测试目标**: 验证调整功能点的综合计算

#### AFP计算测试
```javascript
await chrome.click('tab[data-testid="afp-calculation"]');

// 使用之前计算的UFP和VAF
// UFP = 167.0000 (来自场景七)
// VAF = 1.1200 (来自场景八)

// 执行AFP计算
await chrome.click('button[data-testid="calculate-afp"]');

// 验证AFP计算：167 × 1.12 = 187.04
assert(await chrome.getText('.afp-result') === '187.0400');
```

**预期结果**: AFP = UFP × VAF = 167.0000 × 1.1200 = 187.0400

---

### 场景十：复用度调整计算测试

**测试目标**: 验证复用度调整的准确计算

#### 复用度调整测试
```javascript
await chrome.click('tab[data-testid="reuse-adjustment"]');

// 使用AFP = 187.0400

// 测试高复用度：187.04 × (1/3) = 62.3467
await chrome.select('select[data-testid="reuse-level"]', 'HIGH');
await chrome.click('button[data-testid="calculate-reuse"]');
assert(await chrome.getText('.reuse-result') === '62.3467');

// 测试中复用度：187.04 × (2/3) = 124.6933  
await chrome.select('select[data-testid="reuse-level"]', 'MEDIUM');
await chrome.click('button[data-testid="calculate-reuse"]');
assert(await chrome.getText('.reuse-result') === '124.6933');

// 测试低复用度：187.04 × 1 = 187.0400
await chrome.select('select[data-testid="reuse-level"]', 'LOW');
await chrome.click('button[data-testid="calculate-reuse"]');
assert(await chrome.getText('.reuse-result') === '187.0400');
```

**预期结果**: 
- 高复用度: 62.3467
- 中复用度: 124.6933  
- 低复用度: 187.0400

---

## 💰 成本评估测试场景

### 场景十一：项目成本计算验证

**测试目标**: 验证基于功能点的项目成本评估

#### 成本计算测试
```javascript
await chrome.click('tab[data-testid="cost-estimation"]');

// 使用最终调整功能点数（假设选择中复用度）
// 功能点数 = 124.6933

// 输入人天单价（政府标准）
await chrome.fill('input[data-testid="daily-rate"]', '1200');

// 输入每功能点人天数（行业标准）
await chrome.fill('input[data-testid="days-per-fp"]', '0.8');

// 执行成本计算
await chrome.click('button[data-testid="calculate-cost"]');

// 验证人天计算：124.6933 × 0.8 = 99.7546
assert(await chrome.getText('.total-days') === '99.7546');

// 验证成本计算：99.7546 × 1200 = 119705.52
assert(await chrome.getText('.total-cost') === '119705.5200');

// 验证成本格式化显示
assert(await chrome.getText('.formatted-cost') === '¥119,705.52');
```

**验收标准**:
- ✅ 成本计算公式应用正确
- ✅ BigDecimal精度保持4位小数
- ✅ 成本显示格式符合财务标准

---

## 📋 完整测试执行清单

### Phase 1: 基础功能点计算验证（15分钟）
- [ ] ILF计算测试（简单、一般、复杂）
- [ ] EIF计算测试（简单、一般、复杂）
- [ ] EI计算测试（简单、一般、复杂）
- [ ] EO计算测试（简单、一般、复杂）
- [ ] EQ计算测试（简单、一般、复杂）

### Phase 2: 边界条件验证（10分钟）
- [ ] ILF边界值测试
- [ ] EIF边界值测试
- [ ] EI边界值测试
- [ ] EO边界值测试
- [ ] EQ边界值测试

### Phase 3: 综合计算验证（15分钟）
- [ ] UFP综合计算测试
- [ ] VAF调整因子计算测试
- [ ] AFP调整功能点计算测试
- [ ] 复用度调整计算测试

### Phase 4: 成本评估验证（5分钟）
- [ ] 项目成本计算测试
- [ ] 成本格式化显示测试

### Phase 5: 精度和一致性验证（5分钟）
- [ ] BigDecimal精度验证
- [ ] 政府指南案例验证
- [ ] 计算过程可追溯性验证

---

## 🎯 测试成功标准

### 算法准确性标准
- ✅ **5种功能点类型计算** 100%准确
- ✅ **复杂度判定逻辑** 100%正确
- ✅ **边界值处理** 100%准确
- ✅ **综合计算结果** 与手工计算100%一致
- ✅ **数值精度** BigDecimal 4位小数精度

### 政府合规性标准
- ✅ **计算逻辑** 完全符合政府评审指南
- ✅ **数据精度** 满足政府项目要求
- ✅ **成本评估** 符合财政评审标准
- ✅ **审计追溯** 计算过程完全可追溯

### 用户体验标准
- ✅ **界面交互** 直观易用
- ✅ **计算响应** 实时更新结果
- ✅ **错误处理** 友好的错误提示
- ✅ **数据验证** 输入数据有效性检查

---

## 🚨 关键验证要点

### 必须通过的测试项
1. **ILF复杂度矩阵判定** - DET和RET边界值100%准确
2. **EIF权重配置** - 简单5点、一般7点、复杂10点
3. **EI/EO/EQ计算公式** - FTR和DET组合判定准确
4. **UFP累加计算** - 各功能点类型求和准确
5. **VAF计算公式** - 0.65 + 0.01 × 影响度总和
6. **AFP最终计算** - UFP × VAF精度保持
7. **复用度调整** - 1/3、2/3、1倍系数准确应用
8. **成本评估算法** - 功能点 × 人天系数 × 单价

### 关键数值验证
- **预期UFP**: 167.0000 
- **预期VAF**: 1.1200
- **预期AFP**: 187.0400
- **预期成本**: ¥119,705.52（中复用度）

---

这才是真正有价值的测试用例！它验证了项目的核心价值——NESMA功能点评估算法的准确性和政府标准的合规性。之前的基础UI测试确实太肤浅了。