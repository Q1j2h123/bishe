import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi } from '../api/user'
import type { UserInfo } from '../types/api'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<UserInfo | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))

  // 设置用户信息
  const setUserInfo = (user: UserInfo | null) => {
    currentUser.value = user
  }

  // 设置token
  const setToken = (newToken: string | null) => {
    console.log('设置token:', newToken)
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
      console.log('token已保存到localStorage')
    } else {
      localStorage.removeItem('token')
      console.log('token已从localStorage移除')
    }
  }

  // 获取当前用户信息
  const getCurrentUser = async () => {
    if (!token.value) {
      console.log('无token，无法获取用户信息')
      return null
    }
    try {
      console.log('开始获取用户信息...')
      const response = await userApi.getCurrentUser()
      console.log('获取用户信息成功:', response.data)
      setUserInfo(response.data)
      return response.data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      setUserInfo(null)
      setToken(null)
      return null
    }
  }

  // 退出登录
  const logout = () => {
    setUserInfo(null)
    setToken(null)
  }

  return {
    currentUser,
    token,
    setUserInfo,
    setToken,
    getCurrentUser,
    logout
  }
}) 