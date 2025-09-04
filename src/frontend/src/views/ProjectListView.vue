<template>
  <div class="project-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="title-section">
        <h1 class="page-title">项目管理</h1>
        <p class="page-description">软件规模评估项目的统一管理平台</p>
      </div>
      <div class="action-section">
        <el-button type="primary" icon="Plus" @click="handleCreateProject">
          新建项目
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-filter-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索项目名称或编号"
            prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="searchForm.status"
            placeholder="项目状态"
            clearable
            @change="handleSearch"
          >
            <el-option label="草稿" value="DRAFT" />
            <el-option label="计算中" value="CALCULATING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="审核中" value="REVIEWING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 项目列表表格 -->
    <div class="table-section">
      <el-table
        v-loading="loading"
        :data="projectList"
        style="width: 100%"
        row-key="id"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="projectCode" label="项目编号" min-width="120" />
        <el-table-column prop="projectName" label="项目名称" min-width="180" />
        <el-table-column prop="description" label="项目描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="项目状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="功能点数" width="100" align="right">
          <template #default="{ row }">
            <span v-if="row.totalFunctionPoints">
              {{ row.totalFunctionPoints }}
            </span>
            <span v-else class="text-muted">--</span>
          </template>
        </el-table-column>
        <el-table-column label="预估成本" width="120" align="right">
          <template #default="{ row }">
            <span v-if="row.estimatedCost">
              ¥{{ row.estimatedCost.toLocaleString() }}
            </span>
            <span v-else class="text-muted">--</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdBy" label="创建者" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewProject(row)">
              查看
            </el-button>
            <el-button link type="primary" @click="handleEditProject(row)">
              编辑
            </el-button>
            <el-button link type="success" @click="handleCalculate(row)">
              计算
            </el-button>
            <el-button link type="danger" @click="handleDeleteProject(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div v-if="selectedProjects.length > 0" class="batch-actions">
        <span>已选择 {{ selectedProjects.length }} 项</span>
        <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
        <el-button type="success" @click="handleBatchCalculate">批量计算</el-button>
      </div>

      <!-- 分页器 -->
      <div class="pagination-section">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

// 数据定义
const loading = ref(false)
const projectList = ref<any[]>([])
const selectedProjects = ref<any[]>([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  dateRange: null,
})

// 分页信息
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0,
})

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
  loadProjectList()
})

// 方法定义
const loadProjectList = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取项目列表
    // const response = await projectApi.getProjectList({
    //   ...searchForm,
    //   current: pagination.current,
    //   size: pagination.size,
    // })
    
    // 模拟数据
    const mockData = {
      records: [
        {
          id: 1,
          projectCode: 'PROJ-2025-001',
          projectName: '长沙市政务服务平台升级项目',
          description: '对现有政务服务平台进行功能升级和性能优化',
          status: 'COMPLETED',
          totalFunctionPoints: 2800,
          estimatedCost: 8702416.80,
          createdBy: '张工',
          createTime: '2025-09-01 10:30:00',
        },
        {
          id: 2,
          projectCode: 'PROJ-2025-002',
          projectName: '财政预算管理系统',
          description: '建设新一代财政预算管理和监控系统',
          status: 'CALCULATING',
          totalFunctionPoints: 6200,
          estimatedCost: null,
          createdBy: '李工',
          createTime: '2025-09-02 14:15:00',
        },
        {
          id: 3,
          projectCode: 'PROJ-2025-003',
          projectName: '智慧城市数据中台',
          description: '构建统一的城市数据中台和服务体系',
          status: 'DRAFT',
          totalFunctionPoints: null,
          estimatedCost: null,
          createdBy: '王工',
          createTime: '2025-09-03 09:45:00',
        },
      ],
      total: 3,
    }
    
    projectList.value = mockData.records
    pagination.total = mockData.total
  } catch (error: any) {
    ElMessage.error('加载项目列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadProjectList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    status: '',
    dateRange: null,
  })
  handleSearch()
}

const handleCreateProject = () => {
  router.push('/projects/create')
}

const handleViewProject = (project: any) => {
  router.push(`/projects/${project.id}`)
}

const handleEditProject = (project: any) => {
  router.push(`/projects/${project.id}/edit`)
}

const handleCalculate = (project: any) => {
  router.push(`/projects/${project.id}/calculate`)
}

const handleDeleteProject = async (project: any) => {
  try {
    await ElMessageBox.confirm(
      `确认删除项目"${project.projectName}"吗？此操作不可恢复。`,
      '删除确认',
      {
        type: 'warning',
      }
    )
    
    // TODO: 调用删除API
    // await projectApi.deleteProject(project.id)
    
    ElMessage.success('删除成功')
    loadProjectList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

const handleSelectionChange = (selection: any[]) => {
  selectedProjects.value = selection
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedProjects.value.length} 个项目吗？此操作不可恢复。`,
      '批量删除确认',
      {
        type: 'warning',
      }
    )
    
    // TODO: 调用批量删除API
    // const ids = selectedProjects.value.map(p => p.id)
    // await projectApi.batchDeleteProjects(ids)
    
    ElMessage.success('批量删除成功')
    loadProjectList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败：' + error.message)
    }
  }
}

const handleBatchCalculate = () => {
  ElMessage.info('批量计算功能开发中')
}

const handlePageChange = (page: number) => {
  pagination.current = page
  loadProjectList()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadProjectList()
}

const getStatusText = (status: string) => {
  return (statusMap as any)[status]?.text || status
}

const getStatusType = (status: string) => {
  return (statusMap as any)[status]?.type || 'info'
}
</script>

<style scoped>
.project-list-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.title-section .page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.title-section .page-description {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.search-filter-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.table-section {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.batch-actions {
  padding: 16px 20px;
  background: #f5f7fa;
  border-top: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  gap: 12px;
}

.batch-actions span {
  color: #606266;
  font-size: 14px;
}

.pagination-section {
  padding: 20px;
  text-align: right;
  border-top: 1px solid #e4e7ed;
}

.text-muted {
  color: #c0c4cc;
}

:deep(.el-table) {
  border-radius: 0;
}

:deep(.el-table__header-wrapper) {
  background: #fafafa;
}
</style>