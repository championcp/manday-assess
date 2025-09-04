<template>
  <div class="project-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button icon="ArrowLeft" @click="handleGoBack">返回列表</el-button>
        <div class="title-info">
          <h1 class="project-title">{{ projectDetail?.projectName || '加载中...' }}</h1>
          <div class="project-meta">
            <span class="project-code">{{ projectDetail?.projectCode }}</span>
            <el-tag v-if="projectDetail?.status" :type="getStatusType(projectDetail.status)">
              {{ getStatusText(projectDetail.status) }}
            </el-tag>
          </div>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Edit" @click="handleEdit">编辑项目</el-button>
        <el-button type="success" icon="Check" @click="handleCalculate">开始计算</el-button>
        <el-dropdown @command="handleMoreActions">
          <el-button icon="MoreFilled">
            更多操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="duplicate">复制项目</el-dropdown-item>
              <el-dropdown-item command="export">导出报告</el-dropdown-item>
              <el-dropdown-item command="archive">归档项目</el-dropdown-item>
              <el-dropdown-item command="delete" class="danger-item">删除项目</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 项目概要信息 -->
    <el-row :gutter="20" class="summary-section">
      <el-col :span="6">
        <div class="summary-card">
          <div class="card-header">
            <el-icon class="icon"><Histogram /></el-icon>
            <span>功能点数</span>
          </div>
          <div class="card-value">
            {{ projectDetail?.totalFunctionPoints || '--' }}
          </div>
          <div class="card-desc">未调整功能点</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="summary-card">
          <div class="card-header">
            <el-icon class="icon"><TrendCharts /></el-icon>
            <span>调整后功能点</span>
          </div>
          <div class="card-value">
            {{ projectDetail?.adjustedFunctionPoints || '--' }}
          </div>
          <div class="card-desc">VAF调整后</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="summary-card">
          <div class="card-header">
            <el-icon class="icon"><Money /></el-icon>
            <span>预估成本</span>
          </div>
          <div class="card-value">
            {{ projectDetail?.estimatedCost ? `¥${projectDetail.estimatedCost.toLocaleString()}` : '--' }}
          </div>
          <div class="card-desc">开发成本</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="summary-card">
          <div class="card-header">
            <el-icon class="icon"><Clock /></el-icon>
            <span>预估工期</span>
          </div>
          <div class="card-value">
            {{ projectDetail?.estimatedDuration || '--' }}
          </div>
          <div class="card-desc">人月</div>
        </div>
      </el-col>
    </el-row>

    <!-- 详细信息标签页 -->
    <div class="detail-tabs">
      <el-tabs v-model="activeTab" class="detail-tabs-wrapper">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <div class="tab-content">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="info-group">
                  <h3>项目基本信息</h3>
                  <el-descriptions :column="1" border>
                    <el-descriptions-item label="项目编号">
                      {{ projectDetail?.projectCode }}
                    </el-descriptions-item>
                    <el-descriptions-item label="项目名称">
                      {{ projectDetail?.projectName }}
                    </el-descriptions-item>
                    <el-descriptions-item label="项目描述">
                      {{ projectDetail?.description || '暂无描述' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="项目类型">
                      {{ projectDetail?.projectType || '政府投资信息化项目' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="项目状态">
                      <el-tag :type="getStatusType(projectDetail?.status)">
                        {{ getStatusText(projectDetail?.status) }}
                      </el-tag>
                    </el-descriptions-item>
                  </el-descriptions>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="info-group">
                  <h3>创建信息</h3>
                  <el-descriptions :column="1" border>
                    <el-descriptions-item label="创建者">
                      {{ projectDetail?.createdBy }}
                    </el-descriptions-item>
                    <el-descriptions-item label="创建时间">
                      {{ projectDetail?.createTime }}
                    </el-descriptions-item>
                    <el-descriptions-item label="最后更新">
                      {{ projectDetail?.updateTime }}
                    </el-descriptions-item>
                    <el-descriptions-item label="更新者">
                      {{ projectDetail?.updatedBy }}
                    </el-descriptions-item>
                  </el-descriptions>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- 功能点计算 -->
        <el-tab-pane label="功能点计算" name="calculation">
          <div class="tab-content">
            <div class="calculation-overview">
              <h3>NESMA功能点计算概要</h3>
              <div v-if="!projectDetail?.calculationResult" class="empty-calculation">
                <el-empty description="尚未进行功能点计算">
                  <el-button type="primary" @click="handleCalculate">开始计算</el-button>
                </el-empty>
              </div>
              <div v-else>
                <!-- 计算结果展示 -->
                <el-row :gutter="20">
                  <el-col :span="8">
                    <div class="calculation-card">
                      <h4>数据元素统计</h4>
                      <el-table :data="functionPointElements" size="small">
                        <el-table-column prop="type" label="元素类型" width="80" />
                        <el-table-column prop="count" label="数量" width="60" align="center" />
                        <el-table-column prop="points" label="功能点" width="80" align="right" />
                      </el-table>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="calculation-card">
                      <h4>VAF调整因子</h4>
                      <div class="vaf-info">
                        <div class="vaf-item">
                          <span>VAF总分：</span>
                          <span class="vaf-value">{{ projectDetail.calculationResult.vafScore }}</span>
                        </div>
                        <div class="vaf-item">
                          <span>调整系数：</span>
                          <span class="vaf-value">{{ projectDetail.calculationResult.vafFactor }}</span>
                        </div>
                      </div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="calculation-card">
                      <h4>计算结果</h4>
                      <div class="result-info">
                        <div class="result-item">
                          <span>未调整功能点：</span>
                          <span class="result-value">{{ projectDetail.calculationResult.unadjustedPoints }}</span>
                        </div>
                        <div class="result-item">
                          <span>调整后功能点：</span>
                          <span class="result-value final">{{ projectDetail.calculationResult.adjustedPoints }}</span>
                        </div>
                      </div>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 成本估算 -->
        <el-tab-pane label="成本估算" name="cost">
          <div class="tab-content">
            <h3>成本估算详情</h3>
            <div v-if="!projectDetail?.costEstimation" class="empty-cost">
              <el-empty description="尚未进行成本估算">
                <el-button type="primary" @click="handleCalculate">计算成本</el-button>
              </el-empty>
            </div>
            <div v-else class="cost-details">
              <!-- 成本详情内容 -->
            </div>
          </div>
        </el-tab-pane>

        <!-- 操作历史 -->
        <el-tab-pane label="操作历史" name="history">
          <div class="tab-content">
            <h3>项目操作历史</h3>
            <el-timeline>
              <el-timeline-item
                v-for="(item, index) in operationHistory"
                :key="index"
                :timestamp="item.timestamp"
                :type="item.type"
              >
                <h4>{{ item.operation }}</h4>
                <p>{{ item.description }}</p>
                <span class="operator">操作者：{{ item.operator }}</span>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Edit,
  // Calculator, // 不存在
  MoreFilled,
  ArrowDown,
  Histogram,
  TrendCharts,
  Money,
  Clock,
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 数据定义
const loading = ref(false)
const activeTab = ref('basic')
const projectDetail = ref<any>(null)
const functionPointElements = ref<any[]>([])
const operationHistory = ref<any[]>([])

// 项目状态映射
const statusMap = {
  DRAFT: { text: '草稿', type: 'info' },
  CALCULATING: { text: '计算中', type: 'warning' },
  COMPLETED: { text: '已完成', type: 'success' },
  REVIEWING: { text: '审核中', type: 'primary' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已拒绝', type: 'danger' },
}

// 生命周期
onMounted(() => {
  loadProjectDetail()
})

// 方法定义
const loadProjectDetail = async () => {
  loading.value = true
  try {
    const projectId = route.params.id
    
    // TODO: 调用API获取项目详情
    // const response = await projectApi.getProjectDetail(projectId)
    
    // 模拟数据
    const mockDetail = {
      id: projectId,
      projectCode: 'PROJ-2025-001',
      projectName: '长沙市政务服务平台升级项目',
      description: '对现有政务服务平台进行功能升级和性能优化，包括新增移动端支持、优化审批流程、增强数据安全等功能',
      projectType: '政府投资信息化项目',
      status: 'COMPLETED',
      totalFunctionPoints: 2800,
      adjustedFunctionPoints: 3388,
      estimatedCost: 8702416.80,
      estimatedDuration: 483.47,
      createdBy: '张工',
      createTime: '2025-09-01 10:30:00',
      updateTime: '2025-09-04 16:45:00',
      updatedBy: '张工',
      calculationResult: {
        unadjustedPoints: 2800,
        adjustedPoints: 3388,
        vafScore: 42,
        vafFactor: 1.21,
      },
    }
    
    projectDetail.value = mockDetail
    
    // 模拟功能点元素数据
    functionPointElements.value = [
      { type: 'ILF', count: 12, points: 720 },
      { type: 'EIF', count: 8, points: 400 },
      { type: 'EI', count: 25, points: 1000 },
      { type: 'EO', count: 18, points: 540 },
      { type: 'EQ', count: 14, points: 140 },
    ]
    
    // 模拟操作历史
    operationHistory.value = [
      {
        timestamp: '2025-09-04 16:45:00',
        type: 'success',
        operation: '计算完成',
        description: 'NESMA功能点计算和成本估算已完成',
        operator: '张工',
      },
      {
        timestamp: '2025-09-04 14:30:00',
        type: 'primary',
        operation: '开始计算',
        description: '开始进行NESMA功能点计算',
        operator: '张工',
      },
      {
        timestamp: '2025-09-01 10:30:00',
        type: 'info',
        operation: '创建项目',
        description: '项目创建完成，状态为草稿',
        operator: '张工',
      },
    ]
  } catch (error: any) {
    ElMessage.error('加载项目详情失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleGoBack = () => {
  router.back()
}

const handleEdit = () => {
  router.push(`/projects/${route.params.id}/edit`)
}

const handleCalculate = () => {
  router.push(`/projects/${route.params.id}/calculate`)
}

const handleMoreActions = async (command: string) => {
  switch (command) {
    case 'duplicate':
      ElMessage.info('复制项目功能开发中')
      break
    case 'export':
      ElMessage.info('导出报告功能开发中')
      break
    case 'archive':
      ElMessage.info('归档项目功能开发中')
      break
    case 'delete':
      try {
        await ElMessageBox.confirm(
          `确认删除项目"${projectDetail.value?.projectName}"吗？此操作不可恢复。`,
          '删除确认',
          { type: 'warning' }
        )
        // TODO: 调用删除API
        ElMessage.success('删除成功')
        router.push('/projects')
      } catch (error: any) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败：' + error.message)
        }
      }
      break
  }
}

const getStatusText = (status: string) => {
  return (statusMap as any)[status]?.text || status
}

const getStatusType = (status: string) => {
  return (statusMap as any)[status]?.type || 'info'
}
</script>

<style scoped>
.project-detail-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.title-info .project-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.project-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.project-code {
  color: #909399;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.summary-section {
  margin-bottom: 24px;
}

.summary-card {
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
  color: #606266;
  font-size: 14px;
}

.card-header .icon {
  font-size: 18px;
  color: #409eff;
}

.card-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.card-desc {
  color: #909399;
  font-size: 12px;
}

.detail-tabs {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.tab-content {
  padding: 24px;
}

.info-group {
  margin-bottom: 24px;
}

.info-group h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.calculation-overview h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
}

.empty-calculation,
.empty-cost {
  margin: 40px 0;
}

.calculation-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  height: 200px;
}

.calculation-card h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.vaf-info,
.result-info {
  padding: 12px 0;
}

.vaf-item,
.result-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.vaf-value,
.result-value {
  font-weight: 600;
  color: #303133;
}

.result-value.final {
  color: #67c23a;
  font-size: 18px;
}

.operator {
  color: #909399;
  font-size: 12px;
}

.danger-item {
  color: #f56c6c;
}

:deep(.detail-tabs-wrapper .el-tabs__header) {
  margin: 0;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
}

:deep(.detail-tabs-wrapper .el-tabs__nav-wrap) {
  padding: 0 24px;
}

:deep(.el-descriptions__label) {
  width: 120px;
  font-weight: 600;
}
</style>