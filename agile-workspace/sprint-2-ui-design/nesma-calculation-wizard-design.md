# Sprint 2 - NESMA功能点计算向导界面设计

## 一、设计概述

### 1. 设计目标
- **专业性强：** 严格按照NESMA标准设计计算流程界面
- **操作简化：** 将复杂的功能点计算分解为简单易懂的步骤
- **精度保证：** 确保所有计算结果与政府指南100%一致
- **用户友好：** 提供清晰的指导和实时反馈

### 2. 向导流程架构
```
NESMA功能点计算向导 (6步骤)
├── 第1步：内部逻辑文件 (ILF) 计算
├── 第2步：外部接口文件 (EIF) 计算  
├── 第3步：外部输入 (EI) 计算
├── 第4步：外部输出 (EO) 计算
├── 第5步：外部查询 (EQ) 计算
└── 第6步：计算结果汇总确认
```

## 二、整体界面布局设计

### 1. 向导主框架设计

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         NESMA功能点评估向导                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│ ┌─ 进度指示器 ─────────────────────────────────────────────────────────────┐ │
│ │ ●───●───●───○───○───○  当前步骤: 第3步 / 共6步                         │ │
│ │ ILF  EIF  EI   EO   EQ  汇总                                            │ │
│ │                                                                         │ │
│ │ 📊 实时统计: ILF=45 EIF=23 EI=38 EO=0 EQ=0 | 累计UFP: 106 FP            │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│ ┌─ 当前步骤内容区 ─────────────────────────────────────────────────────────┐ │
│ │                          [ 动态内容区域 ]                               │ │
│ │                        根据当前步骤显示不同内容                          │ │
│ │                                                                         │ │
│ │                          [ 380px 高度固定 ]                              │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│ ┌─ 向导导航区 ─────────────────────────────────────────────────────────────┐ │
│ │                                                                         │ │
│ │ [💾 暂存]   [❓ 帮助]             [◀ 上一步]   [保存并继续 ▶]           │ │
│ │                                                                         │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2. 进度指示器详细设计

```css
/* 进度指示器样式 */
.wizard-progress {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 40px;
  background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
  border-radius: 8px;
  color: white;
}

.step-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
}

.step-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
}

.step-dot.completed {
  background: #16a34a;
  border: 2px solid #16a34a;
}

.step-dot.current {
  background: #ffffff;
  color: #1e40af;
  border: 2px solid #ffffff;
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.3);
}

.step-dot.pending {
  background: transparent;
  border: 2px solid rgba(255, 255, 255, 0.5);
  color: rgba(255, 255, 255, 0.7);
}

.step-connector {
  width: 60px;
  height: 2px;
  background: rgba(255, 255, 255, 0.3);
}

.step-connector.completed {
  background: #16a34a;
}
```

## 三、各步骤界面详细设计

### 第1步：内部逻辑文件 (ILF) 计算界面

