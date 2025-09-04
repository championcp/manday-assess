<template>
  <div class="project-form-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button icon="ArrowLeft" @click="handleGoBack">返回</el-button>
        <h1 class="page-title">{{ isEdit ? '编辑项目' : '新建项目' }}</h1>
      </div>
      <div class="header-right">
        <el-button @click="handleGoBack">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          {{ isEdit ? '更新项目' : '创建项目' }}
        </el-button>
      </div>
    </div>

    <!-- 表单内容 -->
    <div class="form-content">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        size="default"
      >
        <!-- 基本信息 -->
        <el-card class="form-section" header="基本信息">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="项目编号" prop="projectCode">
                <el-input
                  v-model="formData.projectCode"
                  placeholder="系统自动生成或手动输入"
                  :disabled="isEdit"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="项目类型" prop="projectType">
                <el-select v-model="formData.projectType" placeholder="请选择项目类型">
                  <el-option label="政府投资信息化项目" value="GOVERNMENT_IT" />
                  <el-option label="政府升级改造项目" value="GOVERNMENT_UPGRADE" />
                  <el-option label="政府维护项目" value="GOVERNMENT_MAINTENANCE" />
                  <el-option label="其他项目" value="OTHER" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="项目名称" prop="projectName">
            <el-input
              v-model="formData.projectName"
              placeholder="请输入项目名称"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="项目描述" prop="description">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="4"
              placeholder="请详细描述项目内容、目标和主要功能"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="预计开始时间" prop="plannedStartDate">
                <el-date-picker
                  v-model="formData.plannedStartDate"
                  type="date"
                  placeholder="选择开始时间"
                  style="width: 100%"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预计结束时间" prop="plannedEndDate">
                <el-date-picker
                  v-model="formData.plannedEndDate"
                  type="date"
                  placeholder="选择结束时间"
                  style="width: 100%"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <!-- 技术信息 -->
        <el-card class="form-section" header="技术信息">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="开发平台" prop="developmentPlatform">
                <el-select
                  v-model="formData.developmentPlatform"
                  placeholder="请选择开发平台"
                  multiple
                >
                  <el-option label="Java" value="JAVA" />
                  <el-option label="Node.js" value="NODEJS" />
                  <el-option label=".NET" value="DOTNET" />
                  <el-option label="PHP" value="PHP" />
                  <el-option label="Python" value="PYTHON" />
                  <el-option label="其他" value="OTHER" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="数据库类型" prop="databaseType">
                <el-select
                  v-model="formData.databaseType"
                  placeholder="请选择数据库类型"
                  multiple
                >
                  <el-option label="MySQL" value="MYSQL" />
                  <el-option label="PostgreSQL" value="POSTGRESQL" />
                  <el-option label="Oracle" value="ORACLE" />
                  <el-option label="SQL Server" value="SQLSERVER" />
                  <el-option label="MongoDB" value="MONGODB" />
                  <el-option label="Redis" value="REDIS" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="架构类型" prop="architectureType">
                <el-radio-group v-model="formData.architectureType">
                  <el-radio value="MONOLITHIC">单体架构</el-radio>
                  <el-radio value="MICROSERVICES">微服务架构</el-radio>
                  <el-radio value="DISTRIBUTED">分布式架构</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="部署方式" prop="deploymentType">
                <el-checkbox-group v-model="formData.deploymentType">
                  <el-checkbox value="ON_PREMISE">本地部署</el-checkbox>
                  <el-checkbox value="CLOUD">云端部署</el-checkbox>
                  <el-checkbox value="HYBRID">混合部署</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <!-- 团队信息 -->
        <el-card class="form-section" header="团队信息">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="项目经理" prop="projectManager">
                <el-input
                  v-model="formData.projectManager"
                  placeholder="请输入项目经理姓名"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="技术负责人" prop="technicalLead">
                <el-input
                  v-model="formData.technicalLead"
                  placeholder="请输入技术负责人姓名"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="预计团队规模" prop="teamSize">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-input-number
                  v-model="formData.teamSize.developers"
                  :min="0"
                  :max="50"
                  placeholder="开发人员"
                  style="width: 100%"
                />
                <div class="input-label">开发人员</div>
              </el-col>
              <el-col :span="8">
                <el-input-number
                  v-model="formData.teamSize.testers"
                  :min="0"
                  :max="20"
                  placeholder="测试人员"
                  style="width: 100%"
                />
                <div class="input-label">测试人员</div>
              </el-col>
              <el-col :span="8">
                <el-input-number
                  v-model="formData.teamSize.others"
                  :min="0"
                  :max="20"
                  placeholder="其他人员"
                  style="width: 100%"
                />
                <div class="input-label">其他人员</div>
              </el-col>
            </el-row>
          </el-form-item>
        </el-card>

        <!-- 预算信息 -->
        <el-card class="form-section" header="预算信息">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="预算上限" prop="budgetLimit">
                <el-input
                  v-model="formData.budgetLimit"
                  placeholder="请输入预算上限（元）"
                >
                  <template #prefix>¥</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预算类型" prop="budgetType">
                <el-radio-group v-model="formData.budgetType">
                  <el-radio value="FIXED">固定预算</el-radio>
                  <el-radio value="ESTIMATED">预估预算</el-radio>
                  <el-radio value="OPEN">开放预算</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="备注信息" prop="remarks">
            <el-input
              v-model="formData.remarks"
              type="textarea"
              :rows="3"
              placeholder="请输入其他备注信息"
              maxlength="300"
              show-word-limit
            />
          </el-form-item>
        </el-card>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 是否为编辑模式
