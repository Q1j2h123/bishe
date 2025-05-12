import request from '@/utils/request'
import type { BaseResponse } from '@/types/response'

// 用户统计数据类型定义
export interface TypeCount {
  total: number
  solved: number
}

// 用户统计数据接口
export interface UserStatistics {
  totalSolved: number
  totalSubmissions: number
  correctRate: string
  streak: number
  typeCounts: {
    choice: TypeCount
    judge: TypeCount
    program: TypeCount
  }
  difficultyCounts: {
    easy: TypeCount
    medium: TypeCount
    hard: TypeCount
  }
}

// 错题记录接口
export interface ErrorProblem {
  id: number
  title: string
  type: string
  difficulty: string
  lastErrorTime: string
}

// 错题本筛选条件
export interface ErrorBookFilter {
  type?: string
  difficulty?: string
  keyword?: string
  current: number
  size: number
}

// 日历数据接口
export interface CalendarData {
  date: string
  count: number
}

// 用户统计API
export const userStatsApi = {
  // 获取用户统计数据
  async getUserStatistics(): Promise<BaseResponse<UserStatistics>> {
    try {
      return await request.get('/user/statistics')
    } catch (error) {
      console.error('获取用户统计数据失败:', error)
      throw error
    }
  },

  // 获取错题本列表
  async getErrorProblems(params: any): Promise<BaseResponse<{records: ErrorProblem[], total: number}>> {
    try {
      return await request.get('/user/error-problems', { params })
    } catch (error) {
      console.error('获取错题本数据失败:', error)
      throw error
    }
  },

  // 标记题目为已掌握
  async markProblemAsMastered(problemId: number): Promise<BaseResponse<boolean>> {
    try {
      return await request.post(`/user/mark-mastered/${problemId}`)
    } catch (error) {
      console.error('标记题目为已掌握失败:', error)
      throw error
    }
  },

  // 获取用户提交日历数据
  getSubmissionCalendar(startDate?: string, endDate?: string): Promise<BaseResponse<CalendarData[]>> {
    const params = { startDate, endDate }
    return request.get('/user/submission-calendar', { params })
  },

  // 获取用户学习路线推荐
  getLearningRecommendations(): Promise<BaseResponse<any>> {
    return request.get('/api/user/learning-recommendations')
  }
} 