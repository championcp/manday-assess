import axios from 'axios'
import type { AxiosResponse, AxiosError } from 'axios'

/**
 * API 响应数据结构
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

/**
 * 创建 axios 实例
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

/**
 * 请求拦截器
 */
api.interceptors.request.use(
  (config) => {
    // 添加认证token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加时间戳防缓存
    if (config.method === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now(),
      }
    }
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 */
api.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { code, message, data } = response.data
    
    // 请求成功
    if (code === 200) {
      return data
    } else {
      // 业务错误
      throw new Error(message || '请求失败')
    }
  },
  (error: AxiosError) => {
    // HTTP错误处理
    if (error.response) {
      const status = error.response.status
      
      switch (status) {
        case 401:
          // 未认证，清除token并跳转登录
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          throw new Error('访问被拒绝')
        case 404:
          throw new Error('请求的资源不存在')
        case 500:
          throw new Error('服务器内部错误')
        default:
          throw new Error(`请求错误: ${status}`)
      }
    } else if (error.request) {
      // 网络错误
      throw new Error('网络连接失败，请检查网络设置')
    } else {
      // 其他错误
      throw new Error(error.message || '未知错误')
    }
  }
)

export default api