```
┌─ 第1步：内部逻辑文件 (ILF) 计算 ────────────────────────────────────────────┐
│                                                                             │
│ ┌─ 说明指导区 ─────────────────────────────────────────────────────────────┐ │
│ │ 📚 内部逻辑文件 (ILF) 说明                                              │ │
│ │ • ILF是应用程序内部维护的一组逻辑相关数据或控制信息                      │ │
│ │ • 典型例子：用户表、权限表、系统配置表、业务数据表                       │ │
│ │ • 复杂度判定：根据DET(数据元素类型)和RET(记录元素类型)数量确定             │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│ ┌─ ILF录入表格 ──────────────────────────────────────────────────────────┐  │
│ │ [+ 添加ILF]                                    已录入: 3个  总计: 45 FP │  │
│ │                                                                        │  │
│ │ │序号│     ILF名称     │描述│DET│RET│复杂度│FP│   操作   │                │  │
│ │ ├──┼───────────────┼──┼─┼─┼────┼─┼─────────┤                │  │
│ │ │1 │用户信息表         │用户│15│3│中等   │10│ [编辑][删除] │                │  │
│ │ │2 │权限角色表         │权限│12│2│中等   │10│ [编辑][删除] │                │  │
│ │ │3 │项目数据表         │项目│25│4│复杂   │15│ [编辑][删除] │                │  │
│ │ │  │                 │   │  │ │      │  │             │                │  │
│ └────────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ┌─ ILF添加/编辑表单 ──────────────────────────────────────────────────────┐ │
│ │ ILF名称: [________________________] 必填                               │ │
│ │ 功能描述: [________________________]                                   │ │
│ │                                                                         │ │
│ │ DET数量: [___] 个  (数据元素类型数量，如字段数)                         │ │
│ │ RET数量: [___] 个  (记录元素类型数量，如子表数量)                       │ │
│ │                                                                         │ │
│ │ 复杂度等级: [自动判定] → 【中等】 功能点数: 10 FP                      │ │
│ │                                                                         │ │
│ │ ┌─ 复杂度判定规则提示 ───────────────────────────────────────────┐       │ │
│ │ │ ILF复杂度判定矩阵:                                          │       │ │
│ │ │     RET数量    1      2-5     6+                          │       │ │
│ │ │ DET数量                                                    │       │ │
│ │ │   1-19        简单    简单    中等                          │       │ │
│ │ │  20-50        简单    中等    复杂                          │       │ │
│ │ │   51+         中等    复杂    复杂                          │       │ │
│ │ │                                                            │       │ │
│ │ │ 功能点值: 简单=7FP, 中等=10FP, 复杂=15FP                   │       │ │
│ │ └────────────────────────────────────────────────────────────┘       │ │
│ │                                                                         │ │
│ │                                      [取消]  [确认添加]                 │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 第2步：外部接口文件 (EIF) 计算界面

```
┌─ 第2步：外部接口文件 (EIF) 计算 ────────────────────────────────────────────┐
│                                                                             │
│ ┌─ 说明指导区 ─────────────────────────────────────────────────────────────┐ │
│ │ 🔗 外部接口文件 (EIF) 说明                                              │ │
│ │ • EIF是应用程序引用的由其他应用程序维护的一组逻辑相关数据                │ │
│ │ • 典型例子：外部系统接口、第三方服务数据、共享数据库表                   │ │
│ │ • 复杂度判定：根据DET(数据元素类型)和RET(记录元素类型)数量确定             │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│ ┌─ EIF录入表格 ──────────────────────────────────────────────────────────┐  │
│ │ [+ 添加EIF]                                    已录入: 2个  总计: 12 FP │  │
│ │                                                                        │  │
│ │ │序号│     EIF名称     │描述│DET│RET│复杂度│FP│   操作   │                │  │
│ │ ├──┼───────────────┼──┼─┼─┼────┼─┼─────────┤                │  │
│ │ │1 │统一认证接口       │SSO │8 │1│简单   │5 │ [编辑][删除] │                │  │
│ │ │2 │财政系统接口       │财政│12│2│简单   │5 │ [编辑][删除] │                │  │
│ │ │  │                 │   │  │ │      │  │             │                │  │
│ └────────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ┌─ 复杂度判定规则 ─────────────────────────────────────────────────────────┐ │
│ │ EIF复杂度判定矩阵 (与ILF相同):                                           │ │
│ │ • 简单 (7 FP): DET≤19且RET=1, 或DET≤19且RET=2-5                       │ │
│ │ • 中等 (10 FP): DET≤19且RET≥6, 或DET=20-50且RET=2-5                   │ │
│ │ • 复杂 (15 FP): DET≥51, 或DET≥20且RET≥6, 或DET≥20且RET=1              │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 第3步：外部输入 (EI) 计算界面

