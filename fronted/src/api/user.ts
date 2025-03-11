import request from '@/utils/request'
import type { LoginParams, RegisterParams } from '@/types/api'

export interface UserInfo {
  id: number
  userAccount: string
  userName: string
  userAvatar: string | null
  userProfile: string | null
  userRole: string
  createTime: string
  unionId?: string | null
  mpOpenId?: string | null
}

// 后端LoginVO的结构
export interface LoginResponse {
  user: {
    id: number
    userAccount: string
    userName: string
    userAvatar: string | null
    userProfile: string | null
    userRole: string
  }
  token: string
}

export const userApi = {
  // 用户登录
  async login(data: LoginParams) {
    try {
      const response: any = await request.post('/user/login', data)
      
      // 打印原始响应以便调试
      console.log('原始登录响应:', response)
      
      if (!response) {
        throw new Error('登录失败，服务器未返回有效数据')
      }
      
      if (response.code !== 0) {
        throw new Error(response.message || '登录失败')
      }
      
      // 检查响应数据结构是否符合预期
      if (!response.data) {
        console.error('响应缺少data字段:', response)
        throw new Error('登录失败，响应格式错误')
      }
      
      return response.data as LoginResponse
    } catch (error) {
      console.error('登录请求错误:', error)
      throw error
    }
  },

  // 用户注册
  async register(data: RegisterParams) {
    try {
      // 添加更多调试信息
      console.log('注册请求数据:', data)
      
      const response: any = await request.post('/user/register', data)
      
      console.log('注册响应数据:', response)
      
      if (!response) {
        throw new Error('注册失败，服务器未返回有效数据')
      }
      
      if (response.code !== 0) {
        throw new Error(response.message || '注册失败')
      }
      
      return response.data
    } catch (error) {
      console.error('注册请求错误:', error)
      throw error
    }
  },

  // 获取当前登录用户信息
  async getCurrentUser() {
    try {
      const response: any = await request.get('/user/current')
      
      if (!response) {
        throw new Error('获取用户信息失败，服务器未返回有效数据')
      }
      
      if (response.code !== 0) {
        throw new Error(response.message || '获取用户信息失败')
      }
      
      return response.data
    } catch (error) {
      console.error('获取用户信息错误:', error)
      throw error
    }
  },

  // 更新用户信息
  async updateUserInfo(data: Partial<UserInfo>) {
    try {
      const response: any = await request.post('/user/update', data)
      
      if (!response) {
        throw new Error('更新用户信息失败，服务器未返回有效数据')
      }
      
      if (response.code !== 0) {
        throw new Error(response.message || '更新用户信息失败')
      }
      
      return response.data
    } catch (error) {
      console.error('更新用户信息错误:', error)
      throw error
    }
  },

  // 退出登录
  logout() {
    localStorage.removeItem('token')
  }
} 