const isEdit = computed(() => route.name === 'project-edit')

// 数据定义
const saving = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive({
  projectCode: '',
  projectName: '',
  description: '',
  projectType: 'GOVERNMENT_IT',
  plannedStartDate: '',
  plannedEndDate: '',
  developmentPlatform: [],
  databaseType: [],
  architectureType: 'MONOLITHIC',
  deploymentType: [],
  projectManager: '',
  technicalLead: '',
  teamSize: {
    developers: null,
    testers: null,
    others: null,
  },
  budgetLimit: '',
  budgetType: 'ESTIMATED',
  remarks: '',
})

// 表单验证规则
const formRules: FormRules = {
  projectCode: [
    { required: true, message: '请输入项目编号', trigger: 'blur' },
    { min: 3, max: 20, message: '项目编号长度在 3 到 20 个字符', trigger: 'blur' },
  ],
  projectName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 100, message: '项目名称长度在 2 到 100 个字符', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入项目描述', trigger: 'blur' },
    { min: 10, max: 500, message: '项目描述长度在 10 到 500 个字符', trigger: 'blur' },
  ],
  projectType: [
    { required: true, message: '请选择项目类型', trigger: 'change' },
  ],
  plannedStartDate: [
    { required: true, message: '请选择预计开始时间', trigger: 'change' },
  ],
  plannedEndDate: [
    { required: true, message: '请选择预计结束时间', trigger: 'change' },
  ],
  projectManager: [
    { required: true, message: '请输入项目经理姓名', trigger: 'blur' },
  ],
  technicalLead: [
    { required: true, message: '请输入技术负责人姓名', trigger: 'blur' },
  ],
}

// 生命周期
onMounted(() => {
  if (isEdit.value) {
    loadProjectData()
  } else {
    generateProjectCode()
  }
})

// 方法定义
const loadProjectData = async () => {
  try {
    const projectId = route.params.id
    
    // TODO: 调用API获取项目数据
    // const response = await projectApi.getProject(projectId)
    
    // 模拟数据
    const mockData = {
      projectCode: 'PROJ-2025-001',
      projectName: '长沙市政务服务平台升级项目',
      description: '对现有政务服务平台进行功能升级和性能优化，包括新增移动端支持、优化审批流程、增强数据安全等功能',
      projectType: 'GOVERNMENT_IT',
      plannedStartDate: '2025-09-01',
      plannedEndDate: '2025-12-31',
      developmentPlatform: ['JAVA', 'NODEJS'],
      databaseType: ['POSTGRESQL', 'REDIS'],
      architectureType: 'MICROSERVICES',
      deploymentType: ['CLOUD'],
      projectManager: '张工',
      technicalLead: '李工',
      teamSize: {
        developers: 8,
        testers: 3,
        others: 2,
      },
      budgetLimit: '10000000',
      budgetType: 'FIXED',
      remarks: '重点项目，需要按时完成',
    }
    
    Object.assign(formData, mockData)
  } catch (error: any) {
    ElMessage.error('加载项目数据失败：' + error.message)
  }
}

const generateProjectCode = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const random = String(Math.floor(Math.random() * 1000)).padStart(3, '0')
  
  formData.projectCode = `PROJ-${year}${month}${day}-${random}`
}

const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    // 表单验证
    await formRef.value.validate()
    
    saving.value = true
    
    // TODO: 调用保存API
    if (isEdit.value) {
      // await projectApi.updateProject(route.params.id, formData)
      ElMessage.success('项目更新成功')
    } else {
      // await projectApi.createProject(formData)
      ElMessage.success('项目创建成功')
    }
    
    // 跳转到项目列表
    router.push('/projects')
  } catch (error: any) {
    if (error.message) {
      ElMessage.error(`保存失败：${error.message}`)
    }
  } finally {
    saving.value = false
  }
}

const handleGoBack = () => {
  router.back()
}
</script>

<style scoped>
.project-form-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  gap: 12px;
}

.form-content {
  background: #f5f7fa;
  min-height: calc(100vh - 200px);
}

.form-section {
  margin-bottom: 24px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.input-label {
  text-align: center;
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

:deep(.el-card__header) {
  background: #fafafa;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
}

:deep(.el-card__body) {
  padding: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #606266;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-checkbox-group .el-checkbox) {
  margin-right: 16px;
}

:deep(.el-radio-group .el-radio) {
  margin-right: 16px;
}
</style>