```
┌─ 第3步：外部输入 (EI) 计算 ─────────────────────────────────────────────────┐
│                                                                             │
│ ┌─ 说明指导区 ─────────────────────────────────────────────────────────────┐ │
│ │ ➡️  外部输入 (EI) 说明                                                   │ │
│ │ • EI是应用程序接收来自应用程序边界外部的数据或控制信息的基本过程           │ │
│ │ • 典型例子：数据录入、用户登录、文件上传、参数设置                       │ │
│ │ • 复杂度判定：根据DET和FTR(文件类型引用)数量确定                         │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│ ┌─ EI录入表格 ───────────────────────────────────────────────────────────┐  │
│ │ [+ 添加EI]                                     已录入: 5个  总计: 26 FP │  │
│ │                                                                        │  │
│ │ │序号│     EI名称     │描述  │DET│FTR│复杂度│FP│   操作   │              │  │
│ │ ├──┼──────────────┼────┼─┼─┼────┼─┼─────────┤              │  │
│ │ │1 │用户注册         │注册  │12│2│简单   │3 │ [编辑][删除] │              │  │
│ │ │2 │项目信息录入     │录入  │18│3│中等   │4 │ [编辑][删除] │              │  │
│ │ │3 │文档上传         │上传  │8 │1│简单   │3 │ [编辑][删除] │              │  │
│ │ │4 │数据导入         │导入  │25│4│复杂   │6 │ [编辑][删除] │              │  │
│ │ │5 │参数配置         │配置  │15│3│中等   │4 │ [编辑][删除] │              │  │
│ │ │  │               │     │  │ │      │  │             │              │  │
│ └────────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ┌─ 复杂度判定规则 ─────────────────────────────────────────────────────────┐ │
│ │ EI复杂度判定矩阵:                                                        │ │
│ │     FTR数量    1      2      3+                                         │ │
│ │ DET数量                                                                  │ │
│ │   1-4         简单    简单    中等                                        │ │
│ │   5-15        简单    中等    复杂                                        │ │
│ │   16+         中等    复杂    复杂                                        │ │
│ │                                                                         │ │
│ │ 功能点值: 简单=3FP, 中等=4FP, 复杂=6FP                                  │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 第4步：外部输出 (EO) 计算界面

```
┌─ 第4步：外部输出 (EO) 计算 ─────────────────────────────────────────────────┐
│                                                                             │
│ ┌─ 说明指导区 ─────────────────────────────────────────────────────────────┐ │
│ │ ⬅️  外部输出 (EO) 说明                                                   │ │
│ │ • EO是将数据或控制信息从应用程序内部传送到应用程序边界外部的基本过程       │ │
│ │ • 典型例子：报表生成、数据导出、邮件发送、文件下载                       │ │
│ │ • 复杂度判定：根据DET和FTR(文件类型引用)数量确定                         │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│ ┌─ EO录入表格 ───────────────────────────────────────────────────────────┐  │
│ │ [+ 添加EO]                                     已录入: 4个  总计: 18 FP │  │
│ │                                                                        │  │
│ │ │序号│     EO名称     │描述  │DET│FTR│复杂度│FP│   操作   │              │  │
│ │ ├──┼──────────────┼────┼─┼─┼────┼─┼─────────┤              │  │
│ │ │1 │项目评估报表     │报表  │22│3│中等   │5 │ [编辑][删除] │              │  │
│ │ │2 │数据导出Excel    │导出  │15│2│简单   │4 │ [编辑][删除] │              │  │
│ │ │3 │PDF报告生成      │PDF   │18│4│复杂   │7 │ [编辑][删除] │              │  │
│ │ │4 │统计图表         │图表  │12│1│简单   │4 │ [编辑][删除] │              │  │
│ │ │  │               │     │  │ │      │  │             │              │  │
│ └────────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ┌─ 复杂度判定规则 ─────────────────────────────────────────────────────────┐ │
│ │ EO复杂度判定矩阵:                                                        │ │
│ │     FTR数量    1      2      3+                                         │ │
│ │ DET数量                                                                  │ │
│ │   1-5         简单    简单    中等                                        │ │
│ │   6-19        简单    中等    复杂                                        │ │
│ │   20+         中等    复杂    复杂                                        │ │
│ │                                                                         │ │
│ │ 功能点值: 简单=4FP, 中等=5FP, 复杂=7FP                                  │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 第5步：外部查询 (EQ) 计算界面

