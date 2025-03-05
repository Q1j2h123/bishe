import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 不需要认证的接口
const noAuthUrls = ['/user/register', '/user/login']

// 请求拦截器
request.interceptors.request.use(
  config => {
    console.log('发送请求:', {
      url: config.url,
      method: config.method,
      data: config.data,
      headers: config.headers,
      baseURL: config.baseURL,
      fullPath: `${config.baseURL}${config.url}`
    })
    // 检查是否是无需认证的接口
    if (noAuthUrls.some(url => config.url?.includes(url))) {
      return config
    }
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求配置错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    console.log('收到响应:', {
      status: response.status,
      data: response.data,
      headers: response.headers,
      config: response.config
    })

    // 直接返回响应数据，让具体的 API 方法处理业务逻辑
    return response.data
  },
  error => {
    console.error('请求错误:', {
      status: error.response?.status,
      data: error.response?.data,
      config: error.config,
      message: error.message,
      stack: error.stack
    })
    // 处理 HTTP 错误
    const errorMessage = error.response?.data?.message || error.message || '请求失败'
    ElMessage.error(errorMessage)
    
    // 处理 401 未授权错误
    if (error.response?.status === 401 || error.response?.data?.code === 40100) {
      router.push('/login')
    }
    
    return Promise.reject(error)
  }
)

export default request 