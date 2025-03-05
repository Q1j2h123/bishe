import request from '../utils/request'

export interface Problem {
  id: number
  title: string
  difficulty: '简单' | '中等' | '困难'
  acceptRate: string
  submissionCount: number
}

export interface Submission {
  id: number
  problemTitle: string
  status: 'Accepted' | 'Wrong Answer' | 'Time Limit Exceeded' | 'Memory Limit Exceeded' | 'Runtime Error' | 'Compile Error'
  submitTime: string
}

export interface UserStats {
  solvedCount: number
  submissionCount: number
  acceptRate: number
}

export const problemApi = {
  // 获取题目列表
  async getProblems(category?: string) {
    const response = await request.get<Problem[]>('/problem/list', { params: { category } })
    return response.data
  },

  // 获取用户统计信息
  async getUserStats() {
    const response = await request.get<UserStats>('/problem/stats')
    return response.data
  },

  // 获取最近提交记录
  async getRecentSubmissions() {
    const response = await request.get<Submission[]>('/problem/submissions/recent')
    return response.data
  }
} 