```
┌─ 第5步：外部查询 (EQ) 计算 ─────────────────────────────────────────────────┐
│                                                                             │
│ ┌─ 说明指导区 ─────────────────────────────────────────────────────────────┐ │
│ │ 🔍 外部查询 (EQ) 说明                                                   │ │
│ │ • EQ是从应用程序检索数据或控制信息并将其传送到应用程序边界外的基本过程     │ │
│ │ • 典型例子：数据查询、信息检索、状态查看、列表展示                       │ │
│ │ • 复杂度判定：根据DET和FTR(文件类型引用)数量确定                         │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│ ┌─ EQ录入表格 ───────────────────────────────────────────────────────────┐  │
│ │ [+ 添加EQ]                                     已录入: 6个  总计: 21 FP │  │
│ │                                                                        │  │
│ │ │序号│     EQ名称     │描述  │DET│FTR│复杂度│FP│   操作   │              │  │
│ │ ├──┼──────────────┼────┼─┼─┼────┼─┼─────────┤              │  │
│ │ │1 │项目列表查询     │列表  │15│2│简单   │3 │ [编辑][删除] │              │  │
│ │ │2 │用户信息查询     │查询  │12│2│简单   │3 │ [编辑][删除] │              │  │
│ │ │3 │统计数据查询     │统计  │18│3│中等   │4 │ [编辑][删除] │              │  │
│ │ │4 │历史记录查询     │历史  │20│3│中等   │4 │ [编辑][删除] │              │  │
│ │ │5 │高级筛选查询     │筛选  │25│4│复杂   │6 │ [编辑][删除] │              │  │
│ │ │6 │实时状态查询     │实时  │10│1│简单   │3 │ [编辑][删除] │              │  │
│ │ │  │               │     │  │ │      │  │             │              │  │
│ └────────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ┌─ 复杂度判定规则 ─────────────────────────────────────────────────────────┐ │
│ │ EQ复杂度判定矩阵 (与EO相同):                                             │ │
│ │     FTR数量    1      2      3+                                         │ │
│ │ DET数量                                                                  │ │
│ │   1-5         简单    简单    中等                                        │ │
│ │   6-19        简单    中等    复杂                                        │ │
│ │   20+         中等    复杂    复杂                                        │ │
│ │                                                                         │ │
│ │ 功能点值: 简单=3FP, 中等=4FP, 复杂=6FP                                  │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 第6步：计算结果汇总确认界面

```
┌─ 第6步：计算结果汇总确认 ───────────────────────────────────────────────────┐
│                                                                             │
│ ┌─ UFP汇总结果 ──────────────────────────────────────────────────────────┐  │
│ │ 🎯 未调整功能点 (UFP) 计算结果                                          │  │
│ │                                                                        │  │
│ │ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐            │  │
│ │ │   ILF: 3个      │ │   EIF: 2个      │ │   EI: 5个       │            │  │
│ │ │   45 FP         │ │   12 FP         │ │   26 FP         │            │  │
│ │ └─────────────────┘ └─────────────────┘ └─────────────────┘            │  │
│ │                                                                        │  │
│ │ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐            │  │
│ │ │   EO: 4个       │ │   EQ: 6个       │ │   合计          │            │  │
│ │ │   18 FP         │ │   21 FP         │ │   122 FP        │            │  │
│ │ └─────────────────┘ └─────────────────┘ └─────────────────┘            │  │
│ └────────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ┌─ 详细构成表格 ─────────────────────────────────────────────────────────┐   │
│ │ │类型│数量│简单│中等│复杂│简单FP│中等FP│复杂FP│小计FP│               │   │
│ │ ├──┼──┼──┼──┼──┼────┼────┼────┼────┤               │   │
│ │ │ILF │3 │0 │2 │1 │0    │20   │15   │45   │               │   │
│ │ │EIF │2 │2 │0 │0 │14   │0    │0    │14   │               │   │
│ │ │EI  │5 │2 │2 │1 │6    │8    │6    │20   │               │   │
│ │ │EO  │4 │2 │1 │1 │8    │5    │7    │20   │               │   │
│ │ │EQ  │6 │3 │2 │1 │9    │8    │6    │23   │               │   │
│ │ ├──┼──┼──┼──┼──┼────┼────┼────┼────┤               │   │
│ │ │合计│20│9 │7 │4 │37   │41   │34   │122  │               │   │
│ └────────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│ ┌─ 计算验证 ─────────────────────────────────────────────────────────────┐   │
│ │ ✅ 所有必填项目已完成                                                   │   │
│ │ ✅ 复杂度判定规则正确                                                   │   │
│ │ ✅ 功能点计算公式正确                                                   │   │
│ │ ✅ 数值精度处理正确                                                     │   │
│ │                                                                         │   │
│ │ ⚠️  提醒：请仔细检查各功能点的DET和RET/FTR数量是否准确                   │   │
│ │ 📌 建议：可参考PDF指南中的案例进行对比验证                               │   │
│ └─────────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│                [重新检查] [导出明细] [确认完成] [继续VAF调整 ▶]              │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 四、交互设计规范

