<template>
  <div class="nesma-calculate-container page-container page-container--application page-container--professional">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button icon="ArrowLeft" @click="handleGoBack">返回项目</el-button>
        <div class="title-info">
          <h1 class="page-title">NESMA功能点计算</h1>
          <p class="project-info">{{ projectInfo?.projectName }} ({{ projectInfo?.projectCode }})</p>
        </div>
      </div>
      <div class="header-right">
        <el-button @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" :loading="calculating" @click="handleCalculate">
          <el-icon><Check /></el-icon>
          开始计算
        </el-button>
      </div>
    </div>

    <!-- 计算步骤指示器 -->
    <div class="steps-section">
      <el-steps :active="currentStep" align-center finish-status="success">
        <el-step title="功能点录入" description="录入ILF、EIF、EI、EO、EQ数据" />
        <el-step title="VAF调整" description="设置14个技术复杂度因子" />
        <el-step title="复用度设置" description="选择项目复用等级" />
        <el-step title="计算结果" description="查看最终计算结果" />
      </el-steps>
    </div>

    <!-- 计算内容 -->
    <div class="calculate-content">
      <!-- 步骤1: 功能点录入 -->
      <el-card v-show="currentStep === 0" class="step-card">
        <template #header>
          <div class="card-header">
            <span>步骤1: 功能点元素录入</span>
            <el-button text @click="showElementHelp = true">
              <el-icon><QuestionFilled /></el-icon>
              计算说明
            </el-button>
          </div>
        </template>
        
        <div class="function-point-input">
          <!-- ILF - 内部逻辑文件 -->
          <div class="element-section">
            <h3>ILF - 内部逻辑文件 (Internal Logical Files)</h3>
            <p class="element-desc">由应用程序维护的逻辑相关数据的用户可识别组</p>
            
            <el-table :data="functionPointData.ilf" style="width: 100%" class="element-table">
              <el-table-column prop="name" label="文件名称" min-width="200">
                <template #default="{ row, $index }">
                  <el-input v-model="row.name" placeholder="请输入文件名称" />
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="250">
                <template #default="{ row, $index }">
                  <el-input v-model="row.description" placeholder="请输入文件描述" />
                </template>
              </el-table-column>
              <el-table-column prop="det" label="DET数" width="100" align="center">
                <template #default="{ row, $index }">
                  <el-input-number v-model="row.det" :min="1" :max="100" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="ret" label="RET数" width="100" align="center">
                <template #default="{ row, $index }">
                  <el-input-number v-model="row.ret" :min="1" :max="50" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="complexity" label="复杂度" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getComplexityType(row.complexity)">
                    {{ row.complexity }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="points" label="功能点" width="100" align="center">
                <template #default="{ row }">
                  <span class="points-value">{{ row.points }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ row, $index }">
                  <el-button size="small" text type="danger" @click="removeIlf($index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="table-actions">
              <el-button size="small" @click="addIlf">
                <el-icon><Plus /></el-icon>
                添加ILF
              </el-button>
              <div class="total-info">
                ILF总计: <strong>{{ ilfTotal }} 个文件，{{ ilfPoints }} 功能点</strong>
              </div>
            </div>
          </div>

          <!-- EIF - 外部接口文件 -->
          <div class="element-section">
            <h3>EIF - 外部接口文件 (External Interface Files)</h3>
            <p class="element-desc">其他应用程序维护的、被本应用程序引用的逻辑相关数据的用户可识别组</p>
            
            <el-table :data="functionPointData.eif" style="width: 100%" class="element-table">
              <el-table-column prop="name" label="接口名称" min-width="200">
                <template #default="{ row, $index }">
                  <el-input v-model="row.name" placeholder="请输入接口名称" />
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="250">
                <template #default="{ row, $index }">
                  <el-input v-model="row.description" placeholder="请输入接口描述" />
                </template>
              </el-table-column>
              <el-table-column prop="det" label="DET数" width="100" align="center">
                <template #default="{ row, $index }">
                  <el-input-number v-model="row.det" :min="1" :max="100" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="ret" label="RET数" width="100" align="center">
                <template #default="{ row, $index }">
                  <el-input-number v-model="row.ret" :min="1" :max="50" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="complexity" label="复杂度" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getComplexityType(row.complexity)">
                    {{ row.complexity }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="points" label="功能点" width="100" align="center">
                <template #default="{ row }">
                  <span class="points-value">{{ row.points }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ row, $index }">
                  <el-button size="small" text type="danger" @click="removeEif($index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="table-actions">
              <el-button size="small" @click="addEif">
                <el-icon><Plus /></el-icon>
                添加EIF
              </el-button>
              <div class="total-info">
                EIF总计: <strong>{{ eifTotal }} 个接口，{{ eifPoints }} 功能点</strong>
              </div>
            </div>
          </div>

          <!-- EI - 外部输入 -->
          <div class="element-section">
            <h3>EI - 外部输入 (External Inputs)</h3>
            <p class="element-desc">从应用程序边界外部进入的数据或控制信息处理过程</p>
            
            <el-table :data="functionPointData.ei" style="width: 100%" class="element-table">
              <el-table-column prop="name" label="输入名称" min-width="200">
                <template #default="{ row, $index }">
                  <el-input v-model="row.name" placeholder="请输入功能名称" />
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="250">
                <template #default="{ row, $index }">
                  <el-input v-model="row.description" placeholder="请输入功能描述" />
                </template>
              </el-table-column>
              <el-table-column prop="det" label="DET数" width="100" align="center">
                <template #default="{ row, $index }">
                  <el-input-number v-model="row.det" :min="1" :max="100" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="ftr" label="FTR数" width="100" align="center">
                <template #default="{ row, $index }">
                  <el-input-number v-model="row.ftr" :min="1" :max="50" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="complexity" label="复杂度" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getComplexityType(row.complexity)">
                    {{ row.complexity }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="points" label="功能点" width="100" align="center">
                <template #default="{ row }">
                  <span class="points-value">{{ row.points }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ row, $index }">
                  <el-button size="small" text type="danger" @click="removeEi($index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="table-actions">
              <el-button size="small" @click="addEi">
                <el-icon><Plus /></el-icon>
                添加EI
              </el-button>
              <div class="total-info">
                EI总计: <strong>{{ eiTotal }} 个输入，{{ eiPoints }} 功能点</strong>
              </div>
            </div>
          </div>

          <!-- EO - 外部输出 -->
          <div class="element-section">
            <h3>EO - 外部输出 (External Outputs)</h3>
            <p class="element-desc">向应用程序外部发送数据的基本流程，包含派生数据或计算结果</p>
            
            <el-table :data="functionPointData.eo" style="width: 100%" class="element-table">
              <el-table-column prop="name" label="输出名称" min-width="200">
                <template #default="{ row, $index }">
                  <el-input v-model="row.name" placeholder="请输入输出名称" />
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="250">
                <template #default="{ row, $index }">
                  <el-input v-model="row.description" placeholder="请输入输出描述" />
                </template>
              </el-table-column>
              <el-table-column prop="det" label="DET数" width="100" align="center">
                <template #default="{ row, $index }">
                  <el-input-number v-model="row.det" :min="1" :max="100" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="ftr" label="FTR数" width="100" align="center">
                <template #default="{ row, $index }">
                  <el-input-number v-model="row.ftr" :min="1" :max="50" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="complexity" label="复杂度" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getComplexityType(row.complexity)">
                    {{ row.complexity }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="points" label="功能点" width="100" align="center">
                <template #default="{ row }">
                  <span class="points-value">{{ row.points }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ row, $index }">
                  <el-button size="small" text type="danger" @click="removeEo($index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="table-actions">
              <el-button size="small" @click="addEo">
                <el-icon><Plus /></el-icon>
                添加EO
              </el-button>
              <div class="total-info">
                EO总计: <strong>{{ eoTotal }} 个输出，{{ eoPoints }} 功能点</strong>
              </div>
            </div>
          </div>

          <!-- EQ - 外部查询 -->
          <div class="element-section">
            <h3>EQ - 外部查询 (External Inquiries)</h3>
            <p class="element-desc">将数据从应用程序发送到应用程序外部的基本流程，无派生数据</p>
            
            <el-table :data="functionPointData.eq" style="width: 100%" class="element-table">
              <el-table-column prop="name" label="查询名称" min-width="200">
                <template #default="{ row, $index }">
                  <el-input v-model="row.name" placeholder="请输入查询名称" />
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="250">
                <template #default="{ row, $index }">
                  <el-input v-model="row.description" placeholder="请输入查询描述" />
                </template>
              </el-table-column>
              <el-table-column prop="det" label="DET数" width="100" align="center">
                <template #default="{ row, $index }">
                  <el-input-number v-model="row.det" :min="1" :max="100" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="ftr" label="FTR数" width="100" align="center">
                <template #default="{ row, $index }">
                  <el-input-number v-model="row.ftr" :min="1" :max="50" size="small" />
                </template>
              </el-table-column>
              <el-table-column prop="complexity" label="复杂度" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getComplexityType(row.complexity)">
                    {{ row.complexity }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="points" label="功能点" width="100" align="center">
                <template #default="{ row }">
                  <span class="points-value">{{ row.points }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ row, $index }">
                  <el-button size="small" text type="danger" @click="removeEq($index)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="table-actions">
              <el-button size="small" @click="addEq">
                <el-icon><Plus /></el-icon>
                添加EQ
              </el-button>
              <div class="total-info">
                EQ总计: <strong>{{ eqTotal }} 个查询，{{ eqPoints }} 功能点</strong>
              </div>
            </div>
          </div>

          <!-- 功能点汇总 -->
          <div class="summary-section">
            <el-card class="summary-card">
              <template #header>
                <span>功能点汇总</span>
              </template>
              <el-row :gutter="16">
                <el-col :span="4">
                  <div class="summary-item">
                    <div class="item-label">ILF</div>
                    <div class="item-value">{{ ilfPoints }}</div>
                  </div>
                </el-col>
                <el-col :span="4">
                  <div class="summary-item">
                    <div class="item-label">EIF</div>
                    <div class="item-value">{{ eifPoints }}</div>
                  </div>
                </el-col>
                <el-col :span="4">
                  <div class="summary-item">
                    <div class="item-label">EI</div>
                    <div class="item-value">{{ eiPoints }}</div>
                  </div>
                </el-col>
                <el-col :span="4">
                  <div class="summary-item">
                    <div class="item-label">EO</div>
                    <div class="item-value">{{ eoPoints }}</div>
                  </div>
                </el-col>
                <el-col :span="4">
                  <div class="summary-item">
                    <div class="item-label">EQ</div>
                    <div class="item-value">{{ eqPoints }}</div>
                  </div>
                </el-col>
                <el-col :span="4">
                  <div class="summary-item">
                    <div class="item-label">总计</div>
                    <div class="item-value total">{{ totalUnadjustedPoints }}</div>
                  </div>
                </el-col>
              </el-row>
            </el-card>
          </div>
        </div>

        <div class="step-actions">
          <el-button type="primary" @click="nextStep" :disabled="totalUnadjustedPoints === 0">
            下一步：VAF调整
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </el-card>

      <!-- 步骤2: VAF调整 -->
      <el-card v-show="currentStep === 1" class="step-card">
        <template #header>
          <div class="card-header">
            <span>步骤2: VAF技术复杂度调整</span>
            <el-button text @click="showVafHelp = true">
              <el-icon><QuestionFilled /></el-icon>
              VAF说明
            </el-button>
          </div>
        </template>

        <div class="vaf-input">
          <p class="vaf-description">
            请根据项目的技术复杂度，为每个调整因子选择相应的影响程度（0-5分）
          </p>
          
          <el-row :gutter="20">
            <el-col :span="12" v-for="(factor, index) in vafFactors" :key="factor.code">
              <el-card class="vaf-factor-card" :class="{ 'even': index % 2 === 1 }">
                <div class="factor-header">
                  <h4>{{ factor.code }}: {{ factor.name }}</h4>
                </div>
                <p class="factor-description">{{ factor.description }}</p>
                
                <div class="factor-rating">
                  <el-radio-group v-model="vafData[factor.code as keyof typeof vafData]" class="rating-group">
                    <el-radio-button 
                      v-for="score in [0,1,2,3,4,5]" 
                      :key="score" 
                      :value="score"
                      :class="{ 'active': vafData[factor.code as keyof typeof vafData] === score }"
                    >
                      {{ score }}
                    </el-radio-button>
                  </el-radio-group>
                </div>
                
                <div class="factor-examples">
                  <div class="score-desc">
                    <strong>{{ getScoreDescription(vafData[factor.code as keyof typeof vafData] || 0) }}</strong>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- VAF计算结果 -->
          <div class="vaf-result">
            <el-card>
              <template #header>
                <span>VAF计算结果</span>
              </template>
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="result-item">
                    <div class="result-label">VAF总分</div>
                    <div class="result-value">{{ vafTotalScore }} / 70</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="result-item">
                    <div class="result-label">调整系数</div>
                    <div class="result-value">{{ vafAdjustmentFactor }}</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="result-item">
                    <div class="result-label">调整后功能点</div>
                    <div class="result-value adjusted">{{ adjustedFunctionPoints }}</div>
                  </div>
                </el-col>
              </el-row>
            </el-card>
          </div>
        </div>

        <div class="step-actions">
          <el-button @click="prevStep">
            <el-icon><ArrowLeft /></el-icon>
            上一步
          </el-button>
          <el-button type="primary" @click="nextStep">
            下一步：复用度设置
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </el-card>

      <!-- 步骤3: 复用度设置 -->
      <el-card v-show="currentStep === 2" class="step-card">
        <template #header>
          <div class="card-header">
            <span>步骤3: 复用度设置</span>
            <el-button text @click="showReuseHelp = true">
              <el-icon><QuestionFilled /></el-icon>
              复用度说明
            </el-button>
          </div>
        </template>

        <div class="reuse-input">
          <p class="reuse-description">
            请根据项目实际情况选择合适的复用等级，这将影响最终的开发工作量计算
          </p>

          <el-row :gutter="20" class="reuse-options">
            <el-col :span="6" v-for="level in reuseLevels" :key="level.value">
              <el-card 
                class="reuse-level-card" 
                :class="{ 'selected': selectedReuseLevel === level.value }"
                @click="selectReuseLevel(level.value)"
              >
                <div class="level-header">
                  <h3>{{ level.name }}</h3>
                  <div class="level-coefficient">系数: {{ level.coefficient }}</div>
                </div>
                <p class="level-description">{{ level.description }}</p>
                <div class="level-examples">
                  <strong>适用场景:</strong>
                  <ul>
                    <li v-for="example in level.examples" :key="example">{{ example }}</li>
                  </ul>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 复用度计算结果 -->
          <div class="reuse-result" v-if="selectedReuseLevel">
            <el-card>
              <template #header>
                <span>复用度调整结果</span>
              </template>
              <el-row :gutter="20">
                <el-col :span="6">
                  <div class="result-item">
                    <div class="result-label">选择等级</div>
                    <div class="result-value">{{ getCurrentReuseLevel()?.name }}</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="result-item">
                    <div class="result-label">复用系数</div>
                    <div class="result-value">{{ getCurrentReuseLevel()?.coefficient }}</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="result-item">
                    <div class="result-label">调整前功能点</div>
                    <div class="result-value">{{ adjustedFunctionPoints }}</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="result-item">
                    <div class="result-label">最终功能点</div>
                    <div class="result-value final">{{ finalFunctionPoints }}</div>
                  </div>
                </el-col>
              </el-row>
            </el-card>
          </div>
        </div>

        <div class="step-actions">
          <el-button @click="prevStep">
            <el-icon><ArrowLeft /></el-icon>
            上一步
          </el-button>
          <el-button type="primary" @click="nextStep" :disabled="!selectedReuseLevel">
            下一步：查看结果
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </el-card>

      <!-- 步骤4: 计算结果 -->
      <el-card v-show="currentStep === 3" class="step-card">
        <template #header>
          <div class="card-header">
            <span>步骤4: 计算结果</span>
            <el-button type="success" @click="handleExportReport">
              <el-icon><Download /></el-icon>
              导出报告
            </el-button>
          </div>
        </template>

        <div class="result-summary">
          <!-- 最终结果展示 -->
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="final-result-card">
                <div class="card-icon">
                  <el-icon><Histogram /></el-icon>
                </div>
                <div class="card-content">
                  <div class="card-value">{{ totalUnadjustedPoints }}</div>
                  <div class="card-label">未调整功能点</div>
                </div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="final-result-card">
                <div class="card-icon">
                  <el-icon><TrendCharts /></el-icon>
                </div>
                <div class="card-content">
                  <div class="card-value">{{ adjustedFunctionPoints }}</div>
                  <div class="card-label">VAF调整后功能点</div>
                </div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="final-result-card">
                <div class="card-icon">
                  <el-icon><Star /></el-icon>
                </div>
                <div class="card-content">
                  <div class="card-value">{{ finalFunctionPoints }}</div>
                  <div class="card-label">最终功能点</div>
                </div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="final-result-card">
                <div class="card-icon">
                  <el-icon><Money /></el-icon>
                </div>
                <div class="card-content">
                  <div class="card-value">¥{{ estimatedCost.toLocaleString() }}</div>
                  <div class="card-label">预估成本</div>
                </div>
              </div>
            </el-col>
          </el-row>

          <!-- 详细计算过程 -->
          <el-card class="calculation-process">
            <template #header>
              <span>详细计算过程</span>
            </template>
            
            <el-collapse v-model="activeCalculationSteps">
              <el-collapse-item title="功能点元素统计" name="elements">
                <el-table :data="elementsSummary" border>
                  <el-table-column prop="type" label="元素类型" width="120" />
                  <el-table-column prop="count" label="数量" width="80" align="center" />
                  <el-table-column prop="points" label="功能点" width="100" align="right" />
                  <el-table-column prop="percentage" label="占比" width="100" align="right" />
                </el-table>
              </el-collapse-item>
              
              <el-collapse-item title="VAF调整因子" name="vaf">
                <el-table :data="vafSummary" border>
                  <el-table-column prop="code" label="因子编号" width="80" />
                  <el-table-column prop="name" label="因子名称" width="200" />
                  <el-table-column prop="score" label="评分" width="80" align="center" />
                  <el-table-column prop="description" label="影响程度" />
                </el-table>
                <div class="vaf-calculation">
                  <p><strong>VAF计算公式：</strong> VAF = 0.65 + 0.01 × TI</p>
                  <p><strong>其中：</strong> TI = {{ vafTotalScore }}，VAF = {{ vafAdjustmentFactor }}</p>
                </div>
              </el-collapse-item>
              
              <el-collapse-item title="复用度调整" name="reuse">
                <div class="reuse-calculation">
                  <p><strong>复用等级：</strong> {{ getCurrentReuseLevel()?.name }}</p>
                  <p><strong>复用系数：</strong> {{ getCurrentReuseLevel()?.coefficient }}</p>
                  <p><strong>计算公式：</strong> 最终功能点 = AFP × 复用系数</p>
                  <p><strong>计算过程：</strong> {{ finalFunctionPoints }} = {{ adjustedFunctionPoints }} × {{ getCurrentReuseLevel()?.coefficient }}</p>
                </div>
              </el-collapse-item>
              
              <el-collapse-item title="成本估算" name="cost">
                <div class="cost-calculation">
                  <p><strong>估算基础：</strong> 基于长沙市政府项目标准</p>
                  <p><strong>人月生产率：</strong> 7.0 功能点/人月</p>
                  <p><strong>人月单价：</strong> ¥18,000/人月</p>
                  <p><strong>计算公式：</strong> 成本 = (功能点 ÷ 生产率) × 人月单价</p>
                  <p><strong>计算过程：</strong> ¥{{ estimatedCost.toLocaleString() }} = ({{ finalFunctionPoints }} ÷ 7.0) × ¥18,000</p>
                </div>
              </el-collapse-item>
            </el-collapse>
          </el-card>
        </div>

        <div class="step-actions">
          <el-button @click="prevStep">
            <el-icon><ArrowLeft /></el-icon>
            上一步
          </el-button>
          <el-button type="success" :loading="saving" @click="handleSaveResult">
            <el-icon><Check /></el-icon>
            保存计算结果
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 帮助对话框 -->
    <el-dialog v-model="showElementHelp" title="功能点元素说明" width="800px">
      <div class="help-content">
        <h3>NESMA功能点元素类型说明</h3>
        <el-collapse v-model="activeHelpItems">
          <el-collapse-item title="ILF - 内部逻辑文件" name="ilf">
            <p><strong>定义：</strong>由应用程序维护的逻辑相关数据的用户可识别组</p>
            <p><strong>计算方法：</strong>基于DET（数据元素类型）和RET（记录元素类型）数量</p>
            <p><strong>复杂度判断：</strong></p>
            <ul>
              <li>低复杂度：RET=1且DET≤19，或RET=2-5且DET≤20 → 7功能点</li>
              <li>中复杂度：RET=1且DET≥20，或RET=2-5且DET=21-50，或RET≥6且DET≤25 → 10功能点</li>
              <li>高复杂度：RET=2-5且DET≥51，或RET≥6且DET≥26 → 15功能点</li>
            </ul>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  // Calculator, // 不存在该图标，使用其他
  QuestionFilled,
  Plus,
  Delete,
  ArrowRight,
  ArrowLeft as ArrowLeftIcon,
  Download,
  Histogram,
  TrendCharts,
  Star,
  Money,
  Check,
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 数据定义
const calculating = ref(false)
const saving = ref(false)
const currentStep = ref(0)
const showElementHelp = ref(false)
const showVafHelp = ref(false)
const showReuseHelp = ref(false)
const activeCalculationSteps = ref(['elements'])
const activeHelpItems = ref([])

// 项目信息
const projectInfo = ref({
  id: route.params.id,
  projectName: '长沙市政务服务平台升级项目',
  projectCode: 'PROJ-2025-001',
})

// 功能点数据
const functionPointData = reactive({
  ilf: [
    { name: '用户信息表', description: '存储用户基本信息', det: 15, ret: 1, complexity: '低', points: 7 },
    { name: '业务数据表', description: '存储核心业务数据', det: 25, ret: 3, complexity: '中', points: 10 },
  ],
  eif: [
    { name: '第三方认证接口', description: '对接统一身份认证', det: 12, ret: 1, complexity: '低', points: 5 },
  ],
  ei: [
    { name: '用户登录', description: '用户身份验证', det: 8, ftr: 2, complexity: '低', points: 3 },
    { name: '数据录入', description: '业务数据录入', det: 20, ftr: 4, complexity: '中', points: 4 },
  ],
  eo: [],
  eq: [],
})

// VAF数据
const vafData = reactive({
  TF01: 3, TF02: 2, TF03: 4, TF04: 3, TF05: 2,
  TF06: 3, TF07: 4, TF08: 3, TF09: 2, TF10: 1,
  TF11: 3, TF12: 2, TF13: 4, TF14: 3,
})

// VAF因子定义
const vafFactors = [
  { code: 'TF01', name: '数据通信', description: '应用程序如何与处理器通信' },
  { code: 'TF02', name: '分布式数据处理', description: '分布式数据或处理功能' },
  { code: 'TF03', name: '性能', description: '用户对响应时间或吞吐量的要求' },
  { code: 'TF04', name: '重载配置', description: '重载操作配置对应用程序的影响' },
  { code: 'TF05', name: '事务率', description: '事务率对应用程序的影响' },
  { code: 'TF06', name: '在线数据输入', description: '在线数据输入的程度' },
  { code: 'TF07', name: '最终用户效率', description: '应用程序是否为最终用户效率而设计' },
  { code: 'TF08', name: '在线更新', description: 'ILF的在线更新程度' },
  { code: 'TF09', name: '复杂处理', description: '处理的复杂程度' },
  { code: 'TF10', name: '重用性', description: '应用程序是否为其他应用程序的重用而开发' },
  { code: 'TF11', name: '安装便易性', description: '转换和安装的难易程度' },
  { code: 'TF12', name: '操作便易性', description: '操作便易性的程度' },
  { code: 'TF13', name: '多站点', description: '应用程序是否为不同硬件和软件环境而设计' },
  { code: 'TF14', name: '便于变更', description: '应用程序是否便于变更' },
]

// 复用等级
const reuseLevels = [
  {
    value: 'NONE',
    name: '无复用',
    coefficient: 1.0,
    description: '完全新开发，无法复用任何现有组件',
    examples: ['全新系统开发', '创新性产品', '无相关系统可参考'],
  },
  {
    value: 'LOW',
    name: '低复用度',
    coefficient: 1.0,
    description: '少量功能可以复用，主要为全新开发',
    examples: ['复用少量工具类', '参考现有架构', '复用度<30%'],
  },
  {
    value: 'MEDIUM',
    name: '中复用度',
    coefficient: 0.6667,
    description: '部分功能可以复用，需要适度的定制和集成开发',
    examples: ['复用核心框架', '改造现有模块', '复用度30-70%'],
  },
  {
    value: 'HIGH',
    name: '高复用度',
    coefficient: 0.3333,
    description: '大部分功能可以复用现有系统或组件，只需少量定制开发',
    examples: ['基于成熟平台', '升级改造项目', '复用度>70%'],
  },
]

const selectedReuseLevel = ref('LOW')

// 计算属性
const ilfTotal = computed(() => functionPointData.ilf.length)
const ilfPoints = computed(() => functionPointData.ilf.reduce((sum, item) => sum + item.points, 0))

const eifTotal = computed(() => functionPointData.eif.length)
const eifPoints = computed(() => functionPointData.eif.reduce((sum, item) => sum + item.points, 0))

const eiTotal = computed(() => functionPointData.ei.length)
const eiPoints = computed(() => functionPointData.ei.reduce((sum, item) => sum + item.points, 0))

const eoTotal = computed(() => functionPointData.eo.length)
const eoPoints = computed(() => functionPointData.eo.reduce((sum, item) => sum + item.points, 0))

const eqTotal = computed(() => functionPointData.eq.length)
const eqPoints = computed(() => functionPointData.eq.reduce((sum, item) => sum + item.points, 0))

const totalUnadjustedPoints = computed(() => 
  ilfPoints.value + eifPoints.value + eiPoints.value + eoPoints.value + eqPoints.value
)

const vafTotalScore = computed(() => Object.values(vafData).reduce((sum, score) => sum + score, 0))
const vafAdjustmentFactor = computed(() => Number((0.65 + 0.01 * vafTotalScore.value).toFixed(4)))
const adjustedFunctionPoints = computed(() => Number((totalUnadjustedPoints.value * vafAdjustmentFactor.value).toFixed(2)))

const finalFunctionPoints = computed(() => {
  const level = getCurrentReuseLevel()
  return level ? Number((adjustedFunctionPoints.value * level.coefficient).toFixed(2)) : adjustedFunctionPoints.value
})

const estimatedCost = computed(() => Math.round(finalFunctionPoints.value / 7.0 * 18000))

const elementsSummary = computed(() => [
  { type: 'ILF', count: ilfTotal.value, points: ilfPoints.value, percentage: `${((ilfPoints.value / totalUnadjustedPoints.value) * 100).toFixed(1)}%` },
  { type: 'EIF', count: eifTotal.value, points: eifPoints.value, percentage: `${((eifPoints.value / totalUnadjustedPoints.value) * 100).toFixed(1)}%` },
  { type: 'EI', count: eiTotal.value, points: eiPoints.value, percentage: `${((eiPoints.value / totalUnadjustedPoints.value) * 100).toFixed(1)}%` },
])

const vafSummary = computed(() => vafFactors.map(factor => ({
  code: factor.code,
  name: factor.name,
  score: vafData[factor.code as keyof typeof vafData],
  description: getScoreDescription(vafData[factor.code as keyof typeof vafData]),
})))

// 监听数据变化，实时计算复杂度和功能点
watch(functionPointData, () => {
  calculateComplexityAndPoints()
}, { deep: true })

// 生命周期
onMounted(() => {
  calculateComplexityAndPoints()
})

// 方法定义
const calculateComplexityAndPoints = () => {
  // ILF复杂度计算
  functionPointData.ilf.forEach(item => {
    if (item.det && item.ret) {
      if ((item.ret === 1 && item.det <= 19) || (item.ret >= 2 && item.ret <= 5 && item.det <= 20)) {
        item.complexity = '低'
        item.points = 7
      } else if (
        (item.ret === 1 && item.det >= 20) ||
        (item.ret >= 2 && item.ret <= 5 && item.det >= 21 && item.det <= 50) ||
        (item.ret >= 6 && item.det <= 25)
      ) {
        item.complexity = '中'
        item.points = 10
      } else {
        item.complexity = '高'
        item.points = 15
      }
    }
  })

  // EIF复杂度计算
  functionPointData.eif.forEach(item => {
    if (item.det && item.ret) {
      if ((item.ret === 1 && item.det <= 19) || (item.ret >= 2 && item.ret <= 5 && item.det <= 20)) {
        item.complexity = '低'
        item.points = 5
      } else if (
        (item.ret === 1 && item.det >= 20) ||
        (item.ret >= 2 && item.ret <= 5 && item.det >= 21 && item.det <= 50) ||
        (item.ret >= 6 && item.det <= 25)
      ) {
        item.complexity = '中'
        item.points = 7
      } else {
        item.complexity = '高'
        item.points = 10
      }
    }
  })

  // EI复杂度计算
  functionPointData.ei.forEach(item => {
    if (item.det && item.ftr) {
      if ((item.ftr === 1 && item.det <= 15) || (item.ftr === 2 && item.det <= 16)) {
        item.complexity = '低'
        item.points = 3
      } else if (
        (item.ftr === 1 && item.det >= 16) ||
        (item.ftr === 2 && item.det >= 17) ||
        (item.ftr >= 3 && item.det <= 25)
      ) {
        item.complexity = '中'
        item.points = 4
      } else {
        item.complexity = '高'
        item.points = 6
      }
    }
  })

  // EO复杂度计算
  functionPointData.eo.forEach(item => {
    if (item.det && item.ftr) {
      if ((item.ftr === 1 && item.det <= 19) || (item.ftr >= 2 && item.ftr <= 3 && item.det <= 20)) {
        item.complexity = '低'
        item.points = 4
      } else if (
        (item.ftr === 1 && item.det >= 20) ||
        (item.ftr >= 2 && item.ftr <= 3 && item.det >= 21 && item.det <= 50) ||
        (item.ftr >= 4 && item.det <= 25)
      ) {
        item.complexity = '中'
        item.points = 5
      } else {
        item.complexity = '高'
        item.points = 7
      }
    }
  })

  // EQ复杂度计算
  functionPointData.eq.forEach(item => {
    if (item.det && item.ftr) {
      if ((item.ftr === 1 && item.det <= 19) || (item.ftr >= 2 && item.ftr <= 3 && item.det <= 20)) {
        item.complexity = '低'
        item.points = 3
      } else if (
        (item.ftr === 1 && item.det >= 20) ||
        (item.ftr >= 2 && item.ftr <= 3 && item.det >= 21 && item.det <= 50) ||
        (item.ftr >= 4 && item.det <= 25)
      ) {
        item.complexity = '中'
        item.points = 4
      } else {
        item.complexity = '高'
        item.points = 6
      }
    }
  })
}

const getComplexityType = (complexity: string) => {
  return complexity === '低' ? 'success' : complexity === '中' ? 'warning' : 'danger'
}

const getScoreDescription = (score: number) => {
  const descriptions = ['无影响', '轻微影响', '中等影响', '一般影响', '显著影响', '强烈影响']
  return descriptions[score] || '未设置'
}

const getCurrentReuseLevel = () => {
  return reuseLevels.find(level => level.value === selectedReuseLevel.value)
}

// 添加/删除功能点元素
const addIlf = () => {
  functionPointData.ilf.push({
    name: '',
    description: '',
    det: 1,
    ret: 1,
    complexity: '低',
    points: 7,
  })
}

const removeIlf = (index: number) => {
  functionPointData.ilf.splice(index, 1)
}

const addEif = () => {
  functionPointData.eif.push({
    name: '',
    description: '',
    det: 1,
    ret: 1,
    complexity: '低',
    points: 5,
  })
}

const removeEif = (index: number) => {
  functionPointData.eif.splice(index, 1)
}

const addEi = () => {
  functionPointData.ei.push({
    name: '',
    description: '',
    det: 1,
    ftr: 1,
    complexity: '低',
    points: 3,
  })
}

const removeEi = (index: number) => {
  functionPointData.ei.splice(index, 1)
}

const addEo = () => {
  functionPointData.eo.push({
    name: '',
    description: '',
    det: 1,
    ftr: 1,
    complexity: '低',
    points: 4,
  })
}

const removeEo = (index: number) => {
  functionPointData.eo.splice(index, 1)
}

const addEq = () => {
  functionPointData.eq.push({
    name: '',
    description: '',
    det: 1,
    ftr: 1,
    complexity: '低',
    points: 3,
  })
}

const removeEq = (index: number) => {
  functionPointData.eq.splice(index, 1)
}

const selectReuseLevel = (level: string) => {
  selectedReuseLevel.value = level
}

// 步骤控制
const nextStep = () => {
  if (currentStep.value < 3) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

// 保存和计算
const handleSaveDraft = async () => {
  try {
    saving.value = true
    // TODO: 调用保存草稿API
    ElMessage.success('草稿保存成功')
  } catch (error: any) {
    ElMessage.error('保存失败：' + error.message)
  } finally {
    saving.value = false
  }
}

const handleCalculate = async () => {
  try {
    calculating.value = true
    // TODO: 调用计算API
    ElMessage.success('计算完成')
    currentStep.value = 3 // 跳转到结果页
  } catch (error: any) {
    ElMessage.error('计算失败：' + error.message)
  } finally {
    calculating.value = false
  }
}

const handleSaveResult = async () => {
  try {
    saving.value = true
    // TODO: 调用保存结果API
    ElMessage.success('计算结果保存成功')
    router.push(`/projects/${route.params.id}`)
  } catch (error: any) {
    ElMessage.error('保存失败：' + error.message)
  } finally {
    saving.value = false
  }
}

const handleExportReport = () => {
  ElMessage.info('导出报告功能开发中')
}

const handleGoBack = () => {
  router.push(`/projects/${route.params.id}`)
}
</script>

<style scoped>
.nesma-calculate-container {
  /* 使用新的响应式布局系统，移除固定宽度 */
  min-height: 100vh;
  background: #f5f7fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 8px;
}

.header-left {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.title-info .page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.project-info {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.steps-section {
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 8px;
}

.calculate-content {
  min-height: 600px;
}

.step-card {
  min-height: 500px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.element-section {
  margin-bottom: 32px;
}

.element-section h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.element-desc {
  margin: 0 0 16px 0;
  color: #606266;
  font-size: 14px;
}

.element-table {
  margin-bottom: 16px;
}

.table-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-top: 1px solid #e4e7ed;
}

.total-info {
  color: #606266;
  font-size: 14px;
}

.points-value {
  font-weight: 600;
  color: #67c23a;
}

.summary-section {
  margin-top: 32px;
}

.summary-card .summary-item {
  text-align: center;
}

.summary-item .item-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.summary-item .item-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.summary-item .item-value.total {
  color: #409eff;
}

.vaf-input .vaf-description {
  margin: 0 0 24px 0;
  color: #606266;
  font-size: 16px;
}

.vaf-factor-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.vaf-factor-card.even {
  background: #fafafa;
}

.factor-header h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.factor-description {
  margin: 0 0 16px 0;
  color: #606266;
  font-size: 14px;
}

.factor-rating {
  margin-bottom: 12px;
}

.rating-group {
  display: flex;
  gap: 4px;
}

.score-desc {
  color: #409eff;
  font-size: 14px;
}

.vaf-result,
.reuse-result {
  margin-top: 24px;
}

.result-item {
  text-align: center;
}

.result-item .result-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.result-item .result-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.result-item .result-value.adjusted {
  color: #67c23a;
}

.result-item .result-value.final {
  color: #f56c6c;
  font-size: 28px;
}

.reuse-options {
  margin: 24px 0;
}

.reuse-level-card {
  cursor: pointer;
  transition: all 0.3s;
  min-height: 200px;
}

.reuse-level-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.3);
}

.reuse-level-card.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.level-header h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
}

.level-coefficient {
  color: #409eff;
  font-weight: 600;
}

.level-description {
  margin: 12px 0;
  color: #606266;
  font-size: 14px;
}

.level-examples {
  font-size: 12px;
  color: #909399;
}

.level-examples ul {
  margin: 8px 0 0 0;
  padding-left: 16px;
}

.level-examples li {
  margin-bottom: 4px;
}

.final-result-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: white;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s;
}

.final-result-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.2);
}

.final-result-card .card-icon {
  font-size: 32px;
  color: #409eff;
  margin-right: 16px;
}

.final-result-card .card-content .card-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.final-result-card .card-content .card-label {
  color: #909399;
  font-size: 14px;
}

.calculation-process {
  margin-top: 24px;
}

.vaf-calculation,
.reuse-calculation,
.cost-calculation {
  padding: 16px 0;
}

.vaf-calculation p,
.reuse-calculation p,
.cost-calculation p {
  margin: 8px 0;
  color: #606266;
}

.step-actions {
  margin-top: 32px;
  text-align: center;
  padding: 24px;
  border-top: 1px solid #e4e7ed;
}

.step-actions .el-button {
  margin: 0 8px;
}

.help-content h3 {
  margin: 0 0 16px 0;
  color: #303133;
}

:deep(.el-steps--horizontal) {
  padding: 0 20px;
}

:deep(.el-card__header) {
  background: #fafafa;
  border-bottom: 1px solid #e4e7ed;
}

:deep(.el-radio-button__inner) {
  width: 40px;
  text-align: center;
}

:deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
  background-color: #409eff;
  border-color: #409eff;
  color: #fff;
}

:deep(.el-collapse-item__header) {
  font-weight: 600;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-table) {
  max-width: 100% !important;
}

:deep(.el-table .el-table__body-wrapper) {
  overflow-x: auto;
}

.element-table {
  max-width: 100%;
  overflow-x: auto;
}
</style>