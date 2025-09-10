<template>
  <div class="login-container">
    <div class="login-form">
      <div class="login-header">
        <h2>长沙市财政评审中心</h2>
        <h3>软件规模评估系统</h3>
      </div>
      
      <el-form 
        ref="loginFormRef"
        :model="loginForm" 
        :rules="loginRules"
        class="login-form-content"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            size="large"
            prefix-icon="User"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading"
            @click="handleLogin"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-tips">
        <p>默认账号：admin / admin123</p>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const router = useRouter()
const loginFormRef = ref()
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  username: 'admin',
  password: 'admin123'
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

// 登录处理
const handleLogin = async () => {
  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return
    
    loading.value = true
    
    const response = await api.post('/api/auth/login', {
      username: loginForm.username,
      password: loginForm.password
    })
    
    // 存储token和用户信息
    localStorage.setItem('token', response.accessToken)
    localStorage.setItem('userInfo', JSON.stringify({
      userId: response.userId,
      username: response.username,
      realName: response.realName,
      roles: response.roles,
      permissions: response.permissions
    }))
    
    ElMessage.success('登录成功')
    
    // 跳转到项目列表页面
    router.push('/projects')
    
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// 检查是否已登录
onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) {
    router.push('/projects')
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-form {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  
  h2 {
    color: #2c3e50;
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 10px;
  }
  
  h3 {
    color: #7f8c8d;
    font-size: 16px;
    font-weight: 400;
    margin: 0;
  }
}

.login-form-content {
  .el-form-item {
    margin-bottom: 20px;
  }
  
  .el-button {
    margin-top: 10px;
  }
}

.login-tips {
  text-align: center;
  margin-top: 20px;
  
  p {
    color: #95a5a6;
    font-size: 14px;
    margin: 0;
  }
}
</style>