### 1. 数据录入交互

#### 智能表单设计
```javascript
// 智能表单验证示例
const formValidation = {
  // ILF表单验证规则
  ilf: {
    name: {
      required: true,
      minLength: 2,
      maxLength: 50,
      pattern: /^[\u4e00-\u9fa5a-zA-Z0-9\s_-]+$/,
      message: 'ILF名称为必填项，2-50个字符'
    },
    det: {
      required: true,
      type: 'number',
      min: 1,
      max: 999,
      message: 'DET数量必须为1-999的整数'
    },
    ret: {
      required: true,
      type: 'number',
      min: 1,
      max: 99,
      message: 'RET数量必须为1-99的整数'
    }
  },
  
  // 实时计算复杂度
  calculateComplexity: (type, det, ret_or_ftr) => {
    const rules = {
      'ILF': {
        simple: [[1,19],[1,1]], // DET范围, RET范围
        medium: [[1,19],[2,5], [20,50],[1,1]],
        complex: [[1,19],[6,99], [20,50],[2,99], [51,999],[1,99]]
      },
      'EI': {
        simple: [[1,4],[1,1], [5,15],[1,1]],
        medium: [[1,4],[2,2], [5,15],[2,2], [16,999],[1,1]],
        complex: [[1,4],[3,99], [5,15],[3,99], [16,999],[2,99]]
      }
    }
    
    const typeRules = rules[type]
    if (!typeRules) return 'medium'
    
    for (let level of ['simple', 'medium', 'complex']) {
      const conditions = typeRules[level]
      for (let i = 0; i < conditions.length; i += 2) {
        const [detMin, detMax] = conditions[i]
        const [retMin, retMax] = conditions[i + 1]
        if (det >= detMin && det <= detMax && 
            ret_or_ftr >= retMin && ret_or_ftr <= retMax) {
          return level
        }
      }
    }
    
    return 'medium' // 默认值
  }
}
```

#### 实时计算反馈
```css
/* 计算结果高亮动画 */
.calculation-result {
  transition: all 0.3s ease;
  padding: 8px 12px;
  border-radius: 4px;
  font-weight: 600;
}

.calculation-result.updated {
  background-color: #dbeafe;
  border: 2px solid #3b82f6;
  animation: highlight 0.5s ease-in-out;
}

@keyframes highlight {
  0% {
    box-shadow: 0 0 0 0 rgba(59, 130, 246, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(59, 130, 246, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(59, 130, 246, 0);
  }
}

/* 复杂度等级颜色 */
.complexity-simple { 
  background-color: #d1fae5; 
  color: #065f46; 
}
.complexity-medium { 
  background-color: #fef3c7; 
  color: #92400e; 
}
.complexity-complex { 
  background-color: #fee2e2; 
  color: #991b1b; 
}
```

### 2. 步骤导航交互

#### 步骤切换验证
```javascript
// 步骤切换验证逻辑
const stepValidation = {
  // 检查当前步骤是否可以继续
  canProceed: (currentStep, data) => {
    switch (currentStep) {
      case 1: // ILF步骤
        return data.ilf && data.ilf.length > 0
      case 2: // EIF步骤
        return true // EIF可以为空
      case 3: // EI步骤
        return data.ei && data.ei.length > 0
      case 4: // EO步骤
        return data.eo && data.eo.length > 0
      case 5: // EQ步骤
        return data.eq && data.eq.length > 0
      case 6: // 汇总步骤
        return data.confirmed === true
      default:
        return false
    }
  },
  
  // 获取验证错误信息
  getValidationMessage: (currentStep) => {
    const messages = {
      1: 'ILF为必填项，请至少添加一个内部逻辑文件',
      3: 'EI为必填项，请至少添加一个外部输入',
      4: 'EO为必填项，请至少添加一个外部输出',
      5: 'EQ为必填项，请至少添加一个外部查询',
      6: '请确认计算结果无误后再继续'
    }
    return messages[currentStep] || '请完成当前步骤后继续'
  }
}
```

