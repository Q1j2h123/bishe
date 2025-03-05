import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi, type UserInfo } from '../api/user'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<UserInfo | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))

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

  // 获取当前用户信息
  const getCurrentUser = async () => {
    if (!token.value) return null
    try {
      const user = await userApi.getCurrentUser()
      setUserInfo(user)
      return user
    } catch (error) {
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