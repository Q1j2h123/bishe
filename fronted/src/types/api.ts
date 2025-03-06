// 通用响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 用户相关接口
export interface LoginParams {
  userAccount: string
  userPassword: string
}

export interface LoginResponse {
  id: number
  userAccount: string
  userName: string
  userAvatar: string
  userProfile: string
  userRole: string
  createTime: string
}

export interface UserInfo {
  id: number
  userAccount: string
  userName: string
  userAvatar: string
  userProfile: string
  userRole: string
  createTime: string
}

// 这里可以继续添加其他接口的类型定义 