### 3. 数据暂存机制

#### 自动保存设计
```javascript
// 自动保存功能
const autoSave = {
  interval: 30000, // 30秒自动保存
  
  // 启动自动保存
  start() {
    this.timer = setInterval(() => {
      this.saveData()
    }, this.interval)
  },
  
  // 保存数据到本地存储
  saveData() {
    const wizardData = {
      projectId: this.projectId,
      currentStep: this.currentStep,
      data: this.formData,
      timestamp: Date.now(),
      version: '1.0'
    }
    
    localStorage.setItem(
      `nesma_wizard_${this.projectId}`, 
      JSON.stringify(wizardData)
    )
    
    // 显示保存提示
    this.showSaveNotification()
  },
  
  // 从本地存储恢复数据
  loadData() {
    const saved = localStorage.getItem(`nesma_wizard_${this.projectId}`)
    if (saved) {
      try {
        const wizardData = JSON.parse(saved)
        this.currentStep = wizardData.currentStep
        this.formData = wizardData.data
        return true
      } catch (e) {
        console.error('恢复数据失败:', e)
        return false
      }
    }
    return false
  },
  
  // 清理保存的数据
  clearData() {
    localStorage.removeItem(`nesma_wizard_${this.projectId}`)
  }
}
```

## 五、帮助和指导系统

### 1. 上下文敏感帮助

```vue
<template>
  <div class="help-system">
    <!-- 帮助按钮 -->
    <button class="help-button" @click="showHelp" 
            :class="{ 'has-help': hasContextHelp }">
      <i class="icon-help-circle"></i>
      <span>帮助</span>
      <span v-if="hasContextHelp" class="help-indicator">•</span>
    </button>
    
    <!-- 帮助面板 -->
    <div class="help-panel" v-show="helpVisible">
      <div class="help-header">
        <h3>{{ currentStepName }} - 操作指南</h3>
        <button @click="hideHelp" class="close-button">×</button>
      </div>
      
      <div class="help-content">
        <!-- ILF帮助内容 -->
        <div v-if="currentStep === 1" class="help-section">
          <h4>🏗️ 内部逻辑文件 (ILF) 识别指南</h4>
          
          <div class="help-item">
            <h5>什么是ILF？</h5>
            <p>ILF是应用程序内部维护的一组逻辑相关数据或控制信息，通常对应数据库中的主要业务表。</p>
          </div>
          
          <div class="help-item">
            <h5>典型ILF示例：</h5>
            <ul>
              <li>用户信息表 - 存储用户基本信息</li>
              <li>权限角色表 - 管理系统权限</li>
              <li>项目数据表 - 核心业务数据</li>
              <li>系统配置表 - 系统参数设置</li>
            </ul>
          </div>
          
          <div class="help-item">
            <h5>如何计算DET和RET？</h5>
            <ul>
              <li><strong>DET (数据元素类型):</strong> 表中的字段数量</li>
              <li><strong>RET (记录元素类型):</strong> 表中的子表或记录类型数量</li>
            </ul>
          </div>
          
          <div class="help-example">
            <h5>📋 示例：用户信息表</h5>
            <code>
用户ID, 用户名, 密码, 邮箱, 电话, 姓名, 部门ID, 角色ID, 创建时间, 更新时间
DET = 10 (字段数)
RET = 1 (主记录类型)
复杂度 = 简单 (DET≤19, RET=1)
功能点 = 7 FP
            </code>
          </div>
        </div>
        
        <!-- 更多帮助内容... -->
      </div>
      
      <div class="help-actions">
        <button @click="showDetailedHelp">查看详细文档</button>
        <button @click="showExamples">查看案例库</button>
      </div>
    </div>
  </div>
</template>
```

