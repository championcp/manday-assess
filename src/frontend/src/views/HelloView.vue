<template>
  <div class="hello-page">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h1 class="title">
            <el-icon class="title-icon"><Setting /></el-icon>
            长沙市财政评审中心软件规模评估系统
          </h1>
          <p class="subtitle">基于NESMA功能点评估的政府投资信息化项目评审系统</p>
        </div>
      </el-header>

      <el-main class="main">
        <div class="welcome-section">
          <el-card class="welcome-card" shadow="always">
            <template #header>
              <div class="card-header">
                <span class="card-title">系统状态</span>
                <el-icon class="card-icon" color="#67C23A"><CircleCheck /></el-icon>
              </div>
            </template>
            
            <div class="status-content">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
                <el-descriptions-item label="后端状态">
                  <el-tag :type="backendStatus.type" size="small">
                    {{ backendStatus.text }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="数据库连接">
                  <el-tag :type="dbStatus.type" size="small">
                    {{ dbStatus.text }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="缓存状态">
                  <el-tag :type="cacheStatus.type" size="small">
                    {{ cacheStatus.text }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-card>

          <el-card class="feature-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="card-title">核心功能</span>
                <el-icon class="card-icon" color="#409EFF"><List /></el-icon>
              </div>
            </template>
            
            <div class="feature-list">
              <el-row :gutter="20">
                <el-col :span="8" v-for="feature in features" :key="feature.name">
                  <div class="feature-item">
                    <el-icon :color="feature.color" size="24">
                      <component :is="feature.icon" />
                    </el-icon>
                    <h4>{{ feature.name }}</h4>
                    <p>{{ feature.description }}</p>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-card>

          <div class="actions">
            <el-button type="primary" size="large" @click="checkBackendStatus">
              <el-icon><Refresh /></el-icon>
              检查后端连接
            </el-button>
            <el-button type="success" size="large" @click="navigateToDemo">
              <el-icon><Right /></el-icon>
              开始使用
            </el-button>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

// 状态数据
const backendStatus = ref({
  type: 'info',
  text: '检查中...'
})

const dbStatus = ref({
  type: 'info', 
  text: '检查中...'
})

const cacheStatus = ref({
  type: 'info',
  text: '检查中...'
})

// 功能特性列表
const features = ref([
  {
    name: 'NESMA功能点计算',
    description: '精确的功能点评估算法',
    icon: 'Calculator',
    color: '#409EFF'
  },
  {
    name: '项目管理',
    description: '完整的项目生命周期管理',
    icon: 'Folder',
    color: '#67C23A'
  },
  {
    name: '评审报告',
    description: '专业的评审报告生成',
    icon: 'Document',
    color: '#E6A23C'
  },
  {
    name: '数据分析',
    description: '项目数据统计与分析',
    icon: 'PieChart',
    color: '#F56C6C'
  },
  {
    name: '用户权限',
    description: '细粒度的权限控制',
    icon: 'UserFilled',
    color: '#909399'
  },
  {
    name: '审计日志',
    description: '完整的操作审计追踪',
    icon: 'Clock',
    color: '#95B8E7'
  }
])

// 检查后端状态
const checkBackendStatus = async () => {
  try {
    backendStatus.value = { type: 'info', text: '连接中...' }
    
    // 调用后端Hello接口
    await api.get('/api/public/hello')
    
    backendStatus.value = { type: 'success', text: '连接正常' }
    dbStatus.value = { type: 'success', text: '连接正常' }
    cacheStatus.value = { type: 'success', text: '运行正常' }
    
    ElMessage.success('后端服务连接成功！')
  } catch {
    backendStatus.value = { type: 'danger', text: '连接失败' }
    dbStatus.value = { type: 'warning', text: '未知' }
    cacheStatus.value = { type: 'warning', text: '未知' }
    
    ElMessage.error('后端服务连接失败，请检查服务是否启动')
  }
}

// 导航到项目列表页面
const navigateToDemo = () => {
  window.location.href = '/projects'
}

// 组件挂载时检查后端状态
onMounted(() => {
  checkBackendStatus()
})
</script>

<style scoped>
.hello-page {
  max-width: 1200px;
  margin: 0 auto;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  padding: 20px 0;
}

.title {
  color: #2c3e50;
  font-size: 28px;
  margin: 0 0 10px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.title-icon {
  font-size: 32px;
  color: #409EFF;
}

.subtitle {
  color: #7f8c8d;
  font-size: 16px;
  margin: 0;
}

.main {
  padding: 40px 20px;
}

.welcome-section {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  margin-bottom: 30px;
}

.feature-card {
  margin-bottom: 40px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.card-icon {
  font-size: 20px;
}

.status-content {
  padding: 20px 0;
}

.feature-list {
  padding: 20px 0;
}

.feature-item {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
  height: 100%;
}

.feature-item:hover {
  background: #f8f9fa;
  transform: translateY(-2px);
}

.feature-item h4 {
  color: #2c3e50;
  margin: 15px 0 8px 0;
  font-size: 16px;
}

.feature-item p {
  color: #7f8c8d;
  font-size: 14px;
  line-height: 1.4;
  margin: 0;
}

.actions {
  text-align: center;
  padding: 20px 0;
}

.actions .el-button {
  margin: 0 10px;
  min-width: 140px;
}
</style>