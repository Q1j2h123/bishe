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
  token: string
}

export interface RegisterParams {
  userAccount: string
  userPassword: string
  checkPassword: string
  userName: string
}

export interface UserInfo {
  id: number
  userAccount: string
  userName: string
  userAvatar: string
  userProfile: string
  userBio: string
  userRole: string
  createTime: string
}

// 用户管理相关接口
export interface UserListVO {
  id: number
  userAccount: string
  userName: string
  userAvatar: string | null
  userRole: string
  createTime: string
}

export interface UserManageVO {
  id: number
  userAccount: string
  userName: string
  userAvatar: string | null
  userProfile: string | null
  userRole: string
  createTime: string
  submissionCount: number
  acceptedCount: number
  totalSolvedCount: number
  acceptanceRate: number
  lastAcceptedProblem: string | null
}

export interface UserListParams {
  current: number
  pageSize: number
  userName?: string
  userAccount?: string
  createTimeOrder?: 'asc' | 'desc'
}

// 这里可以继续添加其他接口的类型定义 