### 2. 错误处理和提示

```vue
<template>
  <div class="error-handling">
    <!-- 全局错误提示 -->
    <div class="error-banner" v-if="globalError" :class="errorType">
      <div class="error-icon">
        <i :class="errorIconClass"></i>
      </div>
      <div class="error-content">
        <div class="error-title">{{ errorTitle }}</div>
        <div class="error-message">{{ errorMessage }}</div>
        <div class="error-actions" v-if="errorActions.length > 0">
          <button v-for="action in errorActions" 
                  :key="action.name"
                  @click="action.handler"
                  :class="action.class">
            {{ action.label }}
          </button>
        </div>
      </div>
      <button @click="dismissError" class="error-close">×</button>
    </div>
    
    <!-- 字段级错误提示 -->
    <div class="field-error" v-if="fieldError">
      <i class="icon-warning"></i>
      <span>{{ fieldError }}</span>
      <button @click="clearFieldError" class="error-clear">×</button>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      errorTypes: {
        'validation': {
          title: '数据验证错误',
          icon: 'icon-alert-triangle',
          class: 'error-warning'
        },
        'calculation': {
          title: '计算错误',
          icon: 'icon-x-circle',
          class: 'error-danger'
        },
        'network': {
          title: '网络连接错误',
          icon: 'icon-wifi-off',
          class: 'error-info'
        },
        'system': {
          title: '系统错误',
          icon: 'icon-alert-circle',
          class: 'error-danger'
        }
      }
    }
  },
  
  methods: {
    // 显示错误信息
    showError(type, message, actions = []) {
      this.globalError = true
      this.errorType = type
      this.errorMessage = message
      this.errorActions = actions
      
      // 3秒后自动消失
      if (type !== 'system') {
        setTimeout(() => {
          this.dismissError()
        }, 3000)
      }
    },
    
    // 字段验证错误处理
    validateField(field, value, rules) {
      for (let rule of rules) {
        if (rule.required && !value) {
          this.fieldError = rule.message || `${field}为必填项`
          return false
        }
        
        if (rule.pattern && !rule.pattern.test(value)) {
          this.fieldError = rule.message || `${field}格式不正确`
          return false
        }
        
        if (rule.min && value < rule.min) {
          this.fieldError = `${field}不能小于${rule.min}`
          return false
        }
        
        if (rule.max && value > rule.max) {
          this.fieldError = `${field}不能大于${rule.max}`
          return false
        }
      }
      
      this.fieldError = null
      return true
    }
  }
}
</script>
```

## 六、性能优化设计

### 1. 组件懒加载
```javascript
// 步骤组件懒加载
const StepComponents = {
  1: () => import('./components/ILFCalculation.vue'),
  2: () => import('./components/EIFCalculation.vue'),
  3: () => import('./components/EICalculation.vue'),
  4: () => import('./components/EOCalculation.vue'),
  5: () => import('./components/EQCalculation.vue'),
  6: () => import('./components/ResultSummary.vue')
}

// 根据当前步骤动态加载组件
computed: {
  currentStepComponent() {
    return StepComponents[this.currentStep] || null
  }
}
```

### 2. 数据缓存策略
```javascript
// 计算结果缓存
const calculationCache = {
  cache: new Map(),
  
  // 生成缓存键
  generateKey(type, det, ret_or_ftr) {
    return `${type}-${det}-${ret_or_ftr}`
  },
  
  // 获取缓存的计算结果
  get(type, det, ret_or_ftr) {
    const key = this.generateKey(type, det, ret_or_ftr)
    return this.cache.get(key)
  },
  
  // 设置缓存
  set(type, det, ret_or_ftr, result) {
    const key = this.generateKey(type, det, ret_or_ftr)
    this.cache.set(key, result)
  },
  
  // 清理缓存
  clear() {
    this.cache.clear()
  }
}
```

这个NESMA功能点计算向导界面设计提供了完整的6步骤计算流程，包括详细的界面布局、交互设计、验证规则、帮助系统和性能优化方案。设计严格遵循NESMA标准和政府系统要求，确保用户能够高效、准确地完成功能点评估工作。