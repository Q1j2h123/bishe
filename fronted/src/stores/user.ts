import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi } from '@/api/user'
import type { UserInfo } from '@/api/user'
import type { LoginParams } from '@/types/api'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string | null>(localStorage.getItem('token'))
  const currentUser = ref<UserInfo | null>(null)

  // 设置用户信息
  const setUserInfo = (user: UserInfo | null) => {
    currentUser.value = user
  }

  // 设置token
  const setToken = (newToken: string | null) => {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  // 登录方法
  const login = async (loginParams: LoginParams) => {
    try {
      // 获取登录响应
      const response = await userApi.login(loginParams)
      
      // 添加详细日志
      console.log('登录响应数据:', response)
      
      // 检查响应是否包含必要的数据
      if (!response) {
        throw new Error('登录失败，响应数据为空')
      }
      
      // 后端返回的数据结构是 { user: {...}, token: "..." }
      const { user, token: userToken } = response
      
      if (!userToken) {
        throw new Error('登录失败，未获取到有效的令牌')
      }
      
      // 设置令牌
      console.log('Store中设置token:', userToken)
      setToken(userToken)
      
      // 确认localStorage中已经设置token
      console.log('确认localStorage token:', localStorage.getItem('token'))
      
      // 设置用户信息
      if (user) {
        setUserInfo({
          id: user.id,
          userAccount: user.userAccount,
          userName: user.userName,
          userAvatar: user.userAvatar || null,
          userProfile: user.userProfile || null,
          userRole: user.userRole || 'user',
          createTime: new Date().toISOString() // 后端没有返回创建时间，使用当前时间代替
        })
      } else {
        console.error('响应中缺少用户信息:', response)
      }
      
      return response
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }

  // 获取当前用户信息
  const getCurrentUser = async () => {
    if (!token.value) return null
    
    try {
      const response = await userApi.getCurrentUser()
      setUserInfo(response)
      return response
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果获取用户信息失败，可能是token过期
      // logout()
      setToken(null)
      setUserInfo(null)
      throw error
    }
  }

  // 登出
  const logout = () => {
    setToken(null)
    setUserInfo(null)
    userApi.logout()
  }

  // 更新用户信息
  const updateUserInfo = async (data: Partial<UserInfo>) => {
    try {
      const response: any = await userApi.updateUserInfo(data)
      if (response) {
        // 更新成功后重新获取用户信息
        await getCurrentUser()
        return { code: 0, data: true }
      }
      return { code: 500, message: '更新失败' }
    } catch (error) {
      console.error('更新用户信息错误:', error)
      return { code: 500, message: error instanceof Error ? error.message : '更新失败' }
    }
  }

  return {
    token,
    currentUser,
    setUserInfo,
    setToken,
    login,
    getCurrentUser,
    logout,
    updateUserInfo
  }
}) 