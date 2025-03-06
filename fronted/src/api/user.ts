import request from '@/utils/request'
import type { ApiResponse, LoginParams, UserInfo, LoginResponse } from '@/types/api'
import { ElMessage } from 'element-plus'

export interface RegisterParams {
  userAccount: string
  userPassword: string
  checkPassword: string
  userName: string
  userAvatar?: string
  userProfile?: string
}

export const userApi = {
  // 用户登录
  async login(data: LoginParams) {
    try {
      const res = (await request.post<ApiResponse<LoginResponse>>('/user/login', data)) as unknown as ApiResponse<LoginResponse>
      console.log('登录响应:', res)
      if (res.code === 0 || res.code === 200) {
        if (res.data) {
          // 生成一个临时token（实际项目中应该由后端返回）
          const token = btoa(JSON.stringify(res.data))
          localStorage.setItem('token', token)
          ElMessage.success('登录成功')
          return {
            token,
            userInfo: res.data
          }
        }
      }
      throw new Error(res.message || '登录失败')
    } catch (error: any) {
      console.error('登录错误:', error)
      ElMessage.error(error.message || '登录失败')
      throw error
    }
  },

  // 用户注册
  async register(data: RegisterParams) {
    try {
      const res = (await request.post<ApiResponse<number>>('/user/register', data)) as unknown as ApiResponse<number>
      console.log('注册响应:', res)
      if (res.code === 0 || res.code === 200) {
        return res.data
      }
      throw new Error(res.message || '注册失败')
    } catch (error: any) {
      console.error('注册错误:', error)
      ElMessage.error(error.message || '注册失败')
      throw error
    }
  },

  // 获取当前登录用户信息
  async getCurrentUser() {
    const response = await request.get<ApiResponse<UserInfo>>('/user/current')
    return response.data
  },

  // 更新用户信息
  async updateUserInfo(data: Partial<UserInfo>) {
    const response = await request.post<ApiResponse<boolean>>('/user/update', data)
    return response.data
  },

  // 退出登录
  logout() {
    localStorage.removeItem('token')
  }
} 