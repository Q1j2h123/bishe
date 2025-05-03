import request from '@/utils/request'
import type { 
  LoginParams, 
  RegisterParams, 
  UserListParams,
  UserListVO,
  UserManageVO 
} from '@/types/api'

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
    userBio: string | null
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
      const response: any = await request.get('user/current')
      
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
      
      return response
    } catch (error) {
      console.error('更新用户信息错误:', error)
      throw error
    }
  },

  // 退出登录
  logout() {
    localStorage.removeItem('token')
  },

  // 获取用户列表
  async getUserList(params: UserListParams) {
    try {
      const response: any = await request.post('/user/list', params)
      
      console.log('获取用户列表响应:', response)
      
      if (!response) {
        throw new Error('获取用户列表失败，服务器未返回有效数据')
      }
      
      if (response.code !== 0) {
        throw new Error(response.message || '获取用户列表失败')
      }
      
      if (!response.data || !response.data.records) {
        console.error('响应数据格式错误:', response)
        throw new Error('获取用户列表失败，响应数据格式错误')
      }
      
      return {
        code: response.code,
        message: response.message,
        data: {
          records: response.data.records || [],
          total: response.data.total || 0
        }
      }
    } catch (error) {
      console.error('获取用户列表错误:', error)
      throw error
    }
  },

  // 获取用户管理详情
  async getUserManageDetail(userId: number) {
    try {
      const response: any = await request.get(`/user/manage/detail/${userId}`)
      
      console.log('获取用户详情响应:', response)
      
      if (!response) {
        throw new Error('获取用户详情失败，服务器未返回有效数据')
      }
      
      if (response.code !== 0) {
        throw new Error(response.message || '获取用户详情失败')
      }
      
      if (!response.data) {
        console.error('响应数据格式错误:', response)
        throw new Error('获取用户详情失败，响应数据格式错误')
      }
      
      return {
        code: response.code,
        message: response.message,
        data: response.data
      }
    } catch (error) {
      console.error('获取用户详情错误:', error)
      throw error
    }
  },

  // 更新用户角色
  async updateUserRole(userId: number, role: string) {
    try {
      const response: any = await request.post(`/user/manage/role/${userId}?role=${role}`)
      
      console.log('更新用户角色响应:', response)
      
      if (!response) {
        throw new Error('更新用户角色失败，服务器未返回有效数据')
      }
      
      if (response.code !== 0) {
        throw new Error(response.message || '更新用户角色失败')
      }
      
      return response.data
    } catch (error) {
      console.error('更新用户角色错误:', error)
      throw error
    }
  },

  // 重置用户密码
  async resetUserPassword(userId: number) {
    try {
      const response: any = await request.post(`/user/manage/reset-password/${userId}`)
      
      if (!response) {
        throw new Error('重置密码失败，服务器未返回有效数据')
      }
      
      if (response.code !== 0) {
        throw new Error(response.message || '重置密码失败')
      }
      
      return response.data
    } catch (error) {
      console.error('重置密码错误:', error)
      throw error
    }
  },
  
  // 封禁用户
  async banUser(userId: number, reason?: string) {
    try {
      let url = `/user/manage/ban/${userId}`;
      if (reason) {
        url += `?reason=${encodeURIComponent(reason)}`;
      }
      
      const response: any = await request.post(url);
      
      if (!response) {
        throw new Error('封禁用户失败，服务器未返回有效数据');
      }
      
      if (response.code !== 0) {
        throw new Error(response.message || '封禁用户失败');
      }
      
      return response.data;
    } catch (error) {
      console.error('封禁用户错误:', error);
      throw error;
    }
  },
  
  // 解封用户
  async unbanUser(userId: number) {
    try {
      const response: any = await request.post(`/user/manage/unban/${userId}`);
      
      if (!response) {
        throw new Error('解封用户失败，服务器未返回有效数据');
      }
      
      if (response.code !== 0) {
        throw new Error(response.message || '解封用户失败');
      }
      
      return response.data;
    } catch (error) {
      console.error('解封用户错误:', error);
      throw error;
    }
  }
} 